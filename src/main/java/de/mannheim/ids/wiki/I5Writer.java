package de.mannheim.ids.wiki;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cocoon.xml.SaxBuffer;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import de.mannheim.ids.builder.IdsCorpusBuilder;
import de.mannheim.ids.builder.IdsDocBuilder;
import de.mannheim.ids.builder.IdsTextBuilder;
import de.mannheim.ids.transform.WikiI5Part;

/** Creates the WikiI5Corpus output file and also validates the content.
 * 
 * @author margaretha
 *
 */
public class I5Writer {
	private IdsTextBuilder idsTextHandler;
	private XMLReader reader;
	private IndentingXMLStreamWriter writer;
	private I5ErrorHandler errorHandler;

	private Configuration config;
	private Statistics stats;

	/** Constructs I5Writer from the given variables.
	 * @param config
	 * @param errorHandler
	 * @param statistics
	 * @throws I5Exception
	 */
	public I5Writer(Configuration config, I5ErrorHandler errorHandler, Statistics statistics) throws I5Exception {		
		this.errorHandler = errorHandler;
		this.config = config;
		setXMLReader();
		setWriter(config);
		idsTextHandler = new IdsTextBuilder(config, writer);	
		stats = statistics;
	}

	/** Creates the output file and an IndentingXMLStreamWriter for writing (XML-based) 
	 * I5 into the output file.
	 * 
	 * @param config
	 * @throws I5Exception
	 */
	private void setWriter(Configuration config) throws I5Exception {
		File file = new File(config.getOutputFile());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		}
		catch (FileNotFoundException e) {
			throw new I5Exception(
					"Failed creating FileOutputStream. File is not found.", e);
		}

		XMLOutputFactory f = XMLOutputFactory.newInstance();
		XMLStreamWriter w = null;
		try {
			w = f.createXMLStreamWriter(new OutputStreamWriter(fos, config
					.getOutputEncoding()));
		}
		catch (UnsupportedEncodingException e) {
			throw new I5Exception(
					"Failed creating an OutputStreamWriter. Encoding"
							+ config.getOutputEncoding() + " is not supported.", e);
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed creating an XMLStreamWriter", e);
		}
		writer = new IndentingXMLStreamWriter(w);
		writer.setIndent(" ");
	}

	/** Creates a SAX parser and returns the XML reader inside the SAX parser. 
	 * @throws I5Exception
	 */
	private void setXMLReader() throws I5Exception {
		SAXParserFactory saxfactory = SAXParserFactory.newInstance();
		saxfactory.setValidating(true);
		saxfactory.setNamespaceAware(true);

		try {
			saxfactory
					.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
		}
		catch (SAXNotRecognizedException | SAXNotSupportedException
				| ParserConfigurationException e) {
			throw new I5Exception("Failed setting the secure processing "
					+ "feature to a sax factory.", e);
		}

		SAXParser parser = null;
		try {
			parser = saxfactory.newSAXParser();
		}
		catch (ParserConfigurationException | SAXException e) {
			throw new I5Exception("Failed creating a SAX parser.", e);
		}

		try {
			reader = parser.getXMLReader();
		}
		catch (SAXException e) {
			throw new I5Exception(
					"Failed getting the XML Reader from a SAX parser.", e);
		}
		reader.setErrorHandler(errorHandler);
	}

	/** Writes the content of WikiI5Part based on its type (startDoc, idstext or endDoc)
	 * @param w
	 * @throws I5Exception
	 */
	public void write(WikiI5Part w) throws I5Exception {
		if (w == null) {
			throw new IllegalArgumentException("WikiI5Part cannot be null.");
		}

		if (w.isIDSText()) {
			if (w.getBos() != null){
				validateDTD(w);			
			}			
		}
		else if (w.isStartDoc()) {			
			writeStartIdsCorpus(w);
		}
		else {
			try {
				writer.writeEndElement();
				writer.flush();
			}
			catch (XMLStreamException e) {
				throw new I5Exception(
						"Failed writing end idsCorpus end element.", e);
			} // idsCorpus	
		}
	}

	/** Writes the start document, the idsCorpus element and its corresponding idsHeader.
	 * @throws I5Exception
	 */
	public synchronized void writeStartDocument() throws I5Exception {

		try {
			synchronized (writer) {
				writer.writeStartDocument(config.getOutputEncoding(), "1.0");

				writer.writeDTD("<!DOCTYPE idsCorpus PUBLIC \"-//IDS//DTD IDS-I5 1.0//EN\" "
						+ "\"http://corpora.ids-mannheim.de/I5/DTD/i5.dtd\">");

				IdsCorpusBuilder cb = new IdsCorpusBuilder(writer, config);
				cb.createIdsCorpusStartElement();
				writer.flush();
			}
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed writing start document.", e);
		}
	}

	/** Creates and writes an idsDoc start element and its idsHeader from the given wikiI5Part object.
	 * @param w
	 * @throws I5Exception
	 */
	private synchronized void writeStartIdsCorpus(WikiI5Part w)
			throws I5Exception {
		IdsDocBuilder db = new IdsDocBuilder(writer);
		String docTitle = db.createIdsDocTitle(config.getNamespaceKey(),
				w.getIndex(), w.getDocNr());
		
		String index = w.getIndex();		
		String docNr = String.format("%02d", w.getDocNr());
		String docSigle = config.getKorpusSigle() + "/" + index + docNr;
		try {
			if (StringUtils.isNumeric(w.getIndex())){
				index = "_"+index;
			}	
			db.createStartElement(index + docNr);
			db.createIdsHeader(docSigle, docTitle);
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed writing idsCorpus start element.", e);
		}
	}

	/** Validates the transformation results against DTD by using the SAX parser. 
	 * 
	 * @param w idsText wikiI5Part
	 * @throws I5Exception
	 */
	public void validateDTD(WikiI5Part w) throws I5Exception {
		SaxBuffer saxBuffer = new SaxBuffer();
		reader.setContentHandler(saxBuffer);
		
		try {
			InputStream is = new ByteArrayInputStream(w.getBos().toByteArray());
			reader.parse(new InputSource(is));
		}
		catch (SAXException | IOException e) {
			stats.addDtdValidationError();
			//System.out.println(w.getBos());
			errorHandler.write(w.getWikiPath(), "DVD validation failed.", e);		
			return;
		}

		try {
			idsTextHandler.setPageId(w.getPageId());
			saxBuffer.toSAX(idsTextHandler);
			stats.addTransformedPages();
		}
		catch (SAXException e) {
			stats.addSaxParserError();
			errorHandler.write(w.getWikiPath(),
					e.getMessage(), e);			
		}
	}

	public void close() throws I5Exception {
		try {
			writer.writeEndDocument();
			writer.close();			
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed closing document.", e);
		}
	}
}
