package de.mannheim.ids.wiki;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.mannheim.ids.config.Configuration;

public class WikiXMLConverterWithPropertiesTest {

	@Test
	public void testGenerateArticleWikiXMLWithProperties()
			throws IOException, ParseException, ParserConfigurationException, SAXException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "dewiki-article.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
	
	@Test
	public void testGenerateTalkWikiXMLWithProperties()
			throws IOException, ParseException, ParserConfigurationException, SAXException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "dewiki-talk.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
	
	@Test
	public void testGenerateWikipediaLöschkandidaten()
			throws IOException, ParseException, ParserConfigurationException, SAXException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "dewiki-löschkandidaten.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
	
	@Test
	public void testGenerateWikipediaRedundanz()
			throws IOException, ParseException, ParserConfigurationException, SAXException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "dewiki-redundanz.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
}
