package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.mannheim.ids.util.Utilities;

/** This class writes an XML file for each XML-ized wiki page.
 * 
 * @author margaretha
 *
 */
public class MultipleXMLWriter{

	private String xmlOutputDir, language;
	private String encoding;
	
	public MultipleXMLWriter(Configuration config) {
		
		if (config == null){
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		
		this.xmlOutputDir = config.getOutputFolder();
		this.language = config.getLanguageCode();
		this.encoding = config.getOutputEncoding();
	}
	
	public void write(WikiPage wikiPage) throws IOException {	
		
		if (wikiPage==null){
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}
		
		OutputStreamWriter writer;
		
		if (!wikiPage.isTextEmpty() && !wikiPage.wikitext.isEmpty()) {		
			
			writer = Utilities.createWriter(xmlOutputDir + "/" + wikiPage.getPageIndex() + "/", 
					wikiPage.getPageId()+".xml", this.encoding);
			
			System.out.println(xmlOutputDir + "/"+ wikiPage.getPageIndex()+
					"/"+wikiPage.getPageId()+".xml");
			
			writer.append("<?xml version=\"1.0\" encoding=\"");
			writer.append(this.encoding);
			writer.append("\"?>\n");
			
			String [] arr = wikiPage.pageStructure.split("<text></text>");			
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
			writer.close();
		}		
	}
}
