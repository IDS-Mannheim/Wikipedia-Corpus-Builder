package de.mannheim.ids.wiki;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main class for Wikitext to WikiXML conversion
 * 
 * WikiXMLConverter takes 8 arguments:
 * -w Wikidump filename
 * -l 2-letter language code of the Wikipedia, e.g. en, de, fr, hu, it
 * -u User page in the Wikipedia language, e.g. Benutzer in German Wikipedia
 * -c User contribution page in the Wikipedia language, e.g.
 * Spezial:Beiträge in German Wikipedia
 * -s Signature page in the Wikidump language, e.g.
 * Wikipedia:Signature in English, Hilfe:Signature in German
 * -k Namespace key of the Wikipedia pages to convert
 * -x Number of maximal threads allowed to run concurrently
 * -e The output encoding, e.g. utf-8 or iso-8859-1
 * 
 * Example arguments:
 * -l de -w data/dewiki-20130728-signature-sample.xml -u Benutzer
 * -c Spezial:Beiträge -s Hilfe:Signatur -k 1 -x 3 -e utf-8
 * 
 * Alternatively, WikiXMLConverter can read a properties file.
 * Run with arguments: -prop config.properties
 * 
 * @author margaretha
 * 
 */

public class WikiXMLConverter {

	private Options options;

	public WikiXMLConverter() {
		options = new Options();
		options.addOption("w", true, "Wikidump filename");
		options.addOption("l", true, "2-letter language code of the Wikipedia");
		options.addOption("u", true,
				"User page in the Wikipedia language, e.g. "
						+ "\"Benutzer\" in German Wikipedia");
		options.addOption("c", true, "User contribution page in the Wikipedia "
				+ "language, e.g. \"Spezial:Beiträge\" in German Wikipedia");
		options.addOption("s", true,
				"Signature page in the Wikipedia dump language.");
		options.addOption("k", true,
				"Namespace key of the Wikipedia pages to convert");
		options.addOption("x", true,
				"Number of maximal threads allowed to run " + "concurrently");
		options.addOption("e", true, "Encoding: utf-8 or iso-8859-1");
		options.addOption("prop", true, "Properties file");
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter.createConfig(args);
		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();

		long endTime = System.nanoTime();
		long duration = TimeUnit.NANOSECONDS.toHours(endTime - startTime);
		System.out.println("WikiXMLConverter execution time " + duration);
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
			return new Configuration(cmd);
		}
	}
}
