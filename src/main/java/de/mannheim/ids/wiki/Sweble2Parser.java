package de.mannheim.ids.wiki;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngine;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngCompiledPage;
import org.sweble.wikitext.engine.output.HtmlRenderer;
import org.sweble.wikitext.engine.output.HtmlRendererCallback;
import org.sweble.wikitext.engine.output.MediaInfo;
import org.sweble.wikitext.engine.utils.DefaultConfigEn;
import org.sweble.wikitext.parser.parser.LinkTargetException;

/* This class generates an Abstract Syntax Tree representation (AST) representation of the wikitext
 * using the Sweble Parser and eventually generates an XML representation using a visitor class.
 * */
public class Sweble2Parser {
	
	public String parseText(String wikitext, String pagetitle) 
			throws JAXBException, CompilerException, LinkTargetException, IOException {
				
		WikiConfig config = DefaultConfigEn.generate();
		
		final int wrapCol = 80;
		
		// Instantiate Sweble parser compiler
		WtEngine engine = new WtEngine(config);		
		
		// Dummy pagetitle
		PageTitle pageTitle = PageTitle.make(config, pagetitle);		
		PageId pageId = new PageId(pageTitle, -1);
		
		//wikitext = FileUtils.readFileToString(new File("xml/test.wikitext"));
		// Compile wikitext to AST
		EngCompiledPage cp = engine.postprocess(pageId, wikitext, null);		
		//System.out.println(cp);
		
		// Render AST to XML		
		String wikiXML = XMLRenderer.print(new MyRendererCallback(), config, pageTitle, cp.getPage());
		//String wikiXML = HtmlRenderer.print(new MyRendererCallback(), config, pageTitle, cp.getPage());
		
		return wikiXML;
	}	
	
	private static final class MyRendererCallback
	implements
		HtmlRendererCallback
		{
		@Override
		public boolean resourceExists(PageTitle target)
		{
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public MediaInfo getMediaInfo(
				String title,
				int width,
				int height) throws Exception
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
}
