package de.mannheim.ids.wiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class XMLWikiProcessor {
	
	private String indent="",wikitext="";
	private static String filepath = "./";
	private boolean textFlag;
	private TagSoupParser tagSoupParser = new TagSoupParser();
	private SwebleParser swebleParser = new SwebleParser();	
	long startTime, endTime, duration;
	
	
	public void process(String inputFile, String articleOutput, String discussionOutput) throws IOException{	
		
		int counter=1;
		boolean readFlag = false, discussionFlag=false;	
		String strLine, page="";

		FileWriter articleWriter = createWriter(articleOutput);
		FileWriter discussionWriter = createWriter(discussionOutput);
		
		FileInputStream fs = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));
				
		while ((strLine = br.readLine()) != null)   {					
			if (strLine.trim().startsWith("<page>")){ // start reading
				page=strLine+"\n";	
				readFlag = true;
				discussionFlag=false;
			}			
			else if(readFlag && !strLine.trim().equals("</mediawiki>")){
				
				strLine = StringEscapeUtils.unescapeHtml(strLine); // unescape HTML tags			
				strLine = StringEscapeUtils.unescapeHtml(strLine); // unescape %nbsp;				
				
				if (strLine.trim().startsWith("<title") ){
					System.out.println(counter++ + strLine);
					if( strLine.contains("Diskussion") ||
						strLine.contains("Discussion") ||
						strLine.contains("Vita") ||
						strLine.contains("Discussione") ||
						strLine.contains("Diskusjon") ||
						strLine.contains("Dyskusja") 
						){	
						
						discussionWriter.append(page + strLine + "\n");
						discussionFlag = true;
					}
					else {
						articleWriter.append(page + strLine + "\n");
					}
				}				
				else if (discussionFlag){
					discussionWriter = handlePageContent(strLine, discussionWriter);
				}
				else{
					//startTime = System.nanoTime();
					articleWriter = handlePageContent(strLine, articleWriter);
					//endTime = System.nanoTime();
					//duration = endTime - startTime;
					//System.out.println("Article execution time "+duration);					
				}				
			}
		}
		br.close();
		fs.close();
		
		articleWriter.append("</articles>");
		discussionWriter.append("</articles>");
		
		articleWriter.close();
		discussionWriter.close();		
	}
	
	public FileWriter handlePageContent(String strLine, FileWriter xml) throws IOException{
		
		if (strLine.trim().endsWith("</text>")){ // finish collecting text
			
			// text starts and ends at the same line
			if (strLine.trim().startsWith("<text")){ 						
				strLine = cleanTextStart(indent, strLine);
				xml.append( indent + "<text>\n" );
			}							
			wikitext += strLine.trim().replaceFirst("</text>", "") + "\n";
			wikitext = wikitext.replaceAll(":\\{\\|", "{|");			
			wikitext = wikitext.replaceAll("/>", " />");			
			wikitext = wikitext.replaceAll("<([^!!/a-zA-Z\\s])", "< $1");
			
			
			try{
				//startTime = System.nanoTime();								
				wikitext = tagSoupParser.generateCleanHTML(wikitext);				
				//endTime = System.nanoTime();
				//duration = endTime - startTime;
				//System.out.println("Tagsoup execution time "+duration);				
				
				/*if(wikitext.contains("Datei:Americium"))
					System.out.println(wikitext);*/
				
				//startTime = System.nanoTime();
				xml.append(swebleParser.parseText(wikitext));				
				//endTime = System.nanoTime();
				//duration = endTime - startTime;
				//System.out.println("Sweble execution time "+duration);
				
				xml.append(indent +"</text>\n");
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			wikitext="";
			this.textFlag=false;
		}
		else if (textFlag){ // continue collecting text
			wikitext += strLine + "\n";
		}
		else if (strLine.trim().startsWith("<text")){ // start collecting text
			wikitext += cleanTextStart(indent, strLine);
			xml.append(this.indent + "<text>\n");
			this.textFlag=true;
		}
		else{ // bypass page metadata		
			xml.append(strLine + "\n");
		}		
		return xml;
	}
	
	public String cleanTextStart(String indent, String strLine){
		if (indent.equals("")){
			this.indent = StringUtils.repeat(" ", strLine.indexOf("<"));
		}											
		return strLine.trim().replaceFirst("<text.*\">", "") + "\n";		
	}
	
	private FileWriter createWriter (String outputFile) throws IOException {	
		File file = new File(filepath+outputFile);		
		if (!file.exists()) file.createNewFile();		

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		fw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fw.append("<articles>\n");
		return fw;
	}
}
