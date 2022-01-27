package de.mannheim.ids.wiki;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IndexingTest {

    public static void testIndexingWikiXML(String type, String language)
            throws IOException, InterruptedException,
            ParserConfigurationException, SAXException, I5Exception {
        String xmlFolder = "wikixml-"+language+"/" + type + "/";
        String index = "index/"+language+"wiki-" + type + "-index.xml";

        Process p = Runtime.getRuntime().exec(
                "./WikiXMLCorpusIndexer.sh " + type + " " + xmlFolder + " "
                        + index);
        p.waitFor();

        File f = new File(index);
        assertTrue(f.exists());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document doc = builder.parse(f);
        NodeList list = doc.getElementsByTagName("index");
        assertTrue(list.getLength() > 0);
    }
}
