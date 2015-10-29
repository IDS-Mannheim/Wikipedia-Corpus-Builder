package de.mannheim.ids.wiki;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main class for Wikitext to WikiXML conversion
 * 
 * WikiXMLConverter reads a properties file and set the conversion configuration
 * based on the file.
 * 
 * Run with arguments: -prop config.properties
 * 
 * @author margaretha
 * 
 */

public class WikiXMLConverter {

	private Options options;

	public WikiXMLConverter() {
		options = new Options();
		options.addOption("prop", true, "Properties file");
	}

	public static void main(String[] args) throws Exception {
		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter.createConfig(args);
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();
	}

	public Configuration createConfig(String[] args) throws ParseException,
			IOException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		String propertiesFile = cmd.getOptionValue("prop");
		if (propertiesFile != null) {
			return new Configuration(propertiesFile);
		}
		else {
			throw new IllegalArgumentException(
					"Please specify the location of the .properties file.");
		}
	}
}
