package de.mannheim.ids.builder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.message.SAX2EventRecorder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;
import javanet.staxutils.IndentingXMLStreamWriter;

public class CategoryBuilder extends DefaultHandler2 {

	private Logger log = Logger.getLogger(CategoryBuilder.class);

	private IndentingXMLStreamWriter writer;
	private String pageId;

	private SAX2EventRecorder categoryEvents;

	public CategoryBuilder(Configuration config, OutputStream outputStream,
			String pageId, SAX2EventRecorder categoryEvents)
			throws I5Exception {
		setWriter(config, outputStream);
		this.pageId = pageId;
		this.categoryEvents = categoryEvents;
	}

	private void setWriter(Configuration config, OutputStream os)
			throws I5Exception {
		XMLOutputFactory f = XMLOutputFactory.newInstance();
		XMLStreamWriter w = null;
		try {
			w = f.createXMLStreamWriter(os, config.getOutputEncoding());
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed creating an XMLStreamWriter", e);
		}
		writer = new IndentingXMLStreamWriter(w);
		writer.setIndent(" ");
	}

	@Override
	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
		try {
			writer.writeDTD("<!DOCTYPE " + name + " PUBLIC '" + publicId + "' '"
					+ systemId + "'>");
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed writing DTD " + name, e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		try {

			writer.writeStartElement(localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				if (!IdsTextBuilder.addedAttributes
						.contains(attributes.getLocalName(i))
						&& attributes.getLocalName(i) != null
						&& attributes.getValue(i) != null) {
					writer.writeAttribute(attributes.getQName(i),
//						writer.writeAttribute(attributes.getLocalName(i),
								StringEscapeUtils
										.escapeXml(attributes.getValue(i)));
				}
				else {
					log.debug("pageId " + pageId + " element " + localName
							+ " att "
							+ attributes.getLocalName(i)
							+ " value " + attributes.getValue(i));
				}
			}
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating start element " + localName, e);
		}

		try {
			if (localName.equals("profileDesc")) {
				writer.writeStartElement("textClass");
				writer.writeStartElement("classCode");
				writer.writeAttribute("scheme",
						"https://en.wikipedia.org/wiki/Portal:Contents/Categories");
				 categoryEvents.replay(new SAXEventHandler(writer, pageId));
				writer.writeEndElement();
				writer.writeEndElement();
			}
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating category elements");
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
				text = IdsTextBuilder.spacePattern.matcher(text)
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
