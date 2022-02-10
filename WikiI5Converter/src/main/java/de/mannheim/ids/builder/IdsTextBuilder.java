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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.AttributesImpl;

import de.mannheim.ids.db.LanguageLinks;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.WikiI5Processor;

/** IdsTextBuilder is a SAX content handler that writes events 
 * from {@link IdsTextBuffer} to an {@link OutputStream}. This 
 * class especially handles writing category and footnote 
 * events to their appropriate positions in I5.  
 * 
 * @author margaretha
 *
 */
public class IdsTextBuilder extends DefaultHandler2 {

//	private Logger log = Logger.getLogger(IdsTextBuilder.class);

	public static String enScheme = "https://en.wikipedia.org/wiki/Category:Contents";
	
	private XMLStreamWriter writer;
	private String pageId;
	private String wikiXMLPath;
	private String categorySchema;

	private SAXBuffer categoryEvents;
	private SAXBuffer englishCategoryEvents;
	private LinkedHashMap<String, SAXBuffer> noteEvents;
	
	private SAXBuffer extendedIdsText;

	private boolean isDiscussion = false;
	private boolean includeLanguageLinks = false;
	
	private String pageTitle;

	public IdsTextBuilder(Configuration config, OutputStream outputStream,
			String pageId, String wikiXMLPath, IdsTextBuffer idsTextBuffer)
			throws I5Exception {
		createWriter(config, outputStream);
		this.includeLanguageLinks = config.isIncludeLangLinks();
		this.pageId = pageId;
		this.pageTitle = idsTextBuffer.getPageTitle();
		this.categoryEvents = idsTextBuffer.getCategoryEvents();
		this.englishCategoryEvents = idsTextBuffer.getEnglishCategoryEvents();
		this.noteEvents = idsTextBuffer.getNoteEvents();
		this.extendedIdsText = new SAXBuffer();

		if (config.isDiscussion()) {
			isDiscussion = true;
		}
		this.categorySchema = config.getCategoryScheme();
	}

	private void createWriter(Configuration config, OutputStream os)
			throws I5Exception {
		XMLOutputFactory f = XMLOutputFactory.newInstance();
		try {
			writer = f.createXMLStreamWriter(os, config.getOutputEncoding());
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed creating an XMLStreamWriter", e);
		}
	}
	
	public SAXBuffer getExtendedIdsText() {
		return extendedIdsText;
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
			
			AttributesImpl attr = new AttributesImpl();
			for (int i = 0; i < attributes.getLength(); i++) {
//				if (!IdsTextBuffer.addedAttributes
//						.contains(attributes.getLocalName(i))
//						&& attributes.getLocalName(i) != null
//						&& attributes.getValue(i) != null) {

					String text = StringEscapeUtils
							.escapeXml10(attributes.getValue(i));
					writer.writeAttribute(attributes.getQName(i), text);
					attr.addAttribute("", attributes.getLocalName(i),
							attributes.getQName(i), attributes.getType(i), text);					
//				}
			}
			extendedIdsText.startElement(uri, localName, qName, attr);
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException(
					"Failed creating start element " + localName, e);
		}

