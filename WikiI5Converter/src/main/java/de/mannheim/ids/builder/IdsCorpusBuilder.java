package de.mannheim.ids.builder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.mannheim.ids.wiki.Configuration;

/** Creates an idsCorpus element and the idsHeader inside the idsCorpus element.
 *  
 * @author margaretha
 *
 */
public class IdsCorpusBuilder extends BaseBuilder {
	private Configuration config;
	private boolean setInitialRev = true;

	public static final Map<String, String> textTypes;
	static {
		textTypes = new HashMap<String, String>();
		textTypes.put("article", "Enzyklopädie");
		textTypes.put("talk", "Diskussionen zu Enzyklopädie-Artikeln");
		textTypes.put("user-talk", "Benutzerdiskussionen");
		textTypes.put("loeschkandidaten", "Löschkandidaten");
		textTypes.put("redundanz", "Redundanzdiskussionen");
	}

	public IdsCorpusBuilder(XMLStreamWriter writer,
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
		if (setInitialRev){
			createRevisionDesc();
		}
		writer.writeEndElement(); // idsHeader
	}
	
	/** FIX ME: Currently only generate an initial release by EM. The date/time does not 
	 * 	signify when the corpus is finally made, but the start of the corpus building.  
	 * 
	 * */
	private void createRevisionDesc() throws XMLStreamException{
		writer.writeStartElement("revisionDesc");
		writer.writeStartElement("listChange");
		
		writer.writeStartElement("change");
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");		
		writer.writeAttribute("when", df.format(new Date()));
		writer.writeAttribute("who", config.getCreator());
		writer.writeCharacters("public release");
		writer.writeEndElement(); // change
		
		writer.writeEndElement(); // listChange
		writer.writeEndElement(); // revisionDesc
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
		createSimpleElement("textType", textTypes.get(config.getPageType()));
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
						+ "Eliza Margaretha and Harald Lüngen (2014): Building Linguistic Corpora "
						+ "from Wikipedia Articles and Discussions. In: Journal of Language "
						+ "Technology and Computational Linguistics (JLCL) 29 (2). Special Issue on "
						+ "Building and Annotating Corpora of Computer-mediated Communication: "
						+ "Issues and Challenges at the Interface between Computational and Corpus "
						+ "Linguistics, edited by Michael Beißwenger, Nelleke Oostdijk, Angelika "
						+ "Storrer and Henk van den Heuvel. URL: "
						+ "http://www.jlcl.org/2014_Heft2/Heft2-2014.pdf");
						

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
		sb.append(titleTypes.get(config.getPageType()));
		return sb.toString();
	}

	@Override
	protected void createPublicationStmt() throws XMLStreamException {
		writer.writeStartElement("publicationStmt");

		createSimpleElement("distributor", "Leibniz-Institut für Deutsche Sprache");
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
				+ "Reference Corpora at IDS. It is published under the Creative Commons "
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
		createSimpleElement("editor", "Wikimedia Foundation");
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
		writer.writeStartElement("pubPlace");
		writer.writeStartElement("ref");
		writer.writeAttribute("target", "http://" + config.getLanguageCode() + ".wikipedia.org");
		writer.writeEndElement(); // ref
		writer.writeEndElement(); // pubPlace
		
		writer.writeStartElement("pubDate");
		writer.writeAttribute("type", "year");
		writer.writeCharacters(config.getYear());
		writer.writeEndElement(); // pubDate
		
		writer.writeStartElement("pubDate");
		writer.writeAttribute("type", "month");
		writer.writeCharacters(config.getDumpFilename().substring(11, 13));
		writer.writeEndElement(); // pubDate
		
		writer.writeStartElement("pubDate");
		writer.writeAttribute("type", "day");
		writer.writeCharacters(config.getDumpFilename().substring(13, 15));
		writer.writeEndElement(); // pubDate
		
		
		writer.writeEndElement(); // imprint
	}
}
