package de.mannheim.ids.transform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

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
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5ErrorHandler;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.Statistics;

/** Initializes an XSLT Transformer as a ThreadLocal, transforms wikiXML into I5 
 * 	and returns the result as ByteArrayOutputStream. 
 * @author margaretha
 *
 */
public class Transformer implements Callable<WikiI5Part> {

	private static final ThreadLocal<XsltTransformer> transformer = new ThreadLocal<XsltTransformer>() {

		protected XsltTransformer initialValue() {

			XsltCompiler compiler = processor.newXsltCompiler();
			compiler.setXsltLanguageVersion("3.0");
			compiler.setURIResolver(new TemplateURIResolver());

			XsltExecutable executable;
			try {
				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream("Templates.xsl");
				executable = compiler.compile(new StreamSource(is));
			}
			catch (SaxonApiException e) {
				throw new RuntimeException(
						"Failed compiling the XSLT Stylesheet.", e);
			}

			XsltTransformer transformer = executable.load();
			try {
				transformer.setInitialTemplate(new QName("main"));
			}
			catch (SaxonApiException e) {
				throw new RuntimeException(
						"Failed setting the initial template for "
								+ "a transformer.", e);
			}

			transformer.setParameter(new QName("type"), new XdmAtomicValue(
					config.getNamespaceKey()));
			transformer.setParameter(new QName("origfilename"),
					new XdmAtomicValue(config.getDumpFilename()));
			transformer.setParameter(new QName("korpusSigle"),
					new XdmAtomicValue(config.getKorpusSigle()));
			transformer.setParameter(new QName("lang"), new XdmAtomicValue(
					config.getLanguageCode()));
			transformer.setParameter(new QName("pubDay"), new XdmAtomicValue(
					config.getDumpFilename().substring(13, 15)));
			transformer.setParameter(new QName("pubMonth"), new XdmAtomicValue(
					config.getDumpFilename().substring(11, 13)));
			transformer.setParameter(new QName("pubYear"), new XdmAtomicValue(
					config.getYear()));
			transformer.setParameter(new QName("inflectives"),
					new XdmAtomicValue(config.getInflectives()));
			transformer.setParameter(new QName("encoding"), new XdmAtomicValue(
					config.getOutputEncoding()));
			transformer.setErrorListener(errorHandler);

			return transformer;
		}
	};

	private static final Processor processor = new Processor(true);

	private File wikiXML;
	private String index;
	private String pageId;

	private static Configuration config;
	private static I5ErrorHandler errorHandler;
	private Statistics statistics;

	/** Constructs a Transformer from the given variables.
	 * @param config
	 * @param statistics
	 * @param errorHandler
	 * @param wikiXMLFile
	 * @param index
	 * @param pageId
	 */
	public Transformer(Configuration config, Statistics statistics, I5ErrorHandler 
			errorHandler, File wikiXMLFile, String index, String pageId) {
		this.config = config;
		this.wikiXML = wikiXMLFile;
		this.index = index;
		this.pageId = pageId;
		this.errorHandler = errorHandler;
		this.statistics = statistics;
	}

	/** Returns a copy of the threadlocal XsltTransformer. 
	 * @return an XsltTransformer
	 */
	public static XsltTransformer getTransfomer() {
		return transformer.get();
	}

	@Override
	public WikiI5Part call() throws Exception {
		InputStream is = null;
		WikiI5Part w = null;
		ByteArrayOutputStream bos = null;
		try {
			is = new FileInputStream(config.getWikiXMLFolder() + "/" + wikiXML);
			StreamSource source = new StreamSource(is);
			bos = doTransformation(source);
			w = new WikiI5Part(bos, wikiXML, pageId);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		finally {
			is.close();
		}
		return w;
	}

	/** Performs the transformation and return the results in ByteArrayOutputStream. 
	 * @param source
	 * @return the transformation result in ByteArrayOutputStream
	 * @throws I5Exception
	 */
	private ByteArrayOutputStream doTransformation(Source source) throws I5Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 4);
		try {
			XdmNode node = processor.newDocumentBuilder().build(source);
			XsltTransformer transformer = getTransfomer();
			transformer.setInitialContextNode(node);
			transformer.setParameter(new QName("letter"), new XdmAtomicValue(
					index));

			Destination destination = createDestination(bos);
			transformer.setDestination(destination);
			transformer.transform();
		}
		catch (SaxonApiException e) {	
			statistics.addTransformationError();
			errorHandler.write(wikiXML.getPath(), "Tranformation error. ", e);
			bos=null;
		}		
		
		return bos;
	}

	/** Creates a Destination for the Transformer, that serializes the transformation 
	 * 	result as XML into the given OutputStream.
	 * 
	 * @param os OutputStream
	 * @return a Destination
	 */
	private Destination createDestination(OutputStream os) {
		Serializer d = new Serializer(os);
		d.setOutputProperty(Serializer.Property.METHOD, "xml");
		d.setOutputProperty(Serializer.Property.INDENT, "yes");
		d.setOutputProperty(Serializer.Property.ENCODING, config.getOutputEncoding());
		return d;
	}
}
