package de.mannheim.ids.wiki;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Setting all the configurations for the conversion process.
 * 
 * @author margaretha
 * 
 */
public class Configuration {

	private String wikidump;
	private String languageCode;
	private String userPage;
	private String userContribution;
	private String unsigned;
	private String signature;
	private String outputFolder;
	private String outputEncoding;
	private String wikitextFolder;
	private String pageType;
	private String titlePrefix = "";
	private int namespaceKey;
	private int maxThreads;
	private List<String> excludedPages = new ArrayList<String>();

	private boolean isDiscussion;
	private boolean wikitextToGenerate = false;

	// Wikipedia namespaces
	// public static final Map<Integer, String> namespaceMap;
	// static {
	// namespaceMap = new HashMap<Integer, String>();
	// namespaceMap.put(0, "article");
	// namespaceMap.put(1, "talk");
	// namespaceMap.put(2, "user");
	// namespaceMap.put(3, "user-talk");
	// namespaceMap.put(4, "wikipedia");
	// namespaceMap.put(5, "wikipedia-talk");
	// namespaceMap.put(6, "file");
	// namespaceMap.put(7, "file-talk");
	// namespaceMap.put(8, "mediawiki");
	// namespaceMap.put(9, "mediawiki-talk");
	// namespaceMap.put(10, "template");
	// namespaceMap.put(11, "template-talk");
	// namespaceMap.put(12, "help");
	// namespaceMap.put(13, "help-talk");
	// namespaceMap.put(14, "category");
	// namespaceMap.put(15, "category-talk");
	// }

	/**
	 * Constructs a conversion configuration from all the given variables.
	 * 
	 * @param wikidump
	 *            the wikidump filename
	 * @param language
	 *            the 2-letter language code of the wikidump
	 * @param userPage
	 *            the user page prefix in the Wikidump language, e.g. User in
	 *            English, Benutzer in German
	 * @param userContribution
	 *            the user contribution page prefix in the Wikidump language,
	 *            e.g. Special:Contributions in English, Spezial:Beiträge in
	 *            German
	 * @param helpSignature
	 *            the signature page title in the Wikidump language, e.g.
	 *            Wikipedia:Signatures in English, Hilfe:Signatur in German.
	 *            See: https://en.wikipedia.org/wiki/Wikipedia:Signatures
	 * @param unsigned
	 *            the unsigned template keyword in the Wikidump language, e.g.
	 *            unsigned in English, unsigniert in German, non signé in French
	 * @param namespaceKey
	 *            the namespace key of the Wikipedia pages to convert, e.g 0 for
	 *            articles, 1 for talk pages, 3 for user talk pages
	 * @param pageType
	 *            the type of the wiki pages, e.g. article
	 * 
	 * @param titlePrefix
	 *            the prefix that the page titles start with
	 * @param encoding
	 *            the output encoding, by default is utf-8
	 * @param maxThread
	 *            the number of maximal threads allowed to run concurrently
	 * @param generateWikitext
	 *            a boolean signifies whether a wikitext file should be
	 *            generated for each wikipage
	 */
	public Configuration(String wikidump, String language, String userPage,
			String userContribution, String helpSignature, String unsigned,
			int namespaceKey, String pageType, String titlePrefix,
			String encoding, int maxThread, boolean generateWikitext) {

		setNamespaceKey(namespaceKey);
		setDiscussion(namespaceKey);
		setWikidump(wikidump);
		setLanguageCode(language);
		setUserPage(userPage);
		setUserContribution(userContribution);
		setUnsigned(unsigned);
		setSignature(helpSignature);
		setPageType(pageType);
		setTitlePrefix(titlePrefix);
		setOutputFolder("wikixml-" + languageCode + "/" + pageType);
		if (encoding == null || encoding.isEmpty()) {
			encoding = "utf-8";
		}
		setOutputEncoding(encoding);
		setMaxThreads(maxThread);
		setWikitextFolder("wikitext-" + languageCode + "/" + pageType);
		setWikitextToGenerate(generateWikitext);
	}

