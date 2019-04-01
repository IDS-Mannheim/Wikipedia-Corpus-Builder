package de.mannheim.ids.wiki.page;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.Utilities;
import de.mannheim.ids.wiki.WikiXMLProcessor;
import de.mannheim.ids.writer.WikiErrorWriter;

/**
 * This class implements methods for handling Wikipages including reading page
 * content, cleaning wikitext (pre-processing), parsing, and validating the XML
 * output.
 * 
 * @author margaretha
 * 
 */
public abstract class WikiPageHandler implements Runnable {

	private Logger log = LogManager.getLogger(WikiPageHandler.class);
	
	private static final Pattern nonTagPattern = Pattern
			.compile("<([^!!/a-zA-Z\\s])");
	private static final Pattern nonTagPattern2 = Pattern.compile("<([^>]*)");
	
	private static final Pattern endTagPattern = Pattern.compile("([^\\s])/>");

	private static final Pattern stylePattern = Pattern
			.compile("(\\[\\[.+>\\]\\])");

	protected WikiPage wikiPage;
	protected WikiStatistics wikiStatistics;

	private TagSoupParser tagSoupParser;
	protected WikiErrorWriter errorWriter;

	protected Configuration config;

	private boolean DEBUG = false;

	/**
	 * Constructs a WikiPageHandler for the given wikipage using the other given
	 * variables.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param wikipage
	 *            a wikipage to be processed
	 * @param wikiStatistics
	 *            the wikistatistics counter
	 * @param errorWriter
	 *            the writer for logging errors
	 */
	public WikiPageHandler(Configuration config, WikiPage wikipage,
			WikiStatistics wikiStatistics, WikiErrorWriter errorWriter) {
		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		if (wikipage == null) {
			throw new IllegalArgumentException("Wikipage cannot be null.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException(
					"WikiStatistics cannot be null.");
		}
		if (errorWriter == null) {
			throw new IllegalArgumentException("Error writer cannot be null.");
		}

		this.config = config;
		this.wikiPage = wikipage;
		this.wikiStatistics = wikiStatistics;
		this.errorWriter = errorWriter;
		this.tagSoupParser = new TagSoupParser();
	}

	/**
	 * Parses the given wikitext into XML by first unescaping XML tags in the
	 * wikitext, fixing improper tags using TagSoup Parser and finally generates
	 * WikiXML by using Sweble Parser.
	 * 
	 * @param pageId
	 *            the id of the wikipage
	 * @param pageTitle
	 *            the title of the wikipage
	 * @param wikitext
	 *            the content of the wikipage
	 * @return WikiXML
	 * @throws IOException
	 *             an IOException of failed parsing the given wikitext to
	 *             wikiXML
	 */
	protected String parseToXML(String pageId, String pageTitle,
			String wikitext) throws IOException {
		if (wikitext == null) {
			throw new IllegalArgumentException("Wikitext cannot be null.");
		}

		if (DEBUG) log.debug("original wikitext: " +wikitext);
		// unescape XML tags
		wikitext = StringEscapeUtils.unescapeXml(wikitext);
		wikitext = cleanPattern(wikitext);
		if (DEBUG) log.debug("cleaned wikitext: " +wikitext);
		
		// italic and bold are not repaired because they are written in
		// wiki-mark-ups
		try {
			wikitext = tagSoupParser.generate(wikitext, true);
		}
		catch (SAXException e) {
			errorWriter.logErrorPage("TAGSOUP", pageTitle, pageId, e.getCause(),
					"");
		}
		if (DEBUG) log.debug("tagSoup output: " + wikitext);
		
		// repair comment tags
//		wikitext = wikitext.replace("&lt;!--", "<!--");
//		wikitext = wikitext.replace("--&gt;", "-->");
		
		Sweble2Parser swebleParser = new Sweble2Parser(pageId, pageTitle,
				wikitext, config.getLanguageCode(), wikiStatistics,
				errorWriter, WikiXMLProcessor.wikiConfig);
		swebleParser.run();

		/*Thread swebleThread = new Thread(swebleParser, pageTitle);
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
		}*/
		return swebleParser.getWikiXML();
	}

