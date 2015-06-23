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
		String type = "all";
		String encoding = "iso-8859-1";
		
		WikiXMLConverter.convert(new Configuration(wikidump, language, type, xmlOutputDir, encoding));		
	}	
}
