package de.mannheim.ids.wiki.page;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import de.mannheim.ids.wiki.Configuration;

/**
 * This class reads the wikidump and generates WikiPage objects from the page
 * metadata, such as title, namespace and id. It also collects wikitext per page
 * as the wikipage content.
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

	/**
	 * Constructs a WikiPageReader by adopting the given variable to initialize
	 * its corresponding fields.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param wikipages
	 *            a blocking queue containing wikipages
	 * @param endwikipage
	 *            a dummy wikipage signifying the last page
	 * @param wikiStatistics
	 *            the wiki statistics counter
	 */
	public WikiPageReader(Configuration config,
			BlockingQueue<WikiPage> wikipages, WikiPage endwikipage,
			WikiStatistics wikiStatistics) {

		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException(
					"WikiStatistics cannot be null.");
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

	/**
	 * Reads through the wikidump and generates a {@link WikiPage} object for
	 * each wiki page.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void read() throws IOException, InterruptedException {

		String inputFile = config.getWikidump();
		FileInputStream fs = new FileInputStream(new File(inputFile));
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));

		Matcher matcher;
		String strLine, trimmedStrLine;
		boolean isToRead = false, searchId = true;
		boolean isDiscussion = config.isDiscussion();
		boolean hasPrefix = !config.getTitlePrefix().isEmpty();

		WikiPage wikiPage = null;

		while ((strLine = br.readLine()) != null) {
			trimmedStrLine = strLine.trim();

			// Start reading a page
			if (trimmedStrLine.startsWith("<page>")) {
				wikiPage = new WikiPage(hasPrefix);
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
						String title = matcher.group(1);
						title = Normalizer.normalize(title, Form.NFKC);
						if (!config.getTitlePrefix().isEmpty()) {
							if (!title.startsWith(config.getTitlePrefix())) {
								// skip page
								isToRead = false;
								continue;
							}
						}
						wikiPage.setPageTitle(title);
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
						String id = matcher.group(1);
						if (config.getExcludedPages().contains(id)){
							isToRead = false;
							wikiStatistics.addSkippedPages();
						}
						else{	
							wikiPage.setPageId(id);
							wikiPage.setPageIndex(isDiscussion);
							pageStructureBuilder.append(strLine);
							pageStructureBuilder.append("\n");
							searchId = false;
						}
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
		wikipages.put(endPage); // end of wikipages queue
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
		else
			return trimmedStrLine.replace("<text xml:space=\"preserve\">", "");
	}

	/**
	 * Collects the wikitext content of the given wikipage.
	 * 
	 * @param wikiPage
	 *            a wikipage
	 * @param strLine
	 *            the current line
	 * @param trimmedStrLine
	 *            the trimmed version of the current line
	 * @return true if wikitext has not finished, false otherwise.
	 */
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
			if (trimmedStrLine.endsWith("<text/>") || trimmedStrLine
					.equals("<text xml:space=\"preserve\" />")) {
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

	/*
	 * Strangely, using StringBuilder leads to memory leaks because its internal
	 * char[] is kept in memory. The real culprit is unknown?!
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
