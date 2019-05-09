package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.wiki.config.Configuration;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiPageReader;
import de.mannheim.ids.wiki.page.WikiStatistics;

public class WikiPageReaderTest extends GermanTestBase{

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
		Configuration config = createUserTalkConfig(wikidump);
		WikiPage wikiPage = readPage(config);
		assertEquals("9756545", wikiPage.getPageId());
		assertEquals("Benutzer Diskussion:Abu-Dun/Archiv/2017",
				wikiPage.getPageTitle());
		assertEquals("A", wikiPage.getPageIndex());
		assertNotNull(wikiPage.getPageStructure());
		assertTrue(wikiPage.textSegments.size() > 0);
	}

	@Test
	public void testTitle() throws IOException, InterruptedException, XMLStreamException {
		String wikidump = "src/test/resources/wikitext/dewiki-20170701-1422522.xml";
		Configuration config = createTalkConfig(wikidump);
		WikiPage wikiPage = readPage(config);
		assertEquals("1422522", wikiPage.getPageId());
		assertEquals("Diskussion:.460 S&amp;W Magnum",
				wikiPage.getPageTitle());
		assertEquals("Char", wikiPage.getPageIndex());
		assertNotNull(wikiPage.getPageStructure());
		assertTrue(wikiPage.textSegments.size() > 0);
	}
}
