package de.mannheim.ids.wiki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NekoHTMLParser {
	
	 static StringWriter writer = new StringWriter();
     static Transformer transformer;


     public String generate(String wiki) throws SAXException, IOException, TransformerException{
    	 //DOMParser parser = new DOMParser();
		 HTMLConfiguration HtmlConfig = new HTMLConfiguration();
		 HtmlConfig.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		 StringWriter s = new StringWriter();
		 DOMParser parser = new DOMParser(HtmlConfig);
		 InputSource is = new InputSource();
		 
		 //for (String p : wiki.split("\n\n")){					 
			 is.setCharacterStream(new StringReader(wiki));	        
			 parser.parse(is);			 		         
			 printDocument(parser.getDocument(),s);				
		 //}       
		 String cleanWikitext = StringUtils.replaceEach(s.toString(), 
				new String[] {"<html>", "<body>", "<head xmlns=\"http://www.w3.org/1999/xhtml\"/>", "</body>","</html>"}, 
				new String[] {"", "", "", "", "\n"});	
		 
		 return cleanWikitext.trim();        
    }
     
    public static void print(Node node, String indent) {
        //System.out.println(indent+node.getClass().getName());
        Node child = node.getFirstChild();        
        //System.out.println(node.getNodeName());
        //System.out.println(node.getTextContent());
        while (child != null) {
            print(child, indent+" ");
            child = child.getNextSibling();            
        }
    }
    
	public static void printDocument(Document doc, StringWriter out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");        

        transformer.transform(new DOMSource(doc), new StreamResult(out));
    }
}
