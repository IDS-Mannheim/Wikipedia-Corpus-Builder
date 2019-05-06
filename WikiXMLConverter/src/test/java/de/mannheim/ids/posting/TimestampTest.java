package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.mannheim.ids.base.GermanTestBase;
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

public class TimestampTest extends GermanTestBase {

	@Test
	public void testMissingTextBug()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {

		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Benutzer Diskussion:Abu-Dun/Archiv/2017");
		wikiPage.setPageId("9756545");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add("Moin Abu-Dun,&lt;br /&gt;der Artikel "
				+ "[[Journey (Computerspiel 2012)]] enthält ziemlich viele "
				+ "defekte Weblinks. Magst Du Dich evtl. mal darum kümmern?"
				+ "&lt;br /&gt;Vielen Dank und viele Grüße, Grueslayer "
				+ "21:08, 27. Feb. 2017 (CET)");

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiPostTime postTime = new WikiPostTime("test", "talk");

		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(),
				new WikiErrorWriter(), postUser, postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		// System.out.println(wikiXML);
		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/posting/p/a[1]").get(0);
		assertEquals("Journey (Computerspiel 2012)", a.getValue());

		Node timestamp = doc.query("/posting/p/autoSignature/timestamp[1]")
				.get(0);
		assertEquals("21:08, 27. Feb. 2017 (CET)", timestamp.getValue());
	}
}
