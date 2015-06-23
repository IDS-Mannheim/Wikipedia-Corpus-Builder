package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
	
	private List<Integer> namespaces;
	
	public Configuration (String wikidump, String language, String userPage, 
			String talkPage, String userContribution, String pageType, 
			String xmlOutputDir, String encoding){		
		
		setWikidump(wikidump);
		setLanguageCode(language);
		setUserPage(userPage);
		setTalkPage(talkPage);
		setUserContribution(userContribution);
		setNamespaces(pageType);
		setOutputFolder(xmlOutputDir);
		setOutputEncoding(encoding);		
	}
	
	public Configuration(CommandLine cmd){
		String wikidump = cmd.getOptionValue("w");
		String language = cmd.getOptionValue("l");
		String userPage = cmd.getOptionValue("up");
		String talkPage = cmd.getOptionValue("tp");
		String userContribution = cmd.getOptionValue("uc");
		String pageType = cmd.getOptionValue("t");		
		String xmlOutputDir = cmd.getOptionValue("o");
		String encoding = cmd.getOptionValue("e");	
		
		setWikidump(wikidump);
		setLanguageCode(language);
		setUserPage(userPage);
		setTalkPage(talkPage);
		setUserContribution(userContribution);
		setNamespaces(pageType);
		setOutputFolder(xmlOutputDir);
		setOutputEncoding(encoding);		
	}

	public Configuration(String properties) throws IOException {
		InputStream is = Configuration.class.getClassLoader().
				getResourceAsStream(properties);
		
		Properties config = new Properties();
		config.load(is);
		
		setLanguageCode(config.getProperty("language_code"));
		setUserPage(config.getProperty("user_page"));
		setTalkPage(config.getProperty("talk_page"));
		setUserContribution(config.getProperty("user_contribution"));
		setWikidump(config.getProperty("wikidump"));
		setNamespaces(config.getProperty("page_type","articles"));
		setOutputFolder(config.getProperty("output_folder"));
		setOutputEncoding(config.getProperty("output_encoding"));
		
		is.close();		
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		if (languageCode == null || languageCode.isEmpty()){
			throw new IllegalArgumentException("Please specify the 2-letter Wikipedia language code.");
		}			
		this.languageCode = languageCode;
	}
	
	public String getWikidump() {
		return wikidump;
	}
	
	public void setWikidump(String wikidump) {
		if (wikidump == null || wikidump.isEmpty()){
			throw new IllegalArgumentException("Please specify the Wiki dump file.");
		}
		this.wikidump = wikidump;
	}
	
	public List<Integer> getNamespaces() {
		return namespaces;
	}
	
	public void setNamespaces(String pageType) {
		if (pageType == null || pageType.isEmpty()){
			throw new IllegalArgumentException("Please specify the Wikipage type to convert.");
		}
			
		List<Integer> namespaces = new ArrayList<Integer>();
		if (pageType.equals("all")){
			namespaces.add(0);
			namespaces.add(1);
			namespaces.add(3);
		}
		else if (pageType.equals("articles")){
			namespaces.add(0);
		}
		else if (pageType.equals("discussions")){
			namespaces.add(1);
		}		
		else if (pageType.equals("user_discussions")){
			namespaces.add(3);
		}
		else {
			throw new IllegalArgumentException("The type is not recognized. " +
					"Please specify the type as: articles, discussions, " +
					"user_discussions or all");
		}
		
		this.namespaces = namespaces;
	}
	
	public String getOutputFolder() {
		return outputFolder;
	}
	
	public void setOutputFolder(String xmlOutputDir) {
		if (xmlOutputDir == null || xmlOutputDir.isEmpty()){
			throw new IllegalArgumentException("Please specify the XML output directory.");
		}			
		this.outputFolder = xmlOutputDir;
	}
	
	public String getOutputEncoding() {
		return outputEncoding;
	}
		
	public void setOutputEncoding(String encoding) {
		if (encoding == null || encoding.isEmpty()){
			encoding = "utf-8";
		}
		this.outputEncoding = encoding;
	}

	public String getUserPage() {
		return userPage;
	}

	public void setUserPage(String userPage) {
		if (userPage == null || userPage.isEmpty()){
			throw new IllegalArgumentException("Please specify the user page " +
					"in the language of the Wikipedia dump.");
		}	
		this.userPage = userPage;
	}

	public String getTalkPage() {
		return talkPage;
	}

	public void setTalkPage(String talkPage) {
		if (talkPage == null || talkPage.isEmpty()){
			throw new IllegalArgumentException("Please specify the talk page " +
					"in the language of the Wikipedia dump.");
		}
		this.talkPage = talkPage;
	}

	public String getUserContribution() {
		return userContribution;
	}

	public void setUserContribution(String userContribution) {
		if (userContribution == null || userContribution.isEmpty()){
			throw new IllegalArgumentException("Please specify the user " +
					"contribution page in the language of the Wikipedia dump.");
		}
		this.userContribution = userContribution;
	}
	
}
