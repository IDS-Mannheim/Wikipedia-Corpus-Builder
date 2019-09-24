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

public class InterwikiLinkTest {
	private Builder builder;
	private WikiConfig wikiConfig;

	public InterwikiLinkTest()
			throws IOException, ParserConfigurationException, SAXException {
		builder = new Builder();
		wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");
	}

	@Test
	public void testTicketNumberLink()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "Der folgende Hinweis erreichte das Support-Team "
				+ "in [[:ticket:2015082110007769]]:";

		Sweble2Parser swebleParser = new Sweble2Parser("51901",
				"Diskussion:Aristoteles", wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node a = doc.query("/p/a").get(0);
		String href = a.query("@href").get(0).getValue();
		assertEquals("https://ticket.wikimedia.org/otrs/index.pl?Action="
				+ "AgentTicketZoom&TicketNumber=2015082110007769",
				href);
	}
	
	@Test
	public void testImageLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[[File:Blue ribbon.svg|8px|link=:en:Blue Ribbon "
				+ "Online Free Speech Campaign]] ";

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
				"https://en.wikipedia.org/wiki/Blue_Ribbon_Online_Free_"
						+ "Speech_Campaign",
				href);
		assertEquals("Datei:Blue ribbon.svg", a.getValue());

	}

	@Test
	public void testAppropediaLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "[[appropedia:Open-Island|Open Island]]";

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
