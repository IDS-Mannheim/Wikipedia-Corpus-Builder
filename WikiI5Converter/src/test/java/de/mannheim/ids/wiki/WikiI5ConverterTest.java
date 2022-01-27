package de.mannheim.ids.wiki;

import static org.junit.Assert.assertNotNull;
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

/**
 * The tests in require:
 * <ul>
 * <li>Language link database</li>
 * <li>WikiXML article pages, outputs of WikiXMLConverter, e.g. in
 * <code>wikixml-de/articles</code> folder.</li>
 * 
 * </ul>
 * 
 * Some tests also require customizing properties files.
 * You may use your own properties files.
 * 
 * @author margaretha
 */
public class WikiI5ConverterTest {
	
	/**
	 * This test requires wikixml folder and its index file
	 * 
	 * @throws I5Exception
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test
	public void testWikiI5ProcessorArticle() throws I5Exception, IOException,
			SAXException, ParserConfigurationException, ParseException,
			InterruptedException, SQLException {
		
		IndexingTest.testIndexingWikiXML("article","de");
		WikiI5Converter converter = new WikiI5Converter();
		Configuration config = converter.createConfig(
				new String[]{"-prop", "dewiki-article.properties",
						"-storeCategories"});

		Connection conn = DriverManager.getConnection(config.getDatabaseUrl(),
				config.getDatabaseUsername(), config.getDatabasePassword());
		Statement s = conn.createStatement();
		s.execute("drop table if exists de_category");
		conn.close();
		
		WikiI5Processor processor = new WikiI5Processor(config);
		processor.run();

		String outputFile = config.getOutputFile();
		testI5File(outputFile);
	}

	/**
	 * This test requires wikixml folder and its index file
	 * 
	 * @throws I5Exception
	 * @throws IOException
	 * @throws ParseException
	 * @throws SQLException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws InterruptedException
	 */
	@Test
	public void testWikiI5ConverterTalk()
			throws I5Exception, IOException, ParseException, SQLException,
			ParserConfigurationException, SAXException, InterruptedException {
		IndexingTest.testIndexingWikiXML("talk","de");

		WikiI5Converter.main(new String[]{"-prop", "dewiki-talk.properties"});

		String outputFile = "i5/dewiki-20170701-talk.i5.xml";
		testI5File(outputFile);
	}

	public static void testI5File(String outputFile)
			throws ParserConfigurationException, SAXException, IOException {
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
	}
}
