package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.mannheim.ids.util.Utilities;

public class WikiTalkTime {

	private OutputStreamWriter timeWriter; 
	private int counter;
	
	public WikiTalkTime() throws IOException{	
		timeWriter = Utilities.createWriter("talk-timeline.xml");
		counter=0;
		
		timeWriter.append("<timeline>\n");		
	}	
	
	public String getTimeId(String timeline) throws IOException{
		String timeId = generateTimeId();
		timeWriter.append("   <when xml:id=\""+timeId+"\"");
		timeWriter.append(" absolute=\""+timeline+"\"/>\n");
		return timeId;
	}
	
	private String generateTimeId() {
		String timeId = "t"+String.format("%08d", counter);
		counter++;
		return timeId;
	}		
	
	public void closeWriter() throws IOException{
		timeWriter.append("</timeline>\n");
		timeWriter.close();
	}
	
	
}
