package de.mannheim.ids.wiki.page;

import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.writer.WikiErrorWriter;

/**
 * An extension of WikiPageHandler for handling article pages
 * 
 * @author margaretha
 * 
 */
public class WikiArticleHandler extends WikiPageHandler {

	public WikiArticleHandler(Configuration config, WikiPage wikipage,
			WikiStatistics wikiStatistics, WikiErrorWriter errorWriter) {
		super(config, wikipage, wikiStatistics, errorWriter);
	}

	@Override
	public void run() {

		try {
			if (config.isWikitextToGenerate()) {
				writeWikitext();
			}
			
			System.out.println(wikiPage.getPageIndex() + "/"+wikiPage.getPageId() + ".xml");
			String wikiXML = parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), wikiPage.getWikitext());
			//System.out.println("finished "+wikiPage.getPageId() + ".xml");
			wikiPage.setWikiXML(wikiXML);
			writeWikiXML();
		}
		catch (Exception e) {
			wikiStatistics.addUnknownErrors();
			errorWriter.logErrorPage("HANDLER", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e, "");
		}
	}
}
