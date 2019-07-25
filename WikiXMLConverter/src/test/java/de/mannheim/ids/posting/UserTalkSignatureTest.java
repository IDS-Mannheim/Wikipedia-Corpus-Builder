package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.WikiXMLProcessor;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class UserTalkSignatureTest extends GermanTestBase {

	private WikiPostUser postUser;

	public UserTalkSignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
	}

	@Test
	public void testIncorrectUserTalkFormat()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "viele Grüße, [[Benutzer diskussion:"
				+ "Grueslayer|Grueslayer]] 21:08, 27. Feb. 2017 (CET)";
		WikiPage wikiPage = createWikiPage(
				"Benutzer Diskussion:Abu-Dun/Archiv/2017", "9756545", true,
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(1, doc.query("/posting/p/a").size());
		assertEquals("viele Grüße, Grueslayer 21:08, 27. Feb. 2017 (CET)",
				doc.query("/posting/p").get(0).getValue());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("21:08, 27. Feb. 2017 (CET)", timestamp.getValue());
	}

	@Test
	public void testUserTalkSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Magst Du Dich evtl. mal darum kümmern?&lt;br "
				+ "/&gt;Vielen Dank und viele Grüße, [[Benutzer Diskussion:"
				+ "Grueslayer|Grueslayer]] 21:08, 27. Feb. 2017 (CET)";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:Abu-Dun/"
				+ "Archiv/2017", "9756545", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals("2017-02-27T21:08+01",
				doc.query("/posting/@when-iso").get(0).getValue());
		assertEquals("Magst Du Dich evtl. mal darum kümmern?Vielen Dank und "
				+ "viele Grüße, GrueslayerGrueslayer21:08, 27. "
				+ "Feb. 2017 (CET)",
				doc.query("/posting/p").get(0).getValue());
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("21:08, 27. Feb. 2017 (CET)", timestamp.getValue());
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Grueslayer", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref").get(0);
		assertEquals("Grueslayer", ref.getValue());
		assertEquals("https://de.wikipedia.org/wiki/Benutzer_Diskussion:"
				+ "Grueslayer", ref.query("@target").get(0).getValue());
	}

	@Test
	public void testUserTalkSignatureLowercase()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Das angebliche Zitat sollte ebenfalls mal geprüft "
				+ "werden, da es den Ursprung ja offensichtlich nicht zu "
				+ "geben scheint. --[[benutzer diskussion: freak 1.5|"
				+ "Freak1.5]] 15:58, 20. Okt. 2006 (CEST)";
		WikiPage wikiPage = createWikiPage("Diskussion:Kamelopedia",
				"262282", true, wikitext);

		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("15:58, 20. Okt. 2006 (CEST)", timestamp.getValue());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("Freak1.5", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref").get(0);
		assertEquals("Freak1.5", ref.getValue());
		assertEquals("https://de.wikipedia.org/wiki/benutzer_diskussion:"
				+ "_freak_1.5", ref.query("@target").get(0).getValue());
	}

	@Test
	public void testUserTalkSignatureLowercaseAndUnderscore()
			throws IOException, ValidityException, ParsingException {

		String wikitext = "VG --[[benutzer_diskussion: freak 1.5|"
				+ "Freak1.5]] 15:58, 20. Okt. 2006 (CEST)";
		WikiPage wikiPage = createWikiPage("Diskussion:Kamelopedia",
				"262282", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
	}

	@Test
	public void testUserTalkSignatureUnderscore()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "The DieBucheBot has now the flag on the it:wiki. "
				+ "Good work :-) [[Benutzer_Diskussion:Gac|&lt;small&gt;&lt;"
				+ "font color=&quot;green&quot;&gt;'''Gac'''&lt;/font&gt;&lt;"
				+ "/small&gt;]] 08:55, 15. Mär. 2007 (CET)";

		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:DieBuche",
				"328552", true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(userTalkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/signed/date").get(0);
		assertEquals("08:55, 15. Mär. 2007 (CET)", timestamp.getValue());

		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("&lt;small&gt;&lt;font color=&quot;green&quot;&gt;"
				+ "&apos;&apos;&apos;Gac&apos;&apos;&apos;&lt;/font&gt;&lt;/small&gt;",
				StringEscapeUtils.escapeXml10(name.getValue()));
		Node ref = doc.query("/posting/p/signed/ref").get(0);
		assertEquals("&lt;small&gt;&lt;font color=&quot;green&quot;&gt;"
				+ "&apos;&apos;&apos;Gac&apos;&apos;&apos;&lt;/font&gt;&lt;/small&gt;",
				StringEscapeUtils.escapeXml10(name.getValue()));
		assertEquals("https://de.wikipedia.org/wiki/Benutzer_Diskussion:"
				+ "Gac", ref.query("@target").get(0).getValue());
	}

	@Test
	public void testUserTalkSignatureWithoutTimestamp()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "* 34 (30/4) [[:Benutzer Diskussion:Meffo|"
				+ "Meffo]] 2007-09-08 11:32 &amp;ndash; 2009-12-27 10:29";
		WikiPage wikiPage = createWikiPage("Diskussion:Definition", "1134",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node link = doc.query("/posting/ul/li/a").get(0);
		assertEquals("Meffo", link.getValue());
	}

	// There are very few people that would use English signature format in
	// the German Wiki. As far as I see only Leif Czerny signed using "user
	// talk" link only. There does not seem to be "User talk", "User_talk"
	// or "user_talk" link only signatures
	@Test
	public void testUserTalkSignatureEnglishFormat()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "::Von &quot;Dorf&quot; und  &quot;Wagen ziehen&quot; "
				+ "steht da nichts.-- &lt;small&gt;[[User Talk:Leif Czerny|Leif "
				+ "Czerny]]&lt;/small&gt; 12:30, 13. Feb. 2019 (CET)";

		// String wikitext = "please tell me on [[user talk:pcu123456789|my "
		// + "talk page]]. [[Benutzer:Pcu123456789|Pcu123456789]] 04:14, "
		// + "27 January 2007 (UTC)";
		WikiPage wikiPage = createWikiPage("Diskussion:Demokratie", "1173",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node posting = doc.query("/posting/p/small").get(0);
		assertEquals(0, posting.query("a").size());

		Node signature = posting.query("signed/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node name = posting.query("signed/name").get(0);
		assertEquals("Leif Czerny", name.getValue());
		Node ref = posting.query("signed/ref").get(0);
		assertEquals("Leif Czerny", ref.getValue());
		assertEquals("https://de.wikipedia.org/wiki/User_Talk:Leif_Czerny",
				ref.query("@target").get(0).getValue());
	}

	@Test
	public void testUserTalkSignatureEnglish()
			throws IOException, ValidityException, ParsingException {
		WikiXMLProcessor.Wikipedia_URI = "https://en.wikipedia.org/wiki/";
		InputStream is = UserTalkSignatureTest.class.getClassLoader()
				.getResourceAsStream("enwiki-talk.properties");
		Properties properties = new Properties();
		properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
		is.close();
		Configuration config = new Configuration(properties);
		
		String wikitext = "I think that this trick is much older. It is "
				+ "mentioned in Suppes, &quot;Axiomatic Set Theory&quot;, "
				+ "(1960), but I'm triyng to find a better reference. "
				+ "[[User Talk:Kismalac|kismalac]] 22:02, 14 April 2011"
				+ "(UTC)";
		WikiPage wikiPage = createWikiPage("Talk:Cardinal number", "6177",
				true, wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("kismalac", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref").get(0);
		assertEquals("kismalac", ref.getValue());
		assertEquals("https://en.wikipedia.org/wiki/User_Talk:"
				+ "Kismalac", ref.query("@target").get(0).getValue());
	}

	@Test
	public void testUserTalkSignatureUnderscoreEnglish()
			throws IOException, ValidityException, ParsingException {
		WikiXMLProcessor.Wikipedia_URI = "https://en.wikipedia.org/wiki/";
		InputStream is = UserTalkSignatureTest.class.getClassLoader()
				.getResourceAsStream("enwiki-talk.properties");
		Properties properties = new Properties();
		properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
		is.close();
		Configuration config = new Configuration(properties);
		
		String wikitext = ":::I think the idea of starting with the featured "
				+ "article set of photos and discussing changes from there "
				+ "is a good one.  -- [[User_Talk:SiobhanHansa|SiobhanHansa]] "
				+ "15:08, 6 November 2007 (UTC)";
		WikiPage wikiPage = createWikiPage("Talk:New York City/Archive 10",
				"6908", true, wikitext);
		
		
		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());

		Node signature = doc.query("/posting/p/signed/@type").get(0);
		assertEquals("signed", signature.getValue());
		
		Node name = doc.query("/posting/p/signed/name").get(0);
		assertEquals("SiobhanHansa", name.getValue());
		Node ref = doc.query("/posting/p/signed/ref").get(0);
		assertEquals("SiobhanHansa", ref.getValue());
		assertEquals("https://en.wikipedia.org/wiki/User_Talk:"
				+ "SiobhanHansa", ref.query("@target").get(0).getValue());
	}

}
