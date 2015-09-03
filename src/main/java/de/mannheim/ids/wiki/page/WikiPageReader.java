package de.mannheim.ids.wiki.page;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
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
	private BlockingQueue<WikiPage> wikipages;
	private boolean textFlag;
	private WikiPage endPage;

	private StringBuilder pageStructureBuilder;

	public WikiPageReader(Configuration config,
			BlockingQueue<WikiPage> wikipages, WikiPage endwikipage,
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
		catch (InterruptedException | IOException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private void read() throws IOException, InterruptedException {

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
				pageStructureBuilder = new StringBuilder();
				pageStructureBuilder.append(strLine);
				pageStructureBuilder.append("\n");

				isToRead = true;
				searchId = true;
				textFlag = false;
			}
			// End reading a page
			else if (isToRead && trimmedStrLine.endsWith("</page>")) {
				if (!wikiPage.getWikitext().isEmpty()) {
					pageStructureBuilder.append(strLine);
					wikiPage.setPageStructure(pageStructureBuilder.toString());
					wikiPage.setPageIndent(setIndent(strLine));

					wikipages.put(wikiPage);
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
						pageStructureBuilder.append(strLine);
						pageStructureBuilder.append("\n");
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
							pageStructureBuilder.append(strLine);
							pageStructureBuilder.append(trimmedStrLine);
							pageStructureBuilder.append("\n");
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
						pageStructureBuilder.append(strLine);
						pageStructureBuilder.append("\n");
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
				else {
					isToRead = collectText(wikiPage, strLine, trimmedStrLine);
				}
			}
		}
		wikipages.add(endPage); // end of wikipages queue
		br.close();
	}

	public String setIndent(String strLine) {
		return StringUtils.repeat(" ", strLine.indexOf("<"));
	}

	public static String cleanTextStart(String trimmedStrLine) {
		Matcher matcher = textPattern.matcher(trimmedStrLine);
		if (matcher.find()) {
			return matcher.group(1) + "\n";
		}
		else return trimmedStrLine.replace("<text xml:space=\"preserve\">", "");
	}

	private boolean collectText(WikiPage wikiPage, String strLine,
			String trimmedStrLine) {

		if (wikiPage == null) {
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}

		if (trimmedStrLine.endsWith("</text>")) { // finish collecting text
			// text starts and ends at the same line
			if (trimmedStrLine.startsWith("<text")) {
				pageStructureBuilder.append("      <text>");
				trimmedStrLine = cleanTextStart(trimmedStrLine);
			}
			trimmedStrLine = trimmedStrLine.replaceFirst("</text>", "");
			wikiPage.textSegments.add(trimmedStrLine);

			// String w = buildWikitext(wikiPage);
			String w = "";
			for (String segment : wikiPage.textSegments) {
				w += segment + "\n";
			}
			wikiPage.setWikitext(w.trim());

			if (!config.isDiscussion()) {
				wikiPage.textSegments = null;
			}

			pageStructureBuilder.append("</text>\n");
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
				pageStructureBuilder.append("      <text>");
				wikiPage.textSegments.add(cleanTextStart(trimmedStrLine));
				this.textFlag = true;
			}
		}
		// continue collecting text
		else if (textFlag) {
			wikiPage.textSegments.add(strLine);
		}
		// copy page metadata
		else {
			pageStructureBuilder.append(strLine);
			pageStructureBuilder.append("\n");
		}
		return true;
	}

	/* Strangely, using StringBuilder leads to memory leaks because its internal
	   char[] is kept in memory. The real culprit is unknown?!
	  */
	@SuppressWarnings("unused")
	private String buildWikitext(WikiPage wikiPage) {
		StringBuilder sb = new StringBuilder(2 * 1024);
		for (String segment : wikiPage.textSegments) {
			sb.append(segment);
			sb.append("\n");
		}

		return sb.toString();

	}
}
