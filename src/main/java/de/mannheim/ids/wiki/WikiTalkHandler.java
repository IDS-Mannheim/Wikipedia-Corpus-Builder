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

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.util.WikiStatistics;

public class WikiTalkHandler {	
			
	private Pattern textPattern = Pattern.compile("<text.*\">(.*)");
	private Pattern levelPattern = Pattern.compile("^(:+)");	
	private Pattern headerPattern = Pattern.compile("^\'*(=+[^=]+=+)");
	private Pattern timePattern = Pattern.compile( "\\s*([0-9]{2}:[^\\)]*\\))(.*)");	
	private Pattern signaturePattern, specialContribution;
	
	private TagSoupParser tagSoupParser;
	private Sweble2Parser swebleParser;	
	private String posting="", language;
	private boolean textFlag;
	private WikiPage wikiPage;
	private WikiStatistics wikiStatistics;
		
	public WikiTalkHandler(String language, String user, String contribution , WikiStatistics wikiStatistics) {		
		signaturePattern = Pattern.compile("([^-]*)-{0,2}\\s*\\[\\[:?"+user+":[^\\|]+\\|([^\\]]+)\\]\\](.*)");
		specialContribution = Pattern.compile("(.*)\\[\\["+contribution+"/([^\\|]+)\\|[^\\]]+\\]\\](.*)");
		
		tagSoupParser = new TagSoupParser();
		swebleParser = new Sweble2Parser();
		this.language = language;
		this.wikiStatistics = wikiStatistics;
	} 
	
	protected void handleDiscussion(WikiPage wikiPage, String strLine, String trimmedStrLine) 
			throws IOException {
		this.wikiPage = wikiPage;
		
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text
			segmentPosting(strLine.replace("</text>", "") );			
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", posting.trim());
				posting="";
			}
			wikiPage.pageStructure += "      <text/>\n";
			textFlag=false;
		}
		else if (textFlag){ // continue collecting text			
			segmentPosting(strLine);
		}
		else if(trimmedStrLine.startsWith("<text")) {	
			if (trimmedStrLine.endsWith("/>")){ // empty text				
				wikiPage.pageStructure += "        <text lang=\""+language+"\"/>\n";
				wikiPage.wikitext="";
				wikiPage.setEmpty(true);
			}
			else { // start collecting text				
				Matcher matcher = textPattern.matcher(trimmedStrLine);
				if (matcher.find()){					
					segmentPosting(matcher.group(1));					
				}
				matcher.reset();
				this.textFlag=true;				
			}
		}
		else{ // copy page metadata			
			wikiPage.pageStructure += strLine + "\n";
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
		Matcher matcher = signaturePattern.matcher(trimmedText);
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
			
			text = StringEscapeUtils.unescapeXml(WikiPageHandler.cleanTextStart(matcher.group(1))); // unescape XML tags
			wikiPage.wikitext+=parseToXML(text.trim())+"\n";
			matcher.reset();			
		}		
		
		// Line Marker		
		else if (trimmedText.startsWith("---")){
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", posting.trim());				
				posting="";
			}			
		}
		
		else posting+=trimmedText+"\n";			
		
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
		posting = WikiPageHandler.cleanPattern(posting);		
		
		try {
			posting = tagSoupParser.generate(posting,true);
			posting = swebleParser.parseText(posting, wikiPage.getPageTitle());
		} catch (JAXBException | CompilerException | LinkTargetException | IOException | SAXException e) {				
			e.printStackTrace();			
			wikiStatistics.addSwebleErrors();
			wikiStatistics.errorPages.add(wikiPage.getPageTitle());
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
		
		wikiPage.wikitext+=sb.toString();
	}
	
}
