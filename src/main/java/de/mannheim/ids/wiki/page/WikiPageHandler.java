package de.mannheim.ids.wiki.page;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.WikiXMLWriter;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;

/**
 * This class implements methods for handling Wikipages including reading page
 * content, cleaning wikitext (pre-processing), parsing, and XML validation.
 * 
 * @author margaretha
 * 
 */
public class WikiPageHandler implements Runnable {

	private static final Pattern pattern = Pattern
			.compile("<([^!!/a-zA-Z\\s])");
	private static final Pattern stylePattern = Pattern
			.compile("(\\[\\[.+>\\]\\])");
	private static final Pattern textPattern = Pattern
			.compile("<text.*\">(.*)");

	private WikiPage wikiPage;
	private WikiStatistics wikiStatistics;
	private WikiXMLWriter wikiXMLWriter;
	private WikiErrorWriter errorWriter;

	private WikiPostUser postUser;
	private WikiPostTime postTime;

	private Configuration config;

	private DOMParser domParser;
	private TagSoupParser tagSoupParser;

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

		this.wikiPage = wikipage;
		this.wikiStatistics = wikiStatistics;
		this.errorWriter = errorWriter;
		this.config = config;

		wikiXMLWriter = new WikiXMLWriter(config);
		domParser = new DOMParser();
		tagSoupParser = new TagSoupParser();
	}

	@Override
	public void run() {

		try {
			if (config.isDiscussion()) {
				WikiPostHandler th = new WikiPostHandler(config, wikiPage,
						wikiStatistics, errorWriter, postUser, postTime,
						tagSoupParser);
				th.handlePosts();
			}
			else {
				wikiPage.wikitext = parseToXML(wikiPage.wikitext,
						wikiPage.getPageTitle());
			}

			if (wikiPage.wikitext.isEmpty()) {
				wikiStatistics.addEmptyParsedPages();
			}
			else {
				validateXML(wikiPage);
				wikiXMLWriter.write(wikiPage);
				wikiStatistics.addTotalNonEmptyPages();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String cleanPattern(String wikitext) {
		wikitext = StringUtils.replaceEach(wikitext,
		// new String[] { ":{|" , "<br/>", "<br />"},
		// new String[] { "{|" , "&lt;br/&gt;", "&lt;br /&gt;"}); //start table
		// notation
				new String[] { ":{|" }, new String[] { "{|" }); // start table
																// notation

		Matcher matcher = pattern.matcher(wikitext); // space for non-tag
		wikitext = matcher.replaceAll("&lt; $1");
		matcher.reset();

		matcher = stylePattern.matcher(wikitext); // escape for style containing
													// tag
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String replace = StringEscapeUtils.escapeHtml(matcher.group(1));
			replace = Matcher.quoteReplacement(replace);
			matcher.appendReplacement(sb, replace);
		}
		matcher.appendTail(sb);
		wikitext = sb.toString();
		return wikitext;
	}

	public static String cleanTextStart(String trimmedStrLine)
			throws IOException {
		Matcher matcher = textPattern.matcher(trimmedStrLine);
		return matcher.replaceFirst("") + "\n";
	}

	@SuppressWarnings("deprecation")
	public String parseToXML(String wikitext, String pagetitle)
			throws IOException {

		wikitext = StringEscapeUtils.unescapeXml(wikitext); // unescape XML tags
		wikitext = WikiPageHandler.cleanPattern(wikitext);

		// italic and bold are not repaired because they have wiki-mark-ups
		try {
			wikitext = tagSoupParser.generate(wikitext, true);
		}
		catch (SAXException e) {
			errorWriter.logErrorPage("TAGSOUP", pagetitle, e.getCause());
		}

		Sweble2Parser swebleParser = new Sweble2Parser(wikitext.trim(),
				pagetitle, config.getLanguageCode(), wikiStatistics,
				errorWriter);
		Thread swebleThread = new Thread(swebleParser);

		try {
			swebleThread.start();
			swebleThread.join(1000 * 6);
			if (swebleThread.isAlive()) {
				swebleThread.stop();
			}
			wikitext = swebleParser.getWikiXML();
		}
		catch (InterruptedException e) {
			wikiStatistics.addSwebleErrors();
			errorWriter.logErrorPage("SWEBLE", pagetitle, e.getCause());
			wikitext = "";
		}
		return wikitext;
	}

	public void validateXML(WikiPage wikiPage) {

		if (wikiPage == null) {
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}

		String t = "";
		try {
			t = "<text>" + wikiPage.wikitext + "</text>";
			// test XML validity
			domParser.parseXML(new ByteArrayInputStream(t.getBytes("utf-8")));
		}
		catch (Exception e) {
			wikiStatistics.addParsingErrors();
			errorWriter.logErrorPage("DOM", wikiPage.getPageTitle(),
					e.getCause());
			wikiPage.wikitext = "";
		}

		try {
			wikiPage.pageStructure = wikiPage.getPageIndent()
					+ tagSoupParser.generate(wikiPage.pageStructure, false);
		}
		catch (Exception e) {
			wikiStatistics.addPageStructureErrors();
			errorWriter.logErrorPage("PAGE ", wikiPage.getPageTitle(),
					e.getCause());
		}
	}

	public WikiPostUser getPostUser() {
		return postUser;
	}

	public void setPostUser(WikiPostUser postUser) {
		this.postUser = postUser;
	}

	public WikiPostTime getPostTime() {
		return postTime;
	}

	public void setPostTime(WikiPostTime postTime) {
		this.postTime = postTime;
	}
}
