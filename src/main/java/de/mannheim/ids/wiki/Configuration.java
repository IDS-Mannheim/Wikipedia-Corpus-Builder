package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

public class Configuration {

	private String wikiXMLFolder;
	private String pageType;
	private String dumpFilename;
	private String outputFile;
	private String encoding;
	private String inflectives;
	private String wikiXMLIndex;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
		
	public Configuration() {
		System.setProperty("entityExpansionLimit", "0");
		System.setProperty("totalEntitySizeLimit", "0");
		System.setProperty("PARAMETER_ENTITY_SIZE_LIMIT", "0");
	}
	
	public Configuration(String xmlFolder, String type, String dumpFilename, 
			String inflectives, String encoding, String outputFile, String index, 
			String url, String username, String password){
		
		this.setDumpFilename(dumpFilename);
		this.setPageType(type);
		this.setWikiXMLFolder(xmlFolder);
		this.setWikiXMLIndex(index);
		this.setOutputFile(outputFile);
		this.setEncoding(encoding);
		this.setInflectives(inflectives);
		this.setDatabaseUrl(url);
		this.setDatabaseUsername(username);
		this.setDatabasePassword(password);
		
		System.setProperty("entityExpansionLimit", "0");
		System.setProperty("totalEntitySizeLimit", "0");
		System.setProperty("PARAMETER_ENTITY_SIZE_LIMIT", "0");
	}
	
	public void setConfigFromProperties(String propertiesFilename) 
			throws I5Exception {
		
		InputStream is = Configuration.class.getClassLoader().
				getResourceAsStream(propertiesFilename);		
		Properties config = new Properties();
		try {
			config.load(is);
			this.setDumpFilename(config.getProperty("wikidump"));
			this.setPageType(config.getProperty("page_type"));
			this.setWikiXMLFolder(config.getProperty("wikixml_folder"));
			this.setWikiXMLIndex(config.getProperty("wikixml_index"));
			this.setOutputFile(config.getProperty("output_file"));			
			this.setEncoding(config.getProperty("output_encoding"));
			this.setInflectives(config.getProperty("inflective_file"));
			this.setDatabaseUrl(config.getProperty("db_url"));
			this.setDatabaseUsername(config.getProperty("db_username"));
			this.setDatabasePassword(config.getProperty("db_password"));
			is.close();
			
		} catch (IOException e) {
			throw new I5Exception(e.getMessage());
		}
	}
	
	public void setConfigFromCommandLine(CommandLine cmd){
		this.setWikiXMLFolder(cmd.getOptionValue("x"));
		this.setPageType(cmd.getOptionValue("t"));
		this.setDumpFilename(cmd.getOptionValue("w"));
		this.setOutputFile(cmd.getOptionValue("o"));
		this.setEncoding(cmd.getOptionValue("e"));
		this.setInflectives(cmd.getOptionValue("inf"));
		this.setWikiXMLIndex(cmd.getOptionValue("i"));
		this.setDatabaseUrl(cmd.getOptionValue("d"));
		this.setDatabaseUsername(cmd.getOptionValue("u"));
		this.setDatabasePassword(cmd.getOptionValue("p"));
	}

	public String getWikiXMLFolder() {
		return wikiXMLFolder;
	}
	
	public void setWikiXMLFolder(String wikiXMLFolder) {
		if (wikiXMLFolder == null){
			throw new IllegalArgumentException("Please specify the WikiXML root folder.");
		}		
		this.wikiXMLFolder = wikiXMLFolder;
	}
	
	public String getPageType() {
		return pageType;
	}

	public void setPageType(String type) {
		if (type == null){
			throw new IllegalArgumentException("Please specify the Wikipage type.");
		}
		if (!type.equals("articles") && 
				!type.equals("discussions") && 
				!type.equals("user_discussions")){
			throw new IllegalArgumentException("The type is not recognized. " +
					"Please specify the type as: articles / dicussions / user_discussions");
		}
		this.pageType = type;
	}

	public String getDumpFilename() {
		return dumpFilename;
	}

	public void setDumpFilename(String dumpFilename) {
		if (dumpFilename == null){
			throw new IllegalArgumentException("Please specify the Wiki dump file.");
		}
		this.dumpFilename = dumpFilename;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		if (outputFile == null){
			throw new IllegalArgumentException("Please specify the output file.");
		}
		this.outputFile = outputFile;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (encoding == null){
			encoding = "utf-8"; // default encoding
		}
		this.encoding = encoding;
	}

	public String getInflectives() {
		return inflectives;
	}

	public void setInflectives(String inflectives) {
		this.inflectives = inflectives;
	}

	public String getWikiXMLIndex() {
		return wikiXMLIndex;
	}
	
	public void setWikiXMLIndex(String wikiXMLIndex) {
		if (wikiXMLIndex == null){
			throw new IllegalArgumentException("Please specify the index of the Wikipage "
					+this.pageType+".");
		}
		this.wikiXMLIndex = wikiXMLIndex;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		if (databaseUrl == null){
			throw new IllegalArgumentException("Please specify the Wikipedia language link " +
					"Mysql Database URL.");
		}	
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		if (databaseUsername == null){
			throw new IllegalArgumentException("Please specify the username of the Wikipedia " +
					"language link Mysql Database.");
		}
		this.databaseUsername = databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		if (databasePassword == null){
			throw new IllegalArgumentException("Please specify the password of  Wikipedia " +
					"language link Mysql Database.");
		}
		this.databasePassword = databasePassword;
	}
}
