package de.mannheim.ids.wiki;

/**
 * This code is not a test but an example how to run WikiI5Converter.
 * 
 * WikiI5Converter requires the following data:
 * <ul>
 * <li>WikiXML article pages, outputs of WikiXMLConverter, e.g. in
 * <code>xml-de/articles</code> folder.</li>
 * <li>an index of WikiXML files generated by WikiXMLCorpusIndexer.sh. To
 * generate the index, run in a terminal:
 * 
 * <pre>
 * ./WikiXMLCorpusIndexer.sh articles xml-de/articles articleIndex.xml
 * </pre>
 * 
 * </li>
 * </ul>
 * 
 * WikiXML article pages are converted into I5 format and put
 * together in a single corpusfile.
 * 
 * @author margaretha
 */
public class WikiI5ConverterExample {

	public static void main(String[] args) throws I5Exception {

		int namespacekey = 1;
		String pageType = "talk";
		String language = "Deutsch";
		String korpusSigle = "WPD17";
		int maxThreads = 2;
		String category = "Kategorie";
		String categoryScheme = "https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie";
		String creator = "creatorname";

		// The dumpFilename should be in the following format:
		// [2 letter language code]wiki-[year][month][date]-[type]
		String dumpFilename = "dewiki-20170701-sample.xml";

		String xmlFolder = "wikixml-de/talk";
		String index = "index/dewiki-talk-index.xml";
		// Set the inflectives file path or null if not available
		String inflectives = "inflectives.xml";

		String outputFile = "i5/dewiki-20170701-talk.i5.xml";
		String encoding = "ISO-8859-1";

		String url = "jdbc:mysql://localhost:3306/database";
		String username = "username";
		String password = "password";

		Configuration config = new Configuration(xmlFolder, namespacekey,
				pageType, dumpFilename, language, korpusSigle, inflectives,
				encoding, outputFile, index, url, username, password,
				maxThreads, creator, category, categoryScheme);

		WikiI5Processor processor = new WikiI5Processor(config);
		processor.run();
	}

}
