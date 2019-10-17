package de.mannheim.ids.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;

import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.writer.WikiErrorWriter;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class LinkTest {

	private Builder builder;
	private WikiConfig wikiConfig;

	public LinkTest()
			throws IOException, ParserConfigurationException, SAXException {
		builder = new Builder();
		wikiConfig = LanguageConfigGenerator.generateWikiConfig("de");
	}

	@Test
	public void testThumbnailLink()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "[[File:Segre.jpg|thumb|links|175px|Emilio Gino "
				+ "Segrè, Entdecker des Astats]]";

		Sweble2Parser swebleParser = new Sweble2Parser("352",
				"Antoni van Leeuwenhoek", wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node div = doc.query("/div/div").get(0);
		assertEquals("thumbinner", div.query("@class").get(0).getValue());
		assertEquals("Datei:Segre.jpg", div.query("a").get(0).getValue());
		assertEquals("thumbcaption", div.query("div/@class").get(0).getValue());
		assertEquals("Emilio Gino Segrè, Entdecker des Astats",
				div.query("div").get(0).getValue());

	}

	@Test
	public void testInternalLinkWithTitle()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[[:File:Leeuwenhoek-A Specimen of Some Observations "
				+ "Made by a Microscope.pdf|Volltext]]";

		Sweble2Parser swebleParser = new Sweble2Parser("352",
				"Antoni van Leeuwenhoek", wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a[1]").get(0);
		String href = a.query("@href").get(0).getValue();

		assertEquals(
				"https://de.wikipedia.org?title=Datei:Leeuwenhoek-A_Specimen_"
						+ "of_Some_Observations_Made_by_a_Microscope.pdf",
				href);
		assertEquals("Volltext", a.getValue());
	}

	@Test
	public void testEmbeddedInternalLink()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[Datei:Vladimir Putin (2018-05-14).jpg|mini|hochkant"
				+ "|Wladimir Putin (2018)[[Datei:Putin signature.svg|rahmenlos|"
				+ "zentriert|150px|Unterschrift von Wladimir Putin]]]]";

		Sweble2Parser swebleParser = new Sweble2Parser("19955",
				"Wladimir Wladimirowitsch Putin", wikitext, "de",
				new WikiStatistics(), new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a[1]").get(0);

		assertEquals(
				"https://de.wikipedia.org?title=Datei:Vladimir_Putin_(2018"
						+ "-05-14).jpg",
				a.query("@href").get(0).getValue());
		assertEquals(
				"Wladimir Putin (2018)[[Datei:Putin signature.svg|rahmenlos|"
						+ "zentriert|150px|Unterschrift von Wladimir Putin]]",
				a.query("@title").get(0).getValue());

		assertEquals("Datei:Vladimir Putin (2018-05-14).jpg", a.getValue());
	}

	@Test
	public void testInternalLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[[Latein]]";

		Sweble2Parser swebleParser = new Sweble2Parser("352",
				"Antoni van Leeuwenhoek", wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a[1]").get(0);
		String href = a.query("@href").get(0).getValue();

		assertEquals(
				"https://de.wikipedia.org?title=Latein",
				href);
		assertEquals("Latein", a.getValue());
	}
	
	@Test
	public void testQuoteInLink()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "''[[11'09&quot;01 – September 11]]''";

		Sweble2Parser swebleParser = new Sweble2Parser("5071",
				"Terroranschläge am 11. September 2001", wikitext, "de",
				new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		assertEquals(
				"<p><i><a href=\"https://de.wikipedia.org?title="
				+ "11%2709%2201_%E2%80%93_September_11\" title="
				+ "\"11&#39;09&quot;01 – September 11\">"
				+ "11&#39;09&amp;quot;01 – September 11</a></i></p>",
				wikiXML.trim());
	}

	@Test
	public void testFrenchInternalLinkWithApostrophe()
			throws ValidityException, ParsingException, IOException,
			ParserConfigurationException, SAXException {
		String wikitext = "Avec ''[[L'Interprétation des rêves]]''";

		WikiConfig frWikiConfig = LanguageConfigGenerator
				.generateWikiConfig("fr");
		Sweble2Parser swebleParser = new Sweble2Parser("42242",
				"Psychanalyse", wikitext, "fr", new WikiStatistics(),
				new WikiErrorWriter(), frWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		
		assertEquals("<p>Avec <i><a href=\"https://fr.wikipedia.org?"
				+ "title=L%27Interpr%C3%A9tation_des_r%C3%AAves\" title="
				+ "\"L&#39;Interprétation des rêves\">L&#39;Interprétation "
				+ "des rêves</a></i></p>", wikiXML.trim());
	}

	@Test
	public void testCategoryLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[[:Kategorie:Anwendungsprogramm]]";

		Sweble2Parser swebleParser = new Sweble2Parser("22684",
				"Diskussion:Anwendungssoftware", wikitext, "de",
				new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a[1]").get(0);
		String href = a.query("@href").get(0).getValue();

		assertEquals(
				"https://de.wikipedia.org?title=Kategorie:Anwendungsprogramm",
				href);
		assertEquals("Kategorie:Anwendungsprogramm", a.getValue());
	}

	@Test
	public void testCategoryLinkEn()
			throws IOException, ValidityException, ParsingException,
			ParserConfigurationException, SAXException {
		String wikitext = "[[Category:Anarchism| ]]";

		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("en");
		Sweble2Parser swebleParser = new Sweble2Parser("12",
				"Anarchism", wikitext, "en",
				new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a[1]").get(0);
		String href = a.query("@href").get(0).getValue();

		assertEquals(
				"https://en.wikipedia.org?title=Category:Anarchism",
				href);
		assertEquals("Category:Anarchism", a.getValue());
	}

	@Test
	public void testExternalLinkWithReference()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[http://de.wikipedia.org/w/index.php?title"
				+ "=Benutzer_Diskussion:Tsor&amp;diff=prev&amp;oldid="
				+ "17535752]";

		Sweble2Parser swebleParser = new Sweble2Parser("1483451",
				"Benutzer Diskussion:Jan K.", wikitext, "de",
				new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a").get(0);
		assertEquals("[1]", a.getValue());
	}
}
