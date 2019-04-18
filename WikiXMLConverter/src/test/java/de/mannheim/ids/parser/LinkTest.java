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
	
	public LinkTest() {
		builder = new Builder();
	}

	@Test
	public void testInternalLinkWithTitle()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[:File:Leeuwenhoek-A Specimen of Some Observations "
				+ "Made by a Microscope.pdf|Volltext]]";
		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");

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
	public void testInternalLink()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[Latein]]";
		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");

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
	public void testCategoryLink()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[:Kategorie:Anwendungsprogramm]]";
		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");

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
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
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
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[http://de.wikipedia.org/w/index.php?title"
				+ "=Benutzer_Diskussion:Tsor&amp;diff=prev&amp;oldid="
				+ "17535752]";
		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");

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
