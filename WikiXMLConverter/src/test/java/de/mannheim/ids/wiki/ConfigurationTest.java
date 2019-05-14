package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.mannheim.ids.config.Configuration;

public class ConfigurationTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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
		assertEquals("Unsigniert", config.getUnsigned());
		assertEquals("hilfe:signatur", config.getSignature());
	}

	@Test
	public void testSpecialCharacters() throws ParseException, IOException {

		Configuration config = converter.createConfig(new String[]{"-prop",
				"plwiki-löschkandidaten.properties"});
		assertEquals("Specjalna:Wkład", config.getSpecialContribution());

		config = converter.createConfig(new String[]{"-prop",
				"eswiki-talk.properties"});
		assertEquals("Usuario discusión", config.getUserTalk());

		config = converter.createConfig(new String[]{"-prop",
				"frwiki-talk.properties"});
		assertEquals("Spécial:Contributions", config.getSpecialContribution());
		assertEquals("Non signé", config.getUnsigned());

		config = converter.createConfig(new String[]{"-prop",
				"huwiki-talk.properties"});
		assertEquals("Szerkesztővita", config.getUserTalk());
		assertEquals("Speciális:Contributions",
				config.getSpecialContribution());
		assertEquals("wikipédia:aláírás", config.getSignature());
		assertEquals("Aláíratlan", config.getUnsigned());

		config = converter.createConfig(new String[]{"-prop",
				"rowiki-talk.properties"});
		assertEquals("Special:Contribuții", config.getSpecialContribution());
		assertEquals("ajutor:semnătura personală", config.getSignature());
		assertEquals("Discuție Utilizator", config.getUserTalk());
	}

	@Test(expected = NullPointerException.class)
	public void testNonexistentProperties() throws ParseException, IOException {
		converter.createConfig(new String[]{"-prop",
				"dewiki.properties"});
	}

	@Test
	public void testPropertiesEmpty() {
		Properties properties = new Properties();
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("namespace_key is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingWikidump() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("wikidump is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingLanguageCode() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("language_code is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingPageType() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("page_type is required");
		new Configuration(properties);
	}

	@Test
	public void testArticleProperties() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "0");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "article");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingUser() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("user_page is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingUserTalk() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("user_talk is required");
		new Configuration(properties);
	}
	
	@Test
	public void testPropertiesMissingSpecialContribution() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		properties.setProperty("user_talk", "Benutzer Diskussion");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("special_contribution is required");
		new Configuration(properties);
		
	}

	@Test
	public void testPropertiesMissingUnsigned() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		properties.setProperty("user_talk", "Benutzer Diskussion");
		properties.setProperty("special_contribution", "Spezial:Beiträge");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("unsigned is required");
		new Configuration(properties);
	}

	@Test
	public void testPropertiesMissingSignature() {
		Properties properties = new Properties();
		properties.setProperty("namespace_key", "1");
		properties.setProperty("wikidump", "data/dewiki-20170701-sample.xml");
		properties.setProperty("language_code", "de");
		properties.setProperty("page_type", "talk");
		properties.setProperty("user_page", "Benutzer");
		properties.setProperty("user_talk", "Benutzer Diskussion");
		properties.setProperty("special_contribution", "Spezial:Beiträge");
		properties.setProperty("unsigned", "Unsigniert");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("signature is required");
		new Configuration(properties);
	}

	@Test
	public void testTalkProperties() {
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
