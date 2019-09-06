package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import de.mannheim.ids.base.GermanTestBase;
import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.page.WikiArticleHandler;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.wiki.page.WikiTalkHandler;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostUser;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class WikiPageHandlerTest extends GermanTestBase {

//	@Test
//	public void testEscapedElement() throws IOException {
//		WikiStatistics stat = new WikiStatistics();
//		WikiErrorWriter errorWriter = new WikiErrorWriter();
//		String wikitext = "# &lt;li value=&quot;11&quot;&gt; Gisbert I. (1402–1429) &lt;/li&gt;";
//		WikiPage wikiPage = createWikiPage("Empty-page1", "1", false,
//				wikitext);
//		WikiArticleHandler handler = new WikiArticleHandler(articleConfig,
//				wikiPage, stat, errorWriter);
//		handler.run();
//		
//		String wikiXML = wikiPage.getWikiXML();
//		System.out.println(wikiXML);
//	}

	// the code content is rendered as template
	@Test
	public void testComplexCode() throws IOException, ValidityException, ParsingException {
		WikiStatistics stat = new WikiStatistics();
		WikiErrorWriter errorWriter = new WikiErrorWriter();
		String wikitext = "&lt;div style=&quot;margin-bottom:.4em; padding:0;&quot;&gt;\n"+ 
				"{| {{Bausteindesign3}}\n" +
				"| style=&quot;padding: 2px; width:34px; &quot; | [[Datei:File.svg|30px|center]]\n"	+
				"| Auf dieser Seite werden Abschnitte [[Hilfe:Archivieren#Automatische Archivierung|automatisch archiviert]], deren jüngster Beitrag mehr als 10 Tage zurückliegt und die mindestens 2 [[Hilfe:Signatur|signierte]] Beiträge enthalten. Beiträge, die seit 4 Tagen mit dem Baustein &lt;code&gt;&amp;#123;&amp;#123;{{#if:|subst:}}{{#ifeq:{{padleft:|1|Erledigt}}|:|&amp;#58;[[{{FULLPAGENAME:Erledigt}}]]|{{#switch:{{NAMESPACE:Erledigt}}|{{ns:0}}|{{ns:10}}=[[{{ns:10}}:{{PAGENAME:Erledigt}}|{{PAGENAME:Erledigt}}]]|{{ns:6}}|{{ns:14}}=[[:Erledigt]]|#default=[[Erledigt]]}}}}{{#ifeq:1=&amp;#x7e;&amp;#x7e;&amp;#x7e;&amp;#x7e;|1=&amp;#x7e;&amp;#x7e;&amp;#x7e;&amp;#x7e;|&amp;#124;1=&amp;#x7e;&amp;#x7e;&amp;#x7e;&amp;#x7e;}}{{#ifeq:x|{{{3}}}|&amp;#124;{{{3}}}}}{{#ifeq:x|{{{4}}}|&amp;#124;{{{4}}}}}{{#ifeq:x|{{{5}}}|&amp;#124;{{{5}}}}}{{#ifeq:x|{{{6}}}|&amp;#124;{{{6}}}}}&amp;#125;&amp;#125;&lt;/code&gt; versehen sind, werden ebenfalls archiviert.\n" +
				"|}&lt;/div&gt;";
		WikiPage wikiPage = createWikiPage("Diskussion:Osama bin Laden", "16252", false,
				wikitext);
		WikiArticleHandler handler = new WikiArticleHandler(talkConfig,
				wikiPage, stat, errorWriter);
		handler.run();
		
		String wikiXML = wikiPage.getWikiXML();
		
		Document doc = builder.build(wikiXML, null);
		Node td = doc.query("/div/table/tr/td").get(1);
		assertEquals("template", td.query("p/code/span/@class").get(0).getValue());
	}
	
	@Test
	public void testTalkHandler() throws InterruptedException, IOException {
		String wikidump = "src/test/resources/wikitext/dewiki-20170701-9756545.xml";
		Configuration config = createUserTalkConfig(wikidump);
		WikiPage wikiPage = WikiPageReaderTest.readPage(config);
		assertTrue(wikiPage.getWikiXML().isEmpty());

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiStatistics stat = new WikiStatistics();
		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				stat, new WikiErrorWriter(), postUser);

		handler.run();
		assertNotNull(wikiPage.getWikiXML());

		postUser.close();
		stat.print();
	}

	@Test
	public void testEmptyPage()
			throws IOException, ValidityException, ParsingException {
		WikiStatistics stat = new WikiStatistics();
		WikiErrorWriter errorWriter = new WikiErrorWriter();
		String wikitext = "";
		WikiPage wikiPage = createWikiPage("Empty-page1", "1", false,
				wikitext);
		WikiArticleHandler handler = new WikiArticleHandler(articleConfig,
				wikiPage, stat, errorWriter);
		handler.run();
		assertEquals(1, stat.getEmptyParsedPages());

		wikitext = "too short";
		wikiPage = createWikiPage("Empty-page2", "2", false,
				wikitext);
		handler = new WikiArticleHandler(articleConfig,
				wikiPage, stat, errorWriter);
		handler.run();
		assertEquals(2, stat.getEmptyParsedPages());
	}

	@Test
	public void testEmptyTalkPages()
			throws IOException, ValidityException, ParsingException {
		WikiStatistics stat = new WikiStatistics();
		WikiErrorWriter errorWriter = new WikiErrorWriter();
		WikiPostUser postUser = new WikiPostUser("test", "talk");
		
		String wikitext = "...";
		WikiPage wikiPage = createWikiPage("Benutzer Diskussion:AnjjaBaumann",
				"3", true, wikitext);
		WikiTalkHandler talkHandler = new WikiTalkHandler(userTalkConfig,
				wikiPage, stat, errorWriter, postUser);
		talkHandler.run();
		assertEquals(1, stat.getEmptyParsedPages());

		wikitext = "{{gesperrter Benutzer}}";
		wikiPage = createWikiPage("Benutzer Diskussion:Greekstar", "4", true,
				wikitext);
		talkHandler = new WikiTalkHandler(userTalkConfig, wikiPage, stat,
				errorWriter, postUser);
		talkHandler.run();
		assertEquals(2, stat.getEmptyParsedPages());
	}

}
