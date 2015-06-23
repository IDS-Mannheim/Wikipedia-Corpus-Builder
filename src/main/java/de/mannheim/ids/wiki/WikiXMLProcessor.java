package de.mannheim.ids.wiki;

import java.io.IOException;
import java.util.List;

import de.mannheim.ids.util.Utilities;
import de.mannheim.ids.util.WikiStatistics;

/**
 * 
 * 
 * @author margaretha
 * 
 * */
public class WikiXMLProcessor {
	
	private Configuration config;
	private WikiStatistics wikiStatistics;
	
	public WikiXMLProcessor(Configuration config) throws IOException {
		if (config == null){
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		this.config = config;
		this.wikiStatistics = new WikiStatistics(config.getWikidump(), 
				config.getOutputEncoding());
	}
	
	public void createWikiXML() throws IOException {
		createOutputDirectories();	
		process(new MultipleXMLWriter(config));
	}
	
	public void createSingleWikiXML() throws IOException {
		Utilities.createDirectory(config.getOutputFolder());		
		WikiXMLWriter wikiXMLWriter = new SingleXMLWriter(config, wikiStatistics);		
		process(wikiXMLWriter);
		wikiXMLWriter.close();
	}
	
	private void process(WikiXMLWriter wikiXMLWriter) throws IOException{
				
		if (wikiXMLWriter == null){
			throw new IllegalArgumentException("WikiXMLwriter cannot be null.");
		}
		
		WikiPageReader wikiReader = new WikiPageReader(config, wikiStatistics);
		wikiReader.read(config.getWikidump(),wikiXMLWriter);		
		wikiStatistics.printStatistics();
		wikiStatistics.errorWriter.close();
	}
	
	private void createOutputDirectories(){
		String xmlOutputDir = config.getOutputFolder();
		Utilities.createDirectory(xmlOutputDir);		
		
		List<Integer> namespaces = config.getNamespaces();
		
		if (namespaces.contains(0)) { 
			for (String i:WikiPage.indexList) {
				Utilities.createDirectory(xmlOutputDir+"/articles/"+i);
			}
		}
		
		if (namespaces.contains(1)){ 
			for (String i:WikiPage.indexList) {
				Utilities.createDirectory(xmlOutputDir+"/discussions/"+i);
			}
		}
		
		if (namespaces.contains(3)){ 
			for (String i:WikiPage.indexList) {
				Utilities.createDirectory(xmlOutputDir+"/user_discussions/"+i);
			}
		}	
	}
}
