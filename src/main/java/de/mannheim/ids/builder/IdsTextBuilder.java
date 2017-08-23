package de.mannheim.ids.builder;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.message.SAX2EventRecorder;
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

/**
 * IdsTextBuilder is a SAX handler implementation creating idsText elements from
 * transformed wikitext in I5 per wikipage.
 * 
 * @author margaretha
 *
 */
public class IdsTextBuilder extends DefaultHandler2 {

	private Logger log = Logger.getLogger(IdsTextBuilder.class);
	
	private IndentingXMLStreamWriter writer;

	public static DatabaseManager dbManager;

	private SAX2EventRecorder currentEventRecorder;
	private LinkedHashMap<String, SAX2EventRecorder> noteEvents;

	public static final Pattern spacePattern = Pattern.compile("\\s+");

	private String pageId;
	public static List<String> addedAttributes = new ArrayList<String>();
	static {
		addedAttributes.add("part");
		addedAttributes.add("org");
		addedAttributes.add("uniform");
		addedAttributes.add("complete");
		addedAttributes.add("sample");
	}

	private Map<String, String> refNames;

	private boolean noLangLinks = false;

	private boolean isFootNote = false;
	private boolean isInPtr = false;
	private String idsTextId = "";
	private String noteId;
	private int refCounter;
	private String encoding;

	public IdsTextBuilder(Configuration config, OutputStream os)
			throws I5Exception {
		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null.");
		}

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
		encoding = config.getOutputEncoding();
		setWriter(config, os);
		writeStartDocument(encoding);
		noteEvents = new LinkedHashMap<>();
		refNames = new HashMap<>();
		refCounter = 0;
		currentEventRecorder = new SAX2EventRecorder();
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

	public void close() throws XMLStreamException {
		writer.close();
	}

	/**
	 * Constructs an IdsTextBuilder from the given variables.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param writer
	 *            the I5 output writer
	 * @throws I5Exception
	 *             an {@link I5Exception}
	 */
	@Deprecated
	public IdsTextBuilder(Configuration config, IndentingXMLStreamWriter writer)
			throws I5Exception {
		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null.");
		}

		this.writer = writer;

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

