package de.mannheim.ids.builder;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;
import javanet.staxutils.IndentingXMLStreamWriter;

public class IdsTextValidator extends DefaultHandler {

	private IndentingXMLStreamWriter writer;

	public IdsTextValidator(Configuration config, IndentingXMLStreamWriter writer)
			throws I5Exception {
		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null.");
		}

		this.writer = writer;

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			writer.writeStartElement(localName);

			for (int i = 0; i < attributes.getLength(); i++) {
				writer.writeAttribute(attributes.getLocalName(i),
						attributes.getValue(i));
			}
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed writing start element " + localName,
					e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try {
			writer.writeEndElement();
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating end element " + localName, e);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			String text = new String(ch, start, length);
			if (!text.isEmpty()) {
				writer.writeCharacters(text);
				writer.flush();
			}
			ch = null;
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed writing text.", e);
		}
	}
	
}
