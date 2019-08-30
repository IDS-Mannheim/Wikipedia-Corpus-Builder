package de.mannheim.ids.wiki;

import java.io.File;
import java.util.Properties;

/**
 * Sets a configuration for the whole conversion process.
 * 
 * @author margaretha
 *
 */
public class Configuration {

	private String wikiXMLFolder;
	private String pageType;
	private String dumpFilename;
	private String language;
	private String languageCode;
	private String year;

	private String outputFile;
	private String encoding;
	private String inflectives;
	private String wikiXMLIndex;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
	private String creator;

	private String korpusSigle;
	private String category;
	private String categoryScheme;

	private int namespaceKey;
	private int maxThreads;

	private boolean isDiscussion;
	private boolean storeCategories;

	public static final String[] indexes = {"A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9"};

	public Configuration(String xmlFolder, int namespaceKey, String pageType,
			String dumpFilename, String language, String korpusSigle,
			String inflectives, String encoding, String outputFile,
			String index, String url, String username, String password,
			int maxThreads, String creator, String category,
			String categoryScheme) {
		setDiscussion(namespaceKey);
		init(namespaceKey, xmlFolder, pageType, dumpFilename, language,
				korpusSigle, outputFile, index, creator, url, username,
				password, encoding, inflectives, maxThreads, category,
				categoryScheme);
	}

	public Configuration(Properties properties) {
		String namespace = loadRequiredParameter(properties, "namespace_key");
		int namespaceKey = Integer.parseInt(namespace);
		setDiscussion(namespaceKey);

		category = loadRequiredParameter(properties, "category");
		categoryScheme = loadRequiredParameter(properties,
				"category_scheme");

		init(namespaceKey,
				loadRequiredParameter(properties, "wikixml_folder"),
				loadRequiredParameter(properties, "page_type"),
				loadRequiredParameter(properties, "wikidump"),
				loadRequiredParameter(properties, "language"),
				loadRequiredParameter(properties, "korpusSigle"),
				loadRequiredParameter(properties, "output_file"),
				loadRequiredParameter(properties, "wikixml_index"),
				loadRequiredParameter(properties, "creator"),
				loadRequiredParameter(properties, "db_url"),
				loadRequiredParameter(properties, "db_username"),
				loadRequiredParameter(properties, "db_password"),
				properties.getProperty("output_encoding"),
				properties.getProperty("inflective_file"),
				Integer.parseInt(properties.getProperty("max_threads", "1")),
				category,
				categoryScheme);
	}

	private void init(int namespaceKey, String xmlFolder, String pageType,
			String dumpFilename, String language, String korpusSigle,
			String outputFile, String index, String creator, String url,
			String username, String password, String encoding,
			String inflectives, int maxThreads, String category,
			String categoryScheme) {

		System.setProperty("entityExpansionLimit", "0");
		System.setProperty("totalEntitySizeLimit", "0");
		System.setProperty("PARAMETER_ENTITY_SIZE_LIMIT", "0");

		setNamespaceKey(namespaceKey);
		setCreator(creator);
		setPageType(pageType);

		setDumpFilename(dumpFilename);
		setLanguage(language);
		setLanguageCode();
		setYear();
		setKorpusSigle(korpusSigle);

		setWikiXMLFolder(xmlFolder);
		setWikiXMLIndex(index);
		setOutputFile(outputFile);
		setOutputEncoding(encoding);
		setInflectives(inflectives);
		setDatabaseUrl(url);
		setDatabaseUsername(username);
		setDatabasePassword(password);

		setMaxThreads(maxThreads);
		setCategory(category);
		setCategoryScheme(categoryScheme);
	}

	public String loadRequiredParameter(Properties config, String param) {
		String value = config.getProperty(param);

		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(param + " is required.");
		}
		return value;
	}

	public String getWikiXMLFolder() {
		return wikiXMLFolder;
	}

	public void setWikiXMLFolder(String wikiXMLFolder) {
		if (wikiXMLFolder == null || wikiXMLFolder.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the WikiXML folder (e.g wikixml-de/articles).");
		}
		this.wikiXMLFolder = wikiXMLFolder;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String type) {
		this.pageType = type;
	}

	public String getDumpFilename() {
		return dumpFilename;
	}

	public void setDumpFilename(String dumpFilename) {
		if (dumpFilename == null || dumpFilename.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the Wiki dump file.");
		}
		File f = new File(dumpFilename);
		this.dumpFilename = f.getName();
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		if (outputFile == null || outputFile.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the output file.");
		}
		this.outputFile = outputFile;
	}

	public String getOutputEncoding() {
		return encoding;
	}

	public void setOutputEncoding(String encoding) {
		if (encoding == null || encoding.isEmpty()) {
			encoding = "UTF-8"; // default encoding
		}
		this.encoding = encoding.toUpperCase();
	}

	public String getInflectives() {
		return inflectives;
	}

	public void setInflectives(String inflectives) {
		if (inflectives == null || inflectives.isEmpty()) {
			this.inflectives = null;
		}
		else {
			this.inflectives = inflectives;
		}
	}

	public String getWikiXMLIndex() {
		return wikiXMLIndex;
	}

	public void setWikiXMLIndex(String wikiXMLIndex) {
		if (wikiXMLIndex == null || wikiXMLIndex.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the index of the Wikipage " + this.pageType
							+ ".");
		}
		this.wikiXMLIndex = wikiXMLIndex;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public int getNamespaceKey() {
		return namespaceKey;
	}

	public void setNamespaceKey(int namespaceKey) {
		this.namespaceKey = namespaceKey;
	}

	public boolean isDiscussion() {
		return isDiscussion;
	}

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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode() {
		this.languageCode = dumpFilename.substring(0, 2);
	}

	public String getYear() {
		return year;
	}

	public void setYear() {
		this.year = dumpFilename.substring(7, 11);
	}

	public String getKorpusSigle() {
		return korpusSigle;
	}

	public void setKorpusSigle(String korpusSigle) {
		this.korpusSigle = korpusSigle;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCategoryScheme() {
		return categoryScheme;
	}

	public void setCategoryScheme(String categoryScheme) {
		this.categoryScheme = categoryScheme;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean storeCategories() {
		return storeCategories;
	}

	public void setStoreCategories(boolean storeCategories) {
		this.storeCategories = storeCategories;
	}
}
