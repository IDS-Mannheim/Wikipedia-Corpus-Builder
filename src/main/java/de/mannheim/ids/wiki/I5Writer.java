package de.mannheim.ids.wiki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import de.mannheim.ids.builder.IdsCorpusBuilder;
import de.mannheim.ids.builder.IdsDocBuilder;
import de.mannheim.ids.builder.IdsTextBuilder;
import de.mannheim.ids.builder.IdsTextValidator;
import de.mannheim.ids.transform.WikiI5Part;

/**
 * Writes WikiI5Corpus output and validates its content against IDS I5 DTD.
 * 
 * @author margaretha
 *
 */
public class I5Writer {

	public static Logger logger = Logger.getLogger(I5Writer.class);

	private IdsTextBuilder idsTextBuilder;
	private XMLReader reader;
	private XMLReader validatingReader;
	private IndentingXMLStreamWriter writer;
	private I5ErrorHandler errorHandler;

	private Configuration config;
	private Statistics stats;

	private ByteArrayOutputStream os;

	private IdsTextValidator idsTextHandler;

	/**
	 * Constructs an I5Writer from the given variables.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param errorHandler
	 *            an an I5ErrorHandler
	 * @param statistics
	 *            a statistic counter
	 * @throws I5Exception
	 *             an I5Exception
	 */
	public I5Writer(Configuration config, I5ErrorHandler errorHandler,
			Statistics statistics) throws I5Exception {
		this.errorHandler = errorHandler;
		this.config = config;
		configureSAXParser();
		setWriter(config);
		//idsTextHandler = new IdsTextBuilder(config, writer);
		os = new ByteArrayOutputStream();
		idsTextBuilder = new IdsTextBuilder(config, os);
		idsTextHandler = new IdsTextValidator(config, writer);
		stats = statistics;
	}

	/**
	 * Creates the output file and an IndentingXMLStreamWriter for writing
	 * (XML-based) I5 into the output file.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @throws I5Exception
	 *             an I5Exception
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
			w = f.createXMLStreamWriter(
					new OutputStreamWriter(fos, config.getOutputEncoding()));
		}
		catch (UnsupportedEncodingException e) {
			throw new I5Exception(
					"Failed creating an OutputStreamWriter. Encoding"
							+ config.getOutputEncoding() + " is not supported.",
					e);
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed creating an XMLStreamWriter", e);
		}
		writer = new IndentingXMLStreamWriter(w);
		writer.setIndent(" ");
	}

	/**
	 * Creates a SAX parser and returns the XML reader inside the SAX parser.
	 * 
	 * @throws I5Exception
	 *             an I5Exception
	 */
	private void configureSAXParser() throws I5Exception {
		SAXParserFactory saxfactory = SAXParserFactory.newInstance();
		saxfactory.setValidating(false);
		saxfactory.setNamespaceAware(true);
		
		try {
			saxfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,
					false);
		}
		catch (SAXNotRecognizedException | SAXNotSupportedException
				| ParserConfigurationException e) {
			throw new I5Exception("Failed setting the secure processing "
					+ "feature to a sax factory.", e);
		}

