package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class UserTalkSignatureTest extends GermanTestBase {

	private WikiPostUser postUser;
	private WikiPostTime postTime;

	public UserTalkSignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
		postTime = new WikiPostTime("test", "talk");
	}
	
	@Test
	public void testUserTalkSignature()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Magst Du Dich evtl. mal darum kümmern?&lt;br "
				+ "/&gt;Vielen Dank und viele Grüße, [[Benutzer Diskussion:"
				+ "Grueslayer|Grueslayer]] 21:08, 27. Feb. 2017 (CET)";
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Benutzer Diskussion:Abu-Dun/Archiv/2017");
		wikiPage.setPageId("9756545");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(wikitext);

		int namespaceKey = 3; // benutzer diskussion
		Configuration config = createConfig(wikidump, namespaceKey,
				"user-talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		// System.out.println(wikiXML);
		Document doc = builder.build(wikiXML, null);
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("21:08, 27. Feb. 2017 (CET)", timestamp.getValue());
	}

	@Test
	public void testUserTalkSignatureWithUnderscore()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "The DieBucheBot has now the flag on the it:wiki. "
				+ "Good work :-) [[Benutzer_Diskussion:Gac|&lt;small&gt;&lt;"
				+ "font color=&quot;green&quot;&gt;'''Gac'''&lt;/font&gt;&lt;"
				+ "/small&gt;]] 08:55, 15. Mär. 2007 (CET)";

		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Benutzer Diskussion:DieBuche");
		wikiPage.setPageId("328552");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(wikitext);

		int namespaceKey = 3; // benutzer diskussion
		Configuration config = createConfig(wikidump, namespaceKey,
				"user-talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("08:55, 15. Mär. 2007 (CET)", timestamp.getValue());
	}

	// There are very few people that would use English signature format in
	// the German Wiki. As far as I see only Leif Czerny signed using "user
	// talk" link only. There does not seem to be "User talk", "User_talk"
	// or "user_talk" link only signatures
	@Test
	public void testUserTalkSignatureInEnglish()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "::Liebe IP, dass Du dir dafür diesen Tag ausgesucht "
				+ "hast! Nach Aristoteles hat sich also deiner Meinung nach "
				+ "niemand mehr relevant zum Thema geäußert?-- &lt;small&gt;"
				+ "[[user talk:Leif Czerny|Leif Czerny]]&lt;/small&gt; 17:28, "
				+ "22. Apr. 2017 (CEST)";

		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Diskussion:Demokratie");
		wikiPage.setPageId("1173");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(wikitext);

		int namespaceKey = 1; // diskussion
		Configuration config = createConfig(wikidump, namespaceKey, "talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node signature = doc.query("/posting/p/autoSignature/@type").get(0);
		assertEquals("signed", signature.getValue());

		Node timestamp = doc.query("/posting/p/autoSignature/timestamp").get(0);
		assertEquals("17:28, 22. Apr. 2017 (CEST)", timestamp.getValue());
	}

	@Test
	public void testUserTalkSignatureWithoutTimestamp()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "* 34 (30/4) [[:Benutzer Diskussion:Meffo|"
				+ "Meffo]] 2007-09-08 11:32 &amp;ndash; 2009-12-27 10:29";
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Diskussion:Definition");
		wikiPage.setPageId("1134");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add(wikitext);

		int namespaceKey = 1; // diskussion
		Configuration config = createConfig(wikidump, namespaceKey, "talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser,
				postTime);
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node link = doc.query("/posting/ul/li/a").get(0);
		assertEquals("Meffo", link.getValue());
	}
}
