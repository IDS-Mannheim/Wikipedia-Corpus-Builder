package de.mannheim.ids.main;

import java.io.IOException;

import de.mannheim.ids.util.LanguageProperties;
import de.mannheim.ids.wiki.WikiXMLProcessor;

public class WikiXMLConverter {
	
	public static void main(String[] args) throws IOException {
		long startTime = System.nanoTime();
		
		String language = "de";
		String[] namespaces = {"0","1"};
	
		LanguageProperties lp = new LanguageProperties(language,namespaces);
		
		String wikidump = "input/test4.xml";		
		String xmlOutputDir = "./xml"; 
		
		WikiXMLProcessor wxp = new WikiXMLProcessor(lp);
		//wxp.createWikiXML(wikidump,xmlOutputDir);
		wxp.createSingleWikiXML(wikidump,xmlOutputDir);		
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext XML converter execution time "+duration);
	}
}
