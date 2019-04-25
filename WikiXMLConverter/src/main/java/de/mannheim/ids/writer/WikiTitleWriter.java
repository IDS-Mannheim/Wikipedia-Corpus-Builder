package de.mannheim.ids.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.text.StringEscapeUtils;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.Utilities;

public class WikiTitleWriter {

	public XMLStreamWriter writer;
	
	public WikiTitleWriter(XMLStreamWriter w) {
		this.writer = w;
	}
	
	public WikiTitleWriter(Configuration config) throws IOException {
		
		String directory = "logs/title/";
		String filename = Paths.get(config.getWikidump()).getFileName()
				.toString();
		String outputFile = "titles-" + filename.substring(0, 15) + "-"
				+ config.getPageType() + ".xml";
		
		Utilities.createDirectory(directory);
		File file = new File(directory + "/" + outputFile);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream fos = null;
		fos = new FileOutputStream(file);

		XMLOutputFactory f = XMLOutputFactory.newInstance();
		XMLStreamWriter w = null;
		try {
			w = f.createXMLStreamWriter(
					new OutputStreamWriter(fos, config.getOutputEncoding()));
		}
		catch (UnsupportedEncodingException e) {
			try {
				fos.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		catch (XMLStreamException e) {
			System.err.println("Failed creating an XMLStreamWriter: "+e.getMessage());
		}
		
		writer = new IndentingXMLStreamWriter(w);
		try {
			writer.writeStartDocument();
			writer.writeStartElement("titles");
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public void indexTitle(String pagetitle, String pageId) {
		try {
			writer.writeStartElement("title");
			writer.writeAttribute("id", pageId);
			writer.writeCharacters(StringEscapeUtils.unescapeXml(pagetitle));
			writer.writeEndElement();
			writer.flush();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
		try {
			if (writer != null) {
				writer.writeEndElement(); // titles
				writer.writeEndDocument();
				writer.close();
			}
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
}
