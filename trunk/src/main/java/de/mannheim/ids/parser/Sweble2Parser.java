package de.mannheim.ids.parser;

import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngine;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngCompiledPage;
import org.sweble.wikitext.engine.output.HtmlRendererCallback;
import org.sweble.wikitext.engine.output.MediaInfo;
import org.sweble.wikitext.engine.utils.DefaultConfigEn;

import de.mannheim.ids.util.WikiStatistics;

/** Convert wikitext to XML
 *  
 *  @author margaretha
 *  
 * */
public class Sweble2Parser implements Runnable{
	
	private WikiConfig config;
	private WtEngine engine;
	private String wikitext, pagetitle, language, wikiXML;	
	private WikiStatistics wikiStatistics;
	
	/** Generate an Abstract Syntax Tree representation (AST) representation 
	 *  of a given wikitext using the Sweble Parser 2.0.0-alpha-2-SNAPSHOT version,
	 *  and eventually generates an XML representation using a visitor class.
	 * 
	 * @param wikitext
	 * @param pagetitle
	 * @param language
	 */
	public Sweble2Parser(String wikitext, String pagetitle, String language, WikiStatistics wikiStatistics) {
		config = DefaultConfigEn.generate();
		// Instantiate Sweble parser
		engine = new WtEngine(config);
		
		if (language==null || language.isEmpty()){
			throw new IllegalArgumentException("Language cannot be null or empty.");
		}
		this.wikitext = wikitext;
		this.pagetitle = pagetitle;
		this.language = language;
		this.wikiStatistics = wikiStatistics;		
		this.wikiXML="";
	}
	
	private static final class MyRendererCallback
	implements
		HtmlRendererCallback
		{
		@Override
		public boolean resourceExists(PageTitle target)
		{
			return false;
		}
		
		@Override
		public MediaInfo getMediaInfo(
				String title,
				int width,
				int height) throws Exception
		{
			return null;
		}
	}

	@Override
	public void run(){		
		try {
			PageTitle pageTitle = PageTitle.make(config, pagetitle);
			PageId pageId = new PageId(pageTitle, -1);
			// Parse Wikitext into AST
			EngCompiledPage cp = engine.postprocess(pageId, wikitext, null);
			
			// Render AST to XML		
			String uri = language+".wikipedia.org/wiki/";
			wikiXML = XMLRenderer.print(new MyRendererCallback(), config, pageTitle, cp.getPage(),uri);
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
}
