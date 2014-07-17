package de.mannheim.ids.wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.mannheim.ids.util.LanguageProperties;
import de.mannheim.ids.wiki.WikiXMLProcessor;

/** Main class for Wikitext to XML conversion
 * 
 * @author margaretha
 *
 */

public class WikiXMLConverter {
	
	public static void main(String[] args) throws IOException {
		long startTime = System.nanoTime();
		
		String language = args[0];
		String wikidump = args[1];
		String type = args[2];
		
		List<Integer> namespaces= new ArrayList<Integer>();
		if (type.equals("articles")){
			namespaces.add(0);
		}
		else if (type.equals("dicussions")){
			namespaces.add(1);
		}
		else if (type.equals("both")){
			namespaces.add(0);
			namespaces.add(1);
		}
		else {
			throw new IllegalArgumentException("The type is not recognized. " +
					"Please specify the type as: articles, dicussions, or both");
		}
				
		LanguageProperties lp = new LanguageProperties(language,namespaces);				
		String xmlOutputDir = "./xml-"+language; 
		try{
			WikiXMLProcessor wxp = new WikiXMLProcessor(lp,namespaces);
			wxp.createWikiXML(wikidump,xmlOutputDir);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//wxp.createSingleWikiXML(wikidump,xmlOutputDir);		
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
	}
}
