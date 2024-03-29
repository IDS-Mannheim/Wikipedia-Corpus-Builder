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
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class SpecialContributionSignatureTest extends GermanTestBase {
	private WikiPostUser postUser;

	public SpecialContributionSignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
	}

	@Test
	public void testWithDash()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Bischen krass die behauptung. Bitte genauer "
				+ "beschreiben und belegen. --[[Spezial:Beiträge/"
				+ "91.17.241.151|91.17.241.151]] 19:12, 30. Dez. 2015 "
				+ "(CET)";
		WikiPage wikiPage = createWikiPage("Diskussion:Aldi", "277", true,
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Element signed = (Element) doc.query("/posting/p/signed").get(0);
		assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
				signed.getAttribute("type").getValue());
		assertEquals("19:12, 30. Dez. 2015 (CET)",
				signed.getChildElements("date").get(0).getValue());
		assertEquals(0, signed.getChildElements("name").size());
	}

	@Test
	public void testWithDashAndSpace()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Supinationstrauma ist das &quot;klassische"
				+ "&quot; Umknicken über die Außenkante. -- [[Spezial:"
				+ "Beiträge/134.102.16.228|134.102.16.228]] 16:28, 11. "
				+ "Aug. 2011 (CEST)";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Außenbandruptur des oberen Sprunggelenkes", "131",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());
		assertEquals("16:28, 11. Aug. 2011 (CEST)",
				doc.query("/posting/p/signed/date").get(0).getValue());
	}

	@Test
	public void testWithoutDash()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Sollen wir hier den Antisemitismus aus Sicht der "
				+ "Nazis darstellen? Total bescheuert der Vorschlag der IP. "
				+ "[[Spezial:Beiträge/84.56.116.138|84.56.116.138]] 18:21, "
				+ "2. Jun. 2017 (CEST)";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Antisemitismus (bis 1945)", "333", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());
		assertEquals("18:21, 2. Jun. 2017 (CEST)",
				doc.query("/posting/p/signed/date").get(0).getValue());
	}

	@Test
	public void testWithUserTalkLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Sie sollte aber wenigstens auf den Atikel [[Licht]] "
				+ "zurückverweisen. --[[Spezial:Beiträge/213.39.224.204|"
				+ "213.39.224.204]] ([[Benutzer Diskussion:213.39.224.204|"
				+ "Diskussion]]) 20:16, 16. Jul 2004 (CEST)";
		WikiPage wikiPage = createWikiPage(
				"Diskussion:Licht (Begriffsklärung)", "13982", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());
		assertEquals("20:16, 16. Jul 2004 (CEST)",
				doc.query("/posting/p/signed/date").get(0).getValue());
	}

	@Test
	public void testTextAfterSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "an die Nerven funktion gebunden. rho &lt;br/&gt;"
				+ "&lt;small&gt;(Der vorstehende Beitrag stammt von "
				+ "[[Spezial:Beiträge/217.2.227.180|217.2.227.180]] – 20:50, "
				+ "2. Jan. 2003 (MEZ) – und wurde nachträglich [[Hilfe:"
				+ "Unterschreiben|unterschrieben]].)&lt;/small&gt;";
		WikiPage wikiPage = createWikiPage(
				"Diskussion:Ewiges Leben", "15957", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/small/a").size());
		assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
				doc.query("/posting/p/small/signed/@type").get(0)
						.getValue());

		assertEquals("an die Nerven funktion gebunden. rho (Der vorstehende "
				+ "Beitrag stammt von 20:50, 2. Jan. 2003 (MEZ) – und wurde "
				+ "nachträglich unterschrieben.)",
				doc.query("/posting/p").get(0).getValue());
	}

	@Test
	public void testLowercase()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Der Artikel sollte diese Dichotomie bewusster "
				+ "behandeln.--[[spezial:beiträge/the 141|the artist formerly "
				+ "known as 141.84.69.20]] 14:12, 8. Dez. 2014 (CET)";
		WikiPage wikiPage = createWikiPage("Diskussion:Amok", "324773",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());
		assertEquals("14:12, 8. Dez. 2014 (CET)",
				doc.query("/posting/p/signed/date").get(0).getValue());
//		 System.out.println(wikiXML);
	}

	@Test
	public void testEnglishMarkup()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Zum PS: Da das &quot;muss&quot; mindestens "
				+ "zweifelhaft, die Problematik im eigentlichen Artikel "
				+ "[[Iodmangel]] völlig anders dargestellt und v.a. keine "
				+ "Quelle angegeben ist, schmeiße ich den Satz kurzerhand "
				+ "raus. --[[Special:Contributions/87.123.82.135|"
				+ "87.123.82.135]] 17:48, 16. Nov. 2007 (CET)";
		String wikitext2 = "raus. --[[special:contributions/87.123.82.135|"
				+ "87.123.82.135]] 17:48, 16. Nov. 2007 (CET)";

		WikiPage wikiPage = createWikiPage("Diskussion:Iod", "12765", true,
				wikitext, wikitext2);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = "<page>" + wikiPage.getWikiXML() + "</page>";
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/page/posting/p/a").size());
		Nodes signatures = doc.query("/page/posting/p/signed");
		assertEquals(2, signatures.size());
		for (int i = 0; i < 2; i++) {
			assertEquals(SignatureType.SPECIAL_CONTRIBUTION.toString(),
					signatures.get(i).query("@type").get(0).getValue());
			assertEquals("17:48, 16. Nov. 2007 (CET)",
					signatures.get(i).query("date").get(0).getValue());
		}
	}

}
