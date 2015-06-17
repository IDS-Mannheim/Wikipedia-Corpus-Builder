package de.mannheim.ids.parser;

import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngine;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.output.HtmlRendererCallback;
import org.sweble.wikitext.engine.output.MediaInfo;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.engine.utils.UrlEncoding;
import org.sweble.wikitext.parser.nodes.WtUrl;

import de.mannheim.ids.util.WikiStatistics;

/** Convert wikitext to XML
 *  
 *  @author margaretha
 *  
 * */
public class Sweble2Parser implements Runnable{
	
	public static String wikipedia_URI;
	private WikiConfig config;
	private WtEngine engine;
	private String wikitext, pagetitle, wikiXML;	
	private WikiStatistics wikiStatistics;
	
	/** Generate an Abstract Syntax Tree representation (AST) representation 
	 *  of a given wikitext using the Sweble Parser 2.0.0 version,
	 *  and eventually generates an XML representation using a visitor class.
	 * 
	 * @param wikitext
	 * @param pagetitle
	 * @param language
	 */
	public Sweble2Parser(String wikitext, String pagetitle, String language, WikiStatistics wikiStatistics) {
		config = DefaultConfigEnWp.generate();
		// Instantiate Sweble parser
		engine = new WtEngineImpl(config);
		wikipedia_URI = language+".wikipedia.org/wiki/";
		
		if (language==null || language.isEmpty()){
			throw new IllegalArgumentException("Language cannot be null or empty.");
		}
		this.wikitext = wikitext;
		this.pagetitle = pagetitle;
		this.wikiStatistics = wikiStatistics;		
		this.wikiXML="";
	}
	
	@Override
	public void run(){		
		try {
			PageTitle pageTitle = PageTitle.make(config, pagetitle);
			PageId pageId = new PageId(pageTitle, -1);
			// Parse Wikitext into AST
			EngProcessedPage cp = engine.postprocess(pageId, wikitext, null);
			
			// Render AST to XML
			wikiXML = XMLRenderer.print(new MyRendererCallback(), config, pageTitle, cp.getPage());
		}
		catch (Exception e) {
			wikiStatistics.addSwebleErrors();
			wikiStatistics.logErrorPage("SWEBLE: "+pagetitle + ", cause: "+e.getMessage());			
		}
	}
	
	public String getWikiXML() {
		return wikiXML;
	}
	public void setWikiXML(String wikiXML) {
		this.wikiXML = wikiXML;
	}
	
	private static final class MyRendererCallback implements HtmlRendererCallback {
		protected static final String LOCAL_URL = wikipedia_URI;
		
		@Override
		public boolean resourceExists(PageTitle target)
		{
			return true;
		}
		
		@Override
		public MediaInfo getMediaInfo(
				String title,
				int width,
				int height) throws Exception
		{
			return null;
		}

		public String makeUrl(PageTitle target)
		{
			String page = UrlEncoding.WIKI.encode(target.getNormalizedFullTitle());
			String f = target.getFragment();
			String url = page;
			if (f != null && !f.isEmpty())
				url = page + "#" + UrlEncoding.WIKI.encode(f);
			return LOCAL_URL + url;
		}
		
		public String makeUrl(WtUrl target)
		{
			if (target.getProtocol() == "")
				return target.getPath();
			return target.getProtocol() + ":" + target.getPath();
		}
		
		public String makeUrlMissingTarget(String path)
		{
			return LOCAL_URL + "?title=" + path + "&amp;action=edit&amp;redlink=1";
			
		}
	}
}
