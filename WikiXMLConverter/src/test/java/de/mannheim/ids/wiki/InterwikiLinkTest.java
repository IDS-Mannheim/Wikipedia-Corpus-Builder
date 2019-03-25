package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.writer.WikiErrorWriter;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class InterwikiLinkTest {

	private Builder builder;

	public InterwikiLinkTest() throws ParserConfigurationException {
		builder = new Builder();
	}

	@Test
	public void testImageLink()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[File:Blue ribbon.svg|8px|link=:en:Blue Ribbon Online Free Speech Campaign]] ";

		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");

		Sweble2Parser swebleParser = new Sweble2Parser("5926612",
				"Wikipedia:Löschkandidaten/31. Januar 2011",
				wikitext, "de", new WikiStatistics(), new WikiErrorWriter(),
				wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a[1]").get(0);
		String href = a.query("@href").get(0).getValue();

		assertEquals(
				"https://en.wikipedia.org/wiki/Blue_Ribbon_Online_Free_Speech_Campaign",
				href);
		assertEquals("Datei:Blue ribbon.svg", a.getValue());

	}

	@Test
	public void testAppropediaLink()
			throws IOException, ParserConfigurationException, SAXException,
			ValidityException, ParsingException {
		String wikitext = "[[appropedia:Open-Island|Open Island]]";

		WikiConfig wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");

		Sweble2Parser swebleParser = new Sweble2Parser("5926612",
				"Wikipedia:Löschkandidaten/31. Januar 2011",
				wikitext, "de", new WikiStatistics(), new WikiErrorWriter(),
				wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		String href = doc.query("/p/a[1]/@href").get(0).getValue();

		assertEquals("http://www.appropedia.org/Open-Island", href);
		assertEquals("Open Island", doc.getValue());
	}

}
