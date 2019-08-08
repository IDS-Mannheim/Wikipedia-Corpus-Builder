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
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class CleanTextTest {

	private WikiConfig frWikiConfig;

	public CleanTextTest()
			throws IOException, ParserConfigurationException, SAXException {
		frWikiConfig = LanguageConfigGenerator.generateWikiConfig("fr");
	}

	@Test
	public void testXMLEntityRef()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "Cf. Michel Onfray, ''Le crépuscule d'une idole. "
				+ "L'affabulation freudienne'', éd. Grasset &amp; "
				+ "Fasquelle, LGF, 2010, col. Le Livre de Poche, 632 p.";

		Sweble2Parser swebleParser = new Sweble2Parser("42242",
				"Psychanalyse", wikitext, "fr", new WikiStatistics(),
				new WikiErrorWriter(),
				frWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		assertEquals("<p>Cf. Michel Onfray, <i>Le crépuscule d&#39;une "
				+ "idole. L&#39;affabulation freudienne</i>, éd. Grasset "
				+ "&amp; Fasquelle, LGF, 2010, col. Le Livre de Poche, "
				+ "632 p.</p>",
				wikiXML.trim());

	}
}
