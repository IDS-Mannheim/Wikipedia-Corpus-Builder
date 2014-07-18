package de.mannheim.ids.wiki;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/** This class defines the transformation and validation procedures.
 * 
 * @author margaretha
 *
 */

public class WikiI5Processor {
	
	private I5ErrorHandler errorHandler;	
	
	private String[] indexList = {"A","B","C","D","E","F","G","H","I","J","K","L",
		    "M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
		    "0","1","2","3","4","5","6","7","8","9"};
	
	private Processor processor = new Processor(true);
	private Serializer serializer = new Serializer();	
	private XsltTransformer transformer;
	private DocumentBuilder xmlBuilder;
	
	private XPathFactory xPathFactory = XPathFactory.newInstance();
	private XPath xPath = xPathFactory.newXPath();
	private XPathExpression lastId,group;	
	private File xces;
	
	public String lang,korpusSigle,corpusTitle,textType,xmlFolder;
	private XMLReader reader;
	
	public WikiI5Processor(String xmlFolder, File xsl,String type, String dumpFilename, 
			String inflectives,String encoding) throws Exception {
		
		if (xmlFolder == null || xmlFolder.isEmpty()){
			throw new IllegalArgumentException("xmlfolder cannot be null or empty.");
		}
		if (xsl == null){
			throw new IllegalArgumentException("xsl cannot be null.");
		}
		if (type == null || type.isEmpty()){
			throw new IllegalArgumentException("type cannot be null or empty.");
		}
		if (dumpFilename == null || dumpFilename.isEmpty()){
			throw new IllegalArgumentException("Wikidump filename cannot be " +
			"null or empty.");
		}
		// set default encoding to utf-8
		if (encoding == null || encoding.isEmpty()){
			encoding = "utf-8";
		}
		
		
		this.xmlFolder=xmlFolder;		
		String origfilename = dumpFilename.substring(0,15);	//dewiki-20130728 
		String year = dumpFilename.substring(7,11);
		lang = dumpFilename.substring(0,2);
		korpusSigle = createKorpusSigle(type, lang.substring(0,1).toUpperCase(), 
				dumpFilename.substring(9,11));
		corpusTitle = createCorpusTitle(type,lang,year);
		textType = createTextType(type);
		
		//Setup XSLT serializer and compiler
		serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
		serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
		serializer.setOutputProperty(Serializer.Property.ENCODING, encoding);
		
		XsltCompiler compiler = processor.newXsltCompiler();
		compiler.setXsltLanguageVersion("3.0");
		XsltExecutable executable;
		try {
			executable = compiler.compile(new StreamSource(xsl));
		} catch (SaxonApiException e) {
			throw new SaxonApiException("Failed compiling the XSLT Stylesheet.");
		}		
		// Setup transformer
		setTransformer(executable, type, origfilename, dumpFilename, year, inflectives);
		// Setup temporary xces file
		xces = new File(lang+"wiki-"+type+"-temp.xces");
		// Initialize errorhandler
		errorHandler = new I5ErrorHandler(type,origfilename);
		// Setup documentbuilder for reading xml
		setXmlBuilder();		
		// Setup saxparser for DTD validation
		setSAXParser();
	}	
	
	private void setTransformer(XsltExecutable executable, String type, String origfilename,
			String dumpFilename, String year, String inflectives) throws SaxonApiException{
		transformer = executable.load();
		transformer.setDestination(serializer);
		try {
			transformer.setInitialTemplate(new QName("main"));
		} catch (SaxonApiException e) {
			throw new SaxonApiException("Failed setting the initial template for a transformer.");
		}
		transformer.setParameter(new QName("type"), new XdmAtomicValue(type));		
		transformer.setParameter(new QName("origfilename"), new XdmAtomicValue(origfilename));
		transformer.setParameter(new QName("korpusSigle"), new XdmAtomicValue(korpusSigle));		
		transformer.setParameter(new QName("lang"), new XdmAtomicValue(lang));
		transformer.setParameter(new QName("pubDay"), new XdmAtomicValue(dumpFilename.substring(11,13)));
		transformer.setParameter(new QName("pubMonth"), new XdmAtomicValue(dumpFilename.substring(13,15)));
		transformer.setParameter(new QName("pubYear"), new XdmAtomicValue(year));
		
		if (inflectives !=null && !inflectives.isEmpty())
			transformer.setParameter(new QName("inflectives"), new XdmAtomicValue("../"+inflectives));
		
	}
	
