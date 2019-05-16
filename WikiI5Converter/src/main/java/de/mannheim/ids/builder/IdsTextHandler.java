package de.mannheim.ids.builder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.mannheim.ids.wiki.I5Exception;

public class IdsTextHandler extends DefaultHandler {

	boolean isBody = false;
	private XMLStreamWriter writer;

	public IdsTextHandler(XMLStreamWriter writer)
			throws I5Exception {
		this.writer = writer;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			if (localName.equals("body")){
				isBody=true;
			}
			writer.writeStartElement(localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				writer.writeAttribute(attributes.getQName(i),
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
			if (localName.equals("body")){
				isBody=false;
			}
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
			if (!isBody) {
				if (!text.trim().isEmpty()) {
					writer.writeCharacters(text);
					writer.flush();
				}
			}
			else if (!text.isEmpty()) {
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
