package de.mannheim.ids.wiki;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;

public class WikiPageHandlerTest extends GermanTestBase {

	@Test
	public void testTalkHandler() throws InterruptedException, IOException {
		String wikidump = "src/test/resources/wikitext/dewiki-20170701-9756545.xml";
		Configuration config = createUserTalkConfig(wikidump);
		WikiPage wikiPage = WikiPageReaderTest.readPage(config);
		assertTrue(wikiPage.getWikiXML().isEmpty());

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiPostTime postTime = new WikiPostTime("test", "talk");
		
		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);

		handler.run();
		assertNotNull(wikiPage.getWikiXML());

		postUser.close();
		postTime.close();
	}
}
