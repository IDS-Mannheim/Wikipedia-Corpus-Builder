package de.mannheim.ids.wiki;

import java.io.IOException;

public class WikiXMLConverterExample {

	public static void main(String[] args) throws IOException {
		long startTime = System.nanoTime();

		// Set the language of the Wikipedia
		String language = "de";
		// Set wikidump filepath
		String wikidump = "data/dewiki-20130728-sample.xml";
		// User page in the Wikipedia language, e.g. Benutzer in German
		// Wikipedia
		String userPage = "Benutzer";
		// User contribution page in the Wikipedia language, e.g.
		// Spezial:Beiträge in German Wikipedia
		String userContribution = "Spezial:Beiträge";
		String signature = "Hilfe:Signatur";
		// the output encoding
		String encoding = "iso-8859-1";
		// The namespace of the Wikipages to convert
		int namespaceKey = 1; // talk page
		// Set maximum number of threads running concurrently, e.g. as many as
		// the number of CPUs
		int maxThreads = 4;
		// Set whether a file for each wikipage is to be created or not.
		boolean generateWikitext = true;

		Configuration config = new Configuration(wikidump, language, userPage,
				userContribution, signature, namespaceKey, encoding,
				maxThreads, generateWikitext);

		WikiXMLProcessor wxp = new WikiXMLProcessor(config);
		wxp.run();

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Wikitext to XML execution time " + duration);
	}
}
