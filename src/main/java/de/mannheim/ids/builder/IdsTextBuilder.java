package de.mannheim.ids.builder;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

import de.mannheim.ids.db.DatabaseManager;
import de.mannheim.ids.db.LanguageLinks;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;
import javanet.staxutils.IndentingXMLStreamWriter;

public class IdsTextBuilder extends DefaultHandler2 {

	private Logger log = Logger.getLogger(IdsTextBuilder.class);

	public static final Pattern invalidNativeCharPattern = Pattern
			.compile("&#xd[89a-f]..;");
	public static final String replacementChar = "&#xf8ff;";

	private IndentingXMLStreamWriter writer;
	private String pageId;

	private SAXBuffer categoryEvents;
	private LinkedHashMap<String,SAXBuffer> noteEvents;

	private boolean noLangLinks = false;
	public static DatabaseManager dbManager;

	public IdsTextBuilder(Configuration config, OutputStream outputStream,
			String pageId, SAXBuffer categoryEvents,
			LinkedHashMap<String, SAXBuffer> noteEvents)
			throws I5Exception {
		setWriter(config, outputStream);
		this.pageId = pageId;
		this.categoryEvents = categoryEvents;
		this.noteEvents = noteEvents;

		if (config.isDiscussion()) {
			noLangLinks = true;
		}
		else {
			try {
				dbManager = new DatabaseManager(config.getDatabaseUrl(),
						config.getDatabaseUsername(),
						config.getDatabasePassword(), config.getLanguageCode());
			}
			catch (SQLException e) {
				throw new I5Exception(
						"Failed configuring the database manager.", e);
			}
		}
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
							.escapeXml(attributes.getValue(i));
					text = replaceInvalidCharacters(text);

					// writer.writeAttribute(attributes.getLocalName(i),
					writer.writeAttribute(attributes.getQName(i), text);
				}
				else {
					log.debug("pageId " + pageId + " element " + localName
							+ " att " + attributes.getLocalName(i)
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
					log.debug("empty note " + key);
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

			if (!noLangLinks && "monogr".equals(localName)) {
				try {
					createLangLinks(
							dbManager.retrieveLanguageLinks(pageId));
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
			text = text.trim();
			if (!text.isEmpty()) {
				text = IdsTextBuffer.spacePattern.matcher(text)
						.replaceAll(" ");
				text = replaceInvalidCharacters(text);
				writer.writeCharacters(text);
				writer.flush();
			}
			ch = null;
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed writing foot note text.", e);
		}
	}

	public static String replaceInvalidCharacters(String text) {
		text = invalidNativeCharPattern.matcher(text)
				.replaceAll(replacementChar);
		return text;
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
				keyword = replaceInvalidCharacters(keyword);

				StringBuilder sb = new StringBuilder();
				sb.append("https://");
				sb.append(key);
				sb.append(".wikipedia.org/wiki/");
				sb.append(keyword.replace(" ", "_"));
				writer.writeAttribute("target", sb.toString());

				writer.writeAttribute("xml",
						"https://www.w3.org/XML/1998/namespace", "lang", "key");
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
