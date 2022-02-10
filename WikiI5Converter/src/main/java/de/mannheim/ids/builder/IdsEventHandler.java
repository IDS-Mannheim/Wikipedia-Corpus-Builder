package de.mannheim.ids.builder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class IdsEventHandler extends DefaultHandler {

    private XMLStreamWriter writer;
    private String wikiXMLPath;

    public IdsEventHandler (XMLStreamWriter writer, String wikiXMLPath) {
        this.writer = writer;
        this.wikiXMLPath = wikiXMLPath;
    }


    @Override
    public void startElement (String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        try {
            writer.writeStartElement(localName);
            for (int i = 0; i < attributes.getLength(); i++) {
                //				if (!IdsTextBuffer.addedAttributes
                //						.contains(attributes.getLocalName(i))) {
                //					if (attributes.getLocalName(i) != null
                //							&& attributes.getValue(i) != null) {
                String text = StringEscapeUtils
                        .escapeXml10(attributes.getValue(i));
                writer.writeAttribute(attributes.getQName(i), text);
                //					}
                //					else {
                //						log.debug("pageId " + pageId + " element " + localName
                //								+ " att " + attributes.getLocalName(i)
                //								+ " value " + attributes.getValue(i));
                //					}
                //				}
            }

            writer.flush();
        }
        catch (XMLStreamException e) {
            throw new SAXException("Failed creating start element " + localName
                    + "in " + wikiXMLPath, e);
        }
    }


    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException {
        try {
            writer.writeEndElement();
            writer.flush();
        }
        catch (XMLStreamException e) {
            throw new SAXException("Failed creating end element " + localName
                    + "in " + wikiXMLPath, e);
        }
    }


    @Override
    public void characters (char[] ch, int start, int length)
            throws SAXException {
        try {
            String text = new String(ch, start, length);
            text = text.trim();
            if (!text.isEmpty()) {
                text = IdsTextBuffer.spacePattern.matcher(text).replaceAll(" ");
                writer.writeCharacters(text);
                writer.flush();
            }
            ch = null;
        }
        catch (XMLStreamException e) {
            throw new SAXException(
                    "Failed writing event text in " + wikiXMLPath, e);
        }
    }


    @Override
    public void error (SAXParseException e) throws SAXException {
        throw e;
    }


    @Override
    public void fatalError (SAXParseException e) throws SAXException {
        throw e;
    }


    @Override
    public void warning (SAXParseException e) throws SAXException {
        throw e;
    }
}
