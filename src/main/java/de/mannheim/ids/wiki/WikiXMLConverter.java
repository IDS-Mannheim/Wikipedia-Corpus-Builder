package de.mannheim.ids.wiki;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mannheim.ids.util.LanguageProperties;
import de.mannheim.ids.wiki.WikiXMLProcessor;

/** Main class for Wikitext to WikiXML conversion
 *	
 *	WikiXMLConverter takes 5 arguments:
 *	-l	2-letter language code of the Wikipedia [en | de | fr | hu | it | pl | no ]
 *	-w 	Wikidump filename
 *	-t	The type of Wikipages [articles | discussions | all]
 * 	-o	The WikiXML output directory
 * 	-e	The output encoding, e.g. utf-8 or iso-8859-1
 *
 *	Example arguments:
 *	-l de -w dewiki-20130728-pages-meta-current.xml -t articles
 *	-o xml-de/articles -e utf-8
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
		options.addOption("w", true, "Wikidump filename");
		options.addOption("t", true, "The type of Wikipages [articles | discussions | all]");
		options.addOption("o", true, "The WikiXML output directory");
		options.addOption("e", true, "Encoding: utf-8 or iso-8859-1");
	}
	
	public static void main(String[] args) throws Exception {		
		WikiXMLConverter converter = new WikiXMLConverter();
		converter.run(args);
	}
	
	public void run(String[] args) throws ParseException, IOException {
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);		
						
		Configuration config = new Configuration();
		config.setConfigFromCommandLine(cmd);
		
		convert(config);
	}
	
	public static void convert(Configuration config) throws IOException{
		
			
		long startTime = System.nanoTime();
		LanguageProperties lp = new LanguageProperties(config.getLanguageCode(),
				config.getNamespaces());
		
		WikiXMLProcessor wxp = new WikiXMLProcessor(lp,config.getNamespaces());
		try {
			wxp.createWikiXML(config.getWikidump(), config.getOutputFolder(), config.getEncoding());
		} catch (IOException e) {
			throw new IOException("Failed creating WikiXML.", e);
		}
				
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time "+duration);
	}
}
