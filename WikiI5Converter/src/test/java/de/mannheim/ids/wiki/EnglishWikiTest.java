package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.mannheim.ids.transform.TaskRunner;
import de.mannheim.ids.transform.Transformer;

public class EnglishWikiTest {
    private Configuration config;
    private I5ErrorHandler errorHandler;
    private Statistics statistics;

    public EnglishWikiTest () throws ParseException, IOException, I5Exception {
        config = WikiI5Converter.createConfig(new String[] { "-prop",
                "enwiki-article.properties", "-storeCategories" });
        errorHandler = new I5ErrorHandler(config);
        statistics = new Statistics();
        
        Transformer.resetTransformer(config);
        new WikiI5Processor(config);
    }

//    @Test
//    public void testCategories () throws Exception {
//        TaskRunner t = new TaskRunner(config, errorHandler, statistics,
//                "C/867837.xml", "C", "867837");
//        
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        Processor p = new Processor(true);
//        Serializer s = p.newSerializer(bos);
//        XMLStreamWriter writer = s.getXMLStreamWriter();
//        
//        WikiI5Part w = t.call();
//        SAXBuffer idsTextBuffer = w.getIdsTextBuffer();
//        idsTextBuffer.toSAX(new IdsTextHandler(writer));
//        
//        System.out.println(bos.toString("utf-8"));
//        InputStream is = new ByteArrayInputStream(bos.toByteArray());
//        bos.close();
//        Builder builder = new Builder();
//        Document doc = builder.build(is);
//        
//        Node idsText = doc.query("/idsText").get(0);
//        Node classCode = idsText.query("profileDesc/textClass/classCode").get(0);
//        assertEquals(1, classCode.query("ref").size());
//    }
    
    @Test
    public void testEmptyPage () throws I5Exception {
        /* This page contains a few elements only without text
         
        <div n="0" type="section">
            <p><gap desc="template" reason="omitted"/></p>
            <p><gap desc="template" reason="omitted"/> <gap desc="template" reason="omitted"/></p>
            <p><ref target="https://en.wikipedia.org?title=Category:Qliphoth">Category:Qliphoth</ref> <ref target="https://en.wikipedia.org?title=Category:Kabbalistic_words_and_phrases">Category:Kabbalistic words and phrases</ref></p>
            <p> <gap desc="template" reason="omitted"/></p>
        </div>
         */
        
        TaskRunner t = new TaskRunner(config, errorHandler, statistics,
                "A/3232493.xml", "A", "3232493");
        
        
        I5Exception exception = assertThrows(I5Exception.class, () -> {
            t.call();
        });
        
        assertEquals("A/3232493.xml has empty text.",
                exception.getMessage());
    }


    @Test
    public void testArticleWithEmptyDoc () throws I5Exception, IOException, SAXException,
            ParserConfigurationException, ParseException, InterruptedException,
            SQLException {

        IndexingTest.testIndexingWikiXML("article", "en");

        Connection conn = DriverManager.getConnection(config.getDatabaseUrl(),
                config.getDatabaseUsername(), config.getDatabasePassword());
        Statement s = conn.createStatement();
        s.execute("drop table if exists en_category");
        conn.close();

        WikiI5Processor processor = new WikiI5Processor(config);
        processor.run();

        String outputFile = config.getOutputFile();
        File f = new File(outputFile);
        assertNotNull(f);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document doc = builder.parse(f);
        NodeList list = doc.getElementsByTagName("text");
        assertTrue(list.getLength() > 0);

        String textContent = list.item(0).getTextContent();
        assertTrue(textContent != null);
        assertTrue(!textContent.isEmpty());
        
        NodeList docs = doc.getElementsByTagName("idsDoc");
        assertEquals(3,docs.getLength());
    }
}
