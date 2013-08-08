package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.mannheim.ids.util.Utilities;
import de.mannheim.ids.util.WikiStatistics;

public class MultipleXMLWriter implements WikiXMLWriter{

	String xmlOutputDir, language;
	int counter;
	WikiStatistics wikiStatistics;
	
	public MultipleXMLWriter(String xmlOutputDir, String language, WikiStatistics wikiStatistics) {
		this.xmlOutputDir = xmlOutputDir;
		this.counter=1;
		this.wikiStatistics = wikiStatistics;
	}
	
	@Override
	public void write(WikiPage wikiPage, boolean isDiscussion, String indent)
			throws IOException {	
		
		OutputStreamWriter writer;
		String path;
		
		if (isDiscussion) path = this.xmlOutputDir+"/discussions/";		
		else path = this.xmlOutputDir+"/articles/";		
		writer = Utilities.createWriter(path + wikiPage.getPageIndex()+"/"+wikiPage.getPageId()+".xml");
		
		writePage(writer, wikiPage, indent);		
		writer.close();
	}
	
	public void writePage(OutputStreamWriter writer, WikiPage wikiPage, String indent) throws IOException {
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		if (!wikiPage.isEmpty()) {			
			System.out.println(this.counter++ +" "+ wikiPage.getPageTitle());					
				
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
	public void close() {}

}
