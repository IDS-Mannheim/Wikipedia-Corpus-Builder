package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.xml.sax.SAXException;

public class WikiDiscussionHandler {
	private XMLWikiProcessor xmlWikiProcessor;
	private String posting="";
	private boolean textFlag;
	
	private TagSoupParser tagSoupParser = new TagSoupParser();
	private Sweble2Parser swebleParser = new Sweble2Parser();
	
	protected Matcher matcher;
	private Pattern textPattern = Pattern.compile("<text.*\">(.*)");
	private Pattern levelPattern = Pattern.compile("^(:+)");
	
	private Pattern headerPattern = Pattern.compile("^\'*(=+[^=]+=+)");
	private Pattern timePattern = Pattern.compile( "\\s*([0-9]{2}:[^\\)]*\\))(.*)");	
	private Pattern signaturePattern, specialContribution;
	
		
	public WikiDiscussionHandler(XMLWikiProcessor xWikiProcessor) {
		this.xmlWikiProcessor = xWikiProcessor;
		
		String user = xmlWikiProcessor.languageSetter.getUser();	
		String ctr =  xmlWikiProcessor.languageSetter.getContribution();
		signaturePattern = Pattern.compile("([^-]*)-{0,2}\\s*\\[\\[:?"+user+":[^\\|]+\\|([^\\]]+)\\]\\](.*)");
		specialContribution = Pattern.compile("(.*)\\[\\["+ctr+"/([^\\|]+)\\|[^\\]]+\\]\\](.*)");
		
	} 
	
	protected void handleDiscussion(String strLine, String trimmedStrLine, 
			OutputStreamWriter errorWriter) throws IOException {
				
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text
			segmentPosting( strLine.replace("</text>", "") );			
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", posting.trim());
				posting="";
			}
			xmlWikiProcessor.page += "      <text/>\n";
			textFlag=false;
		}
		else if (textFlag){ // continue collecting text
			//strLine = StringEscapeUtils.unescapeXml(strLine); // unescape XML tags
			//wikitext += strLine + "\n";			
			segmentPosting(strLine);
		}
		else if(trimmedStrLine.startsWith("<text")) {	
			if (trimmedStrLine.endsWith("/>")){ // empty text				
				xmlWikiProcessor.page += "        <text lang=\""+xmlWikiProcessor.languageSetter.getLanguage()+"\"/>\n";
				xmlWikiProcessor.wikitext="";
				xmlWikiProcessor.isEmptyText=true;
			}
			else { // start collecting text
				//wikitext += StringEscapeUtils.unescapeXml(xmlWikiProcessor.cleanTextStart(trimmedStrLine)); // unescape XML tags				
				matcher = textPattern.matcher(trimmedStrLine);
				if (matcher.find()){					
					segmentPosting(matcher.group(1));					
				}
				matcher.reset();
				this.textFlag=true;
			}
		}
		else{ // copy page metadata			
			xmlWikiProcessor.page += strLine + "\n";
		}	
		
	}	
	
	
	private void segmentPosting(String text) throws IOException {
		String trimmedText = text.trim();
		Matcher matcher2;
		
		// Posting before a level marker 		
		if (trimmedText.startsWith(":") && !posting.trim().isEmpty()){
			writePosting("unknown", "", posting.trim());
			posting="";
		}
		
		// User signature
		matcher = signaturePattern.matcher(trimmedText);
		if (matcher.find()){		
			String rest="", timestamp="";
			matcher2 = timePattern.matcher(matcher.group(3));	
			if (matcher2.find()){				
				timestamp=matcher2.group(1);
								
				rest = matcher2.group(2);
			}
			
			posting += matcher.group(1)+"\n";
			writePosting(matcher.group(2), timestamp, posting.trim());
						
			matcher.reset();
			posting="";			
			//if (!rest.isEmpty() || rest!=null){ posting = rest; }			
			return;
		}
		
		// Help signature
		matcher = specialContribution.matcher(trimmedText);
		if (matcher.find()){			
			String timestamp="";
			matcher2 = timePattern.matcher(matcher.group(3));			
			if (matcher2.find()){
				timestamp = matcher2.group(1);				
			}						
			
			String temp = matcher.group(1);
			temp=temp.replace("&lt;small&gt;(''nicht [[Hilfe:Signatur|signierter]] Beitrag von''", "");			
			posting += temp+"\n";			
			writePosting(matcher.group(2), timestamp, posting.trim());				
			
			matcher.reset();
			posting="";
			return;
		}		
		
		// Level Marker
		if (trimmedText.startsWith(":")){			
			writePosting("unknown", "", trimmedText);
			return;
		}
		
		// Header
		matcher = headerPattern.matcher(trimmedText);
		if (matcher.find()){
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", posting.trim());
				posting="";
			}
			
			text = StringEscapeUtils.unescapeXml(xmlWikiProcessor.cleanTextStart(matcher.group(1))); // unescape XML tags
			xmlWikiProcessor.wikitext+=parseToXML(text)+"\n";
			matcher.reset();			
		}		
		
		// Line Marker		
		else if (trimmedText.startsWith("---")){
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", posting.trim());				
				posting="";
			}			
		}		 
		
		else posting+=text+"\n";			
		
	}
	
	private int identifyLevel(String posting){
		
		Matcher matcher = levelPattern.matcher(posting);
		if (matcher.find()){
			return matcher.group(1).length();
		}		
		return 0;
	}
	
	private String parseToXML(String posting) {		
		
		posting = StringEscapeUtils.unescapeXml(posting); // unescape XML tags 
		posting = xmlWikiProcessor.cleanPattern(posting);	
		
		try {
			posting = tagSoupParser.generate(posting,true);
			posting = swebleParser.parseText(posting, xmlWikiProcessor.pagetitle);
		} catch (JAXBException | CompilerException | LinkTargetException | IOException | SAXException e) {				
			e.printStackTrace();
			/*errorWriter.append(pagetitle+"\n");
			wikiStatistics.addSwebleErrors();*/
		}
		return posting;
	}
	
	private void writePosting(String speaker, String timestamp, String posting){
				
		int level = identifyLevel(posting.trim());
		if (level > 0){ 
			posting = posting.substring(level,posting.length()); 
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("        <Posting level=\""+level+"\"");
		
		if (!speaker.isEmpty()){
			sb.append(" who=\""+speaker+"\"");			
		} 
		
		if (!timestamp.isEmpty()){
			sb.append(" timestamp=\""+timestamp+"\"");
		}
		sb.append(">\n");						
		
		sb.append(parseToXML(posting)+"\n");
		sb.append("        </Posting>\n");	
		
		xmlWikiProcessor.wikitext+=sb.toString();
	}
	
}
