package de.mannheim.ids.writer;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

public class TitleWriterTest {

	@Test
	public void testAmpersand() throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter writer = factory.createXMLStreamWriter(bos);
		WikiTitleWriter w = new WikiTitleWriter(writer);
		w.indexTitle("Diskussion:.460 S&amp;W Magnum", "123");
		String title = bos.toString();
		assertEquals("<title id=\"123\">Diskussion:.460 S&amp;W Magnum</title>",
				title);
	}
}
