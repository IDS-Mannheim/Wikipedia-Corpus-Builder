package de.mannheim.ids.wiki;

import java.io.IOException;


public class WikiConverter {
	/*
	 * The main method takes 3 arguments where 
	 * 1. the first argument is the input file
	 * 2. the second argument is the article output file
	 * 3. the third argument is the discussion output file
	 * */
	public static void main(String[] args) throws IOException{
		XMLWikiProcessor processor = new XMLWikiProcessor();
		//processor.process(args[0], args[1],args[2]);
		processor.process("TITLE.wikitext","dewiki-20130122-articles-d-no.xml","dewiki-20130122-discussions-d-no.xml"); 
	}
}
