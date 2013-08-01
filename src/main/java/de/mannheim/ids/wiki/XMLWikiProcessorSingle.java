package de.mannheim.ids.wiki;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.lang.StringUtils;

public class XMLWikiProcessorSingle {
	
	private String xmlOutputDir = "./xml/";	
	private boolean isDiscussion;	
	
	private WikiStatistics wikiStatistics;
	private Helper helper;
	private LanguageSetter languageSetter;
	
	private TagSoupParser tagSoupParser = new TagSoupParser();
	private XMLWikiProcessor xmlWikiProcessor;
	

	/** Constructor
	 * 
	 * @param language	is a LanguageSetter instance defining language properties 
	 * 		of a Wikidump.
	 * 
	 */
	public XMLWikiProcessorSingle(LanguageSetter language) {		
		xmlWikiProcessor = new XMLWikiProcessor(language);
		this.languageSetter = xmlWikiProcessor.languageSetter;	
		this.helper=xmlWikiProcessor.helper;
		this.wikiStatistics = xmlWikiProcessor.wikiStatistics;
	}
	
	/** This method converts a Wikidump into a single XML and stored it in xml/ directory.
	 * 
	 * @param inputFile is the location of the wikidump
	 * @param errorOutput is the filename for listing the titles of the failed parsed 
	 * 		wikipages.
	 * @throws IOException
	 */
	public void process(String inputFile, String errorOutput) throws IOException{
				
		String basename = StringUtils.removeEnd(inputFile,".xml").replaceFirst(".*/", "");
				
		OutputStreamWriter articleWriter = helper.createWriter(this.xmlOutputDir+ basename+"-articles.xml");
		OutputStreamWriter discussionWriter = helper.createWriter(this.xmlOutputDir + basename+"-discussions.xml");
		OutputStreamWriter errorWriter = helper.createWriter(errorOutput);
		
		articleWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		articleWriter.append("<articles>\n");
		
		discussionWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		discussionWriter.append("<discussions>\n");
				
		FileInputStream fs = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));
		
		read(br, articleWriter, discussionWriter, errorWriter);
	
		br.close();
		fs.close();
		
		articleWriter.append("</articles>");
		discussionWriter.append("</discussions>");	

		articleWriter.close();
		discussionWriter.close();		
		errorWriter.close();		

		wikiStatistics.printStatistics();
	}
	
	private void read(BufferedReader br, OutputStreamWriter articleWriter, 
			OutputStreamWriter discussionWriter, OutputStreamWriter errorWriter) 
			throws IOException{		
		
		boolean readFlag = false, isMetapage=false;	
		String strLine, trimmedStrLine;
		
		while ((strLine = br.readLine()) != null)   {					
			trimmedStrLine = strLine.trim();
			
			if (trimmedStrLine.startsWith("<page>")){ // start reading				
				xmlWikiProcessor.page=strLine+"\n";				
				readFlag = true;
				isDiscussion=false;	 xmlWikiProcessor.isEmptyText=false; isMetapage=false;
			}
			else if (trimmedStrLine.endsWith("</page>")  && !isMetapage){
				xmlWikiProcessor.page += strLine;
	
				try { 
					xmlWikiProcessor.page = tagSoupParser.generate(xmlWikiProcessor.page, false);					
//					page = nekoParser.generate(page);	
				} 
				catch (Exception e) { e.printStackTrace();}
				
				if (isDiscussion){ 
					xmlWikiProcessor.write(discussionWriter,strLine);
					if (!xmlWikiProcessor.wikitext.equals("")){ wikiStatistics.addTotalDiscussions(); }
					else if (xmlWikiProcessor.isEmptyText){ wikiStatistics.addEmptyDiscussions(); }
				}
				else{ 
					xmlWikiProcessor.write(articleWriter,strLine);
					if (!xmlWikiProcessor.wikitext.equals("")){ wikiStatistics.addTotalArticles(); }
					else if (xmlWikiProcessor.isEmptyText){ wikiStatistics.addEmptyArticles(); }
				}
				
				xmlWikiProcessor.page=""; xmlWikiProcessor.wikitext="";
			}
			else if(readFlag && !trimmedStrLine.equals("</mediawiki>")){					
																
				if (trimmedStrLine.startsWith("<title") ){
					xmlWikiProcessor.matcher = xmlWikiProcessor.titlePattern.matcher(trimmedStrLine);
					if (xmlWikiProcessor.matcher.find()) { xmlWikiProcessor.pagetitle = xmlWikiProcessor.matcher.group(1); }
					
					if (strLine.contains(":")){
						for (String s: this.languageSetter.getMetapages()){							
							if (strLine.contains(s)){
								//System.out.println("metapage "+ strLine);
								wikiStatistics.addTotalMetapages();								
								readFlag = false; //skip reading metapages								
								isMetapage = true;
								break;
							}
						}
						
						if (!isMetapage){							
							if(strLine.contains(this.languageSetter.getTalk())){
								isDiscussion = true;								
							}
							xmlWikiProcessor.page += xmlWikiProcessor.setIndent(strLine)+ trimmedStrLine+"\n";							
						}
					}
					else{ xmlWikiProcessor.page += xmlWikiProcessor.setIndent(strLine)+ trimmedStrLine+ "\n"; }
				}
				else{ xmlWikiProcessor.handlePageContent(strLine, trimmedStrLine, errorWriter); }				
			}				
		}
	}
	
	
}
