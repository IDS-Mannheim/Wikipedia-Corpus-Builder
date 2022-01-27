package de.mannheim.ids.wiki;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class EnglishWikiTest {

    @Test
    public void testArticle() throws I5Exception, IOException,
            SAXException, ParserConfigurationException, ParseException,
            InterruptedException, SQLException {
        
        IndexingTest.testIndexingWikiXML("article","en");
        WikiI5Converter converter = new WikiI5Converter();
        Configuration config = converter.createConfig(
                new String[]{"-prop", "enwiki-article.properties",
                        "-storeCategories"});

        Connection conn = DriverManager.getConnection(config.getDatabaseUrl(),
                config.getDatabaseUsername(), config.getDatabasePassword());
        Statement s = conn.createStatement();
        s.execute("drop table if exists en_category");
        conn.close();
        
        WikiI5Processor processor = new WikiI5Processor(config);
        processor.run();

        String outputFile = config.getOutputFile();
        WikiI5ConverterTest.testI5File(outputFile);
    }
}
