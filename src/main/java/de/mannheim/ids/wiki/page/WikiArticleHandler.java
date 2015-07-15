package de.mannheim.ids.wiki.page;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.writer.WikiErrorWriter;

/**
 * This class implements methods for handling Wikipages including reading page
 * content, cleaning wikitext (pre-processing), parsing, and XML validation.
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

			String wikiXML = parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), wikiPage.wikitext);
			if (Thread.interrupted()) {
				return;
			}

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
