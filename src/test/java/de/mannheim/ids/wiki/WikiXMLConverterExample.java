package de.mannheim.ids.wiki;

import java.io.IOException;

public class WikiXMLConverterExample {
	
	public static void main(String[] args) throws IOException {		
		// Set the language of the Wikipedia		
		String language = "de";		
		// Set output directory
		String xmlOutputDir = "./xml-"+language;		
		// Set wikidump filepath
		String wikidump = "data/dewiki-20130728-sample.xml";
		// User page in the Wikipedia language, e.g. Benutzer in German Wikipedia
		String userPage = "Benutzer";
		// Talk page in the Wikipedia language, e.g. Diskussion in German Wikipedia
		String talkPage = "Diskussion";
		// User contribution page in the Wikipedia language, e.g. 
		// Spezial:Beiträge in German Wikipedia
		String userContribution = "Spezial:Beiträge";
		// The type of Wikipages to convert
		String type = "all";
		// the output encoding
		String encoding = "iso-8859-1";
		
		Configuration config = new Configuration (wikidump, language, userPage, 
			talkPage, userContribution, type, xmlOutputDir, encoding);
		
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.createWikiXML();	
	}	
}
