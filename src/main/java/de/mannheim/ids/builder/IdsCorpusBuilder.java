package de.mannheim.ids.builder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

import de.mannheim.ids.wiki.Configuration;

public class IdsCorpusBuilder extends BaseBuilder {
	private Configuration config;

	public static final Map<Integer, String> textTypes;
	static {
		textTypes = new HashMap<Integer, String>();
		textTypes.put(0, "Enzyklopädie");
		textTypes.put(1, "Diskussionen zu Enzyklopädie-Artikeln");
		textTypes.put(3, "Benutzerdiskussionen");
	}

	public IdsCorpusBuilder(IndentingXMLStreamWriter writer,
			Configuration config) {
		super(writer);
		this.config = config;
	}

	public void createIdsCorpusStartElement() throws XMLStreamException {
		writer.writeStartElement("idsCorpus");
		writer.writeAttribute("version", "1.0");
		writer.writeAttribute("TEIform", "teiCorpus.2");
		createCorpusHeader();
	}

	private void createCorpusHeader() throws XMLStreamException {
		writer.writeStartElement("idsHeader");
		writer.writeAttribute("type", "corpus");
		writer.writeAttribute("pattern", "allesaußerZtg/Zschr");
		writer.writeAttribute("status", "new");
		writer.writeAttribute("version", "1.0");
		writer.writeAttribute("TEIform", "teiHeader");

		createFileDesc();
		createEncodingDesc();
		createProfileDesc();

		writer.writeEndElement(); // idsHeader
	}

	private void createProfileDesc() throws XMLStreamException {
		writer.writeStartElement("profileDesc");
		createLanguageUsage();
		createTextDesc();
		writer.writeEndElement(); // profileDesc
	}

	private void createLanguageUsage() throws XMLStreamException {
		writer.writeStartElement("langUsage");
		writer.writeStartElement("language");
		writer.writeAttribute("id", config.getLanguageCode());
		writer.writeAttribute("usage", "100");
		writer.writeCharacters(config.getLanguage());
		writer.writeEndElement(); // language
		writer.writeEndElement(); // langUsage
	}

	private void createTextDesc() throws XMLStreamException {
		writer.writeStartElement("textDesc");
		createSimpleElement("textType", textTypes.get(config.getNamespaceKey()));
		writer.writeEmptyElement("textTypeRef");
		writer.writeEndElement(); // textDesc
	}

	private void createEncodingDesc() throws XMLStreamException {
		writer.writeStartElement("encodingDesc");
		writer.writeStartElement("editorialDecl");

		createSimpleElement("conformance", "This document conforms to I5 "
				+ "(see http://jtei.revues.org/508)");

		createSimpleElement(
				"transduction",
				"This document has been generated via a two-stage conversion by Eliza Margaretha. "
						+ "In the first stage, wikitext from a Wikidump is converted into WikiXML "
						+ "by the WikiXMLConverter tool and in the second stage, WikiXML is converted into "
						+ "I5 by the WikiI5Converter tool. The converters are available at "
						+ "http://corpora.ids-mannheim.de/pub/tools/. Reference: "
						+ "Building Linguistic Corpora from Wikipedia Articles and Discussions. "
						+ "In: Beißwenger, Michael/Oostdijk, Nelleke/Storrer, Angelika/van "
						+ "den Heuvel, Henk (Hrsg.): Building and Annotating Corpora of "
						+ "Computer-mediated Communication: Issues and Challenges at the "
						+ "Interface between Computational and Corpus Linguistics. "
						+ "S. 59-82 - Regensburg: GSCL, 2014.");

		writer.writeEndElement(); // editorialDecl
		writer.writeEndElement(); // encodingDesc

	}

	private void createFileDesc() throws XMLStreamException {
		writer.writeStartElement("fileDesc");
		createTitleStmt();

		writer.writeStartElement("editionStmt");
		writer.writeAttribute("version", "1.0");
		writer.writeEndElement(); // editionStmt

		createPublicationStmt();
		createSourceDesc();
		writer.writeEndElement(); // fileDesc
	}

