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
			wikiPage.setWikiXML(parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), wikiPage.wikitext));
			if (config.isWikitextToGenerate()) {
				writeWikitext();
			}
			writeWikiXML();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
