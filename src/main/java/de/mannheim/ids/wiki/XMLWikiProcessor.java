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
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class XMLWikiProcessor {
	
	private String xmlOutputDir = "./xml/";
	private String articleDir = this.xmlOutputDir+"articles/";
	private String discussionDir = this.xmlOutputDir+"discussions/";
	private String wikitext="", page, pagetitle;
	private List<String> indexList;
	
	private boolean textFlag, isDiscussion, isEmptyText;
	private int counter=1;

//	private NekoHTMLParser nekoParser = new NekoHTMLParser();
	private TagSoupParser tagSoupParser = new TagSoupParser();
	private Sweble2Parser swebleParser = new Sweble2Parser();
	private DOMParser dp = new DOMParser();
	private LanguageSetter languageSetter;
	private WikiStatistics wikiStatistics;
	
	private Matcher matcher;
	private Pattern pattern =  Pattern.compile("<([^!!/a-zA-Z\\s])");
	private Pattern textPattern = Pattern.compile("<text.*\">");
	private Pattern titlePattern = Pattern.compile("<title>(.+)</title>");
	private Pattern idPattern = Pattern.compile("<id>(.+)</id>");
	private Pattern hackpattern = Pattern.compile("<([^>]+)/>");
			
	public XMLWikiProcessor(String language) {		
		languageSetter = new LanguageSetter(language);
		wikiStatistics= new WikiStatistics();	

		createDirectory(xmlOutputDir);		
	}
	
	public void processSplit(String inputFile, String errorOutput) throws IOException {
				
		String[] index = {"A","B","C","D","E","F","G","H","I","J","K","L",
			    "M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
			    "0","1","2","3","4","5","6","7","8","9","Char"};
		
		for (String i:index) {
			createDirectory(this.articleDir+i);
			createDirectory(this.discussionDir+i);
		}
		
		this.indexList = Arrays.asList(index);		
		
		FileInputStream fs = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));
		OutputStreamWriter errorWriter = createWriter(errorOutput);
				
		readAndSplit(br, errorWriter);
	
		br.close();
		fs.close();
		errorWriter.close();
		
		printStatistics();		
	}
	
	public void readAndSplit(BufferedReader br, OutputStreamWriter errorWriter) 
			throws IOException{
		
		boolean readFlag = false, idFlag = false, isMetapage = false;	
		String strLine, trimmedStrLine, id = "", index;
		int start;
		
		OutputStreamWriter writer = null;
		
		while ((strLine = br.readLine()) != null)   {					
			trimmedStrLine = strLine.trim();
			
			if (trimmedStrLine.startsWith("<page>")){ // start reading				
				page=strLine+"\n";				
				readFlag = true; idFlag=true;
				isDiscussion=false;	 isEmptyText=false; isMetapage=false;
			}
			else if (trimmedStrLine.endsWith("</page>")  && !isMetapage){
				page += strLine;
				try { 
					page = tagSoupParser.generate(page, false);					
//					page = nekoParser.generate(page);	
				} 
				catch (Exception e) { e.printStackTrace(); }	
				
				if (isDiscussion){
					if (!wikitext.equals("")){ wikiStatistics.addTotalDiscussions(); }
					else if (isEmptyText){ wikiStatistics.addEmptyDiscussions(); }
					else { 
						wikiStatistics.addEmptyParsedDiscussions();						 
						errorWriter.append("Empty parsed discussion:"+ pagetitle); 
					}
				}
				else{ 					
					if (!wikitext.equals("")){ wikiStatistics.addTotalArticles(); }
					else if (isEmptyText){ wikiStatistics.addEmptyArticles(); }
					else {
						wikiStatistics.addEmptyParsedArticles();						 
						errorWriter.append("Empty parsed articles:"+pagetitle); 
					}
				}
				
				write(writer,strLine);
				writer.close();
				
				page=""; wikitext="";				
			}			
			else if(readFlag && !trimmedStrLine.equals("</mediawiki>")){					
																
				if (trimmedStrLine.startsWith("<title") ){
					matcher = titlePattern.matcher(trimmedStrLine);
					if (matcher.find()) { pagetitle = matcher.group(1); }
					
					if (strLine.contains(":")){
						for (String s: this.languageSetter.getMetapages()){							
							if (strLine.contains(s)){
								wikiStatistics.addTotalMetapages();								
								readFlag = false; //skip reading metapages								
								isMetapage = true;
								break;
							}
						}
						
						if (!isMetapage){							
							if(strLine.contains(languageSetter.getTalk())){
								isDiscussion = true;								
							}
							page += setIndent(strLine)+ trimmedStrLine+"\n";							
						}
					}
					else{ page += setIndent(strLine)+ trimmedStrLine+ "\n"; }
				}
				else if (trimmedStrLine.startsWith("<id") && idFlag){
					
					matcher = idPattern.matcher(trimmedStrLine);
					if (matcher.find()) { id = matcher.group(1); }
					
					if (isDiscussion){
						start = languageSetter.getTalk().length() +1;
						index = normalizeIndex(pagetitle.substring(start, start+1));						
						writer = createWriter(this.discussionDir + index+"/"+id+".xml");
					}
					else { 
						index = normalizeIndex(pagetitle.substring(0,1));
						writer = createWriter(this.articleDir + index+"/"+id+".xml");
					}					
					
					writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					
					idFlag=false;
				}
				else{ handlePageContent(strLine, trimmedStrLine, errorWriter); }				
			}				
		}
	}	
	
	public void process(String inputFile, String errorOutput) throws IOException{
				
		String basename = StringUtils.removeEnd(inputFile,".xml").replaceFirst(".*/", "");
				
		OutputStreamWriter articleWriter = createWriter(this.xmlOutputDir+ basename+"-articles.xml");
		OutputStreamWriter discussionWriter = createWriter(this.xmlOutputDir + basename+"-discussions.xml");
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

		printStatistics();
	}
	
	private void read(BufferedReader br, OutputStreamWriter articleWriter, 
			OutputStreamWriter discussionWriter, OutputStreamWriter errorWriter) 
			throws IOException{		
		
		boolean readFlag = false, isMetapage=false;	
		String strLine, trimmedStrLine;
		
		while ((strLine = br.readLine()) != null)   {					
			trimmedStrLine = strLine.trim();
			
			if (trimmedStrLine.startsWith("<page>")){ // start reading				
				page=strLine+"\n";				
				readFlag = true;
				isDiscussion=false;	 isEmptyText=false; isMetapage=false;
			}
			else if (trimmedStrLine.endsWith("</page>")  && !isMetapage){
				page += strLine;
	
				try { 
					page = tagSoupParser.generate(page, false);					
//					page = nekoParser.generate(page);	
				} 
				catch (Exception e) { e.printStackTrace();}
				
				if (isDiscussion){ 
					write(discussionWriter,strLine);
					if (!wikitext.equals("")){ wikiStatistics.addTotalDiscussions(); }
					else if (isEmptyText){ wikiStatistics.addEmptyDiscussions(); }
				}
				else{ 
					write(articleWriter,strLine);
					if (!wikitext.equals("")){ wikiStatistics.addTotalArticles(); }
					else if (isEmptyText){ wikiStatistics.addEmptyArticles(); }
				}
				
				page=""; wikitext="";
			}
			else if(readFlag && !trimmedStrLine.equals("</mediawiki>")){					
																
				if (trimmedStrLine.startsWith("<title") ){
					matcher = titlePattern.matcher(trimmedStrLine);
					if (matcher.find()) { pagetitle = matcher.group(1); }
					
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
							page += setIndent(strLine)+ trimmedStrLine+"\n";							
						}
					}
					else{ page += setIndent(strLine)+ trimmedStrLine+ "\n"; }
				}
				else{ handlePageContent(strLine, trimmedStrLine, errorWriter); }				
			}				
		}
	}
	
	private void handlePageContent(String strLine, String trimmedStrLine, 
			OutputStreamWriter errorWriter) throws IOException {
		
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text			
			page += "      <text/>\n";			

			if (trimmedStrLine.startsWith("<text")){ // text starts and ends at the same line
				trimmedStrLine = cleanTextStart(trimmedStrLine);				
			}			
			
			trimmedStrLine = StringUtils.replaceOnce(trimmedStrLine, "</text>", ""); // remove </text>
			wikitext += (StringEscapeUtils.unescapeXml(trimmedStrLine) + "\n").trim(); // unescape XML tags
			
			if (wikitext.equals("")){ this.isEmptyText=true; return; } // empty text			
						
			wikitext = StringUtils.replaceEach(wikitext, 
					new String[] { ":{|" , "/>"}, 
					new String[] { "{|" , " />"}); //start table notation	
			
			matcher = hackpattern.matcher(wikitext); // remove <x/>
			wikitext = matcher.replaceAll("</$1>");			
			matcher = pattern.matcher(wikitext); // space for non-tag
			wikitext = matcher.replaceAll("< $1");					
			
			try{
				// italic and bold are not repaired because they have wiki-mark-ups
				wikitext = tagSoupParser.generate(wikitext,true);
				wikitext = swebleParser.parseText(wikitext.trim(), pagetitle);				
			}catch (Exception e) {
				errorWriter.append(pagetitle+"\n");
				wikiStatistics.addSwebleErrors();
				wikitext="";
			}
			
			try{ //test XML validity
				String t = "<text>"+wikitext+"</text>";
				dp.parseXML(new ByteArrayInputStream(t.getBytes("utf-8")));				
			}
			catch (Exception e) {
				errorWriter.append("Dom parser exception: "+pagetitle+"\n");
				wikiStatistics.addParsingErrors();
				wikitext="";				
			}
						
			textFlag=false;
		}
		else if (textFlag){ // continue collecting text
			strLine = StringEscapeUtils.unescapeXml(strLine); // unescape XML tags
			wikitext += strLine + "\n";
		}
		else if(trimmedStrLine.startsWith("<text")) {				
			
			if (trimmedStrLine.endsWith("/>")){ // empty text				
				page += "        <text lang=\""+this.languageSetter.getLanguage()+"\"/>\n";
				wikitext="";
				this.isEmptyText=true;
			}
			else { // start collecting text
				wikitext += StringEscapeUtils.unescapeXml(cleanTextStart(trimmedStrLine)); // unescape XML tags
				this.textFlag=true;
			}
		}
		else{ // bypass page metadata			
			page += strLine + "\n";
		}			
	}
	
	private void write(OutputStreamWriter writer, String strLine) throws IOException{
		if (!isEmptyText) {	
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
			}
			else{ //throw new ArrayIndexOutOfBoundsException();								
				System.out.println("Outer Error: "+pagetitle);
				wikiStatistics.addOuterErrors();
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
		File file = new File(outputFile);		
		if (!file.exists()) file.createNewFile();

		OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(file)), "UTF-8");		

		return os;	
	}		
	
	private void createDirectory(String directory){
		File dir = new File(directory);
		if (!dir.exists()) { dir.mkdirs(); }
	}
	
	public String normalizeIndex(String input) throws IOException{
		String normalizedStr = Normalizer.normalize(input,Form.NFKD).toUpperCase();
		normalizedStr = normalizedStr.substring(0,1);	
		
//		if (Character.isLetterOrDigit(normalizedStr.charAt(0))){
//			return normalizedStr.substring(0,1);	
//		}
		if (this.indexList.contains(normalizedStr)){
			return normalizedStr;
		}
		else{ return "Char"; }		
	}
	
	private void printStatistics(){
		System.out.println("Total non-empty articles "+ wikiStatistics.getTotalArticles());
		System.out.println("Total non-empty discussions "+ wikiStatistics.getTotalDiscussions());
		System.out.println("Total empty articles "+ wikiStatistics.getEmptyArticles());
		System.out.println("Total empty discussions "+ wikiStatistics.getEmptyDiscussions());
		System.out.println("Total empty parsed articles "+ wikiStatistics.getEmptyParsedArticles());
		System.out.println("Total empty parsed discussions "+ wikiStatistics.getEmptyParsedDiscussions());		
		System.out.println("Total metapages "+ wikiStatistics.getTotalMetapages());
		System.out.println("Total Sweble exceptions "+ wikiStatistics.getSwebleErrors());
		System.out.println("Total XML parsing exceptions "+ wikiStatistics.getParsingErrors());
		System.out.println("Total page structure exceptions "+ wikiStatistics.getOuterErrors());
	}
}
