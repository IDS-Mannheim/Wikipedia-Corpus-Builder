package de.mannheim.ids.builder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;

/**
 * IdsTextBuilder is a SAX handler implementation creating idsText elements from
 * transformed wikitext in I5 per wikipage.
 * 
 * @author margaretha
 *
 */
public class IdsTextBuffer extends SAXBuffer {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -8276104769505424958L;

	private Logger log = Logger.getLogger(IdsTextBuffer.class);

	private SAXBuffer currentNoteRecorder;
	private LinkedHashMap<String, SAXBuffer> noteEvents;
	private SAXBuffer categoryEvents;

	public static final Pattern spacePattern = Pattern.compile("\\s+");

	private String category;

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

	private boolean isFootNote = false;
	private boolean isInPtr = false;
	private String idsTextId = "";
	private String noteId;
	private int refCounter;

	private boolean isCategoryFound;
	private boolean isTextEmpty = true;

	private boolean isText = false;

	public IdsTextBuffer(Configuration config) throws I5Exception {
		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null.");
		}

		noteEvents = new LinkedHashMap<>();
		refNames = new HashMap<>();
		refCounter = 0;
		currentNoteRecorder = new SAXBuffer();

		try {
			category = URLEncoder.encode("Kategorie:",
					StandardCharsets.UTF_8.name());
			log.debug(category);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		categoryEvents = new SAXBuffer();
	}

	public void clearReferences() {
		this.refNames.clear();
		this.noteEvents.clear();
		this.refCounter = 0;
	}

	public void clearCategories() {
		categoryEvents.recycle();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (localName.equals("idsText")) {
			this.idsTextId = attributes.getValue("id");
			writeStartElement(uri, localName, qName, attributes);
		}
		else if(localName.equals("text")){
			isText=true;
			writeStartElement(uri, localName, qName, attributes);
		}
		else if (localName.equals("ptr")) {
			ptrStartElement(uri, localName, qName, attributes);
		}
		else if (localName.equals("note")) {
			noteStartElement(uri, localName, qName, attributes);
		}
		else if (isFootNote) {
			currentNoteRecorder.startElement(uri, localName, qName,
					attributes);
		}
		else if (localName.equals("ref")) {
			String categoryURL = attributes.getValue("target");

			if (categoryURL != null && !categoryURL.isEmpty()
					&& categoryURL.contains(category)) {

				categoryEvents.startElement(uri, localName, qName, attributes);
				isCategoryFound = true;
			}
			else {
				writeStartElement(uri, localName, qName, attributes);
			}
		}
		else {
			writeStartElement(uri, localName, qName, attributes);
		}

	}

	private void writeStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
	}

	private void noteStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
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

		currentNoteRecorder.startElement(uri, localName, qName,
				attributes);
		isFootNote = true;
		log.debug("note start " + noteId);
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
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (isFootNote) {
			currentNoteRecorder.endElement(uri, localName, qName);
			if (localName.equals("note")) {
				isFootNote = false;

				if (noteEvents.containsKey(noteId)) {
					if (noteEvents.get(noteId).isEmpty()
							&& !currentNoteRecorder.isEmpty()) {
						log.debug("replace note " + noteId);
						noteEvents.remove(noteId);
						noteEvents.put(noteId, currentNoteRecorder);
					}
				}
				else {
					log.debug("put note " + noteId);
					noteEvents.put(noteId, currentNoteRecorder);
					refCounter++;
				}
				currentNoteRecorder = new SAXBuffer();
			}
		}
		else {
			if (localName.equals("ptr")) {
				log.debug("ptr end");
				if (noteEvents.containsKey(noteId)) {
					if (noteEvents.get(noteId).isEmpty()
							&& !currentNoteRecorder.isEmpty()) {
						log.debug("replace ptr " + noteId);
						noteEvents.remove(noteId);
						noteEvents.put(noteId, currentNoteRecorder);
					}
				}
				else {
					noteEvents.put(noteId, currentNoteRecorder);
				}
				currentNoteRecorder = new SAXBuffer();
				super.endElement("", localName, qName);
				isInPtr = false;
			}
			else if (isCategoryFound) {
				categoryEvents.endElement(uri, localName, qName);
				isCategoryFound = false;
			}
			else if (localName.equals("text")) {
				isText=false;
				super.endElement(uri, localName, qName);
			}
			else {
				super.endElement(uri, localName, qName);
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (isFootNote) {
			currentNoteRecorder.characters(ch, start, length);
		}
		else if (isCategoryFound) {
			categoryEvents.characters(ch, start, length);
		}
		else if (isText){
			super.characters(ch, start, length);
			String text = new String(ch, start, length);
			if (isTextEmpty & text.trim().length()>1){
				isTextEmpty=false;
			}
		}
		else {
			super.characters(ch, start, length);
		}
	}

	public boolean isTextEmpty() {
		return isTextEmpty;
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

	public SAXBuffer getCategoryEvents() {
		return categoryEvents;
	}

	public LinkedHashMap<String, SAXBuffer> getNoteEvents() {
		return noteEvents;
	}
}
