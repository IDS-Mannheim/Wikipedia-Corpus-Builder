package de.mannheim.ids.builder;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.axis.message.SAX2EventRecorder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.mannheim.ids.db.DatabaseManager;
import de.mannheim.ids.db.LanguageLinks;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;

/**
 * IdsTextBuilder is a SAX handler implementation creating idsText elements from
 * transformed wikitext in I5 per wikipage.
 * 
 * @author margaretha
 *
 */
public class IdsTextBuilder extends DefaultHandler {

	private IndentingXMLStreamWriter writer;

	private DatabaseManager dbManager;

	private SAX2EventRecorder eventRecorder;

	public static Pattern spacePattern = Pattern.compile("\\s+");

	private String pageId;
	public static List<String> addedAttributes = new ArrayList<String>();
	static {
		addedAttributes.add("part");
		addedAttributes.add("org");
		addedAttributes.add("uniform");
		addedAttributes.add("complete");
		addedAttributes.add("sample");
	}

	private boolean noLangLinks = false;

	private boolean isFootNote = false;

	/**
	 * Constructs an IdsTextBuilder from the given variables.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param writer
	 *            the I5 output writer
	 * @throws I5Exception
	 */
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

		eventRecorder = new SAX2EventRecorder();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		try {
			if (localName.equals("note") || isFootNote) {
				eventRecorder.startElement(uri, localName, qName, attributes);
				isFootNote = true;
			}
			else if (localName.equals("back") && eventRecorder.getLength() > 0) {
				writer.writeStartElement(localName);
				writer.writeStartElement("div");
				writer.writeAttribute("n", "1");
				writer.writeAttribute("complete", "y");
				writer.writeAttribute("type", "footnotes");
				eventRecorder.replay(new FootnoteBuilder(writer));
				writer.writeEndElement();
				writer.flush();
			}
			else {
				writer.writeStartElement(localName);
				for (int i = 0; i < attributes.getLength(); i++) {
					if (!addedAttributes.contains(attributes.getLocalName(i))) {
						writer.writeAttribute(attributes.getLocalName(i),
								StringEscapeUtils
										.escapeXml(attributes.getValue(i)));
					}
				}
				writer.flush();
			}
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed creating start element " + localName,
					e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (isFootNote) {
			eventRecorder.endElement(uri, localName, qName);
			if (localName.equals("note")) {
				isFootNote = false;
			}
		}
		else {
			try {
				if (localName.equals("back")) {
					eventRecorder.clear();
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
	 */
	private void createLangLinks(LanguageLinks ll) throws SAXException {
		Map<String, String> map = ll.getTitleMap();
		try {
			for (String key : map.keySet()) {
				writer.writeStartElement("relatedItem");
				writer.writeAttribute("type", "langlink");

				writer.writeStartElement("ref");

				StringBuilder sb = new StringBuilder();
				sb.append("https://");
				sb.append(key);
				sb.append(".wikipedia.org/wiki/");
				sb.append(map.get(key).replace(" ", "_"));
				writer.writeAttribute("target", sb.toString());

				writer.writeAttribute("xml:lang", key);
				writer.writeCharacters(map.get(key));

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
			eventRecorder.characters(ch, start, length);
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
	 */
	public void setPageId(String pageId) {
		this.pageId = pageId;
	}
}
