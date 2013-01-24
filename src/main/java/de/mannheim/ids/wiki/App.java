package de.mannheim.ids.wiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.ccil.cowan.tagsoup.CommandLine;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.HtmlPrinter;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.LinkTargetException;
import org.xml.sax.SAXException;

public class App
{
	public static void main(String[] args) throws FileNotFoundException, IOException, LinkTargetException, CompilerException, JAXBException
	{
		/*if (args.length < 1)
		{
			System.err.println("Usage: java -jar scm-example.jar TITLE");
			System.err.println();
			System.err.println("  The program will look for a file called `TITLE.wikitext',");
			System.err.println("  parse the file and write an HTML version to `TITLE.html'.");
			return;
		}		
				
		String fileTitle = args[0];*/
		String fileTitle = "TITLE";
		
		String test = "<x>=== Introduction des charges électriques === Le champ émis par une source " +
				"est nommé « champ retardé » Q<sup>R</sub>. Dépouillé de la source, ce champ n'est ''pas'' solution des EM.</x>" +
				"<x>=== Solutions physiques des équations de Maxwell. === Le champ</x>";
		
		test=test.replaceAll("</x>", "gh");
		System.out.println(test);
		

    String x = ":{|=== Introduction des :{|charges électriques === Le champ émis par une source ";
    String replace = "<x>";
    String y = "xyz"; 
    x = x.replaceAll(":\\{\\|", "");
    
    System.out.println(x);
    
		//String html = run(new File(fileTitle + ".wikitext"), fileTitle);
		
//		FileUtils.writeStringToFile(
//		        new File(fileTitle + ".html"),
//		        html);
	}
	
	static String run(File file, String fileTitle) throws FileNotFoundException, IOException, LinkTargetException, CompilerException, JAXBException
	{
		// Set-up a simple wiki configuration
		SimpleWikiConfiguration config = new SimpleWikiConfiguration(
		        "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
		
		// Instantiate a compiler for wiki pages
		Compiler compiler = new Compiler(config);
		
		// Retrieve a page
		PageTitle pageTitle = PageTitle.make(config, fileTitle);
		
		PageId pageId = new PageId(pageTitle, -1);
		
		String wikitext = FileUtils.readFileToString(file);
		StringEscapeUtils seu = new StringEscapeUtils();
		wikitext = seu.unescapeHtml(wikitext);
		System.out.println(wikitext);
		
		String[] options = {"--output-encoding=utf-8", "--omit-xml-declaration", "--nons", "TITLE.wikitext"};
		CommandLine tagsoup = new CommandLine();
		try {
			tagsoup.main(options);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Compile the retrieved page
		CompiledPage cp = compiler.postprocess(pageId, wikitext, null);
		
		//System.out.println(cp.toString());
		
		// Render the compiled page as HTML
		StringWriter w = new StringWriter();
		
		HtmlPrinter p = new HtmlPrinter(w, pageTitle.getFullTitle());
		p.setCssResource("/org/sweble/wikitext/engine/utils/HtmlPrinter.css", "");
		p.setStandaloneHtml(true, "");
		
		p.go(cp.getPage());
		
		return w.toString();
	}	
	
}
