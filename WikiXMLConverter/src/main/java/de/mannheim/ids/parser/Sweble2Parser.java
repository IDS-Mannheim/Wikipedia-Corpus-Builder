package de.mannheim.ids.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngine;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.Interwiki;
import org.sweble.wikitext.engine.config.InterwikiImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.output.HtmlRendererCallback;
import org.sweble.wikitext.engine.output.MediaInfo;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.sweble.wikitext.engine.utils.UrlEncoding;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.xml.sax.SAXException;

import de.mannheim.ids.wiki.WikiXMLProcessor;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.writer.WikiErrorWriter;

/**
 * Convert wikitext to wikiXML using Sweble parser
 * 
 * @author margaretha
 * 
 */
public class Sweble2Parser implements Runnable {

	private String wikitext, wikiXML;
	private String pagetitle, pageIdStr;
	private WikiStatistics wikiStatistics;
	private WikiErrorWriter errorWriter;
	private Logger log = LogManager.getLogger(Sweble2Parser.class);
	private WikiConfig wikiConfig;
	
	public static boolean DEBUG = false;

	/**
	 * Generate an Abstract Syntax Tree representation (AST) representation of a
	 * given wikitext using the Sweble Parser 2.0.0 version, and eventually
	 * generates an XML representation using a visitor class.
	 * 
	 * @param pageId
	 *            the wiki page id
	 * @param pagetitle
	 *            the wiki page title
	 * @param wikitext
	 *            the wikitext content of the page
	 * @param language
	 *            the language of the page
	 * @param wikiStatistics
	 *            a statistic counter
	 * @param errorWriter
	 *            a WikiErrorWriter
	 */
	public Sweble2Parser(String pageId, String pagetitle, String wikitext,
			String language, WikiStatistics wikiStatistics,
			WikiErrorWriter errorWriter, WikiConfig wikiConfig) {
		if (wikitext == null || wikitext.isEmpty()) {
			throw new IllegalArgumentException(
					"Wikitext cannot be null or empty.");
		}
		if (pageId == null || pageId.isEmpty()) {
			throw new IllegalArgumentException(
					"PageId cannot be null or empty.");
		}
		if (pagetitle == null || pagetitle.isEmpty()) {
			throw new IllegalArgumentException(
					"Pagetitle cannot be null or empty.");
		}
		if (language == null || language.isEmpty()) {
			throw new IllegalArgumentException(
					"Language cannot be null or empty.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException(
					"WikiStatistics cannot be null.");
		}
		if (errorWriter == null) {
			throw new IllegalArgumentException(
					"WikiErrorWriter cannot be null.");
		}
		if (wikiConfig == null){
			try {
				this.wikiConfig = LanguageConfigGenerator.generateWikiConfig(language);
			}
			catch (IOException | ParserConfigurationException
					| SAXException e) {
				e.printStackTrace();
			}
		}
		else {
			this.wikiConfig = wikiConfig;
		}

		this.wikitext = wikitext;
		this.pageIdStr = pageId;
		this.pagetitle = pagetitle;
		this.wikiStatistics = wikiStatistics;
		this.errorWriter = errorWriter;
		this.wikiXML = "";
	}

	@Override
	public void run() {
		WtEngine engine = new WtEngineImpl(wikiConfig);
		PageTitle pageTitle = null;
		EngProcessedPage cp = null;
		PageId pageId;
		try {
			pageTitle = PageTitle.make(wikiConfig, pagetitle);
			pageId = new PageId(pageTitle, -1);
//			log.debug(wikitext);
			// Parse Wikitext into AST
			cp = engine.postprocess(pageId, wikitext, null);
//			log.debug(cp);
		}
		catch (LinkTargetException | EngineException e) {
			wikiStatistics.addSwebleErrors();
			errorWriter.logErrorPage("SWEBLE", pagetitle, pageIdStr,
					e.getCause(), wikitext);
			return;
		}

		try {
			Writer w = new StringWriter();
			// Render AST to XML
//			XMLRenderer renderer = new XMLRenderer(new RendererCallbackImpl(),
//					WikiXMLProcessor.wikiconfig, pageTitle, w);
			XMLRenderer3 renderer = new XMLRenderer3(new RendererCallbackImpl(),
					wikiConfig, pageTitle, w);
			renderer.setPageId(pageId);
			renderer.go(cp.getPage());
			wikiXML = w.toString();
		}
		catch (Exception e) {
			wikiStatistics.addRendererErrors();
			errorWriter.logErrorPage("RENDERER", pagetitle, pageIdStr,
					e.getCause(), wikitext);

			//e.printStackTrace();
		}
		cp = null;
	}

	public String getWikiXML() {
		return wikiXML;
	}

	public void setWikiXML(String wikiXML) {
		this.wikiXML = wikiXML;
	}

	private static final class RendererCallbackImpl
			implements
				HtmlRendererCallback {
		
		private Logger log = LogManager.getLogger(RendererCallbackImpl.class);
		protected static final String LOCAL_URL = WikiXMLProcessor.Wikipedia_URI;

		@Override
		public boolean resourceExists(PageTitle target) {
			return true;
		}

		@Override
		public MediaInfo getMediaInfo(String title, int width, int height){
			if (DEBUG) log.debug("MediaInfo: " +title);
			MediaInfo mi = new MediaInfo(title, null, null, width, height, null,
					-1, -1);
			return mi;
		}

		public String makeUrl(PageTitle target) {
			InterwikiImpl interwiki = (InterwikiImpl) target.getInterwikiLink();
			if (interwiki!=null){
				String parametrizedUrl = interwiki.getUrl();
				// url without interwiki prefix
				String title = target.getDenormalizedTitle();
				String encodedTitle = UrlEncoding.WIKI.encode(title);
				 try {
					URL url = new URL(parametrizedUrl.replace("$1", encodedTitle));
					return url.toString();
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
					// title with interwiki prefix
					return target.getUrl().toString();
				}
			}
			else {
				return target.getUrl().toString();
			}
			
//			String page = target.getNormalizedFullTitle();
//			String f = target.getFragment();
//			String url = page;
//			if (f != null && !f.isEmpty())
//				url = page + "#" + UrlEncoding.WIKI.encode(f);
//			try {
//				url = URLEncoder.encode(url, "UTF-8");
//			}
//			catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//
//			return LOCAL_URL + url;
		}

		public String makeUrl(WtUrl target) {
			// Hack for URL ending with &amp; due to Sweble/AST invalid parsing
			// leaving out the ;
			String path = target.getPath();
			if (path.contains("&amp")) {
				path = path.replace("&amp;", "&amp");
				path = path.replace("&amp", "&amp;");
			}
			if (path.contains("&lt")) {
				path = path.replace("&lt;", "&lt");
				path = path.replace("&lt", "&lt;");
			}
			if (path.contains("&gt")) {
				path = path.replace("&gt;", "&gt");
				path = path.replace("&gt", "&gt;");
			}

			if (target.getProtocol() == "")
				return path;
			return target.getProtocol() + ":" + path;
		}

		public String makeUrlMissingTarget(String path) {
			return LOCAL_URL + "?title=" + path
					+ "&amp;action=edit&amp;redlink=1";

		}
	}
}
