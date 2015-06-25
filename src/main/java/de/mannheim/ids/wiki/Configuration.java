package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

public class Configuration {

	private String wikidump;
	private String languageCode;
	private String userPage;
	private String talkPage;
	private String userContribution;
	private String outputFolder;
	private String outputEncoding;
	private String pageType;
	private int namespaceKey;
	private int maxThreads;

	private boolean isDiscussion;

	private static final Map<Integer, String> namespaceMap;
	static {
		namespaceMap = new HashMap<Integer, String>();
		namespaceMap.put(0, "article");
		namespaceMap.put(1, "talk");
		namespaceMap.put(2, "user");
		namespaceMap.put(3, "user-talk");
		namespaceMap.put(4, "wikipedia");
		namespaceMap.put(5, "wikipedia-talk");
		namespaceMap.put(6, "file");
		namespaceMap.put(7, "file-talk");
		namespaceMap.put(8, "mediawiki");
		namespaceMap.put(9, "mediawiki-talk");
		namespaceMap.put(10, "template");
		namespaceMap.put(11, "template talk");
		namespaceMap.put(12, "help");
		namespaceMap.put(13, "help-talk");
		namespaceMap.put(14, "category");
		namespaceMap.put(15, "category-talk");
	}

	public Configuration(String wikidump, String language, String userPage,
			String talkPage, String userContribution, int namespaceKey,
			String encoding, int maxThread) {

		setWikidump(wikidump);
		setLanguageCode(language);
		setUserPage(userPage);
		setTalkPage(talkPage);
		setUserContribution(userContribution);
		setNamespaceKey(namespaceKey);
		setDiscussion(namespaceKey);
		setPageType(namespaceMap.get(namespaceKey));
		setOutputFolder("wikixml-" + languageCode + "/" + pageType);
		setOutputEncoding(encoding);
		setMaxThreads(maxThread);
	}

	public Configuration(CommandLine cmd) {
		setWikidump(cmd.getOptionValue("w"));
		setLanguageCode(cmd.getOptionValue("l"));
		setUserPage(cmd.getOptionValue("up"));
		setTalkPage(cmd.getOptionValue("tp"));
		setUserContribution(cmd.getOptionValue("uc"));

		int namespaceKey = Integer.parseInt(cmd.getOptionValue("k"));
		int maxThread = Integer.parseInt(cmd.getOptionValue("x"));

		setNamespaceKey(namespaceKey);
		setDiscussion(namespaceKey);
		setPageType(namespaceMap.get(namespaceKey));
		setOutputFolder("wikixml-" + languageCode + "/" + pageType);
		setOutputEncoding(cmd.getOptionValue("e"));
		setMaxThreads(maxThread);
	}

	public Configuration(String properties) throws IOException {
		InputStream is = Configuration.class.getClassLoader()
				.getResourceAsStream(properties);

		Properties config = new Properties();
		config.load(is);

		setWikidump(config.getProperty("wikidump"));
		setLanguageCode(config.getProperty("language_code"));
		setUserPage(config.getProperty("user_page"));
		setTalkPage(config.getProperty("talk_page"));
		setUserContribution(config.getProperty("user_contribution"));

		int namespaceKey = Integer
				.parseInt(config.getProperty("namespace_key"));
		setDiscussion(namespaceKey);
		setNamespaceKey(namespaceKey);
		setPageType(namespaceMap.get(namespaceKey));
		setOutputFolder("wikixml-" + languageCode + "/" + pageType);
		setOutputEncoding(config.getProperty("output_encoding"));
		setMaxThreads(Integer.parseInt(config.getProperty("max_threads")));

		is.close();
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		if (languageCode == null || languageCode.isEmpty()) {
			throw new IllegalArgumentException("Please specify the 2-letter "
					+ "Wikipedia language code.");
		}
		this.languageCode = languageCode;
	}

	public String getWikidump() {
		return wikidump;
	}

	public void setWikidump(String wikidump) {
		if (wikidump == null || wikidump.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the Wikidump file.");
		}
		this.wikidump = wikidump;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setOutputEncoding(String encoding) {
		if (encoding == null || encoding.isEmpty()) {
			encoding = "utf-8";
		}
		this.outputEncoding = encoding;
	}

	public String getUserPage() {
		return userPage;
	}

	public void setUserPage(String userPage) {
		if (isDiscussion && (userPage == null || userPage.isEmpty())) {
			throw new IllegalArgumentException("Please specify the user page "
					+ "in the language of the Wikipedia dump.");
		}
		this.userPage = userPage;
	}

	public String getTalkPage() {
		return talkPage;
	}

	public void setTalkPage(String talkPage) {
		if (isDiscussion && (talkPage == null || talkPage.isEmpty())) {
			throw new IllegalArgumentException("Please specify the talk page "
					+ "in the language of the Wikipedia dump.");
		}
		this.talkPage = talkPage;
	}

	public String getUserContribution() {
		return userContribution;
	}

	public void setUserContribution(String userContribution) {
		if (isDiscussion
				&& (userContribution == null || userContribution.isEmpty())) {
			throw new IllegalArgumentException(
					"Please specify the user "
							+ "contribution page in the language of the Wikipedia dump.");
		}
		this.userContribution = userContribution;
	}

	public int getNamespaceKey() {
		return namespaceKey;
	}

	public void setNamespaceKey(int namespaceKey) {
		this.namespaceKey = namespaceKey;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public boolean isDiscussion() {
		return isDiscussion;
	}

	public void setDiscussion(int namespaceKey) {
		if ((namespaceKey & 1) == 0) {
			isDiscussion = false;
		}
		else
			isDiscussion = true;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

}
