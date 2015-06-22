package de.mannheim.ids.wiki;

import java.sql.SQLException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mannheim.ids.db.DatabaseManager;

/** This is the main class for converting the XML-ized Wikipages to XCES
 * 	An argument configuration example: 
 * 	-x xml-de/articles -t articles -w dewiki-20130728-sample.xml -o out.i5 
 * 	-e utf-8 -inf inflectives.xml -i xml-de/articleIndex.xml -u username 
 * 	-p password -d jdbc:mysql://localhost:port/dbname
 *   
 * 	@author margaretha 
 */

public class WikiI5Converter {	
	
	private static Options options;
	
	public static void setOptions(){
		options = new Options();
		options.addOption("x", true, "WikiXML article/discussion folder");
		options.addOption("t", true, "The type of Wikipages (articles or discussions)");
		options.addOption("w", true, "Wiki dump file starting with: [2 character " +
				"language code]wiki-[year][month][date], for example:" +
				"dewiki-20130728-pages-meta-current.xml");
		options.addOption("o", true, "Output file");
		options.addOption("e", true, "Encoding: utf-8 or iso-8859-1");
		options.addOption("inf", true, "Inflective file");
		options.addOption("i", true, "An index of Wiki article/discussion pages");
		options.addOption("u", true, "Mysql database username");
		options.addOption("p", true, "Mysql database password");
		options.addOption("d", true, "Mysql database URL, i.e. jdbc:mysql://localhost:port/dbname");
		options.addOption("prop", true, "Properties filename");
	}
	
	public static void main(String[] args) throws Exception {
		setOptions();
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			throw new I5Exception(e);
		}
		
		Configuration config = new Configuration();
		String propertiesFilename = cmd.getOptionValue("prop");
		if (propertiesFilename != null){
			config.setConfigFromProperties(propertiesFilename);
		}
		else{
			config.setConfigFromCommandLine(cmd);
		}		
		
		convert(config);
	}	
	
	public static void convert(Configuration config) throws 
			I5Exception  {	
		
		I5Corpus corpus = new I5Corpus(config.getDumpFilename(), config.getPageType(), 
				config.getEncoding());
				
		DatabaseManager dbManager = null;
		long startTime=System.nanoTime();
		try {
			// Initializing DatabaseManager
			dbManager = new DatabaseManager(config.getDatabaseUrl(), 
					config.getDatabaseUsername(), config.getDatabasePassword());
		}
		catch (SQLException e) {
			throw new I5Exception(e);
		}
		
		// Initialzing I5Writer
		I5Writer  w = new I5Writer(corpus, config.getOutputFile(), dbManager);
		w.open();
		w.createCorpusHeader();
		
		// Do the convertion and write the resulting I5
		WikiI5Processor wikiI5Processor = new WikiI5Processor(corpus, config.getInflectives());
		try {
			wikiI5Processor.run(config.getWikiXMLFolder(), config.getWikiXMLIndex(), w);
		} catch (XPathExpressionException e) {
			throw new I5Exception(e);
		}
		w.close();
			
		
		long endTime=System.nanoTime();					
		System.out.println("Transformation time "+ (endTime-startTime));
	}
}
