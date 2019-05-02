package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class UnsignedSignatureTest extends GermanTestBase {

	@Test
	public void testGermanUnsigniert()
			throws IOException, ValidityException, ParsingException {

		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Diskussion:Zelle (Biologie)");
		wikiPage.setPageId("14131");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(
				"{{Unsigniert|87.122.213.193|23:57, 18. Nov. 2007|--"
						+ "[[Benutzer:Roo1812|Roo1812]] 08:41, 19. Nov. 2007 "
						+ "(CET)}}");
		wikiPage.textSegments.add(
				"{{unsigniert|87.122.213.193|23:57, 18. Nov. 2007|--"
						+ "[[Benutzer:Roo1812|Roo1812]] 08:41, 19. Nov. 2007 "
						+ "(CET)}}");

		int namespace = 1;
		Configuration config = createConfig(wikidump, namespace, "talk");

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiPostTime postTime = new WikiPostTime("test", "talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build("<text>" + wikiXML + "</text>", null);
		Node signature = doc.query("/text/posting/p/autoSignature/@type")
				.get(0);
		assertEquals("unsigned", signature.getValue());
		signature = doc.query("/text/posting/p/autoSignature/@type").get(1);
		assertEquals("unsigned", signature.getValue());
	}

	@Test
	public void testEnglishUnsigned()
			throws IOException, ValidityException, ParsingException {

		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Diskussion:Synergetik-Therapie/Archiv1");
		wikiPage.setPageId("1152863");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(
				"{{Unsigned|Synergetik-therapie@web.de|[[Benutzer:MBq|"
						+ "MBq]] 09:30, 29. Dez 2005 (CET)}}");
		wikiPage.textSegments.add(
				"{{unsigned|Synergetik-therapie@web.de|[[Benutzer:MBq|"
						+ "MBq]] 09:30, 29. Dez 2005 (CET)}}");

		int namespace = 1;
		Configuration config = createConfig(wikidump, namespace, "talk");

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiPostTime postTime = new WikiPostTime("test", "talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		System.out.println(wikiXML);
		Document doc = builder.build("<text>" + wikiXML + "</text>", null);
		Node signature = doc.query("/text/posting/p/autoSignature/@type")
				.get(0);
		assertEquals("unsigned", signature.getValue());

		signature = doc.query("/text/posting/p/autoSignature/@type").get(1);
		assertEquals("unsigned", signature.getValue());
	}
}
