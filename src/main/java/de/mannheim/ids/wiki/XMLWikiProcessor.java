package de.mannheim.ids.wiki;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class XMLWikiProcessor {
	
	private String wikitext="", language;
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
		this.language=language;
		setLanguageProperties(language);
	}
	
	public void process(String inputFile, String articleOutput, String discussionOutput) throws IOException{	
		
		int counter=1;
		boolean readFlag = false, discussionFlag=false, isArticle=true;	
		String strLine, trimmedStrLine, page="";

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
				
				strLine = StringEscapeUtils.unescapeXml(strLine); // unescape XML tags		
																
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
							trimmedStrLine = cleanElement("<title>" , trimmedStrLine, "</title>");
							if(strLine.contains(talk)){								
								discussionWriter.append(page + trimmedStrLine + "\n");
								discussionFlag = true;							
							}
							else {
								articleWriter.append(page + trimmedStrLine);
							}							
						}
						else{
							isArticle=true;
						}					
					}
					else{
						System.out.println(counter++ + strLine);	
						trimmedStrLine = cleanElement("<title>" , trimmedStrLine, "</title>");
						articleWriter.append(page + trimmedStrLine);
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
	
		 
	public void handlePageContent(String strLine, String trimmedStrLine, OutputStreamWriter xml) throws IOException{
				
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text
			
			// text starts and ends at the same line
			if (trimmedStrLine.startsWith("<text")){ 		
				xml.append(setIndent(strLine) + "<text lang=\""+this.language+"\">\n" );
				trimmedStrLine = cleanTextStart(trimmedStrLine, xml);				
			}			
			
			wikitext += StringUtils.replaceOnce(trimmedStrLine, "</text>", "") + "\n";		
			/*wikitext = StringUtils.replaceEach(wikitext, 
					new String[] { ":{|" , "/>", "&amp;nbsp;" }, 
					new String[] { "{|" , " />", " "}); //start table notation
*/			
			wikitext = StringUtils.replaceEach(wikitext, 
					new String[] { ":{|" , "/>"}, 
					new String[] { "{|" , " />"}); //start table notation
			
			matcher = pattern.matcher(wikitext);
			wikitext = matcher.replaceAll("< $1");					
			
			try{			
				//startTime = System.nanoTime();								
				wikitext = tagSoupParser.generateCleanHTML(wikitext);				
				//endTime = System.nanoTime();
				//duration = endTime - startTime;
				//System.out.println("Tagsoup execution time "+duration);				
								
				//if(wikitext.contains("Auteur-Theorie"))
				//	System.out.println(wikitext);				
												
				//startTime = System.nanoTime();
				xml.append(swebleParser.parseText(wikitext));
				//endTime = System.nanoTime();
//				duration = endTime - startTime;
//				System.out.println("Sweble execution time "+duration);
				
				xml.append(setIndent(strLine) +"</text>\n");
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
			xml.append(setIndent(strLine) + "<text lang=\""+this.language+"\">\n" );
			wikitext += cleanTextStart(trimmedStrLine, xml);
			this.textFlag=true;
		}
		else if (trimmedStrLine.startsWith("<comment>")){		
			trimmedStrLine = cleanElement("<comment>" , trimmedStrLine, "</comment>");				
			xml.append(setIndent(strLine) + trimmedStrLine);			
		}
		else if (trimmedStrLine.startsWith("<username>")){		
			trimmedStrLine = cleanElement("<username>" , trimmedStrLine, "</username>");				
			xml.append(setIndent(strLine) + trimmedStrLine);			
		}
		else if (trimmedStrLine.startsWith("<redirect title=")){
			trimmedStrLine = cleanElement("<redirect title=\">" , trimmedStrLine, "\"/>");
			xml.append(setIndent(strLine) + trimmedStrLine);	
		}
		else{ // bypass page metadata
			xml.append(strLine + "\n");
		}	
		
	}	
		
	private String cleanTextStart(String trimmedStrLine, OutputStreamWriter xml) throws IOException{
		matcher = textPattern.matcher(trimmedStrLine);		
		return matcher.replaceFirst("") + "\n";		
	}	
	
	private String cleanElement(String open, String content, String close){
		content = StringUtils.replaceEach(content, 
		new String[] { open , close}, 
		new String[] { "" , "" });	
		content = de.fau.cs.osr.utils.StringUtils.escHtml(content); 
		return "    "+open + content + close +"\n";		
	}
	
	
	private String setIndent(String strLine){		
		return StringUtils.repeat(" ", strLine.indexOf("<"));				
	}
	
	
	
	private OutputStreamWriter createWriter (String outputFile) throws IOException {
		File file = new File(filepath+outputFile);		
		if (!file.exists()) file.createNewFile();		

		OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(file)), "UTF-8");		
		
		os.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		os.append("<articles>\n");
		return os;	
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
			metapages.add("Média:");
			metapages.add("Spécial:");
			metapages.add("Utilisateur:");
			metapages.add("Discussion utilisateur:");
			metapages.add("Wikipédia:");
			metapages.add("Discussion Wikipédia:");
			metapages.add("Fichier:");
			metapages.add("Discussion fichier:");
			metapages.add("MediaWiki:");
			metapages.add("Discussion MediaWiki:");
			metapages.add("Modèle:");
			metapages.add("Discussion modèle:");
			metapages.add("Aide:");
			metapages.add("Discussion aide:");
			metapages.add("Catégorie:");
			metapages.add("Discussion catégorie:");
			metapages.add("Portail:");
			metapages.add("Discussion Portail:");
			metapages.add("Projet:");
			metapages.add("Discussion Projet:");
			metapages.add("Référence:");
			metapages.add("Discussion Référence:");
			
			talk="Discussion";
		}
	}
	

}
