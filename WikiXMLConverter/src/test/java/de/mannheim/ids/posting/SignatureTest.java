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
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class SignatureTest extends GermanTestBase {

	private WikiPostUser postUser;
	private WikiPostTime postTime;

	public SignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
		postTime = new WikiPostTime("test", "talk");
	}

	@Test
	public void testSignatureInTemplate()
			throws IOException, ValidityException, ParsingException {
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Diskussion:Hauspferd");
		wikiPage.setPageId("12765");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add("{{Diskussion aufgeräumt|24. Februar 2014|2="
				+ "[https://de.wikipedia.org/w/index.php?title=Diskussion:"
				+ "Hauspferd&amp;oldid=124833777 Version vom 25. November 2013"
				+ "]|3=[[Benutzer:Fallen Sheep|Fallen Sheep]] ([[Benutzer "
				+ "Diskussion:Fallen Sheep|Diskussion]]) 00:17, 24. Feb. 2014 "
				+ "(CET)}}{{Autoarchiv |Alter=180 |Ziel='((Lemma))/Archiv/1'"
				+ "|Übersicht=[[Diskussion:Hauspferd/Archiv/1|Archiv]]|"
				+ "Mindestbeiträge=1 |Mindestabschnitte =3 |Frequenz="
				+ "monatlich}}");

		int namespaceKey = 1; // diskussion
		Configuration config = createConfig(wikidump, namespaceKey, "talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Nodes spans = doc.query("/posting/p/span[@class='template']");
		assertEquals(2, spans.size());
	}

	@Test
	public void testSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Ich habe über den Diskussionsteil auch schon "
				+ "Kontakt mit einigen Wikipedianern gehabt. Wie kann "
				+ "ich mir deren Namen merken? Gibt es ein persönliches "
				+ "Adressbuch, oder eine ähnliche Funktion bei Wikipedia?"
				+ "--[[Benutzer:Burggraf17|Burggraf17]] 10:04, 6. Mär 2004 (CET)";
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Benutzer Diskussion:Fantasy");
		wikiPage.setPageId("23159");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(wikitext);

		int namespaceKey = 3; // benutzer diskussion
		Configuration config = createConfig(wikidump, namespaceKey,
				"user-talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("10:04, 6. Mär 2004 (CET)", timestamp.getValue());
	}

	@Test
	public void testSignatureWithoutDash()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Grüsse, und bis bald wiedermal :-) [[Benutzer:Fantasy]] "
				+ "[[Benutzer_Diskussion:Fantasy|容]] 11:28, 17. Jul 2006 (CEST)";
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Benutzer Diskussion:Fantasy");
		wikiPage.setPageId("23159");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(wikitext);

		int namespaceKey = 3; // benutzer diskussion
		Configuration config = createConfig(wikidump, namespaceKey,
				"user-talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("11:28, 17. Jul 2006 (CEST)", timestamp.getValue());
	}
}
