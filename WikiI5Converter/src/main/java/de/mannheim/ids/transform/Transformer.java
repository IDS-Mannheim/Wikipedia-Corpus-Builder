package de.mannheim.ids.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5ErrorHandler;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.Statistics;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Initializes an XSLT Transformer as a ThreadLocal, transforms wikiXML into I5
 * and returns the result as ByteArrayOutputStream.
 * 
 * @author margaretha
 *
 */
public class Transformer implements Callable<WikiI5Part> {

	private static final ThreadLocal<XsltTransformer> transformer = new ThreadLocal<XsltTransformer>() {

		protected XsltTransformer initialValue() {

			XsltCompiler compiler = processor.newXsltCompiler();
			compiler.setURIResolver(new TemplateURIResolver());

			XsltExecutable executable;
			try {
				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream("main-templates.xsl");
				executable = compiler.compile(new StreamSource(is));
				is.close();
			}
			catch (SaxonApiException | IOException e) {
				throw new RuntimeException(
						"Failed compiling the XSLT Stylesheet.", e);
			}

			XsltTransformer transformer = executable.load();
				transformer.setInitialTemplate(new QName("main"));
				
			transformer.setParameter(new QName("origfilename"),
					new XdmAtomicValue(config.getDumpFilename()));
			transformer.setParameter(new QName("lang"),
					new XdmAtomicValue(config.getLanguageCode()));
			transformer.setParameter(new QName("pubDay"), new XdmAtomicValue(
					config.getDumpFilename().substring(13, 15)));
			transformer.setParameter(new QName("pubMonth"), new XdmAtomicValue(
					config.getDumpFilename().substring(11, 13)));
			transformer.setParameter(new QName("pubYear"),
					new XdmAtomicValue(config.getYear()));
			transformer.setParameter(new QName("inflectives"),
					new XdmAtomicValue(config.getInflectives()));
			transformer.setParameter(new QName("encoding"),
					new XdmAtomicValue(config.getOutputEncoding()));
			transformer.setErrorListener(errorHandler);

			return transformer;
		}
	};

	private static final Processor processor = new Processor(true);
	static{
		processor.setConfigurationProperty(Feature.ALLOW_MULTITHREADING, false);
	}

	private String wikiXMLPath;
	private String index;
	private String pageId;

	private static Configuration config;
	private static I5ErrorHandler errorHandler;
	private Statistics statistics;

	private Logger logger = LogManager.getLogger(Transformer.class);

	/**
	 * Constructs a Transformer from the given variables.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param statistics
	 *            a statistic counter
	 * @param errorHandler
	 *            an an I5ErrorHandler
	 * @param wikiXMLFile
	 *            a wikiXML file
	 * @param index
	 *            the index file name of the wikipages
	 * @param pageId
	 *            the page id string
	 */
	public Transformer(Configuration config, Statistics statistics,
			I5ErrorHandler errorHandler, String wikiXMLFile, String index,
			String pageId) {
		Transformer.config = config;
		Transformer.errorHandler = errorHandler;

		this.wikiXMLPath = wikiXMLFile;
		this.index = index;
		this.pageId = pageId;
		this.statistics = statistics;
	}

	/**
	 * Returns a copy of the threadlocal XsltTransformer.
	 * 
	 * @return an XsltTransformer
	 */
	public static XsltTransformer getTransfomer() {
		return transformer.get();
	}

	@Override
	public WikiI5Part call() throws I5Exception, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 4);
		doTransformation(bos);
		logger.debug(bos);
		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		WikiI5Part w = new WikiI5Part(is, wikiXMLPath, pageId);
		bos.close();
		return w;
	}

	/**
	 * Creates a Destination for the Transformer, that serializes the
	 * transformation
	 * result as XML into the given OutputStream.
	 * 
	 * @param os
	 *            OutputStream
	 * @return a Destination
	 */
	private Destination createDestination(OutputStream os) {
		Serializer s = processor.newSerializer(os);
		s.setOutputProperty(Serializer.Property.METHOD, "xml");
		s.setOutputProperty(Serializer.Property.INDENT, "yes");
		s.setOutputProperty(Serializer.Property.SAXON_INDENT_SPACES, "1");
		s.setOutputProperty(Serializer.Property.SAXON_SUPPRESS_INDENTATION, "ref signed p item");
		s.setOutputProperty(Serializer.Property.SAXON_LINE_LENGTH, "512");
		s.setOutputProperty(Serializer.Property.ENCODING,
				config.getOutputEncoding());
		return s;
	}

	/**
	 * Performs the transformation and return the results in
	 * an OutputStream.
	 * 
	 * @return the transformation result in an OutputStream
	 * @throws I5Exception
	 */
	public void doTransformation(OutputStream os)
			throws I5Exception {
		InputStream is = null;
		String filepath = config.getWikiXMLFolder() + "/" + wikiXMLPath;
		try {
			is = new FileInputStream(new File(filepath));
			final StreamSource source = new StreamSource(is);

			XdmNode node = processor.newDocumentBuilder().build(source);
			final XsltTransformer transformer = getTransfomer();
			transformer.setInitialContextNode(node);
			transformer.setParameter(new QName("type"),
                    new XdmAtomicValue(config.getPageType()));
			transformer.setParameter(new QName("korpusSigle"),
                    new XdmAtomicValue(config.getKorpusSigle()));
			transformer.setParameter(new QName("letter"),
					new XdmAtomicValue(index));
			transformer.setParameter(new QName("pageId"),
					new XdmAtomicValue(pageId));

			Destination destination = createDestination(os);
			transformer.setDestination(destination);
			transformer.transform();
		}
        catch (SaxonApiException e) {
            statistics.addTransformationError();
            errorHandler.write(wikiXMLPath, "Tranformation error. ", e);
            //			os = null;
            throw new I5Exception(
                    "Transformation error has occurred in processing "
                            + filepath,
                    e);
        }
		catch (IOException e) {
			errorHandler.write(wikiXMLPath,
					"Failed reading " + filepath, e);
			throw new I5Exception("Failed reading a WikiXML file "
					+filepath , e);
		}
		finally {
			try {
				if (is != null) {
					is.close();
				}
			}
			catch (IOException e) {
				errorHandler.write(wikiXMLPath,
						"Failed closing a WikiXML InputStream", e);
			}
		}
	}

}
