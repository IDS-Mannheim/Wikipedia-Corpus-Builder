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
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import de.mannheim.ids.builder.IdsCorpusBuilder;
import de.mannheim.ids.builder.IdsDocBuilder;
import de.mannheim.ids.builder.IdsTextBuffer;
import de.mannheim.ids.builder.IdsTextBuilder;
import de.mannheim.ids.builder.IdsTextHandler;
import de.mannheim.ids.db.DatabaseManager;
import de.mannheim.ids.transform.WikiI5Part;
import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * Writes WikiI5Corpus output and validates its content against IDS I5 DTD.
 * 
 * @author margaretha
 *
 */
public class I5Writer {

	public static Logger logger = Logger.getLogger(I5Writer.class);
	
	public static String DOCTYPE = "<!DOCTYPE idsText PUBLIC \"-//IDS//DTD IDS-I5 "
			+ "1.0//EN\" \"dtd/i5.dtd\">";

	public static final Pattern invalidNativeCharPattern = Pattern
			.compile("&#xd[89a-f]..;");
	public static final String replacementChar = "&#xf8ff;";

	private XMLReader reader;
	private XMLReader validatingReader;
	private XMLStreamWriter writer;
	private I5ErrorHandler errorHandler;

	private Configuration config;
	private Statistics stats;

	private IdsTextBuffer idsTextBuffer;
	private IdsTextHandler idsTextHandler;

	private IdsDocBuilder idsDocBuilder;

	public static DatabaseManager dbManager;

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
		writer = createWriter(config);

		idsDocBuilder = new IdsDocBuilder(writer);
		idsTextBuffer = new IdsTextBuffer(config);
		idsTextHandler = new IdsTextHandler(writer);
		stats = statistics;

		try {
			dbManager = new DatabaseManager(config.getDatabaseUrl(),
					config.getDatabaseUsername(),
					config.getDatabasePassword(), config.getLanguageCode());
		}
		catch (SQLException e) {
			throw new I5Exception(
					"Failed configuring the database manager.", e);
		}
		
