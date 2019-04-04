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
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class SignatureTest extends GermanTestBase {

	@Test
	public void testSignatureInTemplate()
			throws IOException, ValidityException, ParsingException {
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle("Diskussion:Hauspferd");
		wikiPage.setPageId("12765");
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		wikiPage.textSegments.add("{{Diskussion aufgeräumt|24. Februar 2014|2="
				+ "[https://de.wikipedia.org/w/index.php?title=Diskussion:"
				+ "Hauspferd&amp;oldid=124833777 Version vom 25. November 2013"
				+ "]|3=[[Benutzer:Fallen Sheep|Fallen Sheep]] ([[Benutzer "
				+ "Diskussion:Fallen Sheep|Diskussion]]) 00:17, 24. Feb. 2014 "
				+ "(CET)}}{{Autoarchiv |Alter=180 |Ziel='((Lemma))/Archiv/1'"
				+ "|Übersicht=[[Diskussion:Hauspferd/Archiv/1|Archiv]]|"
				+ "Mindestbeiträge=1 |Mindestabschnitte =3 |Frequenz="
				+ "monatlich}}");

		int namespaceKey = 2; // diskussion
		Configuration config = new Configuration(wikidump, language, userPage,
				userContribution, helpSignature, unsigned, namespaceKey, "talk",
				null, null, 0, generateWikitext);

		WikiPostUser postUser = new WikiPostUser("test", "talk");
		WikiPostTime postTime = new WikiPostTime("test", "talk");

		WikiTalkHandler handler = new WikiTalkHandler(config, wikiPage,
				new WikiStatistics(),
				new WikiErrorWriter(), postUser, postTime);

		handler.run();

		String wikiXML = wikiPage.getWikiXML();
		Document doc = builder.build(wikiXML, null);
		Nodes spans = doc.query("/posting/p/span[@class='template']");
		assertEquals(2,spans.size());
	}
}
