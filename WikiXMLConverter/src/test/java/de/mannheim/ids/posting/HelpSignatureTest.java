package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.wiki.page.WikiTalkHandler.SignatureType;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class HelpSignatureTest extends GermanTestBase {
	private WikiPostUser postUser;

	public HelpSignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
	}

	@Test
	public void testSpecialContribution()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Grüße, A.S. &lt;small&gt;(''nicht [[Hilfe:Signatur|"
				+ "signierter]] Beitrag von'' [[Spezial:Beiträge/77.4.102.159|"
				+ "77.4.102.159]] ([[Benutzer Diskussion:77.4.102.159|"
				+ "Diskussion]]) 18:07, 16. Mai 2011 (CEST)) &lt;/small&gt;";

		WikiPage wikiPage = createWikiPage("Diskussion:Außenbandruptur des "
				+ "oberen Sprunggelenkes", "131", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/small/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/small/signed/@type").get(0)
						.getValue());
	}

	@Test
	public void testUserLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Darstellung bringen und weiter im Text auf die "
				+ "Meinung von Solow eingehen. &lt;small&gt;(''nicht "
				+ "[[Hilfe:Signatur|signierter]] Beitrag von'' [[Benutzer:"
				+ "Wi-infer|Wi-infer]] ([[Benutzer Diskussion:Wi-infer|"
				+ "Diskussion]]&amp;nbsp;|&amp;nbsp;[[Spezial:Beiträge/"
				+ "Wi-infer|Beiträge]]) 18:36, 26. Mär. 2011 (CET)) "
				+ "&lt;/small&gt;";

		WikiPage wikiPage = createWikiPage("Diskussion:Arbeitsmarkt", "359",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node node = doc.query("/posting/p/small").get(0);
		assertEquals(0, node.query("/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				node.query("signed/@type").get(0).getValue());
		assertEquals("Wi-infer", node.query("signed/name").get(0).getValue());
	}

	@Test
	public void testDate()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Ich dachte Perm fängt von 280Millionen Jahre an."
				+ "[[Benutzer:Oskar Lightning]]-- &lt;small&gt;(11:45, 22. "
				+ "Mai 2010 (CEST), ''Datum/Uhrzeit nachträglich eingefügt, "
				+ "siehe [[Hilfe:Signatur]]'')&lt;/small&gt;";
		WikiPage wikiPage = createWikiPage("Diskussion:Erdzeitalter", "1347",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());

	}

	@Test
	public void testWithoutUsername()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":: Entweder ist das schlimme Didaktik, oder ein "
				+ "Troll. -Tom ( 04:00, 7. Jun. 2016 ) &lt;small&gt;(''ohne "
				+ "Benutzername [[Hilfe:Signatur|signierter]] Beitrag von'' "
				+ "[[Spezial:Beiträge/93.221.237.29|93.221.237.29]] ([[Benutzer "
				+ "Diskussion:93.221.237.29|Diskussion]]))&lt;/small&gt;";
		WikiPage wikiPage = createWikiPage("Diskussion:Computerlinguistik",
				"904", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/small/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/small/signed/@type").get(0)
						.getValue());
	}

	@Test
	public void testBelatedSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Was für eine Übertragungsart, wie z. B. [[PSK]] "
				+ "oder [[FSK]], wird für GPRS verwendet?&lt;br/&gt;"
				+ "&lt;small&gt;(Der vorstehende Beitrag stammt von "
				+ "[[Spezial:Beiträge/84.57.145.172|84.57.145.172]] "
				+ "– 21:41, 28. Feb. 2006 (MEZ) – und wurde nachträglich "
				+ "[[Hilfe:Signatur|signiert]].)&lt;/small&gt;";
		WikiPage wikiPage = createWikiPage(
				"Diskussion:General Packet Radio Service",
				"2063", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/small/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/small/signed/@type").get(0)
						.getValue());
	}

	@Test
	public void testTextAfterSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Hi, wie kommen diese Zahlen zustande? Destatis "
				+ "nennt auf Basis AZR 2015 ca. 7800. &lt;small&gt;(''nicht "
				+ "[[Hilfe:Signatur|signierter]] Beitrag von'' [[Spezial:"
				+ "Beiträge/217.110.254.126|217.110.254.126]] ([[Benutzer "
				+ "Diskussion:217.110.254.126|Diskussion]])&lt;nowiki/&gt; "
				+ "12:44, 11. Okt. 2016 (CEST))&lt;/small&gt; Könnte stimmen "
				+ "von der Zahl her auf die Einwohnerzahl umgerechnet cest. ";

		WikiPage wikiPage = createWikiPage("Diskussion:Berlin", "511",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/page/posting/p/small/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/small/signed/@type").get(0)
						.getValue());
		assertEquals("Hi, wie kommen diese Zahlen zustande? Destatis nennt auf "
				+ "Basis AZR 2015 ca. 7800. (nicht signierter Beitrag von 12:44, "
				+ "11. Okt. 2016 (CEST)) Könnte stimmen von der Zahl her auf "
				+ "die Einwohnerzahl umgerechnet cest.",
				doc.query("/posting/p").get(0).getValue());
	}

	@Test
	public void testUnsignedContribution()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Aber verschiedene Wikipedia-Artikel auf der ganzen "
				+ "Welt scheinen es besser zu wissen.&lt;small&gt;(''Der "
				+ "vorstehende, nicht signierte Beitrag – siehe dazu [[Hilfe:"
				+ "Signatur]] – stammt von'' [[Benutzer:78.53.196.134|"
				+ "78.53.196.134]] ([[Benutzer Diskussion:78.53.196.134|"
				+ "Diskussion]] • [[Spezial:Beiträge/78.53.196.134|Beiträge]]) "
				+ "22:28, 23. Sep. 2008 (CEST)) &lt;/small&gt;";

		String wikitext2 = "scheinen es besser zu wissen.&lt;small&gt;(''Der "
				+ "vorstehende, nicht signierte Beitrag – siehe dazu [[Hilfe:"
				+ "Signatur]] – stammt von'' [[Spezial:Beiträge/78.53.196.134|"
				+ "Beiträge]]) 22:28, 23. Sep. 2008 (CEST)) &lt;/small&gt;";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:General Packet Radio Service",
				"2063", true, wikitext, wikitext2);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wrapWithTextElement(wikiPage.getWikiXML());
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/text/posting/p/small/a").size());
		Nodes signatures = doc.query("/text/posting/p/small/signed");
		assertEquals(2, signatures.size());
		for (int i = 0; i < 2; i++) {
			assertEquals(SignatureType.UNSIGNED.toString(),
					signatures.get(i).query("@type").get(0).getValue());
		}
	}

	@Test
	public void testEnglishMarkup()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Thanks a lot for your help. [http://incubator."
				+ "wikimedia.org/wiki/Wp/grc Ἡ Οὐικιπαιδεία] needs you!  "
				+ "&lt;small&gt;—Preceding [[Wikipedia:Signatures|unsigned]] "
				+ "comment added by [[Special:Contributions/190.40.197.5|"
				+ "190.40.197.5]] ([[User talk:190.40.197.5|talk]]) 19:55, "
				+ "30 May 2008 (UTC)&lt;/small&gt;&lt;!-- Template:UnsignedIP"
				+ " --&gt; &lt;!--Autosigned by SineBot--&gt;";
		WikiPage wikiPage = createWikiPage(
				"Benutzer Diskussion:Lefcant",
				"1943350", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Nodes links = doc.query("/posting/p/small/a");
		assertEquals(1, links.size());
		assertEquals("unsigned", links.get(0).getValue());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/small/signed/@type").get(0)
						.getValue());
	}
}
