package de.mannheim.ids.wiki;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.util.WikiStatistics;

public class WikiTalkHandler {	
			
	private Pattern textPattern = Pattern.compile("<text.*\">(.*)");
	private Pattern levelPattern = Pattern.compile("^(:+)");	
	private Pattern headerPattern = Pattern.compile("^\'*(=+[^=]+=+)");
	private Pattern headerPattern2 = Pattern.compile("^\'*(&lt;h[0-9]&gt;.*&lt;/h[0-9]&gt;)");
	private Pattern timePattern = Pattern.compile( "\\s*([0-9]{2}:[^\\)]*\\))(.*)");
	private Pattern unsignedPattern = Pattern.compile("(.*)\\{\\{unsigned\\|([^\\|\\}]+)\\|?(.*)\\}\\}");
	private Pattern signaturePattern, specialContribution;
	
	private TagSoupParser tagSoupParser;
	private Sweble2Parser swebleParser;	
	private String posting="", language;
	private boolean textFlag, sigFlag;
	private WikiPage wikiPage;
	private WikiStatistics wikiStatistics;
	public WikiTalkUser user;
	public WikiTalkTime time;
	private String userLabel, contributionLabel;
//	long st=0;
//	long bla=0;
	
	public WikiTalkHandler(String language, String user, String contribution , WikiStatistics wikiStatistics) throws IOException {		
		signaturePattern = Pattern.compile("([^-]*-{0,2})\\s*\\[\\[:?"+user+":([^\\|]+)\\|([^\\]]+)\\]\\](.*)");
		specialContribution = Pattern.compile("(.*)\\[\\["+contribution+"/([^\\|]+)\\|[^\\]]+\\]\\](.*)");
		
		tagSoupParser = new TagSoupParser();
		swebleParser = new Sweble2Parser();
		this.language = language;
		this.wikiStatistics = wikiStatistics;		
		this.user= new WikiTalkUser(language+".wikipedia.org/wiki/"+user+":");
		this.time = new WikiTalkTime();
		
		this.userLabel = user;
		this.contributionLabel=contribution;
	} 
	
	protected void handleDiscussion(WikiPage wikiPage, String strLine, String trimmedStrLine) 
			throws IOException {
		this.wikiPage = wikiPage;
		
//		String str[]=null;
//		if (strLine.contains("&lt;br&gt;")){		
//			str = strLine.split("&lt;br&gt;");			
//			strLine = str[0];		
//		}
		
		if (trimmedStrLine.endsWith("</text>")){ // finish collecting text
			segmentPosting(strLine.replace("</text>", "") );			
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "","", posting.trim(),"");
				posting="";
			}
			wikiPage.pageStructure += "      <text/>\n";
			textFlag=false;			
			
//			try {				
////				long startTime = System.nanoTime();
//				wikiPage.wikitext = swebleParser.parseText(wikiPage.wikitext, wikiPage.getPageTitle(),language);
////				long endTime = System.nanoTime();
////				long duration = endTime - startTime;			
////				System.out.println("parsing "+duration);	
//			} catch (Exception e) {			
//				wikiStatistics.addSwebleErrors();
//				wikiStatistics.errorPages.add(wikiPage.getPageTitle());
//			}
			
//			System.out.println("segment  "+bla);			
//			long endTime = System.nanoTime();
//			long duration = endTime - st;
//			System.out.println("reading text "+duration);	
		}
		else if (textFlag){ // continue collecting text
//			long startTime = System.nanoTime();
			segmentPosting(strLine);
//			long endTime = System.nanoTime();
//			long duration = endTime - startTime;
//			bla+=duration;
		}
		else if(trimmedStrLine.startsWith("<text")) {
//			bla=0; st = System.nanoTime();
									
			if (trimmedStrLine.endsWith("/>")){ // empty text				
				wikiPage.pageStructure += "        <text lang=\""+language+"\"/>\n";
				wikiPage.wikitext="";
				wikiPage.setEmpty(true);				
			}
			else { // start collecting text				
				Matcher matcher = textPattern.matcher(trimmedStrLine);
				if (matcher.find()){
//					long startTime = System.nanoTime();
					segmentPosting(matcher.group(1));
//					long endTime = System.nanoTime();
//					long duration = endTime - startTime;
//					bla+=duration;
				}
				matcher.reset();
				this.textFlag=true;				
			}
		}
		else{ // copy page metadata
			wikiPage.pageStructure += strLine + "\n";
		}	
		
