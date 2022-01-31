package de.mannheim.ids.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import de.mannheim.ids.builder.IdsTextBuffer;
import de.mannheim.ids.builder.IdsTextBuilder;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5ErrorHandler;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.I5Writer;
import de.mannheim.ids.wiki.Statistics;
import de.mannheim.ids.wiki.WikiI5Processor;

/**
 * TaskRunner handles transformation of idsText,
 * 
 * @author margaretha
 *
 */
public class TaskRunner implements Callable<WikiI5Part> {

    public static String LOCAL_DOCTYPE = "<!DOCTYPE idsText PUBLIC \"-//IDS//DTD IDS-I5 "
            + "1.0//EN\" \"dtd/i5.dtd\">";

    private XMLReader reader;
    private XMLReader validatingReader;

    private I5ErrorHandler errorHandler;
    private Configuration config;
    private Statistics statistics;

    private String wikiXMLPath;
    private String index;
    private String pageId;

    public static Logger logger = LogManager.getLogger(I5Writer.class);

    public TaskRunner (Configuration config, I5ErrorHandler errorHandler,
                       Statistics statistics, String wikiXMLPath, String index,
                       String pageId)
            throws I5Exception {
        this.config = config;
        this.errorHandler = errorHandler;
        this.statistics = statistics;

        this.wikiXMLPath = wikiXMLPath;
        this.index = index;
        this.pageId = pageId;
    }


    @Override
    public WikiI5Part call () throws Exception {

        // XSLT tranformation
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 4);
        doXsltTransformation(bos, wikiXMLPath, index, pageId);

        IdsTextBuffer idsTextBuffer = new IdsTextBuffer(config);
        idsTextBuffer.setPageId(pageId);

        // Collecting category and footnote events
        InputStream is = new ByteArrayInputStream(bos.toByteArray());
        bos.close();
        if (is != null) {
            InputSource inputSource = new InputSource(is);
            collectEvents(inputSource, pageId, wikiXMLPath, idsTextBuffer);
            is.close();
        }

        // Storing categories
        if (config.getPageType().equals("article")
                && config.storeCategories()) {
            storeCategories(wikiXMLPath, idsTextBuffer);
        }

        // Adding events and language links
        ByteArrayOutputStream idsTextOutputStream = new ByteArrayOutputStream(
                1024 * 4);
        SAXBuffer extendedBuffer = new SAXBuffer(addEvents(idsTextOutputStream,
                pageId, wikiXMLPath, idsTextBuffer));

        idsTextBuffer.clearReferences();
        idsTextBuffer.clearCategories();
        idsTextBuffer.getCategories().clear();
        idsTextBuffer.recycle();

        // validate idsText
        byte[] idsTextBytes = ArrayUtils.addAll(LOCAL_DOCTYPE.getBytes(),
                idsTextOutputStream.toByteArray());
        idsTextOutputStream.close();

        validateAgainstDTD(idsTextBytes, wikiXMLPath);

