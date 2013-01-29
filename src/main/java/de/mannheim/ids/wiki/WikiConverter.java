package de.mannheim.ids.wiki;

import java.io.IOException;


public class WikiConverter {
	/*
	 * The main method takes 4 arguments where 
	 * 1. the first argument is the language 
	 * 2. the second argument is the input file 
	 * 3. the third argument is the article output file
	 * 4. the fourth argument is the discussion output file
	 * */
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();		
		
//		XMLWikiProcessor processor = new XMLWikiProcessor(args[0]);
//		processor.process(args[1], args[2],args[3]);
		
		XMLWikiProcessor processor = new XMLWikiProcessor("de");
		processor.process("test2.xml","dewiki-20130122-articles-d-no.xml","dewiki-20130122-discussions-d-no.xml");

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Total execution time "+duration);	
		 
	}
}
