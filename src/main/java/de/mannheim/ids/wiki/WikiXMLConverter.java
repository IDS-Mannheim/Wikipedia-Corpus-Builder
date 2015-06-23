package de.mannheim.ids.wiki;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mannheim.ids.wiki.WikiXMLProcessor;

/** Main class for Wikitext to WikiXML conversion
 *	
 *	WikiXMLConverter takes 8 arguments:
 *	-l	2-letter language code of the Wikipedia, e.g. en, de, fr, hu, it
 *	-w 	Wikidump filename
 *  -up User page in the Wikipedia language, e.g. Benutzer in German Wikipedia
 *  -tp Talk page in the Wikipedia language, e.g. Diskussion in German Wikipedia
 *  -up User contribution page in the Wikipedia language, e.g. 
 *  	Spezial:Beiträge in German Wikipedia  
 *	-t	The type of Wikipages [articles | discussions | all]
 * 	-o	The WikiXML output directory
 * 	-e	The output encoding, e.g. utf-8 or iso-8859-1  
 *
 *	Example arguments:
 *	-l de -w data/dewiki-20130728-sample.xml - up Benutzer -tp Diskussion 
 *	-up Spezial:Beiträge -t articles -o xml-de/articles -e utf-8
 *
 *	Alternatively, WikiXMLConverter can read a properties file. 
 *	Run with arguments: -prop config.properties
 * 
 * @author margaretha
 *
 */

public class WikiXMLConverter {
	
	private Options options;
	
	public WikiXMLConverter() {
		options = new Options();
		options.addOption("l", true, "2-letter language code of the Wikipedia");	
		options.addOption("up", true, "User page in the Wikipedia language, e.g. " +
				"\"Benutzer\" in German Wikipedia");
		options.addOption("tp", true, "Talk page in the Wikipedia language, e.g. " +
				"\"Diskussion\" in German Wikipedia");
		options.addOption("uc", true, "User contribution page in the Wikipedia " +
				"language, e.g. \"Spezial:Beiträge\" in German Wikipedia");
		options.addOption("w", true, "Wikidump filename");
		options.addOption("t", true, "The type of Wikipages [articles | discussions | all]");
		options.addOption("o", true, "The WikiXML output directory");
		options.addOption("e", true, "Encoding: utf-8 or iso-8859-1");
		options.addOption("prop", true, "Properties file");
	}
	
	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		
		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter.createConfig(args);
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.createWikiXML();	
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
	}
	
	public Configuration createConfig(String[] args) throws ParseException, IOException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);		
						
		String propertiesFile = cmd.getOptionValue("prop");
		if (propertiesFile != null){			
			return new Configuration(propertiesFile);
		}
		else{
			return new Configuration(cmd);
		}
	}
}
