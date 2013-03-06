package de.mannheim.ids.wiki;

import java.io.IOException;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class WikiDBConverter {

	public static void main(String[] args) throws WikiApiException, IOException {
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("klinux10.ids-mannheim.de");        
        dbConfig.setDatabase("wikipedia");
        dbConfig.setUser("margaretha");
        dbConfig.setPassword("MRJXxXUDXeCWDhHU");
        dbConfig.setLanguage(Language.german);
                
        XMLDBWikiProcessor wikiProcessor = new XMLDBWikiProcessor("de");
        wikiProcessor.process(dbConfig, "error.log");
	}	
}
