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
	
	private Options options;
	
	public WikiI5Converter() {
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
	}
	
	public static void main(String[] args) throws Exception {		
		WikiI5Converter converter = new WikiI5Converter();
		converter.run(args);
	}
	
	public void run(String[] args) throws I5Exception  {
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			throw new I5Exception(e);
		}
						
		String xmlFolder = cmd.getOptionValue("x");
		String type = cmd.getOptionValue("t");
		String dumpFilename = cmd.getOptionValue("w");
		String outputFile = cmd.getOptionValue("o");		
		String encoding = cmd.getOptionValue("e");				
		String inflectives = cmd.getOptionValue("inf");
		String index = cmd.getOptionValue("i");
		String dbUrl = cmd.getOptionValue("d");
		String username = cmd.getOptionValue("u");
		String password = cmd.getOptionValue("p","secret");
		
		convert(xmlFolder, type, dumpFilename, inflectives, encoding, outputFile, 
				index, dbUrl, username, password);
						
	}
	
	public static void convert(String xmlFolder, String type, String dumpFilename,
			String inflectives, String encoding, String outputFile, String index,
			String dbUrl, String username, String password) throws 
			I5Exception  {	
		
		if (xmlFolder == null){
			throw new IllegalArgumentException("Please specify the WikiXML root folder.");
		}
		
		if (type == null){
			throw new IllegalArgumentException("Please specify a wiki dump file.");
		}
		if (!type.equals("articles") && !type.equals("discussions")){
			throw new IllegalArgumentException("The type is not recognized. " +
					"Please specify the type as: articles or dicussions");
		}
		
		if (dumpFilename == null){
			throw new IllegalArgumentException("Please specify the Wiki dump file.");
		}
		
		if (outputFile == null){
			throw new IllegalArgumentException("Please specify the output file.");
		}
		if (index == null){
			throw new IllegalArgumentException("Please specify the index of the Wikipedia "
					+type+".");
		}
		if (encoding == null){
			encoding = "utf-8"; // default encoding
		}
		if (dbUrl == null){
			throw new IllegalArgumentException("Please specify the Wikipedia language link " +
					"Mysql Database URL.");
		}		
		if (username == null){
			throw new IllegalArgumentException("Please specify the username of the Wikipedia " +
					"language link Mysql Database.");
		}
		if (password == null){
			throw new IllegalArgumentException("Please specify the password of  Wikipedia " +
					"language link Mysql Database.");
		}
		
		System.setProperty("entityExpansionLimit", "0");
		System.setProperty("totalEntitySizeLimit", "0");
		System.setProperty("PARAMETER_ENTITY_SIZE_LIMIT", "0");
				
		I5Corpus corpus = new I5Corpus(dumpFilename, type, encoding);		
		long startTime=System.nanoTime();
		
		try {
			DatabaseManager dbManager = new DatabaseManager(dbUrl, username, password);
			I5Writer  w = new I5Writer(corpus, outputFile, dbManager);
			w.open();
			w.createCorpusHeader();			
			// Do the convertion and write the resulting I5
			WikiI5Processor wikiI5Processor = new WikiI5Processor(corpus, inflectives);
			wikiI5Processor.run(xmlFolder, index, w);
			w.close();
		}
		catch (SQLException | XPathExpressionException e) {
			throw new I5Exception(e);
		}
		
		long endTime=System.nanoTime();					
		System.out.println("Transformation time "+ (endTime-startTime));
	}
}
