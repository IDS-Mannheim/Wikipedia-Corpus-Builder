package de.mannheim.ids.base;

import de.mannheim.ids.wiki.Configuration;
import nu.xom.Builder;

public abstract class GermanTestBase {

	protected Builder builder;
	protected String language = "de";
	protected String wikidump = "data/dewiki-20170701-sample.xml";
	protected String userPage = "Benutzer";
	protected String userContribution = "Spezial:Beiträge";
	protected String helpSignature = "Hilfe:Signatur";
	protected String unsigned = "unsigniert";
	protected int maxThread = 1;
	protected boolean generateWikitext = false;

	public GermanTestBase() {
		builder = new Builder();
	}

	protected Configuration createConfig(String wikidump, int namespace,
			String pageType) {

		Configuration config = new Configuration(wikidump, language, userPage,
				userContribution, helpSignature, unsigned, namespace, pageType,
				null, null, maxThread, generateWikitext);

		return config;
	}
}