	private void setXmlBuilder() throws ParserConfigurationException{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			xmlBuilder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ParserConfigurationException("Failed building a document builder.");
		}
	}
	
	private void setSAXParser() throws Exception{
		
		SAXParserFactory saxfactory = SAXParserFactory.newInstance();		
		saxfactory.setValidating(true);
		saxfactory.setNamespaceAware(true);
		
		try {
			saxfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
		} catch (SAXNotRecognizedException | SAXNotSupportedException
				| ParserConfigurationException e1) {
			throw new Exception("Failed setting the secure processing " +
					"feature to a sax factory.");
		}
		
		SAXParser parser = null;		
		try {			
			parser = saxfactory.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			throw new Exception("Failed creating a SAX parser.");
		}
		
		try{
			reader = parser.getXMLReader();
		} catch (SAXException e) {
			throw new SAXException("Failed getting the XML reader from a SAX parser.");			
		}		
		//reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
		reader.setErrorHandler(errorHandler);
	}
	
	public void run(String path, String type, I5Writer i5Writer) throws 
		SAXException, IOException, XPathExpressionException, XMLStreamException {
		
		if (path == null || path.isEmpty()){
			throw new IllegalArgumentException("Path cannot be null or empty.");
		}
		if (type == null || type.isEmpty()){
			throw new IllegalArgumentException("Type cannot be null or empty.");
		}
		if (i5Writer == null){
			throw new IllegalArgumentException("I5 writer cannot be null.");
		}
		
		Document articleList = xmlBuilder.parse(path);				
		
		// Sort by index
		for (String index :indexList){
			lastId = xPath.compile(type+"/index[@value='"+index+"']/id[last()]");
			int n = (int)(double) lastId.evaluate(articleList, XPathConstants.NUMBER);
			if (n<1) continue; 
			
			// Group docs per 100000
			for (int i=0; i < n/100000+1; i++){
				int docNr = i;				
				String docSigle = index + String.format("%02d",docNr) ;
				System.out.println("DocId "+docSigle);				
				
				group = xPath.compile(type+"/index[@value='"+index+"']/id[xs:integer(xs:integer(.) div 100000) = "+docNr+"]");
				NodeList pagegroup = (NodeList) group.evaluate(articleList,XPathConstants.NODESET);
				
				if (pagegroup.getLength()<1) {continue;}
				
				i5Writer.createIdsDocStartElement(createDocId(index, docSigle));
				String docTitle = i5Writer.createIdsDocTitle(type, index, docNr);
				i5Writer.createIdsDocHeader(korpusSigle+"/"+docSigle, docTitle);		
								
				// Do transformation and validation for each page in the group
				for (int j = 0; j < pagegroup.getLength(); j++) {					
					String xmlPath= index+"/"+pagegroup.item(j).getTextContent()+".xml";
					System.out.println(xmlPath);	
					// Do XSLT transformation					
					transform(index,new File(xmlFolder+"/"+type+"/"+xmlPath));
					errorHandler.reset();
					// Validate the resulting xces file
					validate(xces);
					if (!errorHandler.isValid()) {											
						errorHandler.write(xmlPath);
						continue;												
					}
					// read and copy the xces content to the corpus file
					i5Writer.readIdsText(xces);					
				}				
				i5Writer.createIdsDocEndElement();						
			}		
		}
		errorHandler.close();
	}
	
	private String createDocId(String index,String docSigle) {
		try{
			Integer.parseInt(index);
			return "_"+docSigle;
		}
		catch (Exception e) {
			return docSigle;
		}		
	}
	
	private String createKorpusSigle(String type,String lang,String year) {
		if (type.equals("articles")) {
			return "WP"+lang+year;
		}
		return "WD"+lang+year;
	}
	
	private String createCorpusTitle(String type,String lang,String year) {
		if (type.equals("articles")) {
			return "Wikipedia."+lang+" "+year+" Artikel";
		}
		return "Wikipedia."+lang+" "+year+" Diskussionen";
	}
	
	private String createTextType(String type) {
		if (type.equals("articles")) {
			return "Enzyklopädie";
		}
		return "Diskussionen zu Enzyklopädie-Artikeln";
	}

	private void transform(String index,File xml) {		
		serializer.setOutputFile(xces);		
		try {			
			
			XdmNode source = processor.newDocumentBuilder().build(xml);			
			transformer.setInitialContextNode(source);
			transformer.setParameter(new QName("letter"), new XdmAtomicValue(index));
			transformer.transform();			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	private void validate(File xces) throws IOException {
		try {			
			reader.parse(xces.getName());			
		} catch (Exception e) {
			System.out.println("Invalid");
			e.printStackTrace();			
		}
	}
	
}
