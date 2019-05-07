package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
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
	public void testSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Gibt es ein persönliches Adressbuch, oder eine "
				+ "ähnliche Funktion bei Wikipedia? --[[Benutzer:Burggraf17|"
				+ "Burggraf17]] 10:04, 6. Mär 2004 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("10:04, 6. Mär 2004 (CET)", timestamp.getValue());
	}

	@Test
	public void testSignatureLowercase()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "oder eine ähnliche Funktion bei Wikipedia? "
				+ "--[[benutzer:Burggraf17|Burggraf17]] 10:04, 6. Mär "
				+ "2004 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
	}

	@Test
	public void testSignatureWithoutTimestamp()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "oder eine ähnliche Funktion bei Wikipedia? "
				+ "--[[benutzer:Burggraf17|Burggraf17]]";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(0, doc.query("/posting/p/autoSignature/timestamp").size());
	}

	@Test
	public void testSignatureWithoutLinkText()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Grüsse, und bis bald wiedermal :-) [[Benutzer:Fantasy]]";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(0, doc.query("/posting/p/autoSignature/timestamp").size());
	}

	@Test
	public void testSignatureWithoutDash()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Grüsse, und bis bald wiedermal :-) [[Benutzer:Fantasy]] "
				+ "[[Benutzer_Diskussion:Fantasy|容]] 11:28, 17. Jul 2006 (CEST)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("11:28, 17. Jul 2006 (CEST)", timestamp.getValue());
	}

	@Test
	public void testSignatureWithDashAndSpace()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "oder eine ähnliche Funktion bei Wikipedia? "
				+ "-- [[Benutzer:Burggraf17|Burggraf17]] 10:04, 6. Mär "
				+ "2004 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());
	}

	@Test
	public void testSignatureWithColon()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "::::: Nun, wer hat den Satz ''Gewerkschaften "
				+ "sind keine Verein im rechtlichen Sinne.'' hineingestellt "
				+ "und warum? -- [[:Benutzer:Fgb|Fgb]].";
		WikiPage wikiPage = createWikiPage("Diskussion:Arbeitsmarkt",
				"359", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals(0, doc.query("/posting/p/autoSignature/timestamp").size());
		assertEquals("signed", signature.getValue());
	}

	@Test
	public void testSignatureWithStyle()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Das ist aber nicht mein Fachgebiet, daher mache "
				+ "ich es nicht selbst. --[[Benutzer:Mopskatze|Mopskatze]]"
				+ "℅&lt;small&gt;&lt;sup&gt;[[Benutzer_Diskussion:Mopskatze|"
				+ "Miau!]]&lt;/sup&gt;&lt;/small&gt; 02:46, 4. Jul. 2010 (CEST)";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Außenbandruptur des oberen Sprunggelenkes",
				"131", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("02:46, 4. Jul. 2010 (CEST)", timestamp.getValue());
	}

	@Test
	public void testSignatureUserLinkWithStyle()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Dafür ist doch die Diskussionsseite da. --[["
				+ "Benutzer:ZOiDberg|&lt;span style=&quot;padding: 1px; "
				+ "color: #808080; border-width: 1px; border-style: dotted; "
				+ "border-color: #DC143C;&quot;&gt;z&lt;b style=&quot;color: "
				+ "red;&quot;&gt;O&lt;/b&gt;i&lt;b style=&quot;color: red;"
				+ "&quot;&gt;D&lt;/b&gt;berg&lt;/span&gt;]] &lt;sup&gt;([["
				+ "Benutzer_Diskussion:ZOiDberg|Diskussion]])&lt;/sup&gt; "
				+ "07:42, 16. Aug 2006 (CEST)";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Eulersche Zahl",
				"1338", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
	}

	@Test
	public void testSignatureWithExtraUserVariantLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Kapitel 2, Entstehungszeit endet 1933 mit Beginn von "
				+ "Kapitel 3, Vorkriegszeit. [[Benutzer:Sargoth|Sargoth]][[Benutzer "
				+ "Diskussion:Sargoth|&lt;sup&gt;¿!&lt;/sup&gt;]][[Benutzer:"
				+ "Sargoth/Bewertung|&lt;sup&gt;±&lt;/sup&gt;]] 11:22, 25. Mär. "
				+ "2008 (CET)";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Zeit des Nationalsozialismus/Archiv/1",
				"1169", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
	}

	@Test
	public void testSignatureWithPostscript()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Kann das vielleicht jemand der diskutierenden "
				+ "Gemeinde auf youtube mitteilen, damit niemand unwissend "
				+ "stirbt?--[[Benutzer:Chef|Pangloss]] [[Benutzer Diskussion:"
				+ "Chef|Diskussion]] 05:17, 28. Jul 2006 (CEST)&lt;small&gt; "
				+ "mal wieder eine Nacht sinnvoll verbracht&lt;/small&gt;";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Friedrich Nietzsche/Archiv/1",
				"1627", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node ps = doc.query("/posting/p/small").get(0);
		assertEquals(" mal wieder eine Nacht sinnvoll verbracht",
				ps.getValue());
	}

	@Test
	public void testSignatureInListWithPostscript()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "*{{neutral}}, weil selbst überarbeitet --"
				+ "[[Benutzer:CHK|CHK]] 09:18, 1. Jan 2006 (CET)    "
				+ "PS: Könnte vielleicht irgend jemand ein Mal endlich "
				+ "die Ladungen bei den Reaktionsgleichungen hochstellen!?";
		WikiPage wikiPage = createWikiPage(
				"Diskussion:Außenbandruptur des oberen Sprunggelenkes",
				"131", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/ul/li/a").size());

		Node ps = doc.query("/posting/ul/li/seg").get(0);
		assertEquals("postscript", ps.query("@type").get(0).getValue());
		assertEquals(" PS: Könnte vielleicht irgend jemand ein Mal endlich "
				+ "die Ladungen bei den Reaktionsgleichungen hochstellen!?",
				ps.getValue());
	}

	@Test
	public void testSignatureWithEnglishMarkup()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Dem ist ja nicht so, es handelt sich lediglich "
				+ "um einen Faktor, der im Standardmodell keine Berücksichtigung "
				+ "findet. —[[User:Pill|Pill]] ([[User talk:Pill|Kontakt]]) "
				+ "00:30, 11. Jun. 2009 (CEST)";

		String wikitext2 = "Gruß, —[[user:Pill|Pill]] 00:30, 11. Jun. 2009 (CEST)";

		WikiPage wikiPage = createWikiPage("Diskussion:Arbeitsmarkt", "359",
				wikitext, wikitext2);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = "<page>\n" + wikiPage.getWikiXML() + "\n</page>";
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/page/posting/p/a").size());
		assertEquals(2, doc.query("/page/posting/p/autoSignature").size());
	}

	@Test
	public void testSignatureInTemplate()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "{{Diskussion aufgeräumt|24. Februar 2014|2="
				+ "[https://de.wikipedia.org/w/index.php?title=Diskussion:"
				+ "Hauspferd&amp;oldid=124833777 Version vom 25. November 2013"
				+ "]|3=[[Benutzer:Fallen Sheep|Fallen Sheep]] ([[Benutzer "
				+ "Diskussion:Fallen Sheep|Diskussion]]) 00:17, 24. Feb. 2014 "
				+ "(CET)}}{{Autoarchiv |Alter=180 |Ziel='((Lemma))/Archiv/1'"
				+ "|Übersicht=[[Diskussion:Hauspferd/Archiv/1|Archiv]]|"
				+ "Mindestbeiträge=1 |Mindestabschnitte =3 |Frequenz="
				+ "monatlich}}";
		WikiPage wikiPage = createWikiPage("Diskussion:Hauspferd", "12765",
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Nodes spans = doc.query("/posting/p/span[@class='template']");
		assertEquals(2, spans.size());
	}

	@Test
	public void testUserLinkNotSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Du solltest die Neugliedeurng mit [[benutzer:Ot]] "
				+ "abstimmen. --[[Benutzer:Armin P.|Armin]] ([[Benutzer "
				+ "Diskussion:Armin P.|Diskussion]]) 19:29, 14. Aug. 2012 "
				+ "(CEST)";
		WikiPage wikiPage = createWikiPage("Diskussion:Niccolò Machiavelli",
				"36867", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
	}

	// Problematic case
	// User links are always recognized as signatures
	@Ignore
	@Test
	public void testUserLinkWithoutSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Tatsache ist, das alles mit allem zusammenhängt, "
				+ "und das sowohl Du [[benutzer:DaTroll|Datroll]] recht hast, "
				+ "wie auch der anonyme Benutzer, den Du revertet hast.";
		WikiPage wikiPage = createWikiPage("Diskussion:Mathematik/Archiv/1",
				"3250", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
	}
}