        WikiI5Part w = new WikiI5Part(wikiXMLPath, pageId, extendedBuffer);
        return w;
    }


    /**
     * Runs XSLT transformation for the speficied wikiXML and writes
     * the results into the given ByteArrayOutputStream.
     * 
     * @param bos
     * @param wikiXMLPath
     * @param index
     * @param pageId
     * @throws I5Exception
     */
    private void doXsltTransformation (ByteArrayOutputStream bos,
            String wikiXMLPath, String index, String pageId)
            throws I5Exception {
        Transformer t = new Transformer(config, statistics, errorHandler,
                wikiXMLPath, index, pageId);
        t.doTransformation(bos);
    }


    /**
     * Parses the transformation results and create events for
     * categories and footnotes. The events are stored in
     * IdsTextBuffer and will be added to idsText in {@link
     * #addEvents(OutputStream, String, String, IdsTextBuffer)}.
     * 
     * @param inputSource
     * @param pageId
     * @param pagePath
     * @param idsTextBuffer
     * @throws I5Exception
     * @throws InterruptedException
     */
    private void collectEvents (InputSource inputSource, String pageId,
            String pagePath, IdsTextBuffer idsTextBuffer)
            throws I5Exception, InterruptedException {

        configureSAXParser();
        reader.setContentHandler(idsTextBuffer);
        try {
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                    idsTextBuffer);
            reader.parse(inputSource);
        }
        catch (SAXException | IOException e) {
            statistics.addSaxParserError();
            logger.debug(e);
            errorHandler.write(pagePath, "Failed parsing IdsText.", e);
            throw new InterruptedException(pagePath + ": " + e.getMessage());
        }
        catch (Exception e) {
            statistics.addSaxParserError();
            logger.debug(e);
            errorHandler.write(pagePath, "Failed parsing IdsText.", e);
            throw new I5Exception(pagePath + ": " + e.getMessage());
        }

        statistics.addTransformedPages();
        if (idsTextBuffer.isTextEmpty()) {
            statistics.addEmptyPages();
            throw new I5Exception("Page id: " + pageId + " has empty text.");
        }

        if (config.isDiscussion()) {
            try {
                idsTextBuffer.addCategoryEvents();
            }
            catch (SAXException | SQLException e) {
                statistics.addSaxParserError();
                logger.debug(e);
                errorHandler.write(pagePath, "Failed adding category events",
                        e);
            }
        }

    }


    /**
     * Stores categories to the database.
     * 
     * @param wikiXMLPath
     * @param idsTextBuffer
     * @throws I5Exception
     */
    private void storeCategories (String wikiXMLPath,
            IdsTextBuffer idsTextBuffer) throws I5Exception {
        for (String c : idsTextBuffer.getCategories()) {
            try {
                WikiI5Processor.dbManager.storeCategory(idsTextBuffer.getPageId(), c);
            }
            catch (SQLException e) {
                errorHandler.write(wikiXMLPath, "Failed storing category", e);
            }
        }

    }


    /**
     * Adds category events, footnotes and language links by streaming
     * the given IdsTextBuffer (a SaxBuffer) to IdsTextBuilder (a
     * content handler). The results are written into the given
     * idsTextOutputStream.
     * 
     * @param idsTextOutputStream
     * @param pageId
     * @param wikiXMLPath
     * @param idsTextBuffer
     * @return
     * @throws I5Exception
     */
    private SAXBuffer addEvents (OutputStream idsTextOutputStream,
            String pageId, String wikiXMLPath, IdsTextBuffer idsTextBuffer)
            throws I5Exception {

        IdsTextBuilder idsTextBuilder = new IdsTextBuilder(config,
                idsTextOutputStream, pageId, idsTextBuffer);

        try {
            idsTextBuffer.toSAX(idsTextBuilder);
        }
        catch (SAXException e) {
            statistics.addSaxParserError();
            logger.debug(e);
            errorHandler.write(wikiXMLPath, "Failed adding events.", e);
            // continue silently
        }

        return idsTextBuilder.getExtendedIdsText();
    }



    /**
     * Validates idsText by using a validating SAX parser to parse it.
     * Invalid idsTexts are not included in the I5 corpus.
     * 
     * @param idsTextBytes
     * @param wikiXMLPath
     * @throws I5Exception
     */
    private void validateAgainstDTD (byte[] idsTextBytes, String wikiXMLPath)
            throws I5Exception {

        SAXParserFactory saxfactory = SAXParserFactory.newInstance();
        if (!config.disableDTDValidation()) {
            saxfactory.setValidating(true);
        }
        validatingReader = createXMLReader(saxfactory);

        try {
            InputStream is = new ByteArrayInputStream(idsTextBytes);
            InputSource inputSource = new InputSource(is);
            inputSource.setEncoding(config.getOutputEncoding());

            validatingReader.parse(inputSource);
            is.close();
        }
        catch (SAXException | IOException e) {
            statistics.addDtdValidationError();
            logger.debug(e);
            errorHandler.write(wikiXMLPath, "DTD validation failed. \n"
            //+ new String(idsTextBytes)
                    , e);
            throw new I5Exception(wikiXMLPath + " DTD validation failed\n");
        }

        statistics.addValidPages();
    }


    /**
     * Creates a SAX parser and returns the XML reader inside the SAX
     * parser.
     * 
     * @throws I5Exception
     *             an I5Exception
     */
    private void configureSAXParser () throws I5Exception {
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
    }


    /**
     * Creates a SAXParser from the given {@link SAXParserFactory} and
     * return
     * the XMLReader of the parser.
     * 
     * @param saxfactory
     *            a {@link SAXParserFactory}
     * @return an XMLReader
     * @throws I5Exception
     *             I5Exception
     */
    private XMLReader createXMLReader (SAXParserFactory saxfactory)
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



}