		reader = createXMLReader(saxfactory);
		saxfactory.setValidating(true);
		validatingReader = createXMLReader(saxfactory);
		
	}
	
	private XMLReader createXMLReader(SAXParserFactory saxfactory) throws I5Exception {
		SAXParser parser = null;
		XMLReader reader;
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
		return reader;
	}

	/**
	 * Writes the content of WikiI5Part based on its type (startDoc, idstext or
	 * endDoc)
	 * 
	 * @param w
	 *            a WikiI5Part
	 * @throws I5Exception
	 *             an I5Exception
	 */
	public void write(WikiI5Part w) throws I5Exception {
		if (w == null) {
			throw new IllegalArgumentException("WikiI5Part cannot be null.");
		}

		synchronized (writer) {
			if (w.isIDSText()) {
				if (w.getBos() != null) {
//					logger.debug(w.getBos());

					if (parseIdsText(w)) {
						logger.debug(os);
						validateAgainstDTD(os,w);
						os.reset();
					}
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
							"Failed writing end idsDoc or idsCorpus end element.",
							e);
				} // idsCorpus
			}
		}
	}

	/**
	 * Writes the start document, the idsCorpus element and its corresponding
	 * idsHeader.
	 * 
	 * @throws I5Exception
	 *             an I5Exception
	 */
	public void writeStartDocument() throws I5Exception {

		try {
			synchronized (writer) {
				writer.writeStartDocument(config.getOutputEncoding(), "1.0");

				writer.writeDTD(
						"<!DOCTYPE idsCorpus PUBLIC \"-//IDS//DTD IDS-I5 1.0//EN\" "
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

	/**
	 * Creates and writes an idsDoc start element and its idsHeader from the
	 * given wikiI5Part object.
	 * 
	 * @param w
	 *            a WikiI5Part
	 * @throws I5Exception
	 *             an I5Exception
	 */
	private void writeStartIdsCorpus(WikiI5Part w) throws I5Exception {
		IdsDocBuilder db = new IdsDocBuilder(writer);
		String docTitle = db.createIdsDocTitle(config.getNamespaceKey(),
				w.getIndex(), w.getDocNr());

		String index = w.getIndex();
		String docNr = String.format("%02d", w.getDocNr());
		String docSigle = config.getKorpusSigle() + "/" + index + docNr;
		if (StringUtils.isNumeric(w.getIndex())) {
			index = "_" + index;
		}
		try {
			db.createStartElement(index + docNr);
			db.createIdsHeader(docSigle, docTitle);
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed writing idsCorpus start element.", e);
		}
	}

	private boolean validateAgainstDTD(ByteArrayOutputStream os, WikiI5Part w) throws I5Exception {

		SaxBuffer saxBuffer = new SaxBuffer();
		validatingReader.setContentHandler(saxBuffer);
		try {
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			InputSource inputSource = new InputSource(is);
			inputSource.setEncoding(config.getOutputEncoding());
			validatingReader.parse(inputSource);
			stats.addTransformedPages();
		}
		catch (SAXException | IOException e) {
			stats.addDtdValidationError();
			logger.debug(e);
			errorHandler.write(w.getWikiPath(), "DVD validation failed.", e);
			return false;
		}
		
		try {
			saxBuffer.toSAX(idsTextHandler);
		}
		catch (SAXException e) {
			stats.addSaxParserError();
			errorHandler.write(w.getWikiPath(), e.getMessage(), e);
		}
		return true;
	}
	
	/**
	 * Validates the transformation results against DTD by using the SAX parser.
	 * 
	 * @param w
	 *            idsText wikiI5Part
	 * @throws I5Exception
	 *             an I5Exception
	 */
	private boolean parseIdsText(WikiI5Part w) throws I5Exception {
		SaxBuffer saxBuffer = new SaxBuffer();
		reader.setContentHandler(saxBuffer);
		try {
			reader.setProperty("http://xml.org/sax/properties/lexical-handler",idsTextBuilder);
			InputStream is = new ByteArrayInputStream(w.getBos().toByteArray());
			InputSource inputSource = new InputSource(is);
			inputSource.setEncoding(config.getOutputEncoding());
			reader.parse(inputSource);
		}
		catch (SAXException | IOException e) {
			logger.debug(e);
			errorHandler.write(w.getWikiPath(), "Failed parsing IdsText.", e);
			return false;
		}
		
		writeIdsText(saxBuffer, w);
		return true;
	}

	/**
	 * Handles the transformation output of idsText using {@link IdsTextBuilder}
	 * and writes it to the final corpus file.
	 * 
	 * @param saxBuffer
	 *            a SaxBuffer
	 * @param w
	 *            a WikiI5Part
	 * @throws I5Exception
	 *             an I5Exception
	 */
	private void writeIdsText(SaxBuffer saxBuffer, WikiI5Part w)
			throws I5Exception {
		try {
			idsTextBuilder.setPageId(w.getPageId());
			saxBuffer.toSAX(idsTextBuilder);
			idsTextBuilder.clearReferences();
			idsTextBuilder.clearPtrIds();
			idsTextBuilder.resetNoteCounter();
		}
		catch (SAXException e) {
			stats.addSaxParserError();
			errorHandler.write(w.getWikiPath(), e.getMessage(), e);
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
