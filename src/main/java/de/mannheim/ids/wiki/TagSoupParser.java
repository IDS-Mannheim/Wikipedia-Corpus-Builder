package de.mannheim.ids.wiki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.XMLWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/* This class generate a clean HTML from the wikitext containing improper HTML tags 
 * (e.g the tags do not opened or closed properly) using TagSoup library. TagSoup parser
 * removes a close tag without an open tag, and generates a missing close tag for an open 
 * without a close tag. However, the part of the text nested by the tags may not be correct.
 */
public class TagSoupParser {
	
	private HTMLSchema theSchema = null;	
		 
	public String generateCleanHTML(String wikitext) throws IOException, SAXException{		
		theSchema = new HTMLSchema();
		
		XMLReader r = new Parser();			
		r.setFeature(Parser.namespacesFeature, false); // omit namespace
		r.setProperty(Parser.schemaProperty, theSchema);			
		
		Writer w = new StringWriter();
		ContentHandler h = chooseContentHandler(w);
		r.setContentHandler(h);
		
		// Do process per paragraph because the correction of improper tags  
		// will be accumulated and repeated until the end of the given text.
		for (String p : wikitext.split("\n\n")){
			//System.out.println(p);
			r.parse(new InputSource( new ByteArrayInputStream(p.getBytes())));
		}
		
		String cleanWikitext = w.toString();
		cleanWikitext = StringUtils.replaceEach(cleanWikitext, 
				new String[] {"<html><body>", "</body></html>"}, 
				new String[] {"", "\n"});		
		cleanWikitext = StringEscapeUtils.unescapeHtml(cleanWikitext);
		//System.out.println(cleanWikitext);
		return cleanWikitext;
		
	}
	
	private ContentHandler chooseContentHandler(Writer w) {
		XMLWriter x = new XMLWriter(w);		
		x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");		
		x.setOutputProperty(XMLWriter.ENCODING, "utf-8");
		x.setPrefix(theSchema.getURI(), "");
		return x;
	}
	
}