		currentEventRecorder = new SAX2EventRecorder();
		refNames = new HashMap<>();
		refCounter = 0;
	}

	public void clearReferences() {
		this.refNames.clear();
		this.noteEvents.clear();
	}

	public void resetNoteCounter() {
		this.refCounter = 0;
	}

	public void writeStartDocument(String encoding) throws I5Exception {
		try {
			writer.writeStartDocument(encoding, "1.0");
		}
		catch (XMLStreamException e) {
			throw new I5Exception(
					"Failed writing IdsText start document.", e);
		}
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

		if (localName.equals("idsText")) {
			this.idsTextId = attributes.getValue("id");
		}

		if (localName.equals("ptr")) {
			ptrStartElement(uri, localName, qName, attributes);
		}
		else if (localName.equals("note")) {

			if (attributes.getValue("name") != null) {
				log.debug("note name: " + attributes.getValue("name"));
				noteId = refNames.get(attributes.getValue("name"));
				if (noteId == null) {
					if (!isInPtr) {
						refCounter++;
					}
					noteId = idsTextId + "-f" + refCounter;
				}
			}
			else {
				noteId = idsTextId + "-f" + refCounter;
			}
			attributes = replaceAttributes("id", noteId, "name",
					attributes);

			currentEventRecorder.startElement(uri, localName, qName,
					attributes);
			isFootNote = true;
			log.debug("note start " + noteId);
		}
		else if (isFootNote) {
			currentEventRecorder.startElement(uri, localName, qName,
					attributes);
		}
		else if (localName.equals("back")
				&& noteEvents.size() > 0) {
			try {
				writer.writeStartElement(localName);
				writer.writeStartElement("div");
				writer.writeAttribute("n", "1");
				writer.writeAttribute("complete", "y");
				writer.writeAttribute("type", "footnotes");

				ContentHandler footnoteBuilder = new FootnoteBuilder(writer,
						pageId);
				SAX2EventRecorder eventRecorder;
				for (String key : noteEvents.keySet()) {
					eventRecorder = noteEvents.get(key);
					if (eventRecorder.getLength() < 1) {
						log.debug("empty note " + key);
						AttributesImpl att = new AttributesImpl();
						att.addAttribute("", "id", "id", "ID", key);
						eventRecorder.startElement("", "note", "note",
								att);
						String noteContent = "N/A";
						eventRecorder.characters(
								noteContent.toCharArray(), 0,
								noteContent.length());
						eventRecorder.endElement("", "note", "note");
					}
					eventRecorder.replay(footnoteBuilder);
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
		else {
			writeStartElement(uri, localName, qName, attributes);
		}

	}

	private void writeStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			writer.writeStartElement(localName);

			for (int i = 0; i < attributes.getLength(); i++) {
				if (!addedAttributes.contains(attributes.getLocalName(i))) {
					// EM: check invalid chars in att value?
					writer.writeAttribute(attributes.getLocalName(i),
							StringEscapeUtils
									.escapeXml(attributes.getValue(i)));
				}
			}
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed writing start element " + localName,
					e);
		}
	}

	private Attributes replaceAttributes(String newAtt, String newAttValue,
			String removeAtt, Attributes attributes) throws SAXException {
		AttributesImpl newAttributes = new AttributesImpl();

		newAttributes.addAttribute("", newAtt, newAtt, "CDATA", newAttValue);

		for (int i = 0; i < attributes.getLength(); i++) {
			if (i != attributes.getIndex(removeAtt)) {
				newAttributes.addAttribute(attributes.getURI(i),
						attributes.getLocalName(i),
						attributes.getQName(i),
						attributes.getType(i),
						attributes.getValue(i));
			}
		}
		return newAttributes;
	}

	private void ptrStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		String targetId;
		if (attributes.getValue("name") != null) {
			if (refNames.containsKey(attributes.getValue("name"))) {
				targetId = refNames.get(attributes.getValue("name"));
				log.debug("targetId: " + targetId + " name:"
						+ attributes.getValue("name"));
			}
			else {
				targetId = idsTextId + "-f" + (refCounter + 1);
				refNames.put(attributes.getValue("name"), targetId);
				log.debug("targetId: " + targetId + " name:"
						+ attributes.getValue("name"));
				refCounter++;
			}
		}
		else {
			targetId = idsTextId + "-f" + (refCounter + 1);
			log.debug("targetId: " + targetId + " no name");
			refCounter++;
		}
		noteId = targetId;
		attributes = replaceAttributes("target", targetId, "name",
				attributes);
		writeStartElement(uri, localName, qName, attributes);

		isInPtr = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (isFootNote) {
			currentEventRecorder.endElement(uri, localName, qName);
			if (localName.equals("note")) {
				isFootNote = false;

				if (noteEvents.containsKey(noteId)) {
					log.debug("note end " + noteId + " length "
							+ noteEvents.get(noteId).getLength());
					if (noteEvents.get(noteId).getLength() < 1
							&& currentEventRecorder.getLength() > 0) {
						log.debug("replace note " + noteId);
						noteEvents.remove(noteId);
						noteEvents.put(noteId, currentEventRecorder);
					}
				}
				else {
					log.debug("put note " + noteId);
					noteEvents.put(noteId, currentEventRecorder);
					refCounter++;
				}
				currentEventRecorder = new SAX2EventRecorder();
			}
		}
		else {
			try {
				if (localName.equals("ptr")) {
					log.debug("ptr end");
					if (noteEvents.containsKey(noteId)) {
						if (noteEvents.get(noteId).getLength() < 1
								&& currentEventRecorder.getLength() > 0) {
							log.debug("replace ptr " + noteId);
							noteEvents.remove(noteId);
							noteEvents.put(noteId, currentEventRecorder);
						}
					}
					else {
						log.debug("put ptr " + noteId + " "
								+ currentEventRecorder.getLength());
						noteEvents.put(noteId, currentEventRecorder);
					}
					currentEventRecorder = new SAX2EventRecorder();
					writer.writeEndElement();
					isInPtr = false;
				}
				else if (localName.equals("back")) {
					writer.writeEndElement();
				}
				else {
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
				}
				writer.flush();
			}
			catch (XMLStreamException e) {
				throw new SAXException(
						"Failed creating end element " + localName, e);
			}
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

				writer.writeAttribute("xml:lang", key);
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
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (isFootNote) {
			currentEventRecorder.characters(ch, start, length);
		}
		else {
			try {
				String text = new String(ch, start, length);
				if (!text.isEmpty()) {
					// writer.writeCharacters(StringEscapeUtils.escapeXml(text));
					text = spacePattern.matcher(text).replaceAll(" ");
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

	/**
	 * Gets the current wiki page id
	 * 
	 * @return wiki page id
	 */
	public String getPageId() {
		return pageId;
	}

	/**
	 * Sets the current wiki page id
	 * 
	 * @param pageId
	 *            a wiki page id
	 */
	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

}
