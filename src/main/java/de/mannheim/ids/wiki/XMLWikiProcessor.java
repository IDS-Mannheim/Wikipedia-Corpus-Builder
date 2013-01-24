package de.mannheim.ids.wiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
		
		boolean readFlag = false, discussionFlag=false;	
		String strLine, page="";		
		StringWriter article= new StringWriter(), discussion = new StringWriter();
		int counter=1;
		
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
						
						discussion.write(page + strLine + "\n");						
						discussionFlag = true;
					}
					else {
						
						article.write(page + strLine + "\n");
					}
				}				
				else if (discussionFlag){
					discussion = handlePageContent(strLine.trim(), discussion);
				}
				else{
					//startTime = System.nanoTime();
					article = handlePageContent(strLine, article);
					//endTime = System.nanoTime();
					//duration = endTime - startTime;
					//System.out.println("Article execution time "+duration);					
				}				
			}
		}
		br.close();
		fs.close();
		
		writeXML(article,articleOutput);	
		writeXML(discussion,discussionOutput);
		
	}	

	//public String handlePageContent(String strLine, String xml){
	public StringWriter handlePageContent(String strLine, StringWriter xml){
		
		if (strLine.trim().endsWith("</text>")){ // finish collecting text
			
			// text starts and ends at the same line
			if (strLine.trim().startsWith("<text")){ 						
				strLine = cleanTextStart(indent, strLine);
				xml.write( indent + "<text>\n" );
			}							
			wikitext += strLine.trim().replaceFirst("</text>", "") + "\n";
			wikitext = wikitext.replaceAll(":\\{\\|", "{|");			
			wikitext = wikitext.replaceAll("/>", " />");			
			wikitext = wikitext.replaceAll("<([^/\\w])", "< $1");
									
			try{
				//startTime = System.nanoTime();								
				wikitext = tagSoupParser.generateCleanHTML(wikitext);				
				//endTime = System.nanoTime();
				//duration = endTime - startTime;
				//System.out.println("Tagsoup execution time "+duration);				
				
				if(wikitext.contains("Datei:Americium"))
					System.out.println(wikitext);
				
				//startTime = System.nanoTime();
				xml.append(swebleParser.parseText(wikitext));
				xml.write("\n");
				//endTime = System.nanoTime();
				//duration = endTime - startTime;
				//System.out.println("Sweble execution time "+duration);
				
				xml.write(indent +"</text>\n");
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
			xml.write(this.indent + "<text>\n");
			this.textFlag=true;
		}
		else{ // bypass page metadata		
			xml.write(strLine + "\n");
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
		return fw;
	}
	
	public void writeXML(StringWriter content, String outputFile) throws IOException {		
		 
		File file = new File(filepath+outputFile);		
		if (!file.exists()) file.createNewFile();		

		FileWriter fw = new FileWriter(file.getAbsoluteFile());		
		PrintWriter pw = new PrintWriter(fw);
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<articles>");
		pw.print(content);
		pw.print("</articles>");
		pw.close();		
		
		//new OutputStreamWriter(new BufferedOutpputStream(new FileOutputStream(new File("bla.out"))), "UTF-8");
	}	

}
