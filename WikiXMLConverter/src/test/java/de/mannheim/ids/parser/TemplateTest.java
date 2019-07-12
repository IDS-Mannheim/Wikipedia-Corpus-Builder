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
}
