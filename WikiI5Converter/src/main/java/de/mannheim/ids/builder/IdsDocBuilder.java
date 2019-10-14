package de.mannheim.ids.builder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** Creates idsDoc elements and their corresponding idsHeader elements.
 * 
 * @author margaretha
 *
 */
public class IdsDocBuilder extends BaseBuilder {

	public IdsDocBuilder(XMLStreamWriter writer) {
		super(writer);
	}

	public void createStartElement(String docId) throws XMLStreamException {
		writer.writeStartElement("idsDoc");
		writer.writeAttribute("type", "text");
		writer.writeAttribute("version", "1.0");
		writer.writeAttribute("id", docId);
		writer.flush();
	}

	public void createIdsHeader(String docSigle, String docTitle)
			throws XMLStreamException {
		writer.writeStartElement("idsHeader");
		writer.writeAttribute("type", "document");
		writer.writeAttribute("pattern", "text");
		writer.writeAttribute("version", "1.0");

		createFileDesc(docSigle, docTitle);
		writer.writeEndElement();
		writer.flush();
	}

	private void createFileDesc(String docSigle, String docTitle)
			throws XMLStreamException {

		writer.writeStartElement("fileDesc");
		createTitleStmt(docSigle, docTitle);
		createPublicationStmt();
		createSourceDesc();
		writer.writeEndElement();
	}

	private void createTitleStmt(String docSigle, String docTitle)
			throws XMLStreamException {
		writer.writeStartElement("titleStmt");
		createSimpleElement("dokumentSigle", docSigle);
		createSimpleElement("d.title", docTitle);
		writer.writeEndElement();
	}

	public String createIdsDocTitle(String pageType, String index, int docNr) {

		StringBuilder sb = new StringBuilder();
		sb.append("Wikipedia, ");
		sb.append(titleTypes.get(pageType));
		sb.append(" mit ");

		if (isInteger(index)) {
			sb.append("Anfangszahl ");
		}
		else {
			sb.append("Anfangsbuchstabe ");
		}

		sb.append(index);
		sb.append(", Teil ");
		sb.append(String.format("%02d", docNr));

		return sb.toString();

	}

	private boolean isInteger(String s) {
		boolean isInteger = true;
		try {
			Integer.parseInt(s);
		}
		catch (Exception e) {
			isInteger = false;
		}
		return isInteger;
	}
}
