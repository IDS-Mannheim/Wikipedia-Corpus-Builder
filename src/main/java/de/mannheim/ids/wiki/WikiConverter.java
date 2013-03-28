package de.mannheim.ids.wiki;

import java.io.IOException;

/** Main class running the jar
 *  Convert a wikidump to a set of XML wikipages
 * 
 * @author margaretha
 * @version 1.0 Build Mar 2013
 */
public class WikiConverter {	
	
	private static final int LANGUAGE = 0;
	private static final int WIKIDUMP = 1;
	private static final int FAILED_PARSING_LIST = 2;
	private static final int SPLIT_OPTION = 3;
	
	/** Main method 
	 *  
	 *  @param arg[0] is the language of a wikidump 
	 *  @param arg[1] is the input file 
	 *  @param arg[2] is the the title list of the failed parsed pages
	 *  @param arg[3] is the option to split the XML output
	 *  
	 * */
	public static void main(String[] args) throws IOException{
		long startTime = System.nanoTime();		
		
		/* 	If the language of the input wikidump is defined in the LanguageSetter 
		 * 	class, instantiate the language. Otherwise, create an empty instance 
		 * 	and set its properties.
		*/ 
		LanguageSetter languageSetter = new LanguageSetter(args[LANGUAGE]);
		
		XMLWikiProcessor processor = new XMLWikiProcessor(languageSetter);
		
		if (args[SPLIT_OPTION].equals("split")){
			// convert the input wikidumps into XML wikipages
			processor.processSplit(args[WIKIDUMP], args[FAILED_PARSING_LIST]);	
		}
		else{
			// convert the input wikidumps into a single XML
			processor.process(args[WIKIDUMP], args[FAILED_PARSING_LIST]);		
		}				
				
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);		

	}
}
