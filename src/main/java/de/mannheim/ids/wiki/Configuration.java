package de.mannheim.ids.wiki;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import de.mannheim.ids.util.LanguageProperties;

public class Configuration {

	private String languageCode;
	private String wikidump;
	private String outputFolder;
	private String encoding;
	
	private List<Integer> namespaces;
	
	public Configuration() {}
	
	public Configuration (String wikidump, String language, String type, 
			String xmlOutputDir, String encoding){		
		setLanguageCode(language);
		setWikidump(wikidump);
		setNamespaces(type);
		setOutputFolder(xmlOutputDir);
		setEncoding(encoding);
	}
	
	public void setConfigFromCommandLine(CommandLine cmd){
		String language = cmd.getOptionValue("l");	
		String type = cmd.getOptionValue("t");
		String wikidump = cmd.getOptionValue("w");
		String xmlOutputDir = cmd.getOptionValue("o");
		String encoding = cmd.getOptionValue("e");	
		
		setLanguageCode(language);
		setWikidump(wikidump);
		setNamespaces(type);
		setOutputFolder(xmlOutputDir);
		setEncoding(encoding);	
	}
	
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String language) {
		ArrayList<String> languages = LanguageProperties.getLanguages();
		if (language == null){
			throw new IllegalArgumentException("Please specify the Wikipedia language.");
		}				
		else if (!languages.contains(language)){
			throw new IllegalArgumentException("Language is not supported. Supported " +
					"languages are de (german), fr (french), hu (hungarian), it (italian), " +
					"pl (polish), no (norwegian), en (english).");
		}
		this.languageCode = language;
	}
	
	public String getWikidump() {
		return wikidump;
	}
	
	public void setWikidump(String wikidump) {
		if (wikidump == null){
			throw new IllegalArgumentException("Please specify the Wiki dump file.");
		}
		this.wikidump = wikidump;
	}
	
	public List<Integer> getNamespaces() {
		return namespaces;
	}
	
	public void setNamespaces(String pageType) {
		
		List<Integer> namespaces = new ArrayList<Integer>();
		if (pageType == null || pageType.equals("all")){
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
		if (xmlOutputDir == null){
			throw new IllegalArgumentException("Please specify the XML output directory.");
		}			
		this.outputFolder = xmlOutputDir;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		if (encoding == null || encoding.isEmpty()){
			encoding = "utf-8";
		}
		this.encoding = encoding;
	}
	
}
