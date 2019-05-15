package de.mannheim.ids.builder;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.AttributesImpl;

import de.mannheim.ids.db.LanguageLinks;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.I5Writer;
import javanet.staxutils.IndentingXMLStreamWriter;

/** IdsTextBuilder is a SAX content handler that writes events 
 * from {@link IdsTextBuffer} to an {@link OutputStream}. This 
 * class especially handles writing category and footnote 
 * events to their appropriate positions in I5.  
 * 
 * @author margaretha
 *
 */
public class IdsTextBuilder extends DefaultHandler2 {

	private Logger log = Logger.getLogger(IdsTextBuilder.class);

	private IndentingXMLStreamWriter writer;
	private String pageId;
	private String categorySchema;

	private SAXBuffer categoryEvents;
	private LinkedHashMap<String, SAXBuffer> noteEvents;

	private boolean isDiscussion = false;

	public IdsTextBuilder(Configuration config, OutputStream outputStream,
			String pageId, SAXBuffer categoryEvents,
			LinkedHashMap<String, SAXBuffer> noteEvents)
			throws I5Exception {
		createWriter(config, outputStream);
		this.pageId = pageId;
		this.categoryEvents = categoryEvents;
		this.noteEvents = noteEvents;

		if (config.isDiscussion()) {
			isDiscussion = true;
		}
		this.categorySchema = config.getCategoryScheme();
	}

	private void createWriter(Configuration config, OutputStream os)
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

		if (localName.equals("back") && noteEvents.size() > 0) {
			backStartElement(uri, localName, qName, attributes);
			return;
		}

		try {
			writer.writeStartElement(localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				if (!IdsTextBuffer.addedAttributes
						.contains(attributes.getLocalName(i))
						&& attributes.getLocalName(i) != null
						&& attributes.getValue(i) != null) {

					String text = StringEscapeUtils
							.escapeXml10(attributes.getValue(i));
					writer.writeAttribute(attributes.getQName(i), text);
				}
				// else {
				// log.debug("pageId " + pageId + " element " + localName
				// + " att " + attributes.getLocalName(i)
				// + " value " + attributes.getValue(i));
				// }
			}
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating start element " + localName, e);
		}

		try {
			if (!isDiscussion && localName.equals("profileDesc")) {
				writer.writeStartElement("textClass");
				writer.writeStartElement("classCode");
				writer.writeAttribute("scheme", categorySchema);
				categoryEvents.toSAX(new IdsEventHandler(writer, pageId));
				writer.writeEndElement();
				writer.writeEndElement();
				writer.flush();
			}
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating category elements");
		}
	}

	private void backStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			writer.writeStartElement(localName);
			writer.writeStartElement("div");
			writer.writeAttribute("n", "1");
			writer.writeAttribute("complete", "y");
			writer.writeAttribute("type", "footnotes");

			ContentHandler footnoteBuilder = new IdsEventHandler(writer,
					pageId);
			SAXBuffer event;
			for (String key : noteEvents.keySet()) {
				event = noteEvents.get(key);
				if (event.isEmpty()) {
					// log.debug("empty note " + key);
					AttributesImpl att = new AttributesImpl();
					att.addAttribute("", "id", "id", "ID", key);
					event.startElement("", "note", "note",
							att);
					String noteContent = "N/A";
					event.characters(
							noteContent.toCharArray(), 0,
							noteContent.length());
					event.endElement("", "note", "note");
				}
				event.toSAX(footnoteBuilder);
			}

			writer.writeEndElement();
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating start element " + localName,
					e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try {
			writer.writeEndElement();

			if (!isDiscussion && "monogr".equals(localName)) {
				try {
					createLangLinks(
							I5Writer.dbManager.retrieveLanguageLinks(pageId));
				}
				catch (SQLException e) {
					throw new SAXException(
							"Failed retreving language links.", e);
				}
				catch (UnsupportedEncodingException e) {
					throw new SAXException(
							"Failed converting ll_title to UTF-8", e);
				}
			}

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

	/**
	 * Creates related item elements for the links of the analogs of a wiki
	 * article page in other languages (wikipedias).
	 * 
	 * @param ll
	 *            language links from wikipedia (database)
	 * @throws SAXException
	 *             a {@link SAXException}
	 */
	private void createLangLinks(LanguageLinks ll) throws SAXException {
		Map<String, String> map = ll.getTitleMap();
		try {
			for (String key : map.keySet()) {
				writer.writeStartElement("relatedItem");
				writer.writeAttribute("type", "langlink");

				writer.writeStartElement("ref");
				String keyword = map.get(key);

				StringBuilder sb = new StringBuilder();
				sb.append("https://");
				sb.append(key);
				sb.append(".wikipedia.org/wiki/");
				sb.append(keyword.replace(" ", "_"));
				writer.writeAttribute("target", sb.toString());

				writer.writeAttribute("xml",
						"https://www.w3.org/XML/1998/namespace", "lang", key);
				// writer.writeAttribute("xml:lang", key);
				writer.writeCharacters(keyword);

				writer.writeEndElement(); // ref
				writer.writeEndElement(); // relatedItem
				writer.flush();
			}
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed creating language links.", e);
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
