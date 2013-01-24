package de.mannheim.ids.wiki;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.LinkTargetException;

import de.fau.cs.osr.ptk.common.ast.AstNode;

/* This class generates an Abstract Syntax Tree representation (AST) representation of the wikitext
 * using the Sweble Parser and eventually generates an XML representation using a visitor class.
 * */
public class SwebleParser {
	
	public CharSequence parseText(String wikitext) 
			throws FileNotFoundException, JAXBException, LinkTargetException, CompilerException {
				
		SimpleWikiConfiguration config = new SimpleWikiConfiguration(
		        "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");			
		
		// Instantiate Sweble parser compiler
		Compiler compiler = new Compiler(config);
		
		// Dummy pagetitle
		PageTitle pageTitle = PageTitle.make(config, "Page Title");		
		PageId pageId = new PageId(pageTitle, -1);

		// Compile wikitext to AST
		CompiledPage cp = compiler.postprocess(pageId, wikitext, null);
		
		// Render AST to XML
		StringWriter sw = new StringWriter();
		WikiVisitor visitor = new WikiVisitor(sw);
		
		//long startTime = System.nanoTime();		
		visitor.go(cp.getPage());
		//long endTime = System.nanoTime();
		//long duration = endTime - startTime;
		//System.out.println("Visitor execution time "+duration);
		//System.out.println(cp+"\n");	
		
		return sw.getBuffer();
	}	
}
