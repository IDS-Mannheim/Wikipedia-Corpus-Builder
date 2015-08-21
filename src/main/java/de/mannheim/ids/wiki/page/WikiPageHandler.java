package de.mannheim.ids.wiki.page;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.SAXException;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiXMLWriter;

/**
 * This class implements methods for handling Wikipages including reading page
 * content, cleaning wikitext (pre-processing), parsing, and XML validation.
 * 
 * @author margaretha
 * 
 */
public abstract class WikiPageHandler implements Runnable {

	private static final Pattern nonTagPattern = Pattern
			.compile("<([^!!/a-zA-Z\\s])");
	private static final Pattern nonTagPattern2 = Pattern.compile("<([^>]*)");

	private static final Pattern stylePattern = Pattern
			.compile("(\\[\\[.+>\\]\\])");

	protected WikiPage wikiPage;
	protected WikiStatistics wikiStatistics;

	protected WikiXMLWriter wikiXMLWriter;
	protected WikiErrorWriter errorWriter;

	protected Configuration config;

	public WikiPageHandler(Configuration config, WikiPage wikipage,
			WikiStatistics wikiStatistics, WikiErrorWriter errorWriter) {
		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		if (wikipage == null) {
			throw new IllegalArgumentException("Wikipage cannot be null.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException("WikiStatistics cannot be null.");
		}
		if (errorWriter == null) {
			throw new IllegalArgumentException("Error writer cannot be null.");
		}

		this.config = config;
		this.wikiPage = wikipage;
		this.wikiStatistics = wikiStatistics;
		this.errorWriter = errorWriter;

		this.wikiXMLWriter = new WikiXMLWriter(config);
	}

	@SuppressWarnings("deprecation")
	protected String parseToXML(String pageId, String pageTitle, String wikitext)
			throws IOException {
		if (wikitext == null) {
			throw new IllegalArgumentException("Wikitext cannot be null.");
		}

		// unescape XML tags
		wikitext = StringEscapeUtils.unescapeXml(wikitext);
		wikitext = cleanPattern(wikitext);

		// italic and bold are not repaired because they are written in
		// wiki-mark-ups
		try {
			TagSoupParser tagSoupParser = new TagSoupParser();
			wikitext = tagSoupParser.generate(wikitext, true);
		}
		catch (SAXException e) {
			errorWriter.logErrorPage("TAGSOUP", pageTitle, pageId,
					e.getCause(), "");
		}

		Sweble2Parser swebleParser = new Sweble2Parser(pageId, pageTitle,
				wikitext, config.getLanguageCode(), wikiStatistics, errorWriter);
		Thread swebleThread = new Thread(swebleParser, pageTitle);

		try {
			swebleThread.start();
			swebleThread.join(1000 * 60 * 5);
			if (swebleThread.isAlive()) {
				wikiStatistics.addNumOfThreadDeaths();
				InterruptedException e = new InterruptedException(
						"Sweble thread is taking too long.");
				errorWriter.logErrorPage("THREAD", pageTitle, pageId, e,
						e.getMessage());

				// Usually the sweble thread should be interrupted. However,
				// sweble process would not stop in this way.
				// This event occurs when the wikitext contains very long
				// errors, such as numerous nested opening link mark ups without
				// the corresponding ending mark ups.

				swebleThread.stop();
				throw e;
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return swebleParser.getWikiXML();
	}

	private String cleanPattern(String wikitext) {
		// start table notation
		wikitext = wikitext.replace(":{|", "{|");
		wikitext = wikitext.replace("<Fn", "&lt;Fn");
		wikitext = wikitext.replace("Fn>", "Fn&gt;");
		// space for non-tag
		Matcher matcher = nonTagPattern.matcher(wikitext);
		wikitext = matcher.replaceAll("&lt; $1");
		matcher.reset();
		// < without >
		matcher = nonTagPattern2.matcher(wikitext);
		if (matcher.find()) {
			if (matcher.group(1).contains("<")
			// hack for page id #7420769
					|| matcher.group(1).contains("In:")) {
				wikitext = matcher.replaceAll("&lt;$1");
			}
		}
		matcher.reset();

		// escape for style containing tag
		matcher = stylePattern.matcher(wikitext);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String replace = StringEscapeUtils.escapeHtml(matcher.group(1));
			replace = Matcher.quoteReplacement(replace);
			matcher.appendReplacement(sb, replace);
		}
		matcher.appendTail(sb);
		wikitext = sb.toString();
		matcher.reset();
		return wikitext;
	}

	protected void writeWikiXML() throws IOException {
		if (wikiPage.getWikiXML().isEmpty()) {
			wikiStatistics.addEmptyParsedPages();
		}
		else if (validateDOM() && validatePageStructure()) {
			wikiXMLWriter.write(wikiPage, wikiPage.getWikiXML(),
					config.getOutputFolder());
			wikiStatistics.addTotalNonEmptyPages();
		}
	}

	protected void writeWikitext() throws IOException {
		wikiXMLWriter.write(wikiPage, wikiPage.getWikitext(),
				config.getWikitextFolder());
		wikiPage.setWikitext(null);
	}

	private boolean validateDOM() {
		try {
			String wikiXML = "<text>" + wikiPage.getWikiXML() + "</text>";
			// test XML validity
			DOMParser domParser = new DOMParser();
			domParser.parseXML(new ByteArrayInputStream(wikiXML
					.getBytes("utf-8")));
		}
		catch (Exception e) {
			wikiStatistics.addDomErrors();
			errorWriter.logErrorPage("DOM", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e.getCause(), "");
			return false;
		}
		return true;
	}

	private boolean validatePageStructure() {
		try {
			// try fixing missing tags
			StringBuilder sb = new StringBuilder();
			sb.append(wikiPage.getPageIndent());
			
			TagSoupParser tagSoupParser = new TagSoupParser();
			sb.append(tagSoupParser.generate(wikiPage.getPageStructure(), false));
			wikiPage.setPageStructure(sb.toString());
			sb = null;
		}
		catch (Exception e) {
			wikiStatistics.addPageStructureErrors();
			errorWriter.logErrorPage("PAGE ", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e.getCause(), "");
			return false;
		}
		return true;
	}
}