		try {
			if (!categoryEvents.isEmpty() && localName.equals("profileDesc")) {
				writer.writeStartElement("textClass");
				writer.writeStartElement("classCode");
				writer.writeAttribute("scheme", categorySchema);
				categoryEvents.toSAX(new IdsEventHandler(writer, wikiXMLPath));
				writer.writeEndElement();

				extendedIdsText.startElement("", "textClass", "textClass",
						new AttributesImpl());
				AttributesImpl attr = new AttributesImpl();
				attr.addAttribute("", "scheme", "scheme", "CDATA", categorySchema);
				extendedIdsText.startElement("", "classCode", "classCode", attr);
				extendedIdsText.saxBuffer(categoryEvents);
				extendedIdsText.endElement("", "classCode", "classCode");
				
				if (!englishCategoryEvents.isEmpty()) {
					writer.writeStartElement("classCode");
					writer.writeAttribute("scheme",enScheme);
					englishCategoryEvents.toSAX(new IdsEventHandler(writer, wikiXMLPath));
					writer.writeEndElement();
					
					attr = new AttributesImpl();
					attr.addAttribute("", "scheme", "scheme", "CDATA", enScheme);
					extendedIdsText.startElement("", "classCode", "classCode", attr);
					extendedIdsText.saxBuffer(englishCategoryEvents);
					extendedIdsText.endElement("", "classCode", "classCode");
				}
				writer.writeEndElement();
				writer.flush();
				
				extendedIdsText.endElement("", "textClass", "textClass");
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
			writer.writeAttribute("type", "footnotes");
			
			extendedIdsText.startElement(uri, localName, qName, attributes);
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute("", "n", "n", "CDATA", "1");
			attr.addAttribute("", "type", "type", "CDATA", "footnotes");
			extendedIdsText.startElement("", "div", "div", attr);

			ContentHandler footnoteBuilder = new IdsEventHandler(writer,
			        wikiXMLPath);
			SAXBuffer event;
			for (String key : noteEvents.keySet()) {
				event = noteEvents.get(key);
				if (event.isEmpty()) {
					// log.debug("empty note " + key);
					AttributesImpl att = new AttributesImpl();
					att.addAttribute("", "id", "id", "ID", key);
					event.startElement("", "note", "note", att);
					
					String noteContent = "N/A";
					event.characters(noteContent.toCharArray(), 0,
							noteContent.length());
					event.endElement("", "note", "note");
				}
				event.toSAX(footnoteBuilder);
				extendedIdsText.saxBuffer(event);
			}

			writer.writeEndElement();
			writer.flush();
			
			extendedIdsText.endElement("", "div", "div");
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed creating start element " + localName,
					e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try {
			writer.writeEndElement();
			extendedIdsText.endElement(uri, localName, qName);

			if ("monogr".equals(localName)) {
				try {
					if (isDiscussion) {
						String articleTitle = pageTitle.split(":", 2)[1];
						createLangLinks(WikiI5Processor.dbManager
								.retrieveArticleLinks(articleTitle));
					}
					else if (includeLanguageLinks){
						createLangLinks(WikiI5Processor.dbManager
								.retrieveLanguageLinks(pageId));
					}
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
				
				extendedIdsText.characters(ch, start, length);
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
		if (ll != null) {
			try {
				Map<String, String> map = ll.getTitleMap();
				for (String key : map.keySet()) {
					writer.writeStartElement("relatedItem");
					writer.writeAttribute("type", "langlink");

					AttributesImpl atts = new AttributesImpl();
					atts.addAttribute("", "type", "type", "CDATA", "langlink");
					extendedIdsText.startElement("", "relatedItem", "relatedItem", atts);
					
					String keyword = map.get(key);
					StringBuilder sb = new StringBuilder();
					sb.append("https://");
					sb.append(key);
					sb.append(".wikipedia.org/wiki/");
					sb.append(keyword.replace(" ", "_"));
					String target = sb.toString();
					
					writer.writeStartElement("ref");
					writer.writeAttribute("targetLang",key);
					writer.writeAttribute("target", target);
					writer.writeCharacters(keyword);

					atts = new AttributesImpl();
					atts.addAttribute("", "targetLang", "targetLang", "CDATA", key);
					atts.addAttribute("", "target", "target", "CDATA", target);
					extendedIdsText.startElement("", "ref", "ref", atts);
					extendedIdsText.characters(keyword.toCharArray(), 0, keyword.length());
					
					writer.writeEndElement(); // ref
					writer.writeEndElement(); // relatedItem
					writer.flush();
					
					extendedIdsText.endElement("", "ref", "ref");
					extendedIdsText.endElement("", "relatedItem", "relatedItem");
				}
			}
			catch (XMLStreamException e) {
				throw new SAXException("Failed creating language links.", e);
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
}
