package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiPageReader;
import de.mannheim.ids.wiki.page.WikiStatistics;

public class WikiPageReaderTest {

	public static Configuration createConfig(String wikidump, int namespace,
			String pageType) {
		String language = "de";
		String userPage = "Benutzer";
		String userContribution = "Spezial:Beiträge";
		String helpSignature = "Hilfe:Signatur";
		String unsigned = "unsigniert";
		int maxThread = 1;
		boolean generateWikitext = false;

		Configuration config = new Configuration(wikidump, language, userPage,
				userContribution, helpSignature, unsigned, namespace, pageType,
				null, null, maxThread, generateWikitext);

		return config;
	}

	public static WikiPage readPage(Configuration config) throws InterruptedException {
		BlockingQueue<WikiPage> wikipages = new ArrayBlockingQueue<WikiPage>(
				config.getMaxThreads());

		WikiPageReader wikiReader = new WikiPageReader(config, wikipages,
				new WikiPage(), new WikiStatistics());

		Thread thread = new Thread(wikiReader, "wikiReader");
		thread.start();

		WikiPage wikiPage = wikipages.take();
		return wikiPage;
	}

	@Test
	public void readTalkPage() throws IOException, InterruptedException {
		String wikidump = "src/test/resources/wikitext/dewiki-20170701-9756545.xml";
		Configuration config = createConfig(wikidump, 3, "talk");
		WikiPage wikiPage = readPage(config);
		assertEquals("9756545", wikiPage.getPageId());
		assertEquals("Benutzer Diskussion:Abu-Dun/Archiv/2017",
				wikiPage.getPageTitle());
		assertEquals("A", wikiPage.getPageIndex());
		assertNotNull(wikiPage.getPageStructure());
		assertTrue(wikiPage.textSegments.size() > 0);
	}

}
