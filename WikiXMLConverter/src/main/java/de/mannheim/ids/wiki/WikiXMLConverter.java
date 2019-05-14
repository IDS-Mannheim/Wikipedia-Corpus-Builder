package de.mannheim.ids.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mannheim.ids.config.Configuration;

/**
 * Main class for Wikitext to WikiXML conversion
 * 
 * WikiXMLConverter reads a properties file and set the conversion configuration
 * based on the file.
 * 
 * Run with arguments: -prop article.properties
 * 
 * See the properties folder.
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
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		String propertiesFile = cmd.getOptionValue("prop");
		if (propertiesFile == null) {
			throw new IllegalArgumentException(
					"Please specify the location of the .properties file.");
		}
		else {
			File f = new File(propertiesFile);
			InputStream is;
			if (f.exists()) {
				is = new FileInputStream(f);
			}
			else {
				is = Configuration.class.getClassLoader()
						.getResourceAsStream(propertiesFile);
			}

			if (is == null) {
				throw new NullPointerException(
						"Properties file "+propertiesFile+" is not found.");
			}

			Properties properties = new Properties();
			properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
			is.close();

			return new Configuration(properties);
		}
	}
}
