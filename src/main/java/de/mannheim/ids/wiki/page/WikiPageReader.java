package de.mannheim.ids.wiki.page;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import de.mannheim.ids.wiki.Configuration;

/**
 * This class reads a Wiki page, identify some page metadata, such as title,
 * namespace and id, and pass the page content to a corresponding handler
 * depends on the type of the Wiki page: article or talk page.
 * 
 * @author margaretha
 * 
 */

public class WikiPageReader implements Runnable {

	private WikiStatistics wikiStatistics;

	private static final Pattern titlePattern = Pattern
			.compile("<title>(.+)</title>");
	private static final Pattern nsPattern = Pattern.compile("<ns>(.+)</ns>");
	private static final Pattern idPattern = Pattern.compile("<id>(.+)</id>");
	private static final Pattern textPattern = Pattern
			.compile("<text.*\">(.*)");

	private Configuration config;
	private LinkedBlockingQueue<WikiPage> wikipages;
	private boolean textFlag;
	private WikiPage endPage;

	public WikiPageReader(Configuration config,
			LinkedBlockingQueue<WikiPage> wikipages, WikiPage endwikipage,
			WikiStatistics wikiStatistics) {

		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException("WikiStatistics cannot be null.");
		}

		this.config = config;
		this.wikipages = wikipages;
		this.endPage = endwikipage;
		this.wikiStatistics = wikiStatistics;
	}

	@Override
	public void run() {
		try {
			read();
		}
		catch (IOException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private void read() throws IOException {

		String inputFile = config.getWikidump();
		FileInputStream fs = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));

		Matcher matcher;
		String strLine, trimmedStrLine;
		boolean isToRead = false, searchId = true;
		boolean isDiscussion = config.isDiscussion();

		WikiPage wikiPage = null;

		while ((strLine = br.readLine()) != null) {
			trimmedStrLine = strLine.trim();

			// Start reading a page
			if (trimmedStrLine.startsWith("<page>")) {
				wikiPage = new WikiPage();
				wikiPage.pageStructure = strLine + "\n";

				isToRead = true;
				searchId = true;
				textFlag = false;
			}
			// End reading a page
			else if (isToRead && trimmedStrLine.endsWith("</page>")) {
				wikiPage.pageStructure += strLine;
				wikiPage.setPageIndent(setIndent(strLine));
				if (!wikiPage.wikitext.isEmpty()) {
					wikipages.add(wikiPage);
					wikiStatistics.addTotalPages();
				}
				else {
					wikiStatistics.addEmptyPages();
				}
				isToRead = false;
			}
			else if (isToRead && !trimmedStrLine.equals("</mediawiki>")) {
				// Page title
				if (trimmedStrLine.startsWith("<title>")) {
					matcher = titlePattern.matcher(trimmedStrLine);
					if (matcher.find()) {
						wikiPage.setPageTitle(matcher.group(1));
						wikiPage.pageStructure += strLine + "\n";
					}
					else { // Skip page
						isToRead = false;
					}
				}
				// Page namespace
				else if (trimmedStrLine.startsWith("<ns>")) {
					matcher = nsPattern.matcher(trimmedStrLine);
					if (matcher.find()) {
						int ns = Integer.parseInt(matcher.group(1));
						if (config.getNamespaceKey() == ns) {
							wikiPage.pageStructure += setIndent(strLine)
									+ trimmedStrLine + "\n";
						}
						else { // Stop reading. Skip this page.
							isToRead = false;
						}
					}
				}
				// Page id
				else if (trimmedStrLine.startsWith("<id>") && searchId) {
					matcher = idPattern.matcher(trimmedStrLine);
					if (matcher.find()) {
						wikiPage.setPageId(matcher.group(1));
						wikiPage.setPageIndex(isDiscussion);
						wikiPage.pageStructure += strLine + "\n";
						searchId = false;
					}
					else { // Skip page
						isToRead = false;
						wikiStatistics.addNoId();
					}
				}
				// Redirect page
				else if (trimmedStrLine.startsWith("<redirect")) {
					wikiPage.setRedirect(true);
					wikiStatistics.addRedirectPages();
					isToRead = false;
				}

				else if (isDiscussion) {
					isToRead = collectDiscussionText(wikiPage, strLine,
							trimmedStrLine);
				}
				else {
					isToRead = collectArticleText(wikiPage, strLine,
							trimmedStrLine);
				}
			}
		}
		wikipages.add(endPage); // end of wikipages queue
	}

	public String setIndent(String strLine) {
		return StringUtils.repeat(" ", strLine.indexOf("<"));
	}

	public static String cleanTextStart(String trimmedStrLine) {
		Matcher matcher = textPattern.matcher(trimmedStrLine);
		if (matcher.find()) {
			return matcher.group(1) + "\n";
		}
		else
			return trimmedStrLine.replace("<text xml:space=\"preserve\">", "");
	}

	private boolean collectArticleText(WikiPage wikiPage, String strLine,
			String trimmedStrLine) {

		if (wikiPage == null) {
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}

		// Finish collecting text
		if (trimmedStrLine.endsWith("</text>")) {
			// text starts and ends at the same line
			if (trimmedStrLine.startsWith("<text")) {
				wikiPage.pageStructure += "      <text>";
				trimmedStrLine = cleanTextStart(trimmedStrLine);
			}
			// remove </text>
			trimmedStrLine = trimmedStrLine.replaceFirst("</text>", "");

			wikiPage.wikitext += trimmedStrLine + "\n";
			wikiPage.wikitext = wikiPage.wikitext.trim();
			/*
			 * if (wikiPage.wikitext.equals("")){ // empty text
			 * wikiPage.setEmpty(true); return; }
			 */
			wikiPage.pageStructure += "</text>\n";
			textFlag = false;
		}

		// Continue collecting text
		else if (textFlag) {
			wikiPage.wikitext += strLine + "\n";
		}

		else if (trimmedStrLine.startsWith("<text")) {
			// empty text
			if (trimmedStrLine.endsWith("<text/>")
					|| trimmedStrLine.equals("<text xml:space=\"preserve\" />")) {
				return false;
			}
			else { // start collecting text
				wikiPage.pageStructure += "      <text>";
				wikiPage.wikitext += cleanTextStart(trimmedStrLine);
				this.textFlag = true;
			}
		}
		else { // copy page metadata
			wikiPage.pageStructure += strLine + "\n";
		}

		return true;
	}

	private boolean collectDiscussionText(WikiPage wikiPage, String strLine,
			String trimmedStrLine) {

		if (wikiPage == null) {
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}

		if (trimmedStrLine.endsWith("</text>")) { // finish collecting text
			// text starts and ends at the same line
			if (trimmedStrLine.startsWith("<text")) {
				wikiPage.pageStructure += "      <text>";
				trimmedStrLine = cleanTextStart(trimmedStrLine);
			}
			trimmedStrLine = trimmedStrLine.replaceFirst("</text>", "");
			wikiPage.textSegments.add(trimmedStrLine);

			for (String segment : wikiPage.textSegments) {
				wikiPage.wikitext += segment + "\n";
			}

			wikiPage.wikitext = wikiPage.wikitext.trim();
			wikiPage.pageStructure += "</text>\n";
			textFlag = false;
		}
		else if (trimmedStrLine.startsWith("<text")) {
			// empty text
			if (trimmedStrLine.endsWith("<text/>")
					|| trimmedStrLine.equals("<text xml:space=\"preserve\" />")) {
				return false;
			}
			// start collecting text
			else {
				wikiPage.pageStructure += "      <text>";
				wikiPage.textSegments.add(cleanTextStart(trimmedStrLine));
				this.textFlag = true;
			}
		}
		// continue collecting text
		else if (textFlag) {
			wikiPage.textSegments.add(strLine);
		}
		else { // copy page metadata
			wikiPage.pageStructure += strLine + "\n";
		}
		return true;
	}
}
