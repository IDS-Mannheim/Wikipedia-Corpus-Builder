package de.mannheim.ids.wiki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.util.WikiStatistics;

public class WikiPageHandler {

	private static Pattern pattern =  Pattern.compile("<([^!!/a-zA-Z\\s])");
	private static Pattern stylePattern = Pattern.compile("(\\[\\[.+>\\]\\])");
	private static Pattern textPattern = Pattern.compile("<text.*\">");
	
	private TagSoupParser tagSoupParser;
	private Sweble2Parser swebleParser;
	private DOMParser dp;
	
	private boolean textFlag;
	private String language;
	private WikiStatistics wikiStatistics;
	
	public WikiPageHandler(String language, WikiStatistics wikiStatistics) {
		tagSoupParser = new TagSoupParser();
		swebleParser = new Sweble2Parser();
		dp = new DOMParser();
		
		this.language=language;
		this.wikiStatistics=wikiStatistics;
	}
	
	public void handlePageContent(WikiPage wikiPage, String strLine, String trimmedStrLine) 
			throws IOException {
		
		// Finish collecting text
		if (trimmedStrLine.endsWith("</text>")){ 
			if (trimmedStrLine.startsWith("<text")){ // text starts and ends at the same line
				trimmedStrLine = cleanTextStart(trimmedStrLine);				
			}						
			trimmedStrLine = StringUtils.replaceOnce(trimmedStrLine, "</text>", ""); // remove </text>
			
			String tempText = wikiPage.wikitext;
			tempText += (StringEscapeUtils.unescapeXml(trimmedStrLine) + "\n").trim(); // unescape XML tags
			if (tempText.equals("")){ // empty text
				wikiPage.setEmpty(true); 
				return; 
			} 
			tempText = cleanPattern(tempText);
			
			wikiPage.wikitext= parseToXML(tempText, wikiPage.getPageTitle());
			wikiPage.pageStructure += "      <text/>\n";
			textFlag=false;
		}
		
		// Continue collecting text
		else if (textFlag){ 
			strLine = StringEscapeUtils.unescapeXml(strLine); // unescape XML tags			
			wikiPage.wikitext += strLine+"\n";			
		}		
		
		else if(trimmedStrLine.startsWith("<text")) {				
			
			if (trimmedStrLine.endsWith("/>")){ // empty text				
				wikiPage.pageStructure += "        <text lang=\""+language+"\"/>\n";
				wikiPage.wikitext="";
				wikiPage.setEmpty(true);
			}
			else { // start collecting text
				wikiPage.wikitext += StringEscapeUtils.unescapeXml(cleanTextStart(trimmedStrLine)); // unescape XML tags
				this.textFlag=true;
			}
		}
		else{ // copy page metadata			
			wikiPage.pageStructure += strLine + "\n";
		}	

	}
	
	private String parseToXML(String wikitext, String pagetitle){
		try{
			// italic and bold are not repaired because they have wiki-mark-ups
			wikitext = tagSoupParser.generate(wikitext,true);				
			wikitext = swebleParser.parseText(wikitext.trim(), pagetitle);
		}catch (Exception e) {
			wikiStatistics.addSwebleErrors();
			wikiStatistics.errorPages.add(pagetitle);
			wikitext="";
		}
		return wikitext;
	}
	
	public static String cleanPattern(String wikitext){		 
		wikitext = StringUtils.replaceEach(wikitext, 
				new String[] { ":{|" , "<br/>", "<br />"}, 
				new String[] { "{|" , "&lt;br/&gt;", "&lt;br /&gt;"}); //start table notation	
	
		Matcher matcher = pattern.matcher(wikitext); // space for non-tag			
		wikitext = matcher.replaceAll("&lt; $1");			
		matcher.reset();
		
		matcher = stylePattern.matcher(wikitext); // escape for style containing tag
		StringBuffer sb = new StringBuffer();
        while(matcher.find()){
        	String replace = StringEscapeUtils.escapeHtml(matcher.group(1));
        	replace = matcher.quoteReplacement(replace);
        	matcher.appendReplacement(sb,replace);
        }
        matcher.appendTail(sb);		        
	    wikitext=sb.toString();    
	    return wikitext;
	}
	
	public static String cleanTextStart(String trimmedStrLine) throws IOException{
		Matcher matcher = textPattern.matcher(trimmedStrLine);		
		return matcher.replaceFirst("")+"\n";		
	}
	
	public void validateXML(WikiPage wikiPage) {
		
		try{ //test XML validity
			String t = "<text>"+wikiPage.wikitext+"</text>";
			dp.parseXML(new ByteArrayInputStream(t.getBytes("utf-8")));				
		}
		catch (Exception e) {			
			wikiStatistics.addParsingErrors();
			wikiStatistics.errorPages.add("DOM "+wikiPage.getPageTitle());
			wikiPage.wikitext="";				
		}
		
		try { 
			wikiPage.pageStructure = tagSoupParser.generate(wikiPage.pageStructure, false);
		} 
		catch (Exception e) { 
			e.printStackTrace(); 
		}		
	}
}