		if (config.getPageType().equals("article")){
			try {
				dbManager.createCategoryTable();
			}
			catch (SQLException e) {
				throw new I5Exception("Failed creating category table", e);
			}
		}
	}

	/**
	 * Creates the output file and an XMLStreamWriter for writing
	 * (XML-based) I5 into the output file.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @return 
	 * @throws I5Exception
	 *             an I5Exception
	 */
	private XMLStreamWriter createWriter(Configuration config)
			throws I5Exception {
		
		File i5 = new File("i5");
		if (!i5.exists()) {
			i5.mkdirs();
		}
		
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
			try {
				fos.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new I5Exception(
					"Failed creating an OutputStreamWriter. Encoding"
							+ config.getOutputEncoding() + " is not supported.",
					e);
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed creating an XMLStreamWriter", e);
		}
		
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(w);
		writer.setIndent(" ");
		return writer;
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
		
		if (!config.disableDTDValidation()){
			saxfactory.setValidating(true);
		}
		validatingReader = createXMLReader(saxfactory);
	}

	/**
	 * Creates a SAXParser from the given {@link SAXParserFactory} and return
	 * the XMLReader of the parser.
	 * 
	 * @param saxfactory
	 *            a {@link SAXParserFactory}
	 * @return an XMLReader
	 * @throws I5Exception
	 *             I5Exception
	 */
	private XMLReader createXMLReader(SAXParserFactory saxfactory)
			throws I5Exception {
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
				logger.info(w.getWikiPath());
				
//				String line="";
//				InputStream inputStream = w.getInputStream();
//				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {	
//					while ((line = bufferedReader.readLine()) != null) {
//						System.out.println(line);
//					}
//				}
//				catch (IOException e) {
//					e.printStackTrace();
//				}
				
				if (w.getInputStream() != null && parseIdsText(w)) {
					if (config.getPageType().equals("article")
							&& config.storeCategories()) {
						storeCategories(w.getWikiPath());
					}
					addEvents(w);
				}
				idsTextBuffer.recycle();
				w.close();
			}
			else if (w.isStartDoc()) {
				writeStartIdsDoc(w);
			}
			else {
				try {
					writer.writeEndElement();
					writer.flush();
				}
				catch (XMLStreamException e) {
					throw new I5Exception(
							"Failed writing idsDoc or idsCorpus end element.",
							e);
				} // idsCorpus
			}
		}
	}

	private void storeCategories(String xmlPath) throws I5Exception {
		for (String c: idsTextBuffer.getCategories()){
			try {
				I5Writer.dbManager.storeCategory(idsTextBuffer.getPageId(),
						c);
			}
			catch (SQLException e) {
				errorHandler.write(xmlPath, "Failed storing category", e);
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
	private void writeStartIdsDoc(WikiI5Part w) throws I5Exception {
		String docTitle = idsDocBuilder.createIdsDocTitle(
				config.getPageType(),
				w.getIndex(), w.getDocNr());

		String index = w.getIndex();
		String docNr = String.format("%02d", w.getDocNr());
		String docSigle = config.getKorpusSigle() + "/" + index + docNr;
		if (StringUtils.isNumeric(w.getIndex())) {
			index = "_" + index;
		}
		try {
			idsDocBuilder.createStartElement(index + docNr);
			idsDocBuilder.createIdsHeader(docSigle, docTitle);
			writer.flush();
		}
		catch (XMLStreamException e) {
			throw new I5Exception("Failed writing idsDoc start element.", e);
		}
	}

	/**
	 * Parses the transformation results, adds language links, parse refs and
	 * adds footnotes.
	 * 
	 * @param w
	 *            idsText wikiI5Part
	 * @throws I5Exception
	 *             an I5Exception
	 */
	private boolean parseIdsText(WikiI5Part w) throws I5Exception {

		idsTextBuffer.setPageId(w.getPageId());
		reader.setContentHandler(idsTextBuffer);
		try {
			reader.setProperty("http://xml.org/sax/properties/lexical-handler",
					idsTextBuffer);

			InputStream is = w.getInputStream();
			InputSource inputSource = new InputSource(is);
			reader.parse(inputSource);
			is.close();
		}
		catch (SAXException | IOException e) {
			stats.addSaxParserError();
			logger.debug(e);
			errorHandler.write(w.getWikiPath(), "Failed parsing IdsText.", e);
			return false;
		}
		
		stats.addTransformedPages();
		if (idsTextBuffer.isTextEmpty()){
			stats.addEmptyPages();
			return false;
		}
		
		if (config.isDiscussion()){
			try {
				idsTextBuffer.addCategoryEvents();
			}
			catch (SAXException | SQLException e) {
				stats.addSaxParserError();
				logger.debug(e);
				errorHandler.write(w.getWikiPath(), "Failed adding category events", e);
			}
		}
		return true;
	}

	private void addEvents(WikiI5Part w) throws I5Exception {
		ByteArrayOutputStream idsTextOutputStream = new ByteArrayOutputStream(
				1024 * 4);
		
		IdsTextBuilder idsTextBuilder = new IdsTextBuilder(config,
				idsTextOutputStream, w.getPageId(),
				idsTextBuffer);

		try {
			idsTextBuffer.toSAX(idsTextBuilder);
		}
		catch (SAXException e) {
			stats.addSaxParserError();
			logger.debug(e);
			errorHandler.write(w.getWikiPath(), "Failed adding events.", e);
		}
		
		byte[] idsTextBytes = ArrayUtils.addAll(DOCTYPE.getBytes(),
				idsTextOutputStream.toByteArray());
		try {
			idsTextOutputStream.close();
		}
		catch (IOException e) {
			logger.debug(e);
			errorHandler.write(w.getWikiPath(),
					"Failed closing idsTextOutputStream", e);
		}
		
		if (validateAgainstDTD(idsTextBytes, w.getWikiPath())) {
			writeIdsText(idsTextBuilder.getExtendedIdsText(), w.getWikiPath());
		}
		
		idsTextBuffer.clearReferences();
		idsTextBuffer.clearCategories();
		idsTextBuffer.getCategories().clear();
	}

	/** Validates idsText by using validating SAX parser to parse it. 
	 * The parsing results are not directly written in a I5 but first 
	 * collected in the given saxBuffer, so that invalid idsTexts are 
	 * not included in the I5 corpus.
	 * 
	 * @param idsTextOutputStream
	 * @param saxBuffer
	 * @param wikiXMLPath
	 * @return
	 * @throws I5Exception
	 */
	private boolean validateAgainstDTD(byte[] idsTextBytes, String wikiXMLPath)
			throws I5Exception {
		try {
			InputStream is = new ByteArrayInputStream(idsTextBytes);
			InputSource inputSource = new InputSource(is);
			inputSource.setEncoding(config.getOutputEncoding());
			
			validatingReader.parse(inputSource);
			is.close();
		}
		catch (SAXException | IOException e) {
			stats.addDtdValidationError();
			logger.debug(e);
			errorHandler.write(wikiXMLPath, "DTD validation failed. \n"
					+ idsTextBytes.toString(), e);
			return false;
		}

		stats.addValidPages();
		return true;
	}

	/** Writes idsText from the given saxBuffer.
	 * 
	 * @param saxBuffer
	 * @param wikiXMLPath
	 * @throws I5Exception
	 */
	private void writeIdsText(SAXBuffer saxBuffer, String wikiXMLPath)
			throws I5Exception {

		try {
			saxBuffer.toSAX(idsTextHandler);
		}
		catch (SAXException e) {
			stats.addSaxParserError();
			errorHandler.write(wikiXMLPath, "Failed writing idsText", e);
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