	/**
	 * Constructs a conversion configuration from a properties file.
	 * 
	 * @param properties
	 *            the location of the properties file
	 */
	public Configuration(Properties properties) {

		String namespace = loadRequiredParameter(properties, "namespace_key");
		int namespaceKey = Integer.parseInt(namespace);
		setDiscussion(namespaceKey);
		setNamespaceKey(namespaceKey);

		setWikidump(loadRequiredParameter(properties, "wikidump"));
		setLanguageCode(loadRequiredParameter(properties, "language_code"));
		setPageType(loadRequiredParameter(properties, "page_type"));
		setOutputFolder("wikixml-" + languageCode + "/" + pageType);

		if (isDiscussion) {
			setUserPage(loadRequiredParameter(properties, "user_page"));
			setUserContribution(
					loadRequiredParameter(properties, "user_contribution"));
			setUnsigned(loadRequiredParameter(properties, "unsigned"));
			setSignature(loadRequiredParameter(properties, "signature"));
		}

		// optional
		setOutputEncoding(properties.getProperty("output_encoding", "utf-8"));
		setMaxThreads(Integer.parseInt(properties.getProperty("max_threads", "1")));
		setTitlePrefix(properties.getProperty("title_prefix", ""));
		setWikitextToGenerate(Boolean
				.valueOf(properties.getProperty("generate_wikipage", "false")));
		if (isWikitextToGenerate()){
			setWikitextFolder("wikitext-" + languageCode + "/" + pageType);
		}
		setExcludedPages(properties.getProperty("exclude_page_id", ""));
	}

