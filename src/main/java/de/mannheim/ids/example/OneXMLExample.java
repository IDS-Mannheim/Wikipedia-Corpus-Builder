package de.mannheim.ids.example;

import java.io.IOException;

import de.mannheim.ids.wiki.LanguageSetter;
import de.mannheim.ids.wiki.XMLWikiProcessor;
import de.mannheim.ids.wiki.XMLWikiProcessorSingle;

public class OneXMLExample {
	
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();				
		
		// Set the language properties of the wikidump
		LanguageSetter languageSetter = new LanguageSetter("de");
		// Initialized the processor to convert to XML
		XMLWikiProcessorSingle processor = new XMLWikiProcessorSingle(languageSetter);
		// convert the input wikidump into one XML page
		processor.process("input/test.xml", "error.log");		
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
							 
	}
}
