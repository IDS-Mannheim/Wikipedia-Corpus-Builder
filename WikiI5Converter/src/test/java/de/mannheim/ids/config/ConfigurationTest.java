package de.mannheim.ids.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.WikiI5Converter;

public class ConfigurationTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private int namespacekey = 0;
	private String pageType = "article";
	private String language = "Deutsch";
	private String korpusSigle = "WPD17";
	private int maxThreads = 2;
	private String category = "Kategorie";
	private String categoryScheme = "https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie";
	private String creator = "creatorname";

	// The dumpFilename should be in the following format:
	// [2 letter language code]wiki-[year][month][date]-[type]
	private String dumpFilename = "dewiki-20170701-pages-meta-current.xml";

	private String xmlFolder = "wikixml-de/article";
	private String index = "index/dewiki-article-index.xml";
	// Set the inflectives file path or null if not available
	private String inflectives = "inflectives.xml";

	private String outputFile = "i5/dewiki-20170701-article.i5.xml";
	private String encoding = "ISO-8859-1";

	private String url = "jdbc:mysql://localhost:3306/wikipedia";
	private String username = "wikiuser";
	private String password = "wikipass";
	private String disableDTDValidation = "false";
	

	private WikiI5Converter converter = new WikiI5Converter();

	@Test
	public void testCreateConfig() {
		Configuration config = new Configuration(xmlFolder, namespacekey,
				pageType, dumpFilename, language, korpusSigle, inflectives,
				encoding, outputFile, index, url, username, password,
				maxThreads, creator, category, categoryScheme,disableDTDValidation);

		assertEquals(dumpFilename, config.getDumpFilename());
		assertEquals(outputFile, config.getOutputFile());
		assertEquals(inflectives, config.getInflectives());
		assertEquals(korpusSigle, config.getKorpusSigle());
		assertEquals(encoding, config.getOutputEncoding());
		assertEquals(language, config.getLanguage());
		assertEquals("de", config.getLanguageCode());
		assertEquals("article", config.getPageType());
		assertEquals(xmlFolder, config.getWikiXMLFolder());
		assertEquals(index, config.getWikiXMLIndex());
		assertEquals("2017", config.getYear());
		assertEquals(username, config.getDatabaseUsername());
		assertEquals(url, config.getDatabaseUrl());
		assertEquals(category, config.getCategory());
		assertEquals(categoryScheme, config.getCategoryScheme());
		assertEquals(creator, config.getCreator());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesEmpty() {
		Properties properties = new Properties();
		new Configuration(properties);
		thrown.expectMessage("namespace_key is required");
	}
	
	@Test
	public void testPropertiesMissingCategory() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "0");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("category is required.");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingCategoryScheme() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "0");
		properties.setProperty("category", "Kategorie");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("category_scheme is required.");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingWikiXMLFolder() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("wikixml_folder is required.");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingPageType() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("page_type is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingWikidump() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("wikidump is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingLanguage() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("language is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingCorpusSigle() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("korpusSigle is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingOutputFile() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("output_file is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingIndex() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		properties.setProperty("output_file", "i5/dewiki-20170701-article.i5.xml");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("wikixml_index is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingCreator() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		properties.setProperty("output_file", "i5/dewiki-20170701-article.i5.xml");
		properties.setProperty("wikixml_index", "index/dewiki-article-index.xml");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("creator is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingDatabaseUrl() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		properties.setProperty("output_file", "i5/dewiki-20170701-article.i5.xml");
		properties.setProperty("wikixml_index", "index/dewiki-article-index.xml");
		properties.setProperty("creator", "creator");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("db_url is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingDatabaseUsername() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		properties.setProperty("output_file", "i5/dewiki-20170701-article.i5.xml");
		properties.setProperty("wikixml_index", "index/dewiki-article-index.xml");
		properties.setProperty("creator", "creator");
		properties.setProperty("db_url", url);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("db_username is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingDatabasePassword() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		properties.setProperty("output_file", "i5/dewiki-20170701-article.i5.xml");
		properties.setProperty("wikixml_index", "index/dewiki-article-index.xml");
		properties.setProperty("creator", "creator");
		properties.setProperty("db_url", url);
		properties.setProperty("db_username", username);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("db_password is required");
		new Configuration(properties);
	}
	
	@Test
	public void testMinimalArticleProperties() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("category", "Kategorie");
		properties.setProperty("category_scheme",
				"https://de.wikipedia.org/wiki/Kategorie:!Hauptkategorie");
		properties.setProperty("wikixml_folder", "../WikiXMLConverter/wikixml-de/article");
		properties.setProperty("page_type", "article");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language", "Deutsch");
		properties.setProperty("korpusSigle", "WPD17");
		properties.setProperty("output_file", "i5/dewiki-20170701-article.i5.xml");
		properties.setProperty("wikixml_index", "index/dewiki-article-index.xml");
		properties.setProperty("creator", "creatorname");
		properties.setProperty("db_url", url);
		properties.setProperty("db_username", username);
		properties.setProperty("db_password", password);
		new Configuration(properties);
	}
	
	@Test
	public void testCreateArticleConfigFromProperties()
			throws IOException, ParseException {
		Configuration config = converter.createConfig(
				new String[]{"-prop", "dewiki-article.properties"});

		assertEquals(dumpFilename, config.getDumpFilename());
		assertEquals(outputFile, config.getOutputFile());
		assertEquals(inflectives, config.getInflectives());
		assertEquals(korpusSigle, config.getKorpusSigle());
		assertEquals(encoding, config.getOutputEncoding());
		assertEquals(language, config.getLanguage());
		assertEquals("de", config.getLanguageCode());
		assertEquals("article", config.getPageType());
		assertEquals(xmlFolder, config.getWikiXMLFolder());
		assertEquals(index, config.getWikiXMLIndex());
		assertEquals("2017", config.getYear());
		assertEquals(username, config.getDatabaseUsername());
		assertEquals(url, config.getDatabaseUrl());
		assertEquals(category, config.getCategory());
		assertEquals(categoryScheme, config.getCategoryScheme());
		assertEquals(creator, config.getCreator());
	}
}
