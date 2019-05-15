package de.mannheim.ids.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This is the main class for converting the XML-ized Wikipages to I5.
 * 
 * It requires the following data:
 * <ul>
 * <li>WikiXML files, which are outputs of the WikiXMLConverter, e.g. in
 * <code>wikixml-de/article</code> folder.</li>
 * <li>An index of all the WikiXML files, generated by WikiXMLCorpusIndexer.sh.
 * To generate the index, run WikiXMLCorpusIndexer.sh on a terminal in this
 * format:
 * 
 * ./WikiXMLCorpusIndexer.sh [pageType] [outputFile] [wikiXMLFolder]
 * Example: ./WikiXMLCorpusIndexer.sh article article-index.xml
 * wikixml-de/article
 * 
 * </li>
 * <li>A list of inflectives (only provided for German)</li>
 * <li>A database containing a language links table restored from the wiki
 * language link sql dump, for instance, dewiki-20150430-langlinks.sql.</li>
 * </ul>
 *
 * <p>
 * It takes a properties file to configure the parameters needed by the program.
 * </p>
 * 
 * 
 * @author margaretha
 */

public class WikiI5Converter {

	private static Options options;

	public WikiI5Converter() {
		options = new Options();
		options.addOption("prop", true, "Properties filename");
	}

	public static void main(String[] args) throws ParseException, IOException,
			I5Exception, SQLException {
		WikiI5Converter converter = new WikiI5Converter();
		Configuration config = converter.createConfig(args);
		WikiI5Processor processor = new WikiI5Processor(config);
		processor.run();
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
						"Properties file " + propertiesFile + " is not found.");
			}

			Properties properties = new Properties();
			properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
			is.close();

			return new Configuration(properties);
		}
	}

}
