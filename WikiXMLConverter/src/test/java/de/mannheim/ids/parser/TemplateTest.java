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

public class TemplateTest {

	private Builder builder;
	private WikiConfig wikiConfig;

	public TemplateTest()
			throws IOException, ParserConfigurationException, SAXException {
		builder = new Builder();
		wikiConfig = LanguageConfigGenerator
				.generateWikiConfig("de");
	}

	@Test
	public void testTemplateWithSignatureElement()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "{{Diskussion aufgeräumt|24. Februar 2014|2="
				+ "[https://de.wikipedia.org/w/index.php?title=Diskussion:"
				+ "Hauspferd&amp;oldid=124833777 Version vom 25. November 2013]|"
				+ "3=<signed type=\"signed\"> <date>0:17, 24. "
				+ "Feb. 2014 (CET)</date></signed>}}";

		Sweble2Parser swebleParser = new Sweble2Parser("12765",
				"Diskussion:Hauspferd", wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node span = doc.query("/p/span[1]").get(0);
		String spanClass = span.query("@class").get(0).getValue();

		assertEquals("template", spanClass);
	}

	@Test
	public void testTemplateMinimalS()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "::nun, aus töchtern werden mütter; aber "
				+ "nicht aus müttern töchter; diese waren bzw. sind "
				+ "schon töchter...  {{S}} --[[Benutzer:HilmarHansWerner"
				+ "|HilmarHansWerner]] ([[Benutzer Diskussion:"
				+ "HilmarHansWerner|Diskussion]]) 00:23, 14. Feb. 2016 "
				+ "(CET)";

		Sweble2Parser swebleParser = new Sweble2Parser("10838",
				"Diskussion:Alphastrahlung",
				wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{S}}_]", desc.getValue());
	}

	@Test
	public void testTemplateS()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":::Was war es denn Deiner Meinung nach, kein "
				+ "Angriff {{S|8P}} --[[Benutzer:MBurch|MBurch]] "
				+ "([[Benutzer Diskussion:MBurch|Diskussion]]) 15:34, "
				+ "17. Dez. 2014 (CET)";
		Sweble2Parser swebleParser = new Sweble2Parser("8303069",
				"Diskussion:Wladimir Wladimirowitsch Putin/Archiv/002",
				wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{S|8P}}_]", desc.getValue());
	}

	@Test
	public void testTemplateSmiley()
			throws ValidityException, ParsingException, IOException {
		String wikitext = ":::Du wiederholst dich. Jaja, ich bin an dem "
				+ "Schlamassel und dieser unötigen Zeit- und Byteverschwendung "
				+ "schuld {{Smiley|applaus}} --[[Benutzer:Benqo|Benqo]] "
				+ "([[Benutzer Diskussion:Benqo|Diskussion]]) 17:47, 16. Sep. "
				+ "2015 (CEST)";

		Sweble2Parser swebleParser = new Sweble2Parser("8967849",
				"Diskussion:Flüchtlingskrise in Europa ab 2015/Archiv/1",
				wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), wikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{Smiley|applaus}}_]", desc.getValue());
	}
}
