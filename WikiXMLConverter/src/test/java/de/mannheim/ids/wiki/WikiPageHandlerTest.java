package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.page.WikiArticleHandler;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class WikiPageHandlerTest extends GermanTestBase {

	@Test
	public void testTalkHandler() throws InterruptedException, IOException {
		String wikidump = "src/test/resources/wikitext/dewiki-20170701-9756545.xml";
		Configuration config = createUserTalkConfig(wikidump);
		WikiPage wikiPage = WikiPageReaderTest.readPage(config);
		assertTrue(wikiPage.getWikiXML().isEmpty());

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiStatistics stat = new WikiStatistics();
		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				stat, new WikiErrorWriter(), postUser);

		handler.run();
		assertNotNull(wikiPage.getWikiXML());

		postUser.close();
		stat.print();
	}

	@Test
	public void testEmptyPage()
			throws IOException, ValidityException, ParsingException {
		WikiStatistics stat = new WikiStatistics();
		WikiErrorWriter errorWriter = new WikiErrorWriter();

		String wikitext = "";
		WikiPage wikiPage = createWikiPage("Empty-page1", "1", false,
				wikitext);
		WikiArticleHandler handler = new WikiArticleHandler(articleConfig,
				wikiPage, stat, errorWriter);
		handler.run();
		assertEquals(1, stat.getEmptyParsedPages());

		wikitext = "too short";
		wikiPage = createWikiPage("Empty-page2", "2", false,
				wikitext);
		handler = new WikiArticleHandler(articleConfig,
				wikiPage, stat, errorWriter);
		handler.run();
		assertEquals(2, stat.getEmptyParsedPages());
	}

	@Test
	public void testEmptyTalkPages()
			throws IOException, ValidityException, ParsingException {
		WikiStatistics stat = new WikiStatistics();
		WikiErrorWriter errorWriter = new WikiErrorWriter();
		WikiPostUser postUser = new WikiPostUser("test", "talk");
		
		String wikitext = "...";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:AnjjaBaumann",
				"3", true, wikitext);
		WikiTalkHandler talkHandler = new WikiTalkHandler(userTalkConfig,
				wikiPage, stat, errorWriter, postUser);
		talkHandler.run();
		assertEquals(1, stat.getEmptyParsedPages());

		wikitext = "{{gesperrter Benutzer}}";
		wikiPage = createWikiPage("Benutzer Diskussion:Greekstar", "4", true,
				wikitext);
		talkHandler = new WikiTalkHandler(userTalkConfig, wikiPage, stat,
				errorWriter, postUser);
		talkHandler.run();
		assertEquals(2, stat.getEmptyParsedPages());
	}

}
