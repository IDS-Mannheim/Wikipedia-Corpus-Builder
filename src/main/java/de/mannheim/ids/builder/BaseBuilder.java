package de.mannheim.ids.builder;

import java.util.HashMap;
import java.util.Map;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

/** Constructs basic I5 elements used in other builders.
 *   
 * @author margaretha
 *
 */
public class BaseBuilder {

	protected IndentingXMLStreamWriter writer;

	public static final Map<Integer, String> titleTypes;
	static {
		titleTypes = new HashMap<Integer, String>();
		titleTypes.put(0, "Artikel");
		titleTypes.put(1, "Diskussionen zu Artikeln");
		titleTypes.put(2, "Benutzer");
		titleTypes.put(3, "Benutzerdiskussionen");
		titleTypes.put(4, "Wikipedia");
		titleTypes.put(5, "Wikipediadiskussionen");
		titleTypes.put(6, "Dateien");
		titleTypes.put(7, "Dateidiskussionen");
		titleTypes.put(8, "MediaWiki");
		titleTypes.put(9, "MediaWikidiskussionen");
		titleTypes.put(10, "Vorlagen");
		titleTypes.put(11, "Vorlagediskussionen");
		titleTypes.put(12, "Hilfe");
		titleTypes.put(13, "Hilfediskussionen");
		titleTypes.put(14, "Kategorien");
		titleTypes.put(15, "Kategoriediskussionen");
	}

	public BaseBuilder(IndentingXMLStreamWriter writer) {
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
