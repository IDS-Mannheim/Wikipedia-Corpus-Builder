package de.mannheim.ids.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.writer.WikiErrorWriter;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class LinkTest extends GermanTestBase{

	@Test
	public void testInternalLinkWithTitle()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[:File:Leeuwenhoek-A Specimen of Some Observations "
				+ "Made by a Microscope.pdf|Volltext]]";
		// String wikitext = "[[Latein]]";
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
}
