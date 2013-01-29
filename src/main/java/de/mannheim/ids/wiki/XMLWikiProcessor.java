package de.mannheim.ids.wiki;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class XMLWikiProcessor {
	
	private String indent="",wikitext="";
	private static String filepath = "./";
	private boolean textFlag;
	private TagSoupParser tagSoupParser = new TagSoupParser();
	private SwebleParser swebleParser = new SwebleParser();	
	private Pattern pattern =  Pattern.compile("<([^!!/a-zA-Z\\s])");
	private Pattern textPattern = Pattern.compile("<text.*\">");
	private Matcher matcher;
	private long startTime, endTime, duration, total;
	private int  count=0;	
	
	private static List<String> metapages = new ArrayList<String>();	
	private String talk;
	
	
	public XMLWikiProcessor() {
		// TODO Auto-generated constructor stub
	}
	public XMLWikiProcessor(String language) {
		setLanguageProperties(language);
	}
	
	public void process(String inputFile, String articleOutput, String discussionOutput) throws IOException{	
		
		int counter=1;
		boolean readFlag = false, discussionFlag=false, isArticle=true;	
		String strLine, trimmedStrLine, page="";

//		FileWriter articleWriter = createWriter(articleOutput);
//		FileWriter discussionWriter = createWriter(discussionOutput);
		
		OutputStreamWriter articleWriter = createWriter(articleOutput);
		OutputStreamWriter discussionWriter = createWriter(discussionOutput);
		
		FileInputStream fs = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));
		
		//startTime = System.nanoTime();
		while ((strLine = br.readLine()) != null)   {					
			trimmedStrLine = strLine.trim();
			
			if (trimmedStrLine.startsWith("<page>")){ // start reading
				/*endTime = System.nanoTime();
				duration = endTime - startTime;
				System.out.println("Page execution time "+duration);	
				count++;
				total += duration;*/
				
				page=strLine+"\n";	
				readFlag = true;
				discussionFlag=false;

//				startTime = System.nanoTime();
			}			
			else if(readFlag && !trimmedStrLine.equals("</mediawiki>")){
				
				strLine = StringEscapeUtils.unescapeHtml(strLine); // unescape HTML tags			
				strLine = StringEscapeUtils.unescapeHtml(strLine); // unescape %nbsp;				
				
				if (trimmedStrLine.startsWith("<title") ){					
					
					if (strLine.contains(":")){					
											
						for (String s: metapages){							
							if (strLine.contains(s)){
								System.out.println("metapage "+ strLine);	
								readFlag = false; //skip reading metapages								
								isArticle = false;
								break;
							}
						}
						
						if (isArticle){
							System.out.println(counter++ + strLine);
							if(strLine.contains(talk)){								
								discussionWriter.append(page + strLine + "\n");
								discussionFlag = true;							
							}
							else {								
								articleWriter.append(page + strLine + "\n");
							}							
						}
						else{
							isArticle=true;
						}
					
					}
					else{
						System.out.println(counter++ + strLine);						
						articleWriter.append(page + strLine + "\n");
					}
				}				
				else if (discussionFlag){
					handlePageContent(strLine, trimmedStrLine, discussionWriter);
				}
				else{					
					handlePageContent(strLine, trimmedStrLine, articleWriter);
				}				
			}				
		}
		br.close();
		fs.close();
		
		articleWriter.append("</articles>");
		discussionWriter.append("</articles>");
		
		articleWriter.close();
		discussionWriter.close();		
		//System.out.println(total/count);
	}
	
	
	//public void handlePageContent(String strLine, String trimmedStrLine, FileWriter xml) throws IOException{		 
	public void handlePageContent(String strLine, String trimmedStrLine, OutputStreamWriter xml) throws IOException{
				
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text
			
			// text starts and ends at the same line
			if (trimmedStrLine.startsWith("<text")){ 		
				setIndent(strLine);
				strLine = cleanTextStart(trimmedStrLine, xml);				
			}			
			
			wikitext += StringUtils.replaceOnce(trimmedStrLine, "</text>", "") + "\n";		
			wikitext = StringUtils.replaceEach(wikitext, 
					new String[] { ":{|" , "/>" }, 
					new String[] { "{|" , " />" }); //start table notation
			
			matcher = pattern.matcher(wikitext);
			wikitext = matcher.replaceAll("< $1");		
				
			/*if(wikitext.contains("1,3-Butylenglycol")){
				System.out.println(wikitext);
			}*/
			
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
//				duration = endTime - startTime;
//				System.out.println("Sweble execution time "+duration);
				
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
		else if (trimmedStrLine.startsWith("<text")){ // start collecting text
			setIndent(strLine);
			wikitext += cleanTextStart(trimmedStrLine, xml);
			this.textFlag=true;
		}
		else{ // bypass page metadata		
			xml.append(strLine + "\n");
		}	
		
	}
	
	
	//private String cleanTextStart(String trimmedStrLine, FileWriter xml) throws IOException{		
	private String cleanTextStart(String trimmedStrLine, OutputStreamWriter xml) throws IOException{
		xml.append(this.indent + "<text>\n" );
		
		matcher = textPattern.matcher(trimmedStrLine);		
		return matcher.replaceFirst("") + "\n";		
	}
	
	private void setIndent(String strLine){
		if (this.indent.equals("")){
			this.indent = StringUtils.repeat(" ", strLine.indexOf("<"));
		}		
	}
	
	
	
	//private FileWriter createWriter (String outputFile) throws IOException {	
	private OutputStreamWriter createWriter (String outputFile) throws IOException {
		File file = new File(filepath+outputFile);		
		if (!file.exists()) file.createNewFile();		

		OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(file)), "UTF-8");		
		
		os.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		os.append("<articles>\n");
		return os;
		/*FileWriter fw = new FileWriter(file.getAbsoluteFile());
		fw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fw.append("<articles>\n");
		return fw;
		*/
	}
	
	
	private void setLanguageProperties(String language){
		if (language.equals("de")){			
			metapages.add("Media:");
			metapages.add("Spezial:");
			metapages.add("Benutzer:");
			metapages.add("Benutzer Diskussion:");
			metapages.add("Wikipedia:");
			metapages.add("Wikipedia Diskussion:");
			metapages.add("Datei:");
			metapages.add("Datei Diskussion:");
			metapages.add("MediaWiki:");
			metapages.add("MediaWiki Diskussion:");
			metapages.add("Vorlage:");
			metapages.add("Vorlage Diskussion:");
			metapages.add("Hilfe:");
			metapages.add("Hilfe Diskussion:");
			metapages.add("Kategorie:");
			metapages.add("Kategorie Diskussion:");
			metapages.add("Portal:");
			metapages.add("Portal Diskussion:");
			
			talk="Diskussion";
		}
		else if (language.equals("fr")){
			talk="Discussion";
		}
	}
	
	
	// Slower than pattern
	private String replaceLT(String wikitext){
		
		int i=0;
		Character next;
		StringBuilder sb = new StringBuilder(wikitext);
		
		while ((i=sb.indexOf("<",i)) != -1){
			next= sb.charAt(i+1);
			if (!Character.isLetter(next) && !Character.isWhitespace(next) ||
				!next.equals("/")){
				sb.insert(i + 1, ' ');
				i+=2;
			}
		}
			
		return sb.toString();
	}
}
