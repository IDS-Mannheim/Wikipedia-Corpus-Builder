package de.mannheim.ids.wiki;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public class XMLWikiProcessor {
	
	private static String filepath = "./";
	
	
	private String wikitext="", page, pagetitle;
	private boolean textFlag, discussionFlag=false, isEmptyText=false, isArticle=true;

//	private long startTime, endTime, duration, total;
	private int count=0,swebleErr=0, parsingErr=0, totalMetapages=0, 
			totalDiscussions=0, totalArticles=0, counter=1;		

	private TagSoupParser tagSoupParser = new TagSoupParser();
	private NekoHTMLParser nekoParser = new NekoHTMLParser();
	private Sweble2Parser swebleParser = new Sweble2Parser();
	private DOMParser dp = new DOMParser();
	private LanguageSetter languageSetter;
	
	private Matcher matcher;
	private Pattern pattern =  Pattern.compile("<([^!!/a-zA-Z\\s])");
	private Pattern textPattern = Pattern.compile("<text.*\">");
	private Pattern titlePattern = Pattern.compile("<title>(.+)</title>");
	private Pattern hackpattern = Pattern.compile("<([^>]+)/>");
		
	public XMLWikiProcessor() {
		// TODO Auto-generated constructor stub
	}
	public XMLWikiProcessor(String language) {		
		languageSetter = new LanguageSetter(language);
	}
	
	public void process(String inputFile, String articleOutput, String discussionOutput, 
			String errorOutput) throws IOException{

		OutputStreamWriter articleWriter = createWriter(articleOutput);
		OutputStreamWriter discussionWriter = createWriter(discussionOutput);
		OutputStreamWriter errorWriter = createWriter(errorOutput);
		
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
		
		//System.out.println(total/count);
		System.out.println("Total Articles "+ totalArticles);
		System.out.println("Total Discussions "+ totalDiscussions);
		System.out.println("Total Metapages "+ totalMetapages);
		System.out.println("Total Sweble Exceptions "+ swebleErr);
		System.out.println("Total XML Parsing Exceptions "+ parsingErr);
	}
	
	private void read(BufferedReader br, OutputStreamWriter articleWriter, 
			OutputStreamWriter discussionWriter, OutputStreamWriter errorWriter) 
			throws IOException{		
		
		boolean readFlag = false;	
		String strLine, trimmedStrLine;
		
		while ((strLine = br.readLine()) != null)   {					
			trimmedStrLine = strLine.trim();
			
			if (trimmedStrLine.startsWith("<page>")){ // start reading				
				page=strLine+"\n";				
				readFlag = true;
				discussionFlag=false;
			}
			else if (trimmedStrLine.endsWith("</page>")){
				page += strLine;
				//System.out.println(page);	
				try { 
					page = tagSoupParser.generate(page);					
//					page = nekoParser.generate(page);	
				} 
				catch (Exception e) { e.printStackTrace();}
				//System.out.println(page);	
				
				if (discussionFlag){ 
					write(discussionWriter,strLine);
					if (!wikitext.equals("")) totalDiscussions++;							
				}
				else{ 
					write(articleWriter,strLine);
					if (!wikitext.equals("")) totalArticles++;
				}
				
				page=""; wikitext="";
				isEmptyText=false;
				isArticle=true;
			}
			else if(readFlag && !trimmedStrLine.equals("</mediawiki>")){					
																
				if (trimmedStrLine.startsWith("<title") ){
					matcher = titlePattern.matcher(trimmedStrLine);
					if (matcher.find()) {
						pagetitle = matcher.group(1);						
					}
					
					if (strLine.contains(":")){
						for (String s: this.languageSetter.getMetapages()){							
							if (strLine.contains(s)){
								//System.out.println("metapage "+ strLine);
								totalMetapages++;
								readFlag = false; //skip reading metapages								
								isArticle = false;
								break;
							}
						}
						
						if (isArticle){							
							if(strLine.contains(this.languageSetter.getTalk())){
								discussionFlag = true;
//								page += setIndent(strLine)+ trimmedStrLine+"\n";
							}
//							else {readFlag = false;}
							page += setIndent(strLine)+ trimmedStrLine+"\n";							
						}
					}
					else{						
						page += setIndent(strLine)+ trimmedStrLine+ "\n";
//						readFlag = false;
					}
				}
				else{					
					handlePageContent(strLine, trimmedStrLine, errorWriter);
				}				
			}				
		}
	}
	
	private void handlePageContent(String strLine, String trimmedStrLine, 
			OutputStreamWriter errorWriter) throws IOException {
		
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text			
			page += "      <text/>\n";			
			// text starts and ends at the same line
			if (trimmedStrLine.startsWith("<text")){
				trimmedStrLine = cleanTextStart(trimmedStrLine);				
			}			
			
			trimmedStrLine = StringUtils.replaceOnce(trimmedStrLine, "</text>", ""); // remove </text>		
			
//			if (discussionFlag){
//				wikitext += trimmedStrLine + "\n";
//			}	
//			else{ 
				wikitext += (StringEscapeUtils.unescapeXml(trimmedStrLine) + "\n").trim(); // unescape XML tags
//			}
			
			if (wikitext.equals("")){ return; } // empty text			
						
			wikitext = StringUtils.replaceEach(wikitext, 
					new String[] { ":{|" , "/>"}, 
					new String[] { "{|" , " />"}); //start table notation	
			
			matcher = hackpattern.matcher(wikitext); // get around for sweble error
			wikitext = matcher.replaceAll("</$1>");			
			matcher = pattern.matcher(wikitext); // space for non-tag
			wikitext = matcher.replaceAll("< $1");					
			
			try{
				// italic and bold are not repaired because they have wiki-mark-ups
//				wikitext = nekoParser.generate(wikitext);
				wikitext = tagSoupParser.generate(wikitext); 
//				System.out.println(wikitext.trim());
				wikitext = swebleParser.parseText(wikitext.trim(), pagetitle);
			}catch (Exception e) {
				wikitext="";
				swebleErr++;
				errorWriter.append(pagetitle+"\n");				
				//e.printStackTrace();
			}
			
			try{
				//testing
				String t = "<text>"+wikitext+"</text>";
				dp.parseXML(new ByteArrayInputStream(t.getBytes("utf-8")));				
			}
			catch (Exception e) {
				wikitext="";
				e.printStackTrace();		
				parsingErr++;	
			}
						
			textFlag=false;
		}
		else if (textFlag){ // continue collecting text		
//			if (!discussionFlag){ 
				strLine = StringEscapeUtils.unescapeXml(strLine); // unescape XML tags
//			}
			wikitext += strLine + "\n";
		}
		else if(trimmedStrLine.startsWith("<text")) {				
			
			if (trimmedStrLine.endsWith("/>")){ // empty text				
				page += "        <text lang=\""+this.languageSetter.getLanguage()+"\"/>\n";
				wikitext="";
				this.isEmptyText=true;
			}
			else { // start collecting text
//				if (discussionFlag){
//					wikitext += cleanTextStart(trimmedStrLine);
//				}
//				else {
					wikitext += StringEscapeUtils.unescapeXml(cleanTextStart(trimmedStrLine)); // unescape XML tags
//				}
				this.textFlag=true;
			}
		}
		else{ // bypass page metadata			
			page += strLine + "\n";
		}			
	}
	
	private void write(OutputStreamWriter writer, String strLine) throws IOException{
		if (isArticle && !isEmptyText) {	
			System.out.println(counter++ +" "+ pagetitle);
			
//				String [] arr = page.split("<text/>");				
			String [] arr = page.split("<text></text>");
			if (arr.length >1){				
				writer.append(setIndent(strLine));
				writer.append(arr[0]);
				
				if (wikitext.equals("")){
					writer.append("<text lang=\""+this.languageSetter.getLanguage()+"\"/>" );
				}
				else {
					writer.append("<text lang=\""+this.languageSetter.getLanguage()+"\">\n" );
					writer.append(wikitext+"\n");
					writer.append("      </text>");
				}
								
				writer.append(arr[1]);				
				//writer.append("\n");				
			}
			else{
				
				//throw new ArrayIndexOutOfBoundsException();
				System.out.println("nekoerror");
			}
		}
		else{
			writer.append(page);
		}
	} 
	
	private String cleanTextStart(String trimmedStrLine) throws IOException{
		matcher = textPattern.matcher(trimmedStrLine);		
		return matcher.replaceFirst("") + "\n";		
	}
	
	private String setIndent(String strLine){		
		return StringUtils.repeat(" ", strLine.indexOf("<"));				
	}		
	
	private OutputStreamWriter createWriter (String outputFile) throws IOException {
		File file = new File(filepath+outputFile);		
		if (!file.exists()) file.createNewFile();		

		OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(file)), "UTF-8");		

		return os;	
	}		
	
}
