package de.mannheim.ids.wiki;

import java.io.IOException;


public class WikiConverter {
	/*
	 * The main method takes arguments where 
	 * 1. the first argument is the language 
	 * 2. the second argument is the input file 
	 * 3. the third argument is the title list of the failed parsed pages
	 * */
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();		
		
		XMLWikiProcessor processor = new XMLWikiProcessor(args[0]);
		processor.processSplit(args[1], args[2]);
			
		
//		XMLWikiProcessor processor = new XMLWikiProcessor("de");
//		processor.processSplit("input/dewiki-latest-pages-meta-current.xml", "error.log");
//		processor.process("input/test2.xml","error.log");//
		
//		System.out.println(processor.normalizeIndex("Łódź"));

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
