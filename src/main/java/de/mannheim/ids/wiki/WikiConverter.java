package de.mannheim.ids.wiki;

import java.io.IOException;

/** An example Class to convert a wikidump to a set of XML wikipages
 * 
 * @author margaretha
 * @version 1.0 Build Feb 2013
 */
public class WikiConverter {
	/*
	 * The main method takes arguments where 
	 * 1. the first argument is the language 
	 * 2. the second argument is the input file 
	 * 3. the third argument is the title list of the failed parsed pages
	 * */
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();		
		
		/* 	If the language of the input wikidump is defined in the LanguageSetter 
		 * 	class, instantiate the language. Otherwise, create an empty instance 
		 * 	and set its properties.
		*/ 
		LanguageSetter languageSetter = new LanguageSetter(args[0]);
//		LanguageSetter languageSetter = new LanguageSetter("de");
		
		XMLWikiProcessor processor = new XMLWikiProcessor(languageSetter);
		processor.processSplit(args[1], args[2]);	// convert the input wikidumps into XML wikipages
		//processor.process(args[1], args[2]);		// convert the input wikidumps into a single XML
		
//		processor.processSplit("input/mariavitismus.xml", "error.log");
//		processor.process("input/test2.xml","error.log");//
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
		
//		startTime = System.nanoTime();		
		
//		System.out.println("\nTransforming XML to XCES");
//		XCESWikiProcessor xcesProcessor = new XCESWikiProcessor();
//		xcesProcessor.transformToXCES(articlePath, "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/"+args[2]+".xces", "saxon-articles-error.log");
//		xcesProcessor.transformToXCES(discussionPath, "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/"+args[3]+".xces", "saxon-discussions-error.log");

//		xcesProcessor.transformToXCES("xml/test-articles.xml", "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/test-articles.xces", "saxon-error.log");
//		xcesProcessor.transformToXCES("xml/test-discussions.xml", "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/test-discussions.xces", "saxon-error.log");
				
//		endTime = System.nanoTime();
//		duration = endTime - startTime;
//		System.out.println("Wiki XML to XCES execution time "+duration);
					 
	}
}
