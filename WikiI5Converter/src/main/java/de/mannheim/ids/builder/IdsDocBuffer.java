package de.mannheim.ids.builder;

import javax.xml.stream.XMLStreamWriter;

import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.mannheim.ids.transform.WikiI5Part;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;

public class IdsDocBuffer {

    private SAXBuffer docBuffer;
    private Configuration config;

    public IdsDocBuffer (Configuration config, WikiI5Part w)
            throws SAXException {
        this.config = config;
        docBuffer = new SAXBuffer();
        writeToBuffer(w);
    }


    private void writeToBuffer (WikiI5Part w) throws SAXException {
        String docTitle = createIdsDocTitle(config.getPageType(), w.getIndex(),
                w.getDocNr());
        String index = w.getIndex();
        String docNr = String.format("%02d", w.getDocNr());
        String docSigle = config.getKorpusSigle() + "/" + index + docNr;
        if (StringUtils.isNumeric(w.getIndex())) {
            index = "_" + index;
        }

        createStartElement(index + docNr);
        createIdsHeader(docSigle, docTitle);
    }


    private void createStartElement (String docId) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "type", "", "text");
        attr.addAttribute("", "", "version", "", "1.0");
        attr.addAttribute("", "", "id", "", docId);
        docBuffer.startElement("", "idsDoc", "", attr);
    }


    private void createIdsHeader (String docSigle, String docTitle)
            throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "type", "", "document");
        attr.addAttribute("", "", "pattern", "", "text");
        attr.addAttribute("", "", "version", "", "1.0");

        docBuffer.startElement("", "idsHeader", "", attr);
        createFileDesc(docSigle, docTitle);
        docBuffer.endElement("", "idsHeader", "");
    }


    private void createFileDesc (String docSigle, String docTitle)
            throws SAXException {
        docBuffer.startElement("", "fileDesc", "", new AttributesImpl());
        createTitleStmt(docSigle, docTitle);
        createPublicationStmt();
        createSourceDesc();
        docBuffer.endElement("", "fileDesc", "");
    }


    private void createTitleStmt (String docSigle, String docTitle)
            throws SAXException {
        docBuffer.startElement("", "titleStmt", "", new AttributesImpl());
        createSimpleElement("dokumentSigle", docSigle);
        createSimpleElement("d.title", docTitle);
        docBuffer.endElement("", "titleStmt", "");
    }


    private void createSimpleElement (String elementName, String content)
            throws SAXException {
        docBuffer.startElement("", elementName, "", new AttributesImpl());
        docBuffer.characters(content.toCharArray(), 0, content.length());
        docBuffer.endElement("", elementName, "");
    }


    private void createPublicationStmt () throws SAXException {
        docBuffer.startElement("", "publicationStmt", "", new AttributesImpl());
        createEmptyElement("distributor");
        createEmptyElement("pubAddress");
        createAvailability();
        createEmptyElement("pubDate");
        docBuffer.endElement("", "publicationStmt", "");

    }


    private void createAvailability () throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "region", "", "world");
        attr.addAttribute("", "", "status", "", "free");
        String availability = "CC-BY-SA";
        docBuffer.startElement("", "availability", "", attr);
        docBuffer.characters(availability.toCharArray(), 0,
                availability.length());
        docBuffer.endElement("", "availability", "");

    }


    private void createEmptyElement (String elementName) throws SAXException {
        docBuffer.startElement("",elementName, "", new AttributesImpl());
        docBuffer.endElement("", elementName, "");
    }


    private void createSourceDesc () throws SAXException {
        docBuffer.startElement("", "sourceDesc", "", new AttributesImpl());
        docBuffer.startElement("", "biblStruct", "", new AttributesImpl());
        createMonogr();
        docBuffer.endElement("", "biblStruct", "");
        docBuffer.endElement("", "sourceDesc", "");
    }


    private void createMonogr () throws SAXException {
        docBuffer.startElement("", "monogr", "", new AttributesImpl());

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "type", "", "main");
        docBuffer.startElement("", "h.title", "", attr);
        docBuffer.endElement("", "h.title", "");

        createEmptyElement("imprint");
        docBuffer.endElement("", "monogr", "");
    }


    public String createIdsDocTitle (String pageType, String index, int docNr) {

        StringBuilder sb = new StringBuilder();
        sb.append("Wikipedia, ");
        sb.append(BaseBuilder.titleTypes.get(pageType));
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


    private boolean isInteger (String s) {
        boolean isInteger = true;
        try {
            Integer.parseInt(s);
        }
        catch (Exception e) {
            isInteger = false;
        }
        return isInteger;
    }


    public void writeStartDoc (XMLStreamWriter writer, String wikiPath)
            throws SAXException, I5Exception {
        docBuffer.toSAX(new IdsTextHandler(writer, wikiPath));
    }
}
