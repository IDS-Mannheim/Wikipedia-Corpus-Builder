package de.mannheim.ids.wiki.page;

import de.mannheim.ids.wiki.Configuration;
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
			String wikiXML = parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), wikiPage.getWikitext());

			wikiPage.setWikiXML(wikiXML);
			writeWikiXML();

			if (config.isWikitextToGenerate()) {
				writeWikitext();
			}
		}
		catch (Exception e) {
			wikiStatistics.addUnknownErrors();
			errorWriter.logErrorPage("HANDLER", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e, "");
		}
	}
}
