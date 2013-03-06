package de.mannheim.ids.example;

import java.io.IOException;

import de.mannheim.ids.wiki.LanguageSetter;
import de.mannheim.ids.wiki.XMLWikiProcessor;

public class SplitExample {
	
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();				
		
		// Set the language properties of the wikidump
		LanguageSetter languageSetter = new LanguageSetter("de");
		// Initialized the processor to convert to XML
		XMLWikiProcessor processor = new XMLWikiProcessor(languageSetter);
		// convert the input wikidump into XML wikipages
		processor.processSplit("input/mariavitismus.xml", "error.log");
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
							 
	}
}
