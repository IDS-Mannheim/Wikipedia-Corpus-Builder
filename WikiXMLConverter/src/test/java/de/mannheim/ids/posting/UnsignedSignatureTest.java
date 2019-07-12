package de.mannheim.ids.posting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.WikiXMLConverter;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.wiki.page.WikiTalkHandler.SignatureType;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class UnsignedSignatureTest extends GermanTestBase {
	WikiPostUser postUser;
	public UnsignedSignatureTest() throws IOException {
		postUser = new WikiPostUser("test", "talk");
	}

	@Test
	public void testSimpleUnsigned()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Aus dem Artikel geht das leider nicht so recht "
				+ "hervor. {{unsigned}}";

		WikiPage wikiPage = createWikiPage("Diskussion:Albert Camus", "31869",
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();
		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
	}

	@Test
	public void testSimpleUnsigniert()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Die monotheistischen Weltreligionen sehen in "
				+ "Gott doch auch den Herrscher der Welt.{{Unsigniert}}";

		WikiPage wikiPage = createWikiPage("Diskussion:Theismus", "16009",
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();
		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
	}

	@Test
	public void testUnsigniertWithIP()
			throws IOException, ValidityException, ParsingException {

		String wikitext = "{{Unsigniert|87.122.213.193|23:57, 18. Nov. 2007"
				+ "|--[[Benutzer:Roo1812|Roo1812]] 08:41, 19. Nov. 2007 "
				+ "(CET)}}";
		String wikitext2 = "{{unsigniert|87.122.213.193|23:57, 18. Nov. 2007"
				+ "|--[[Benutzer:Roo1812|Roo1812]] 08:41, 19. Nov. 2007 "
				+ "(CET)}}";
		WikiPage wikiPage = createWikiPage("Diskussion:Zelle (Biologie)",
				"14131", wikitext, wikitext2);

		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wrapWithTextElement(wikiXML), null);
		Node signature = doc.query("/text/posting/p/signed/@type")
				.get(0);
		assertEquals("unsigned", signature.getValue());
		signature = doc.query("/text/posting/p/signed/@type").get(1);
		assertEquals("unsigned", signature.getValue());
	}

	@Test
	public void testUnsignedWithCustomUsername()
			throws IOException, ValidityException, ParsingException {

		String wikitext = "{{Unsigned|Synergetik-therapie@web.de|[["
				+ "Benutzer:MBq|MBq]] 09:30, 29. Dez 2005 (CET)}}";
		String wikitext2 = "{{unsigned|Synergetik-therapie@web.de|[["
				+ "Benutzer:MBq|MBq]] 09:30, 29. Dez 2005 (CET)}}";

		WikiPage wikiPage = createWikiPage("Diskussion:Synergetik-"
				+ "Therapie/Archiv1", "1152863", wikitext, wikitext2);

		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();

		String wikiXML = wrapWithTextElement(wikiPage.getWikiXML());
		Document doc = builder.build(wikiXML, null);
		Node signature = doc.query("/text/posting/p/signed/@type")
				.get(0);
		assertEquals("unsigned", signature.getValue());

		signature = doc.query("/text/posting/p/signed/@type").get(1);
		assertEquals("unsigned", signature.getValue());
	}

	@Test
	public void testUnsignedWithIPAndUsername()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "-Das sehe ich auch so. Und wie wäre es mit einem "
				+ "Hinweis auf die psychoanalytische Bedeutung?{{unsigned|"
				+ "80.219.9.7|Bijick}}";
		WikiPage wikiPage = createWikiPage("Diskussion:Latenz", "27190",
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node p = doc.query("/posting/p").get(0);
		assertEquals("unsigned", p.query("signed/@type").get(0)
				.getValue());
		assertEquals("-Das sehe ich auch so. Und wie wäre es mit einem "
				+ "Hinweis auf die psychoanalytische Bedeutung?", p.getValue());
	}

	@Test
	public void testIncorrectDateFormat()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Wo gerade Sushi absolut en vogue ist (&quot;"
				+ "Rohfisch!!!&quot;) --{{unsigned|62.180.109.219|5. "
				+ "Januar 2008, 05:32 Uhr}}";
		WikiPage wikiPage = createWikiPage("Diskussion:Inuit", "1152863",
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node p = doc.query("/posting/p").get(0);
		assertEquals("unsigned", p.query("signed/@type").get(0)
				.getValue());
		assertEquals("Wo gerade Sushi absolut en vogue ist (\"Rohfisch!!!\") "
				+ "--", p.getValue());
	}

	@Test
	public void testUnsignedWithHelpSignatureLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "(Wenn ich mal Zeit habe ... :) )  "
				+ "Marus {{unsigned| Marus | 23:34, 19. Mär. 2009}} "
				+ "&lt;small&gt;(''[[Hilfe:Signatur|ohne Datum und/oder "
				+ "Uhrzeit signierter]] Beitrag, erstellt um'' 23:34, "
				+ "19. Mär. 2009 (CET))&lt;/small&gt;";

		WikiPage wikiPage = createWikiPage("Diskussion:Salze", "42343",
				wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();
		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());
	}

	@Test
	public void testUnsignedWithUserLink()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "Kapitel 4.3 hat nur ein Unterkapitel: 4.3.1 "
				+ "{{unsigned|80.135.214.113|--[[Benutzer:Chef|Pangloss]] "
				+ "[[Benutzer Diskussion:Chef|Diskussion]] 16:32, 20. Mai "
				+ "2006 (CEST)}}";
		WikiPage wikiPage = createWikiPage("Diskussion:Friedrich Nietzsche/"
				+ "Archiv/1", "1627", wikitext);
		WikiTalkHandler handler = new WikiTalkHandler(talkConfig, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();
		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		Node signature = doc.query("/posting/p/signed").get(0);
		assertEquals(SignatureType.UNSIGNED.toString(),
				signature.query("@type").get(0).getValue());
		assertEquals("16:32, 20. Mai 2006 (CEST)",
				signature.query("date").get(0).getValue());
	}

	@Test
	public void testUnsignedUnderscore() throws ParseException, IOException,
			ValidityException, ParsingException {
		String wikitext = "una sección que justamente trata de apoyo al "
				+ "estándar.{{no_firmado|200.84.136.144}}";
		WikiPage wikiPage = createWikiPage("Discusión:Mozilla Firefox",
				"152144", wikitext);

		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter.createConfig(new String[]{"-prop",
				"eswiki-talk.properties"});
		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(), new WikiErrorWriter(), postUser);
				
		handler.run();
		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		assertEquals(0, doc.query("/posting/p/a").size());
		assertEquals(SignatureType.UNSIGNED.toString(),
				doc.query("/posting/p/signed/@type").get(0).getValue());
	}
}
