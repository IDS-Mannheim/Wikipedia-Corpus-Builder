package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.mannheim.ids.util.Utilities;
import de.mannheim.ids.util.WikiStatistics;

public class SingleXMLWriter implements WikiXMLWriter {

	int counter;
	private String language;	
	private WikiStatistics wikiStatistics;
	private OutputStreamWriter articleWriter, discussionWriter;
	
	public SingleXMLWriter(String xmlOutputDir, String language, WikiStatistics wikiStatistics) {
		this.language = language;
		this.wikiStatistics = wikiStatistics;
		this.counter=1;
		
		try {
			articleWriter = Utilities.createWriter(xmlOutputDir+"/wiki-articles.xml");		
			discussionWriter = Utilities.createWriter(xmlOutputDir+"/wiki-discussions.xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	@Override
	public void write(WikiPage wikiPage, boolean isDiscussion, String indent)
			throws IOException {		
		
		if (isDiscussion)
			writePage(discussionWriter, wikiPage, indent);		
		else
			writePage(articleWriter, wikiPage, indent);
	}
	
	public void writePage(OutputStreamWriter writer, WikiPage wikiPage, 
			String indent) throws IOException {
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		if (!wikiPage.isEmpty()) {			
			System.out.println(this.counter++ +" "+ wikiPage.getPageTitle());						
//				String [] arr = page.split("<text/>");				
			String [] arr = wikiPage.pageStructure.split("<text></text>");
			//System.out.println(wikiPage.pageStructure);
			if (arr.length >1){				
				writer.append(indent);
				writer.append(arr[0]);	
				
				if (wikiPage.wikitext.equals("")){
					writer.append("<text lang=\""+language+"\"/>" );
				}
				else {
					writer.append("<text lang=\""+language+"\">\n" );
					writer.append(wikiPage.wikitext+"\n");
					writer.append("      </text>");
				}
				
				writer.append(arr[1]);				
			}
			else{ //throw new ArrayIndexOutOfBoundsException();								
				System.out.println("Outer Error: "+wikiPage.getPageTitle());
				wikiStatistics.addPageStructureErrors();
			} 
		}
		else{
			writer.append(wikiPage.pageStructure);
		}
	}
	
	@Override
	public void close() throws IOException{		
		articleWriter.close();
		discussionWriter.close();		
	}
}
