package de.mannheim.ids.wiki;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.mannheim.ids.config.Configuration;

public class WikiXMLConverterTest {

	@Test
	public void testGenerateArticleWikiXML()
			throws IOException, ParserConfigurationException, SAXException {
		// Set the language of the Wikipedia
		String language = "de";
		// Set wikidump filepath
		String wikidump = "data/dewiki-20170701-sample.xml";
		// the output encoding
		String encoding = "utf-8";
		// The namespace of the Wikipages to convert
		int namespaceKey = 0; // article page
		// Set maximum number of threads running concurrently, e.g. as many as
		// the number of CPUs
		int maxThreads = 4;
		// Set whether a file for each wikipage is to be created or not.
		boolean generateWikitext = false;

		String pageType = "article";
		String titlePrefix = "";

		Configuration config = new Configuration(wikidump, language, null, null,
				null, null, null, namespaceKey, pageType, titlePrefix, encoding,
				maxThreads, generateWikitext);

		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}

	@Test
	public void testGenerateTalkWikiXML()
			throws IOException, ParserConfigurationException, SAXException {
		String language = "de";
		String wikidump = "data/dewiki-20130728-sample.xml";
		// User page in the Wikipedia language, e.g. Benutzer in German
		// Wikipedia
		String userPage = "Benutzer";
		String userTalk = "Benutzer Diskussion";
		// User contribution page in the Wikipedia language, e.g.
		// Spezial:Beiträge in German Wikipedia
		String userContribution = "Spezial:Beiträge";
		String signature = "Hilfe:Signatur";
		String encoding = "utf-8";
		int namespaceKey = 1; // talk page
		int maxThreads = 4;
		boolean generateWikitext = false;
		// Unsigned template in the Wikipedia language, e.g. unsigniert in
		// German.
		String unsigned = "unsigniert";

		String pageType = "talk";
		String titlePrefix = "";

		Configuration config = new Configuration(wikidump, language, userPage,
				userTalk, userContribution, signature, unsigned, namespaceKey,
				pageType, titlePrefix, encoding, maxThreads, generateWikitext);

		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}

}
