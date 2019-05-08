package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigurationTest {

	@Rule
	public ExpectedException thrown= ExpectedException.none();
	
	WikiXMLConverter converter;
	public ConfigurationTest() {
		converter = new WikiXMLConverter();
	}

	@Test
	public void testArticleConfiguration() throws ParseException, IOException {
		Configuration config = converter.createConfig(new String[]{"-prop",
				"dewiki-article.properties"});

		assertEquals("article", config.getPageType());
		assertTrue(!config.isDiscussion());
		assertEquals("de", config.getLanguageCode());
		assertEquals(0, config.getNamespaceKey());
		assertEquals(4, config.getMaxThreads());

		assertEquals("wikixml-de/article", config.getOutputFolder());
		assertTrue(!config.isWikitextToGenerate());
		assertEquals(null, config.getWikitextFolder());
	}

	@Test
	public void testTalkConfiguration() throws ParseException, IOException {
		Configuration config = converter.createConfig(new String[]{"-prop",
				"dewiki-talk.properties"});

		assertTrue(config.isDiscussion());
		assertEquals(1, config.getNamespaceKey());
		assertEquals(1, config.getMaxThreads());
		assertEquals("wikixml-de/talk", config.getOutputFolder());
		assertTrue(config.isWikitextToGenerate());
		assertEquals("wikitext-de/talk", config.getWikitextFolder());

		assertEquals("Benutzer", config.getUserPage());
		assertEquals("Spezial:Beiträge", config.getSpecialContribution());
		assertEquals("unsigniert", config.getUnsigned());
		assertEquals("Hilfe:Signatur", config.getSignature());
	}
	
	@Test
	public void testPolskiConfiguration() throws ParseException, IOException {

		Configuration config = converter
				.createConfig(new String[]{"-prop",
						"plwiki-löschkandidaten.properties"});

		assertEquals("Specjalna:Wkład", config.getSpecialContribution());
	}

	@Test(expected = NullPointerException.class)
	public void testNonexistentProperties() throws ParseException, IOException {
		converter.createConfig(new String[]{"-prop",
				"dewiki.properties"});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesEmpty(){
		Properties properties = new Properties();
		new Configuration(properties);
		thrown.expectMessage("namespace_key is required");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingWikidump(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		new Configuration(properties);
		thrown.expectMessage("wikidump is required");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingLanguageCode(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		new Configuration(properties);
		thrown.expectMessage("language_code is required");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingPageType(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		new Configuration(properties);
		thrown.expectMessage("page_type is required");
	}
	
	@Test
	public void testArticleProperties(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "0");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "article");
		new Configuration(properties);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingUser(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		new Configuration(properties);
		thrown.expectMessage("user_page is required");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingUserContribution(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		new Configuration(properties);
		thrown.expectMessage("user_contribution is required");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingUnsigned(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		properties.setProperty("user_contribution", "Spezial:Beiträge");
		new Configuration(properties);
		thrown.expectMessage("unsigned is required");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesMissingSignature(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		properties.setProperty("user_contribution", "Spezial:Beiträge");
		properties.setProperty("unsigned", "insigniert");
		new Configuration(properties);
		thrown.expectMessage("signature is required");
	}
	
	@Test
	public void testTalkProperties(){
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		properties.setProperty("user_talk", "Benutzer Diskussion");
		properties.setProperty("special_contribution", "Spezial:Beiträge");
		properties.setProperty("unsigned", "insigniert");
		properties.setProperty("signature", "Hilfe:Signatur");
		new Configuration(properties);
	}
}
