package de.mannheim.ids.wiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class XCESWikiProcessor {
	
	public void transformToXCES(String xml, String xsl, String xces, String error) throws FileNotFoundException {				
		
		PrintStream ps = new PrintStream(new File(error));
		
		Processor processor = new Processor(true);
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable;
        XsltTransformer transformer = null;
        
		try {
			//compiler.setXsltLanguageVersion("3.0");
			executable = compiler.compile(new StreamSource(new File(xsl)));
		
        XdmNode source = processor.newDocumentBuilder().build(new StreamSource(new File(xml)));
        
        Serializer serializer = new Serializer();
        serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
        serializer.setOutputFile(new File(xces));
        
        transformer = executable.load();
        transformer.setInitialTemplate(new QName("main"));
        transformer.setInitialContextNode(source);
        transformer.setDestination(serializer);
        transformer.setParameter(new QName("filename"), new XdmAtomicValue("../"+xml));        
        transformer.transform();
        
		} catch (Exception e) {
			e.printStackTrace(ps);
		}
		
	}
}
