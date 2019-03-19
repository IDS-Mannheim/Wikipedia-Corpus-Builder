package de.mannheim.ids.wiki;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

	private int namespaceKey;
	private int maxThreads;

	private boolean isDiscussion;

	public static final String[] indexes = {"A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9"};

	public Configuration() {
		System.setProperty("entityExpansionLimit", "0");
		System.setProperty("totalEntitySizeLimit", "0");
		System.setProperty("PARAMETER_ENTITY_SIZE_LIMIT", "0");
		creator = "N/A";
	}

	public Configuration(String xmlFolder, int namespaceKey, String pageType,
			String dumpFilename, String language, String korpusSigle,
			String inflectives, String encoding, String outputFile,
			String index, String url, String username, String password,
			int maxThreads, String creator) {
		this();

		setCreator(creator);
		setNamespaceKey(namespaceKey);
		setPageType(pageType);
		setDiscussion(namespaceKey);

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
	}

	public Configuration(String propertiesFilename) throws IOException {
		InputStream is = Configuration.class.getClassLoader()
				.getResourceAsStream(propertiesFilename);

		Properties config = new Properties();
		config.load(is);

		int namespaceKey = Integer
				.parseInt(config.getProperty("namespace_key", "1"));
		setNamespaceKey(namespaceKey);
		setDiscussion(namespaceKey);

		setCreator(config.getProperty("creator"));
		setPageType(config.getProperty("page_type"));
		setDumpFilename(config.getProperty("wikidump"));
		setLanguage(config.getProperty("language"));
		setLanguageCode();
		setYear();
		setKorpusSigle(config.getProperty("korpusSigle"));

		setWikiXMLFolder(config.getProperty("wikixml_folder"));
		setWikiXMLIndex(config.getProperty("wikixml_index"));
		setOutputFile(config.getProperty("output_file"));
		setOutputEncoding(config.getProperty("output_encoding"));
		setInflectives(config.getProperty("inflective_file"));
		setDatabaseUrl(config.getProperty("db_url"));
		setDatabaseUsername(config.getProperty("db_username"));
		setDatabasePassword(config.getProperty("db_password"));

		setMaxThreads(Integer.parseInt(config.getProperty("max_threads", "1")));

		is.close();
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
		if (databaseUrl == null || databaseUrl.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the Wikipedia language link "
							+ "Mysql Database URL.");
		}
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		if (databaseUsername == null || databaseUsername.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the username of the Wikipedia "
							+ "language link Mysql Database.");
		}
		this.databaseUsername = databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		if (databasePassword == null || databasePassword.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the password of  Wikipedia "
							+ "language link Mysql Database.");
		}
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
		if (korpusSigle == null || korpusSigle.isEmpty()) {
			throw new IllegalArgumentException(
					"Please specify the korpusSigle.");
		}
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
}
