package de.mannheim.ids.builder;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Constructs basic I5 elements used in other builders.
 * 
 * @author margaretha
 *
 */
public class BaseBuilder {

	protected XMLStreamWriter writer;

	public static final Map<String, String> titleTypes;
	static {
		titleTypes = new HashMap<String, String>();
		titleTypes.put("article", "Artikel");
		titleTypes.put("talk", "Diskussionen zu Artikeln");
		titleTypes.put("user-talk", "Benutzerdiskussionen");
		titleTypes.put("loeschkandidaten", "Löschkandidaten");
		titleTypes.put("redundanz", "Redundanzdiskussionen");
	}

	/**
	 * Construct a BaseBuilder
	 * 
	 * @param writer
	 *            an IndentingXMLStreamWriter writing I5 to the output file
	 */
	public BaseBuilder(XMLStreamWriter writer) {
		this.writer = writer;
	}

	protected void createPublicationStmt() throws XMLStreamException {
		writer.writeStartElement("publicationStmt");
		writer.writeEmptyElement("distributor");
		writer.writeEmptyElement("pubAddress");

		writer.writeStartElement("availability");
		writer.writeAttribute("region", "world");
		writer.writeCharacters("CC-BY-SA");
		writer.writeEndElement(); // availability

		writer.writeEmptyElement("pubDate");
		writer.writeEndElement();
	}

	protected void createSourceDesc() throws XMLStreamException {
		writer.writeStartElement("sourceDesc");

		writer.writeStartElement("biblStruct");
		writer.writeAttribute("Default", "n");

		createMonogr();
		writer.writeEndElement(); // biblStruct
		writer.writeEndElement();
	}

	private void createMonogr() throws XMLStreamException {
		writer.writeStartElement("monogr");

		writer.writeStartElement("h.title");
		writer.writeAttribute("type", "main");
		writer.writeEndElement(); // h.title

		writer.writeEmptyElement("imprint");
		writer.writeEndElement();
	}

	public void createSimpleElement(String elementName, String content)
			throws XMLStreamException {
		writer.writeStartElement(elementName);
		writer.writeCharacters(content);
		writer.writeEndElement();
	}
}
