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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import de.mannheim.ids.builder.IdsCorpusBuilder;
import de.mannheim.ids.builder.IdsDocBuilder;
import de.mannheim.ids.builder.IdsTextBuilder;
import de.mannheim.ids.transform.WikiI5Part;

public class I5Writer {
	private IdsTextBuilder idsTextHandler;
	private XMLReader reader;
	private IndentingXMLStreamWriter writer;
	private I5ErrorHandler errorHandler;
	private SaxBuffer saxBuffer;
	private Configuration config;

	public I5Writer(Configuration config) throws I5Exception {
		setWriter(config);
		idsTextHandler = new IdsTextBuilder(config, writer);

		errorHandler = new I5ErrorHandler(config);
		saxBuffer = new SaxBuffer();
		setXMLReader();

		this.config = config;
	}

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
					.getEncoding()));
		}
		catch (UnsupportedEncodingException e) {
			throw new I5Exception(
					"Failed creating an OutputStreamWriter. Encoding"
							+ config.getEncoding() + " is not supported.", e);
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed creating an XMLStreamWriter", e);
		}
		writer = new IndentingXMLStreamWriter(w);
		writer.setIndent("  ");
	}

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

		reader.setContentHandler(saxBuffer);
		reader.setErrorHandler(errorHandler);
	}

	public void write(WikiI5Part w) throws I5Exception {
		if (w == null) {
			throw new IllegalArgumentException("WikiI5Part cannot be null.");
		}

		if (w.isIDSText()) {
			validateDTD(w);
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

	public synchronized void writeStartDocument() throws I5Exception {

		try {
			synchronized (writer) {
				writer.writeStartDocument("iso-8859-1", "1.0");

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

	private synchronized void writeStartIdsCorpus(WikiI5Part w)
			throws I5Exception {
		IdsDocBuilder db = new IdsDocBuilder(writer);
		String docTitle = db.createIdsDocTitle(config.getNamespaceKey(),
				w.getIndex(), w.getDocNr());
		String docId = w.getIndex() + String.format("%02d", w.getDocNr());
		String docSigle = config.getKorpusSigle() + "/" + docId;
		try {
			db.createStartElement(docId);
			db.createIdsHeader(docSigle, docTitle);
			writer.flush();
		}
		catch (XMLStreamException e) {
			Thread.currentThread().interrupt();
			throw new I5Exception("Failed writing idsCorpus start element.", e);
		}
	}

	public void validateDTD(WikiI5Part w) throws I5Exception {

		try {
			InputStream is = new ByteArrayInputStream(w.getBos().toByteArray());
			reader.parse(new InputSource(is));
		}
		catch (SAXException | IOException e) {
			errorHandler.write(w.getWikiPath(), "DVD validation failed.", e);
			// throw new I5Exception("Failed parsing: " + w.getWikiPath(), e);
		}

		try {
			idsTextHandler.setPageId(w.getPageId());
			saxBuffer.toSAX(idsTextHandler);
		}
		catch (SAXException e) {
			errorHandler.write(w.getWikiPath(),
					"Failed transferring SAXBuffer to SAX.", e);
			// throw new I5Exception("Failed transferring SAXBuffer to SAX for "
			// + w.getWikiPath(), e);
		}
	}

	public void close() throws I5Exception {
		try {
			writer.writeEndDocument();
			writer.close();
			errorHandler.close();
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed closing document.", e);
		}
	}
}
