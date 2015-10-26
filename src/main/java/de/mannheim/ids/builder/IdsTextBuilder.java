package de.mannheim.ids.builder;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.mannheim.ids.db.DatabaseManager;
import de.mannheim.ids.db.LanguageLinks;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;

public class IdsTextBuilder extends DefaultHandler {

	private IndentingXMLStreamWriter writer;

	private DatabaseManager dbManager;
	
	private static Pattern space = Pattern.compile("\\s+");
	
	private String pageId;
	private static List<String> addedAttributes = new ArrayList<String>();
	static {
		addedAttributes.add("part");
		addedAttributes.add("org");
		addedAttributes.add("uniform");
		addedAttributes.add("complete");
		addedAttributes.add("sample");
	}

	private boolean noLangLinks = false;

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
						config.getDatabasePassword(),
						config.getLanguageCode());
			}
			catch (SQLException e) {
				throw new I5Exception("Failed configuring the database manager.", e);
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			writer.writeStartElement(localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				if (!addedAttributes.contains(attributes.getLocalName(i))) {
					writer.writeAttribute(attributes.getLocalName(i),
							StringEscapeUtils.escapeXml(attributes.getValue(i)));
				}
			}
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed creating start element "+localName,e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		try {
			writer.writeEndElement();
			if (!noLangLinks && "monogr".equals(localName)) {
				try {
					createLangLinks(dbManager.retrieveLanguageLinks(pageId));
				}
				catch (SQLException e) {
					throw new SAXException("Failed retreving language links.",e);
				}
				catch (UnsupportedEncodingException e) {
					throw new SAXException("Failed converting ll_title to UTF-8",e);
				}
			}
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed creating end element "+localName,e);
		}
	}

	private void createLangLinks(LanguageLinks ll) throws SAXException {
		Map<String, String> map = ll.getTitleMap();
		try{
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
		try {			
			String text = new String(ch, start, length);
			if (!text.isEmpty()) {
				//writer.writeCharacters(StringEscapeUtils.escapeXml(text));
				text = space.matcher(text).replaceAll(" ");
				writer.writeCharacters(text);
				writer.flush();
			}
			ch = null;
		}
		catch (XMLStreamException e) {
			throw new SAXException("Failed writing text.", e);
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

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}
}
