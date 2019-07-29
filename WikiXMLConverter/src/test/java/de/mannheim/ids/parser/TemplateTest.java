package de.mannheim.ids.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;

import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.writer.WikiErrorWriter;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class TemplateTest {

	private Builder builder;
	private WikiConfig deWikiConfig, enWikiConfig;

	public TemplateTest()
			throws IOException, ParserConfigurationException, SAXException {
		builder = new Builder();
		deWikiConfig = LanguageConfigGenerator.generateWikiConfig("de");
		enWikiConfig = LanguageConfigGenerator.generateWikiConfig("en");
	}

	@Test
	public void testTemplateWithSignatureElement()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "{{Diskussion aufgeräumt|24. Februar 2014|2="
				+ "[https://de.wikipedia.org/w/index.php?title=Diskussion:"
				+ "Hauspferd&amp;oldid=124833777 Version vom 25. November 2013]|"
				+ "3=<signed type=\"signed\"> <date>0:17, 24. "
				+ "Feb. 2014 (CET)</date></signed>}}";

		Sweble2Parser swebleParser = new Sweble2Parser("12765",
				"Diskussion:Hauspferd", wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), deWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node span = doc.query("/p/span[1]").get(0);
		String spanClass = span.query("@class").get(0).getValue();

		assertEquals("template", spanClass);
	}

	@Test
	public void testTemplateMinimalS()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "::nun, aus töchtern werden mütter; aber "
				+ "nicht aus müttern töchter; diese waren bzw. sind "
				+ "schon töchter...  {{S}} --[[Benutzer:HilmarHansWerner"
				+ "|HilmarHansWerner]] ([[Benutzer Diskussion:"
				+ "HilmarHansWerner|Diskussion]]) 00:23, 14. Feb. 2016 "
				+ "(CET)";

		Sweble2Parser swebleParser = new Sweble2Parser("10838",
				"Diskussion:Alphastrahlung",
				wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), deWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{S}}_]", desc.getValue());
	}

	@Test
	public void testTemplateS()
			throws IOException, ValidityException, ParsingException {
		String wikitext = ":::Was war es denn Deiner Meinung nach, kein "
				+ "Angriff {{S|8P}} --[[Benutzer:MBurch|MBurch]] "
				+ "([[Benutzer Diskussion:MBurch|Diskussion]]) 15:34, "
				+ "17. Dez. 2014 (CET)";
		Sweble2Parser swebleParser = new Sweble2Parser("8303069",
				"Diskussion:Wladimir Wladimirowitsch Putin/Archiv/002",
				wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), deWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{S|8P}}_]", desc.getValue());
	}

	@Test
	public void testTemplateSmiley()
			throws ValidityException, ParsingException, IOException {
		String wikitext = ":::Du wiederholst dich. Jaja, ich bin an dem "
				+ "Schlamassel und dieser unötigen Zeit- und Byteverschwendung "
				+ "schuld {{Smiley|applaus}} --[[Benutzer:Benqo|Benqo]] "
				+ "([[Benutzer Diskussion:Benqo|Diskussion]]) 17:47, 16. Sep. "
				+ "2015 (CEST)";

		Sweble2Parser swebleParser = new Sweble2Parser("8967849",
				"Diskussion:Flüchtlingskrise in Europa ab 2015/Archiv/1",
				wikitext, "de", new WikiStatistics(),
				new WikiErrorWriter(), deWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{Smiley|applaus}}_]", desc.getValue());
	}

	@Test
	public void testTemplateEnglish()
			throws IOException, ValidityException, ParsingException {
		String wikitext = "::: Thank you for picking up that torch on behalf "
				+ "of all us Linux users.  {{(:}}  I was working on the a"
				+ "ssumption that ...";

		Sweble2Parser swebleParser = new Sweble2Parser("101997",
				"User talk:Tim Starling", wikitext, "en", new WikiStatistics(),
				new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();

		Document doc = builder.build(wikiXML, null);
		Node figure = doc.query("/dl/dd/dl/dd/dl/dd/figure").get(0);
		assertEquals("emoji", figure.query("@type").get(0).getValue());
		assertEquals("template", figure.query("@creation").get(0).getValue());

		Node desc = figure.query("desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{(:}}_]", desc.getValue());
	}

	@Test
	public void testTemplateSmiley2()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "* Now I feel bad about this... I had no clue you "
				+ "were an admin! {{Smiley2|doh}}";

		Sweble2Parser swebleParser = new Sweble2Parser("1586091",
				"User talk:Rogerd", wikitext, "en", new WikiStatistics(),
				new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);

		Node desc = doc.query("/ul/li/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{Smiley2|doh}}_]", desc.getValue());
	}

	@Test
	public void testTemplateSymbol2()
			throws ValidityException, ParsingException, IOException {
		String wikitext = ":{{ping|JackofOz}} It is now.[https://en.wikipedia"
				+ ".org/w/index.php?title=Ron_Clarke&amp;diff=667291416&amp;"
				+ "oldid=667285447] Albeit a bit late. {{=2|facepalm}} "
				+ "[[user:220 of Borg|'''220''']] [[Special:Contributions/220 "
				+ "of Borg|''&lt;small&gt;of&lt;/small&gt;'']] &lt;sup&gt;"
				+ "[[User talk:220 of Borg|''Borg'']]&lt;/sup&gt; 05:55, 17 "
				+ "June 2015 (UTC)";

		Sweble2Parser swebleParser = new Sweble2Parser("2436332",
				"Talk:Ron Clarke", wikitext, "en", new WikiStatistics(),
				new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/dl/dd/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{=2|facepalm}}_]", desc.getValue());
	}

	@Test
	public void testTemplateSmiley3()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "Yes, I meant it as a completment only! {{smiley3|nice}}. ";

		Sweble2Parser swebleParser = new Sweble2Parser("1038222",
				"User talk:Chealer", wikitext, "en", new WikiStatistics(),
				new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/p/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{smiley3|nice}}_]", desc.getValue());
	}

	@Test
	public void testOldTemplate()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "Thank you {{Oldsmiley|10}} [[User:Psiĥedelisto|"
				+ "Psiĥedelisto]] ([[User talk:Psiĥedelisto|talk]]) 04:41, "
				+ "17 January 2017 (UTC)";

		Sweble2Parser swebleParser = new Sweble2Parser("28121",
				"Talk:Smiley", wikitext, "en", new WikiStatistics(),
				new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/p/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{Oldsmiley|10}}_]", desc.getValue());
	}

	@Test
	public void testTemplateSert()
			throws ValidityException, ParsingException, IOException {
		String wikitext = ":Why, thank you too, {{u|Skyes(BYU)|Skyes}}! "
				+ "You've been working very hard as well! {{sert|smiley}}"
				+ "--&lt;span style=&quot;font-family:Bradley Hand ITC"
				+ "&quot;&gt;[[User:Farang Rak Tham|&lt;span style=&quot;"
				+ "color:blue;font-weight:900&quot;&gt;Farang Rak Tham"
				+ "&lt;/span&gt;]]  [[User talk:Farang Rak Tham|(Talk)]]"
				+ "&lt;/span&gt; 07:31, 30 June 2018 (UTC)";

		Sweble2Parser swebleParser = new Sweble2Parser("173025",
				"Talk:Temperance movement", wikitext, "en",
				new WikiStatistics(), new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/dl/dd/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{sert|smiley}}_]", desc.getValue());
	}

	@Test
	public void testTemplateEmoji()
			throws ValidityException, ParsingException, IOException {
		String wikitext = ":: Love the new image! {{emoji|2764|theme=noto}}"
				+ "{{emoji|2640|theme=noto}} [[User:Qzekrom|Qzekrom]] "
				+ "([[User talk:Qzekrom|talk]]) 17:47, 19 February 2019 (UTC)";

		Sweble2Parser swebleParser = new Sweble2Parser("45161",
				"Talk:Woman", wikitext, "en", new WikiStatistics(),
				new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/dl/dd/dl/dd/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{emoji|2764|theme=noto}}_]", desc.getValue());
	}

	@Test
	public void testTemplateEmote()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "I haven't tried every which way to try to hack "
				+ "unauthorized HTML tags through Lua yet. {{emote|horns}} "
				+ "[[User:Wnt|Wnt]] ([[User talk:Wnt|talk]]) 23:40, 20 "
				+ "January 2014 (UTC)";

		Sweble2Parser swebleParser = new Sweble2Parser("40734823",
				"Wikipedia:Lua/Requests/Archive_3", wikitext, "en",
				new WikiStatistics(), new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/p/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{emote|horns}}_]", desc.getValue());
	}

	@Test
	public void testTemplateEmoteWithUnicode()
			throws ValidityException, ParsingException, IOException {
		String wikitext = "::You're welcome. {{emote|U+1F601}} "
				+ "[[User:Jd02022092|jd22292]] &lt;span style=&quot;"
				+ "background-color:#368ec9; color:#6babd6&quot;&gt;"
				+ "(Jalen D. Folf)&lt;/span&gt; 17:42, 2 July 2017 (UTC)";

		Sweble2Parser swebleParser = new Sweble2Parser("28121",
				"Wikipedia:Lua/Requests/Archive 3", wikitext, "en",
				new WikiStatistics(), new WikiErrorWriter(), enWikiConfig);

		swebleParser.run();
		String wikiXML = swebleParser.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Node desc = doc.query("/dl/dd/dl/dd/figure/desc").get(0);
		assertEquals("template", desc.query("@type").get(0).getValue());
		assertEquals("[_EMOJI:{{emote|U+1F601}}_]", desc.getValue());

	}
}
