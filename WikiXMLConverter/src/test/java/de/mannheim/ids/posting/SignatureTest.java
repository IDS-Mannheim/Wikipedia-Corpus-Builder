package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Ignore;
import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.WikiXMLProcessor;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class SignatureTest extends GermanTestBase {

	private WikiPostUser postUser;

	public SignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
	}

	@Test
	public void testSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Gibt es ein persönliches Adressbuch, oder eine "
				+ "ähnliche Funktion bei Wikipedia? --[[Benutzer:Burggraf17|"
				+ "Burggraf17]] 10:04, 6. Mär 2004 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node date = doc.query("/posting/p/signed/date").get(0);
		assertEquals("10:04, 6. Mär 2004 (CET)", date.getValue());
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Burggraf17", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://de.wikipedia.org/wiki/Benutzer:Burggraf17",
				ref.getValue());
	}

	@Test
	public void testSignatureLowercase()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "oder eine ähnliche Funktion bei Wikipedia? "
				+ "--[[benutzer:Burggraf17|Burggraf17]] 10:04, 6. Mär "
				+ "2004 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

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
				"23159", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(0, doc.query("/posting/p/signed/date").size());
	}

	@Test
	public void testSignatureWithoutLinkText()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Grüsse, und bis bald wiedermal :-) [[Benutzer:Fantasy]]";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(0, doc.query("/posting/p/signed/date").size());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Benutzer:Fantasy", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://de.wikipedia.org/wiki/Benutzer:Fantasy",
				ref.getValue());
	}

	@Test
	public void testSignatureWithoutDash()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":Grüsse, und bis bald wiedermal :-) [[Benutzer:Fantasy]] "
				+ "[[Benutzer_Diskussion:Fantasy|容]] 11:28, 17. Jul 2006 (CEST)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("11:28, 17. Jul 2006 (CEST)", timestamp.getValue());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Benutzer:Fantasy", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://de.wikipedia.org/wiki/Benutzer:Fantasy",
				ref.getValue());
	}

	@Test
	public void testSignatureWithDashAndSpace()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "oder eine ähnliche Funktion bei Wikipedia? "
				+ "-- [[Benutzer:Burggraf17|Burggraf17]] 10:04, 6. Mär "
				+ "2004 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Fantasy",
				"23159", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
	}

	@Test
	public void testSignatureWithColon()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "::::: Nun, wer hat den Satz ''Gewerkschaften "
				+ "sind keine Verein im rechtlichen Sinne.'' hineingestellt "
				+ "und warum? -- [[:Benutzer:Fgb|Fgb]].";
		WikiPage wikiPage = createWikiPage("Diskussion:Arbeitsmarkt",
				"359", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals(0, doc.query("/posting/p/signed/date").size());
		assertEquals("signed", signature.getValue());
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Fgb", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://de.wikipedia.org/wiki/Benutzer:Fgb",
				ref.getValue());
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
				"131", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("02:46, 4. Jul. 2010 (CEST)", timestamp.getValue());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Mopskatze", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://de.wikipedia.org/wiki/Benutzer:Mopskatze",
				ref.getValue());
	}

	@Test
	public void testSignatureWithStyleEnglish()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Thanks again.  &lt;span style=&quot;border: "
				+ "solid 2px black; border-radius: 6px; box-shadow: gray "
				+ "3px 3px 3px;&quot;&gt;&amp;nbsp;[[User:Unician|Unician]]"
				+ "&amp;nbsp;[[User talk:Unician|'''&amp;nabla;''']]&amp;"
				+ "nbsp;&lt;/span&gt; 21:54, 14 August 2014 (UTC)";

		WikiPage wikiPage = createWikiPage("User talk:Tim Starling",
				"101997", true, wikitext);

		InputStream is = SpecialContributionSignatureTest.class.getClassLoader()
				.getResourceAsStream("enwiki-talk.properties");
		Properties properties = new Properties();
		properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
		is.close();

		Configuration config = new Configuration(properties);
		WikiXMLProcessor.Wikipedia_URI = "https://" + config.getLanguageCode()
				+ ".wikipedia.org/wiki/";

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();
		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("21:54, 14 August 2014 (UTC)", timestamp.getValue());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Unician", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://en.wikipedia.org/wiki/User:Unician",
				ref.getValue());

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
				"1338", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("&lt;span style=&quot;padding: 1px; color: #808080; "
				+ "border-width: 1px; border-style: dotted; border-color: "
				+ "#DC143C;&quot;&gt;z&lt;b style=&quot;color: red;"
				+ "&quot;&gt;O&lt;/b&gt;i&lt;b style=&quot;color: red;"
				+ "&quot;&gt;D&lt;/b&gt;berg&lt;/span&gt;",
				StringEscapeUtils.escapeXml10(name.getValue()));
		Node ref = doc.query("/posting/p/signed/ref/@target").get(0);
		assertEquals("https://de.wikipedia.org/wiki/Benutzer:ZOiDberg",
				ref.getValue());
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
				"1169", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Sargoth", name.getValue());
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
				"1627", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node ps = doc.query("/posting/p/small").get(0);
		assertEquals(" mal wieder eine Nacht sinnvoll verbracht",
				ps.getValue());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Pangloss", name.getValue());
	}

	// Problematic parsing because of the angle brackets
	@Test
	@Ignore
	public void testSignatureWithAngleBrackets()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "&lt;Verstoß gegen KPA, daher gelöscht!--"
				+ "[[Benutzer:Alberto568|Alberto568]] ([[Benutzer "
				+ "Diskussion:Alberto568|Diskussion]]) 15:20, 5. Mär. "
				+ "2014 (CET)&gt;[https://de.wikipedia.org/w/index.php?"
				+ "title=Feminismus&amp;action=history]";

		WikiPage wikiPage = createWikiPage(
				"Diskussion:Feminismus/Archiv/006",
				"8144626", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		System.out.println(wikiXML);
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
				"131", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/ul/li/a").size());

		Node ps = doc.query("/posting/ul/li/seg").get(0);
		assertEquals("postscript", ps.query("@type").get(0).getValue());
		assertEquals(" PS: Könnte vielleicht irgend jemand ein Mal endlich "
				+ "die Ladungen bei den Reaktionsgleichungen hochstellen!?",
				ps.getValue());

		Node name = doc.query("/posting/ul/li/signed/name").get(0);
		assertEquals("CHK", name.getValue());
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
				true,
				wikitext, wikitext2);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = "<page>\n" + wikiPage.getWikiXML() + "\n</page>";
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/page/posting/p/a").size());
		assertEquals(2, doc.query("/page/posting/p/signed").size());
		Node name = doc.query("/page/posting/p/signed/name").get(0);
		assertEquals("Pill", name.getValue());
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
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

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
				"36867", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Armin", name.getValue());
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
				"3250", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
	}

	@Test
	public void testUserLinkAtStart()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[[Benutzer:Katharina]] wurde bereits nachdrücklich, "
				+ "aber erfolglos,  ersucht, ihre pauschalen Löschereien zu "
				+ "'''begründen'''.";

		WikiPage wikiPage = createWikiPage("Diskussion:Feminismus/Archiv/001",
				"438636", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
	}

	@Test
	public void testSignatureAtStartFrench()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":: [[Utilisateur:L'amateur d'aéroplanes|L&amp;#39;"
				+ "amateur d&amp;#39;aéroplanes]] 24 mai 2007 à 00:37 (CEST) "
				+ "Ajoutez simplement la/les référence/s ou vous avez vu cela. "
				+ "Il des milliers de sinistres par an.";

		WikiPage wikiPage = createWikiPage(
				"Discussion:Attentats du 11 septembre 2001/Archive 1",
				"4046600", true, wikitext);

		InputStream is = SpecialContributionSignatureTest.class.getClassLoader()
				.getResourceAsStream("frwiki-talk.properties");
		Properties properties = new Properties();
		properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
		is.close();

		Configuration config = new Configuration(properties);
		WikiXMLProcessor.Wikipedia_URI = "https://" + config.getLanguageCode()
				+ ".wikipedia.org/wiki/";

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/signed").size());
		Node posting = doc.query("/posting/p").get(0);
		assertEquals("L&amp;#39;amateur d&amp;#39;aéroplanes"
				+ "L&amp;#39;amateur d&amp;#39;aéroplanes24 mai 2007 à "
				+ "00:37 (CEST) Ajoutez simplement la/les référence/s ou "
				+ "vous avez vu cela. Il des milliers de sinistres par an.",
				StringEscapeUtils.escapeXml10(posting.getValue()));
	}
}
