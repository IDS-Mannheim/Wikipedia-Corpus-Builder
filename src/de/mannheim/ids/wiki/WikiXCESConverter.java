package de.mannheim.ids.wiki;

import java.io.File;

/** This is the main class for converting the XML-ized Wikipages to XCES
 *  This class takes 6 arguments as inputs:
 *  1. the folder path of the XML-ized Wikipages,
 *  2. the type of the pages (articles or discussions), 
 *  3. the filename of the Wikipedia dump in format:
 *     [2 character language code]wiki-[year][month][date]-pages-meta-current.xml
 *     example: dewiki-20130728-pages-meta-current.xml
 *  4. the output file,   
 *  5. the path to the XML file containing the list of inflectives.
 *  6. the relative path to the DTD file from the location of the XSLT stylesheets 
 * 
 * @author margaretha
 * 
 */

public class WikiXCESConverter {		
		
	public static void main(String[] args) throws Exception {
		
		String xmlFolder=args[0];
		String type=args[1];
		String dumpFilename=args[2];
		String outputFile=args[3];
		String inflectives=args[4];		
		String dtdfile=args[5];
		
//		String dumpFilename="dewiki-20130728-pages-meta-current.xml";
//		String outputFile="output.xces";
//		String type="articles";
//		String xmlFolder ="xml/";
//		String inflectives="../inflectives.xml";
		
		File output = new File(outputFile);
		File xsl = new File ("xslt/Templates.xsl");
		
		WikiXCESProcessor wikiXCESProcessor = new WikiXCESProcessor(xmlFolder,xsl,
				type,dumpFilename,inflectives);
		
		long startTime=System.nanoTime();
		XCESWriter w = new XCESWriter(output,dtdfile);		
		w.write(xmlFolder,type,dumpFilename,wikiXCESProcessor);		
		long endTime=System.nanoTime();					
		System.out.println("Transformation time "+ (endTime-startTime));
		
	}

}
