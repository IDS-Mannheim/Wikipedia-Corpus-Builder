package de.mannheim.ids.wiki;


/**
 * This code is an example how to run the WikiXCESConverter.
 * 
 * It needs the following data:
 * <ul>
 * <li>WikiXML article pages, outputs of WikiXMLConverter, in
 * <code>xml-de/articles</code> folder.</li>
 * <li>an index of WikiXML files generated by WikiXMLCorpusIndexer.sh. To
 * generate the index, run:
 * 
 * <pre>
 * <em>
 *       ./WikiXMLCorpusIndexer.sh articles xml-de/articles articleIndex.xml
 *    </em>
 * </pre>
 * 
 * on a terminal.</li>
 * </ul>
 * 
 * In this example, WikiXML article pages are converted into I5 format and put
 * together in a
 * single corpusfile.
 * 
 * @author margaretha
 * */
public class WikiI5ConverterExample {

	public static void main(String[] args) throws I5Exception {

		String xmlFolder = "xml-de/articles";
		int namespacekey = 0;
		String index = "xml-de/articleIndex.xml";

		String language = "Deutsch";
		String korpusSigle = "WPD13";
		
		// The dumpFilename should be in the following format:
		// [2 letter language code]wiki-[year][month][date]-[type]
		String dumpFilename = "dewiki-20130728-sample.xml";
		String outputFile = "i5/dewiki-20130728-articles.i5";

		// Set the inflectives filepath or null if not available
		String inflectives = "inflectives.xml";
		String encoding = "UTF-8";

		String url = "jdbc:mysql://host:port/dbname";
		String username = "username";
		String password = "password";
		int maxThreads = 3;
		
		Configuration config = new Configuration(xmlFolder, namespacekey, dumpFilename, 
				language, korpusSigle, inflectives, encoding, outputFile, index, url, 
				username, password, maxThreads);

		WikiI5Processor processor = new WikiI5Processor(config);
		processor.run();
	}

}