	/**
	 * Normalizes some wiki mark-ups, such as the table markup, angle brackets
	 * and so on.
	 * 
	 * @param wikitext
	 * @return normalized wikitext
	 */
	private String cleanPattern(String wikitext) {
		// start table notation
		wikitext = wikitext.replace(":{|", "{|");
		wikitext = wikitext.replace("<Fn", "&lt;Fn");
		wikitext = wikitext.replace("Fn>", "Fn&gt;");
		
		// space before />
		Matcher matcher = endTagPattern.matcher(wikitext);
		wikitext = matcher.replaceAll("$1 />");
				
		// space for non-tag
		matcher = nonTagPattern.matcher(wikitext);
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
			String replace = StringEscapeUtils.escapeHtml4(matcher.group(1));
			replace = Matcher.quoteReplacement(replace);
			matcher.appendReplacement(sb, replace);
		}
		matcher.appendTail(sb);
		wikitext = sb.toString();
		matcher.reset();
		return wikitext;
	}

	/**
	 * Validates the WikiXML structure and generates a WikiXML file for the
	 * wikipage.
	 * 
	 * @throws IOException
	 *             if failed writing the wikiXML
	 */
	protected void writeWikiXML() throws IOException {
		if (wikiPage.getWikiXML().isEmpty()) {
			wikiStatistics.addEmptyParsedPages();
		}
		else if (validateDOM() && validatePageStructure()) {
			writeWikiXML(wikiPage.getWikiXML(), config.getOutputFolder());
			wikiStatistics.addTotalNonEmptyPages();
		}
	}

	/**
	 * Writes the current wikipage in wiki mark-ups in a file.
	 * 
	 * @throws IOException
	 *             an IOException if failed writing wikitext
	 */
	protected void writeWikitext() throws IOException {
		writeWikiXML(wikiPage.getWikitext(), config.getWikitextFolder());
		wikiPage.setWikitext(null);
	}

	/**
	 * Validate the WikiXML of the current page by using a DOM parser.
	 * 
	 * @return true if the validation is valid, false otherwise.
	 */
	private boolean validateDOM() {
		String wikiXML = "<text>" + wikiPage.getWikiXML() + "</text>";
		try {
			// test XML validity
//			DOMParser domParser = new DOMParser();
//			domParser.parseXML(
//					new ByteArrayInputStream(wikiXML.getBytes("utf-8")));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.parse(new ByteArrayInputStream(wikiXML.getBytes("utf-8")));
		}
		catch (Exception e) {
			wikiStatistics.addDomErrors();
			errorWriter.logErrorPage("DOM", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e.getCause(), "");
			return false;
		}
		return true;
	}

	/**
	 * Validates the page structure / metadata of the current wikipage by using
	 * the TagSoup parser.
	 * 
	 * @return true if the validation is valid, false otherwise.
	 */
	private boolean validatePageStructure() {
		try {
			// try fixing missing tags
			StringBuilder sb = new StringBuilder();
			sb.append(wikiPage.getPageIndent());

			sb.append(
					tagSoupParser.generate(wikiPage.getPageStructure(), false));
			wikiPage.setPageStructure(sb.toString());
		}
		catch (Exception e) {
			wikiStatistics.addPageStructureErrors();
			errorWriter.logErrorPage("PAGE ", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e.getCause(), "");
			return false;
		}
		return true;
	}

	/**
	 * Writes the content (wikitext or wikiXML) of the wikipage into the given
	 * output folder.
	 * 
	 * @param content
	 *            the wikipage content to write
	 * @param outputFolder
	 *            the output folder
	 * @throws IOException an IOException if failed writing wikiXML
	 */
	private void writeWikiXML(String content, String outputFolder)
			throws IOException {

		if (wikiPage == null) {
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}
		if (outputFolder == null || outputFolder.isEmpty()) {
			throw new IllegalArgumentException(
					"Output folder cannot be null or empty.");
		}

		if (content != null && !content.isEmpty()) {

			String path = outputFolder + "/" + wikiPage.getPageIndex() + "/";
			//System.out.println(path + wikiPage.getPageId() + ".xml");

			OutputStreamWriter writer = Utilities.createWriter(path,
					wikiPage.getPageId() + ".xml", config.getOutputEncoding());

			writer.append("<?xml version=\"1.0\" encoding=\"");
			writer.append(config.getOutputEncoding());
			writer.append("\"?>\n");

			String[] arr = wikiPage.getPageStructure().split("<text></text>");
			writer.append(arr[0]);
			writer.append("<text lang=\"" + config.getLanguageCode() + "\">\n");
			writer.append(content);
			if (!config.isDiscussion()) {
				writer.append("\n");
			}
			writer.append("      </text>");
			writer.append(arr[1]);
			writer.close();
		}
	}
}
