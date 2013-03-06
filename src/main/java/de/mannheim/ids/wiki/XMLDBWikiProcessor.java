package de.mannheim.ids.wiki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/** This class converts Wikipages from Database into XML using JWPL API  
 * Problem: Jwpl does not parse all meta-elements from a wikipage. 
 * 
 * 
 * @author margaretha
 * @version 1.0 Build Mar 2013
 */
public class XMLDBWikiProcessor implements WikiConstants	{
	
	private String xmlOutputDir = "./xml/";
	private String articleDir = this.xmlOutputDir+"articles/";
	private String discussionDir = this.xmlOutputDir+"discussions/";
		
	private Matcher matcher;
	private Pattern pattern =  Pattern.compile("<([^!!/a-zA-Z\\s])");
	private Pattern hackpattern = Pattern.compile("<([^>]+)/>");
	
	private TagSoupParser tagSoupParser = new TagSoupParser();
	private Sweble2Parser swebleParser = new Sweble2Parser();
	private DOMParser dp = new DOMParser();
	
	private WikiStatistics wikiStatistics;
	private Helper helper;

	private List<String> indexList;
	private int counter = 1;
	private String language;
	
	public XMLDBWikiProcessor(String lang) {
		this.language=lang;
		this.wikiStatistics= new WikiStatistics();	
		this.helper = new Helper();
		
		helper.createDirectory(xmlOutputDir);
		
		this.indexList = helper.createIndexList();
		for (String i:indexList) {
			helper.createDirectory(this.articleDir+i);
			helper.createDirectory(this.discussionDir+i);
		}
	}		
	
    public void process(DatabaseConfiguration dbConfig, String errorOutput) throws WikiApiException, IOException    {    	
    	
        Wikipedia wiki = new Wikipedia(dbConfig);
        Iterable<Page> pages = wiki.getPages();
        
        OutputStreamWriter writer, errorWriter = helper.createWriter(errorOutput);            	
        
        int pageid;
        int start = "Discussion:".length();
        String pagetitle, pagetext, wikitext;
        
        for (Page page : pages){
        	pagetitle = page.getTitle().toString();
			pageid = page.getPageId();
			pagetext = page.getText();
			wikitext = parse(pagetitle, page.getText(), errorWriter);
			
			if (pagetitle.startsWith("Discussion:")){				
				writer = createWriter(pageid, pagetitle.substring(start, start+1), this.discussionDir);
				
				if (!wikitext.isEmpty()){ wikiStatistics.addTotalDiscussions(); }
				else if (pagetext.isEmpty()){ wikiStatistics.addEmptyDiscussions(); }
				else { wikiStatistics.addEmptyParsedDiscussions(); }
			}
			else {				
				writer = createWriter(pageid, pagetitle.substring(0,1), this.articleDir);
				
				if (!wikitext.isEmpty()){ wikiStatistics.addTotalArticles(); }
				else if (pagetext.isEmpty()){ wikiStatistics.addEmptyArticles(); }
				else { wikiStatistics.addEmptyParsedArticles(); }
			}
						
			write(writer,pagetitle,pageid,wikitext);        	 
        }
        
        errorWriter.close();		
		wikiStatistics.printStatistics();		
    }
    
    private void write(OutputStreamWriter writer, String pagetitle, int pageid, String wikitext) throws IOException{    	
    			
    	System.out.println(counter++ +" "+ pagetitle);
    	
    	writer.append("<page>\n");
    	writer.append("  <title>"); 
    	writer.append(pagetitle); 
    	writer.append("</title>\n");
    	writer.append("  <id>"+pageid); 
    	writer.append("</id>\n");
    	writer.append("  <revision>\n");  
    	
    	if (!wikitext.isEmpty()){
    		writer.append("    <text lang=\""+this.language+"\">\n" );
			writer.append(wikitext+"\n");
			writer.append("    </text>\n");
		}
		else {
			writer.append("<text lang=\""+this.language+"\"/>\n" );			
		}
    	
    	writer.append("  </revision>\n");
    	writer.append("</page>");
    	
    	writer.close();
    }
    
	private OutputStreamWriter createWriter(int pageid, String index, String dir) throws IOException{
		String normalizedIndex = helper.normalizeIndex(index, this.indexList);	
		OutputStreamWriter writer = helper.createWriter(dir + normalizedIndex+"/"+pageid+".xml"); 
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		return writer;
	}
	
	private String parse(String pagetitle, String wikitext, OutputStreamWriter errorWriter) throws IOException{
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
		
		return wikitext;
	}
	
}
