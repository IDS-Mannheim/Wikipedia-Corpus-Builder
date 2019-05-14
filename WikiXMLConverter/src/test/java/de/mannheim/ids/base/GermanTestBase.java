package de.mannheim.ids.base;

import java.io.IOException;

import de.mannheim.ids.config.Configuration;
import de.mannheim.ids.wiki.page.WikiPage;
import nu.xom.Builder;

public abstract class GermanTestBase {

	protected Builder builder;
	protected String language = "de";
	protected String wikidump = "data/dewiki-20170701-sample.xml";
	protected String userPage = "Benutzer";
	protected String userTalk = "Benutzer Diskussion";
	protected String userContribution = "Spezial:Beiträge";
	protected String helpSignature = "Hilfe:Signatur";
	protected String unsigned = "Unsigniert";
	protected int maxThread = 1;
	protected boolean generateWikitext = false;

	protected static Configuration talkConfig, userTalkConfig;

	public GermanTestBase() {
		builder = new Builder();
		talkConfig = createTalkConfig(wikidump);
		userTalkConfig = createUserTalkConfig(wikidump);
	}

	protected Configuration createTalkConfig(String wikidump) {
		return createConfig(wikidump, 1, "talk");
	}

	protected Configuration createUserTalkConfig(String wikidump) {
		return createConfig(wikidump, 3, "user-talk");
	}

	private Configuration createConfig(String wikidump, int namespace,
			String pageType) {

		Configuration config = new Configuration(wikidump, language, userPage,
				userTalk, userContribution, helpSignature, unsigned, namespace,
				pageType, null, null, maxThread, generateWikitext);

		return config;
	}

	protected WikiPage createWikiPage(String pageTitle, String pageId,
			String... wikitext) throws IOException {
		WikiPage wikiPage = new WikiPage();
		wikiPage.setPageTitle(pageTitle);
		wikiPage.setPageId(pageId);
		wikiPage.setPageIndex(true);
		wikiPage.setPageStructure("<page><text></text></page>");
		for (String text : wikitext) {
			wikiPage.textSegments.add(text);
		}
		return wikiPage;
	}

	protected String wrapWithTextElement(String wikiXML) {
		return "<text>" + wikiXML + "</text>";

	}
}
