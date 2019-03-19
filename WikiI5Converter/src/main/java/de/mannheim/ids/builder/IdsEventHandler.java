package de.mannheim.ids.builder;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javanet.staxutils.IndentingXMLStreamWriter;

public class IdsEventHandler extends DefaultHandler {

	private Logger log = Logger.getLogger(IdsEventHandler.class);

	private IndentingXMLStreamWriter writer;
	private String pageId;

	public IdsEventHandler(IndentingXMLStreamWriter writer, String pageId) {
		this.writer = writer;
		this.pageId = pageId;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {

			writer.writeStartElement(localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				if (!IdsTextBuffer.addedAttributes
						.contains(attributes.getLocalName(i))) {
					if (attributes.getLocalName(i) != null
							&& attributes.getValue(i) != null) {
						String text = StringEscapeUtils
								.escapeXml10(attributes.getValue(i));
						writer.writeAttribute(attributes.getQName(i), text);
					}
					else {
						log.debug("pageId " + pageId + " element " + localName
								+ " att " + attributes.getLocalName(i)
								+ " value " + attributes.getValue(i));
					}
				}
			}

			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating start element " + localName, e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try {
			writer.writeEndElement();
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed creating end element " + localName,
					e);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			String text = new String(ch, start, length);
			text = text.trim();
			if (!text.isEmpty()) {
				text = IdsTextBuffer.spacePattern.matcher(text)
						.replaceAll(" ");
				writer.writeCharacters(text);
				writer.flush();
			}
			ch = null;
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed writing foot note text.", e);
		}
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		throw e;
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		throw e;
	}
}