//		if (str !=null){
//			handleDiscussion(wikiPage, str[1], str[1].trim());
//		}		
	}	
	
	
	private void segmentPosting(String text) throws IOException {		
		
		String trimmedText = text.trim();		
		sigFlag=false;
		
		// Posting before a level marker 		
		if (trimmedText.startsWith(":") && !posting.trim().isEmpty()){
			writePosting("unknown", "", "", posting.trim(),"");
			posting="";
		}
		
		// Level Marker
		if (trimmedText.startsWith(":")){			
			writePosting("unknown", "", "", trimmedText,"");
			return;
		}
		
		// Line Marker		
		if (trimmedText.startsWith("---")){
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", "", posting.trim(),"");				
				posting="";
			}
			return;
		}
		
		// Header
		if (trimmedText.contains("=")){
			Matcher matcher = headerPattern.matcher(trimmedText);
			if (headerHandler(trimmedText, matcher)) return;
		}
		
		if (trimmedText.contains("&lt;h")){
			Matcher matcher = headerPattern2.matcher(trimmedText);	
			if (headerHandler(trimmedText, matcher)) return;
		}
		
		// User signature
		if (trimmedText.contains(this.userLabel)){
			if (handleSignature(trimmedText)) return;			
		}
		
		// Help signature
		if (trimmedText.contains(this.contributionLabel)){
			if (handleHelp(trimmedText)) return;			
		}
		
		// Unsigned
		if (trimmedText.contains("unsigned")){
			if (handleUnsigned(trimmedText)) return;			
		}
		
		//else posting+=trimmedText+"\n";			
		posting+=text+"\n";
		
		
	}
	
	private boolean handleSignature(String trimmedText) throws IOException{		 
		Matcher matcher = signaturePattern.matcher(trimmedText);
		if (matcher.find()){		
			String rest="", timestamp="";
			Matcher matcher2 = timePattern.matcher(matcher.group(4));	
			if (matcher2.find()){				
				timestamp=matcher2.group(1);
				rest = matcher2.group(2);
			}
			sigFlag=true;
			posting += matcher.group(1)+"\n";
			writePosting(matcher.group(3), matcher.group(2), timestamp, posting.trim(),rest.trim());
						
			matcher.reset();
			posting="";
			return true;
		}		
		return false;
	}
	
	private boolean handleHelp(String trimmedText) throws IOException{
		Matcher matcher = specialContribution.matcher(trimmedText);
		if (matcher.find()){			
			String timestamp="";
			Matcher matcher2 = timePattern.matcher(matcher.group(3));			
			if (matcher2.find()){
				timestamp = matcher2.group(1);				
			}						
			
			String temp = matcher.group(1);
			temp=temp.replace("&lt;small&gt;(''nicht [[Hilfe:Signatur|signierter]] Beitrag von''", "");			
			posting += temp+"\n";			
			writePosting(matcher.group(2), "", timestamp, posting.trim(),"");				
			
			matcher.reset();
			posting="";
			return true;
		}		
		return false;
	}
	
	private boolean handleUnsigned(String trimmedText) throws IOException{
		Matcher matcher = unsignedPattern.matcher(trimmedText);
		if (matcher.find()){	
			String timestamp="";			
			if (matcher.group(3) != null){				
				Matcher matcher2 = timePattern.matcher(matcher.group(3));			
				if (matcher2.find()){
					timestamp = matcher2.group(1);				
				}
			}			
			posting += matcher.group(1)+"\n";
			writePosting(matcher.group(2), "", timestamp, posting.trim(),"");
			
			matcher.reset();
			posting="";
			return true;
		}
		return false;	
	}
		
	private boolean headerHandler(String text, Matcher matcher) throws IOException{
		if (matcher.find()){
			if (!posting.trim().isEmpty()){
				writePosting("unknown", "", "", posting.trim(),"");
				posting="";
			}
		
			text = StringEscapeUtils.unescapeXml(WikiPageHandler.cleanTextStart(matcher.group(1))); // unescape XML tags
					
			wikiPage.wikitext+=parseToXML(text.trim())+"\n";
			matcher.reset();
			return true;
		}
		return false;		
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
			posting = swebleParser.parseText(posting, wikiPage.getPageTitle(),language);
		} catch (Exception e) {			
			wikiStatistics.addSwebleErrors();
			wikiStatistics.errorPages.add(wikiPage.getPageTitle());
		}
		return posting;
	}
	
	private void writePosting(String speaker, String speakerLabel, String timestamp, String posting,String postscript) throws IOException{
		
		if (posting.isEmpty()) return;		
		else wikiStatistics.addTotalPostings();
		
		int level = identifyLevel(posting.trim());
		if (level > 0){ 
			posting = posting.substring(level,posting.length()); 
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("        <Posting indentLevel=\""+level+"\"");
		
		if (!speaker.isEmpty()){			
			sb.append(" who=\""+user.getTalkUser(speaker,speakerLabel,sigFlag)+"\"");
			if (!speaker.equals("unknown")) posting += "<autoSignature/>";
		} 
		
		if (!timestamp.isEmpty()){
			sb.append(" timestamp=\""+time.getTimeId(timestamp)+"\"");
		}
		sb.append(">\n");						
				
		sb.append(parseToXML(posting)+"\n");
		
		//System.out.println("parsing "+duration);
		if (postscript.toLowerCase().startsWith("ps") || postscript.toLowerCase().startsWith("p.s")){
			//System.out.println("postscript "+postscript);
			sb.append("<seg type=\"postscript\">");
			sb.append(postscript);
			sb.append("</seg>\n");
		}
		
		sb.append("        </Posting>\n");	
		wikiPage.wikitext+=sb.toString();
	}
	
}