	private void createTitleStmt() throws XMLStreamException {
		writer.writeStartElement("titleStmt");
		createSimpleElement("korpusSigle", config.getKorpusSigle());
		createSimpleElement("c.title", createCorpusTitle());
		writer.writeEndElement();

	}

	private String createCorpusTitle() {
		StringBuilder sb = new StringBuilder();
		sb.append("Wikipedia.");
		sb.append(config.getLanguageCode());
		sb.append(" ");
		sb.append(config.getYear());
		sb.append(" ");
		sb.append(titleTypes.get(config.getNamespaceKey()));
		return sb.toString();
	}

	@Override
	protected void createPublicationStmt() throws XMLStreamException {
		writer.writeStartElement("publicationStmt");

		createSimpleElement("distributor", "Institut für Deutsche Sprache");
		createSimpleElement("pubAddress", "Postfach 10 16 21, D-68016 Mannheim");
		createSimpleElement("telephone", "+49 (0)621 1581 0");
		createEAddress("www", "http://www.ids-mannheim.de");
		createEAddress("www", "http://www.ids-mannheim.de/kl/projekte/korpora/");
		createEAddress("email", "dereko@ids-mannheim.de");
		createAvailability();

		writer.writeStartElement("pubDate");
		writer.writeAttribute("type", "year");
		writer.writeCharacters(String.valueOf(Calendar.getInstance().get(
				Calendar.YEAR)));
		writer.writeEndElement(); // pubDate

		writer.writeEndElement(); // publicationStmt
	}

	private void createEAddress(String type, String value)
			throws XMLStreamException {
		writer.writeStartElement("eAddress");
		writer.writeAttribute("type", type);
		writer.writeCharacters(value);
		writer.writeEndElement();
	}

	private void createAvailability() throws XMLStreamException {
		writer.writeStartElement("availability");
		writer.writeAttribute("status", "restricted");
		writer.writeCharacters("This document, "
				+ "the IDS-Wikipedia."
				+ config.getLanguageCode()
				+ "-Corpus, is part of the Archive of General "
				+ "Reference Corpora at the IDS. It is published under the Creative Commons "
				+ "Attribution-ShareAlike License. See http://creativecommons.org/licenses/"
				+ "by-sa/3.0/legalcode for details. See http://www.ids-mannheim.de/kl/projekte/"
				+ "korpora/releases.html on how to refer to this document.");
		writer.writeEndElement();
	}

	@Override
	protected void createSourceDesc() throws XMLStreamException {
		writer.writeStartElement("sourceDesc");

		writer.writeStartElement("biblStruct");
		writer.writeAttribute("Default", "n");
		createMonogr();
		writer.writeEndElement(); // biblStruct

		writer.writeEndElement(); // sourceDesc
	}

	private void createMonogr() throws XMLStreamException {
		writer.writeStartElement("monogr");

		writer.writeStartElement("h.title");
		writer.writeAttribute("type", "main");
		writer.writeCharacters("Wikipedia");
		writer.writeEndElement(); // h.title

		writer.writeEmptyElement("h.author");
		createSimpleElement("editor", "wikipedia.org");
		createEdition();
		createImprint();
		writer.writeEndElement(); // monogr
	}

	private void createEdition() throws XMLStreamException {
		writer.writeStartElement("edition");

		writer.writeStartElement("further");
		writer.writeCharacters("Dump file \"");
		writer.writeCharacters(config.getDumpFilename());
		writer.writeCharacters("\" retrieved from http://dumps.wikimedia.org");
		writer.writeEndElement(); // further

		writer.writeEmptyElement("kind");
		writer.writeEmptyElement("appearance");
		writer.writeEndElement(); // edition

	}

	private void createImprint() throws XMLStreamException {
		writer.writeStartElement("imprint");

		createSimpleElement("publisher", "Wikipedia");
		createSimpleElement("pubPlace",
				"URL:http://" + config.getLanguageCode() + ".wikipedia.org");

		writer.writeEndElement(); // imprint
	}
}
