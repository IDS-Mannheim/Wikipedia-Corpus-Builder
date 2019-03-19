package de.mannheim.ids.wiki;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class WikiXMLConverterWithPropertiesTest {

	@Test
	public void testGenerateArticleWikiXMLWithProperties()
			throws IOException, ParseException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "test-article.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
	
	@Test
	public void testGenerateTalkWikiXMLWithProperties()
			throws IOException, ParseException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "test-talk.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
	
	@Test
	public void testGenerateWikipediaLöschkandidaten()
			throws IOException, ParseException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "test-wikipedia-löschkandidaten.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
	
	@Test
	public void testGenerateWikipediaRedundanz()
			throws IOException, ParseException {

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "test-wikipedia-redundanz.properties"});
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}
}