	public String loadRequiredParameter(Properties config, String param) {
		String value = config.getProperty(param);

		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(param + " is required.");
		}
		return value;
	}

	/**
	 * Gets the language code of the wikidump.
	 * 
	 * @return the wikidump language code
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * Sets the 2-letter language code of the wikidump.
	 * 
	 * @param languageCode
	 *            2-letter language code of the wikidump
	 */
	public void setLanguageCode(String languageCode) {
		if (languageCode == null || languageCode.isEmpty()) {
			throw new IllegalArgumentException("Please specify the 2-letter "
					+ "Wikipedia language code.");
		}
		this.languageCode = languageCode;
	}

	/**
	 * Gets the wikidump filename.
	 * 
	 * @return the wikidump filename
	 */
	public String getWikidump() {
		return wikidump;
	}

	/**
	 * Sets the wikidump filename.
	 * 
	 * @param wikidump
	 *            the wikidump filename.
	 */
	public void setWikidump(String wikidump) {
		if (wikidump == null || wikidump.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the Wikidump file.");
		}
		this.wikidump = wikidump;
	}

	/**
	 * Gets the output file encoding.
	 * 
	 * @return the output file encoding
	 */
	public String getOutputEncoding() {
		return outputEncoding;
	}

	/**
	 * Sets the output file encoding, by default is utf-8.
	 * 
	 * @param encoding
	 *            the output file encoding
	 */
	public void setOutputEncoding(String encoding) {
		this.outputEncoding = encoding;
	}

	/**
	 * Gets the user page prefix.
	 * 
	 * @return the user page prefix
	 */
	public String getUserPage() {
		return userPage;
	}

	/**
	 * Sets the user page prefix.
	 * 
	 * @param userPage
	 *            the user page prefix
	 */
	public void setUserPage(String userPage) {
		if (isDiscussion && (userPage == null || userPage.isEmpty())) {
			throw new IllegalArgumentException("Please specify the user page "
					+ "in the language of the Wikipedia dump.");
		}
		this.userPage = userPage;
	}

	/**
	 * Gets the user contribution prefix.
	 * 
	 * @return the user contribution prefix
	 */
	public String getUserContribution() {
		return userContribution;
	}

	/**
	 * Sets the user contribution prefix.
	 * 
	 * @param userContribution
	 *            the user contribution prefix
	 */
	public void setUserContribution(String userContribution) {
		if (isDiscussion
				&& (userContribution == null || userContribution.isEmpty())) {
			throw new IllegalArgumentException("Please specify the user "
					+ "contribution page in the language of the Wikipedia dump.");
		}

		this.userContribution = userContribution;
	}

	/**
	 * Gets the signature page title (link keyword in wiki markup).
	 * 
	 * @return the signature page
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets the signature page title (link keyword in wiki markup).
	 * 
	 * @param signature
	 *            the signature page title
	 */
	public void setSignature(String signature) {
		if (isDiscussion && (signature == null || signature.isEmpty())) {
			throw new IllegalArgumentException(
					"Please specify the signature page in the language of the Wikipedia dump.");
		}
		this.signature = signature;
	}

	/**
	 * Gets the title prefix of the wikipages to convert.
	 * 
	 * @return the titlePrefix
	 */
	public String getTitlePrefix() {
		return titlePrefix;
	}

	/**
	 * Sets the title prefix of the wikipages to convert.
	 * 
	 * @param titlePrefix
	 *            the titlePrefix to set
	 */
	public void setTitlePrefix(String titlePrefix) {
		if (titlePrefix != null && !titlePrefix.isEmpty()) {
			this.titlePrefix = Normalizer.normalize(titlePrefix, Form.NFKC);
		}
	}

	/**
	 * Gets the namespacekey (nummer) of the wikipages to convert.
	 * 
	 * @return the namespacekey
	 */
	public int getNamespaceKey() {
		return namespaceKey;
	}

	/**
	 * Sets the namespace key (number) of the wikipages to convert.
	 * 
	 * @param namespaceKey
	 *            the namespace key (number) of the wikipages to convert.
	 */
	public void setNamespaceKey(int namespaceKey) {
		this.namespaceKey = namespaceKey;
	}

	/**
	 * Gets the root output folder path where the wikiXML files should be
	 * placed.
	 * 
	 * @return the output folder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * Sets the root output folder path where the wikiXML files should be
	 * placed.
	 * 
	 * @param outputFolder
	 *            the root output folder path where the wikiXML files should be
	 *            placed
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * Determines if the wikipages to convert is of a discussion type or not.
	 * 
	 * @return true or false
	 */
	public boolean isDiscussion() {
		return isDiscussion;
	}

	/**
	 * Sets if wikipages to convert is of a discussion type or not.
	 * 
	 * @param namespaceKey
	 *            the namespace key (number) of the wikipages to convert
	 */
	public void setDiscussion(int namespaceKey) {
		// The pages in Wikipedia namespace e.g. with title prefixes
		// Wikipedia:Redundanz and Wikipedia:Löschkandidaten are
		// discussions.
		if (namespaceKey == 4)
			this.isDiscussion = true;
		else {
			this.isDiscussion = (namespaceKey % 2) == 0 ? false : true;
		}
	}

	/**
	 * Gets the number of maximum threads allowed to run concurrently.
	 * 
	 * @return the number of maximum threads
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Sets the number of maximum threads allowed to run concurrently.
	 * 
	 * @param maxThreads
	 *            the number of maximum threads allowed to run concurrently.
	 */
	public void setMaxThreads(int maxThreads) {
		if (maxThreads > 0) {
			this.maxThreads = maxThreads;
		}
		else
			this.maxThreads = 1;
	}

	/**
	 * Gets the wikipage type
	 * 
	 * @return the wikipage type
	 */
	public String getPageType() {
		return pageType;
	}

	/**
	 * Sets the wikipage type
	 * 
	 * @param pageType
	 *            the wikipage type
	 */
	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	/**
	 * Gets the wikitext folder where the generated wikitext files are to be
	 * placed.
	 * 
	 * @return the output folder containing generated wikitext files
	 */
	public String getWikitextFolder() {
		return wikitextFolder;
	}

	/**
	 * Sets the wikitext folder where the generated wikitext files are to be
	 * placed.
	 * 
	 * @param wikitextFolder
	 *            the wikitext folder where the generated wikitext files are to
	 *            be placed.
	 */
	public void setWikitextFolder(String wikitextFolder) {
		this.wikitextFolder = wikitextFolder;
	}

	/**
	 * Determines if the wikitext files are to be generated.
	 * 
	 * @return true if the wikitext files are to be generated, false otherwise.
	 */
	public boolean isWikitextToGenerate() {
		return wikitextToGenerate;
	}

	/**
	 * Sets if the wikitext files are to be generated.
	 * 
	 * @param wikitextToGenerate
	 *            true if the wikitext files are to be generated, false
	 *            otherwise.
	 */
	public void setWikitextToGenerate(boolean wikitextToGenerate) {
		this.wikitextToGenerate = wikitextToGenerate;
	}

	/**
	 * Gets the keyword for the unsigned template
	 * 
	 * @return the keyword for the unsigned template
	 */
	public String getUnsigned() {
		return unsigned;
	}

	/**
	 * Sets the keyword for the unsigned template.
	 * 
	 * @param unsigned
	 *            the keyword for the unsigned template
	 */
	public void setUnsigned(String unsigned) {
		if (isDiscussion && (unsigned == null || unsigned.isEmpty())) {
			throw new IllegalArgumentException(
					"Please specify the unsigned template in the language of the Wikipedia dump.");
		}
		this.unsigned = unsigned;
	}

	public List<String> getExcludedPages() {
		return excludedPages;
	}

	public void setExcludedPages(String str) {
		String[] ids = str.split(",");
		for (String id : ids) {
			excludedPages.add(id.trim());
		}
	}
}
