package de.mannheim.ids.wiki;

import java.io.IOException;


public class WikiConverter {
	/*
	 * The main method takes arguments where 
	 * 1. the first argument is the language 
	 * 2. the second argument is the input file 
	 * 3. the third argument is the article output file
	 * 4. the fourth argument is the discussion output file
	 * 5. the fifth argument is the title list of the failed parsed pages
	 * */
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();		
		
//		String articlePath="xml/"+args[2]+".xml";
//		String discussionPath = "xml/"+args[3]+".xml";
				
//		XMLWikiProcessor processor = new XMLWikiProcessor(args[0]);
//		processor.process(args[1],  articlePath, discussionPath, args[4]);
//		
		XMLWikiProcessor processor = new XMLWikiProcessor("de");
		processor.process("xml/auro.xml","xml/test-articles.xml","xml/test-discussions.xml","xml/error.log");
//		processor.process("xml/test.wikitext","xml/TITLE-articles.xml","xml/TITLE-discussions.xml", "xml/error.log");

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
		
//		startTime = System.nanoTime();		
		
		System.out.println("\nTransforming XML to XCES");
		XCESWikiProcessor xcesProcessor = new XCESWikiProcessor();
		xcesProcessor.transformToXCES("xml/test-articles.xml", "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/test-articles.xces", "saxon-error.log");
		xcesProcessor.transformToXCES("xml/test-discussions.xml", "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/test-discussions.xces", "saxon-error.log");
//		xcesProcessor.transformToXCES(articlePath, "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/"+args[2]+".xces", "saxon-articles-error.log");
//		xcesProcessor.transformToXCES(discussionPath, "Wikipedia-Konvertierung/xmlwiki2xces.xsl", "xces/"+args[3]+".xces", "saxon-discussions-error.log");
				
//		endTime = System.nanoTime();
//		duration = endTime - startTime;
//		System.out.println("Wiki XML to XCES execution time "+duration);
					 
	}
}
