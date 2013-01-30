package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.select.Evaluator.ContainsOwnText;
import org.sweble.wikitext.engine.Page;
import org.sweble.wikitext.lazy.encval.IllegalCodePoint;
import org.sweble.wikitext.lazy.parser.Bold;
import org.sweble.wikitext.lazy.parser.DefinitionDefinition;
import org.sweble.wikitext.lazy.parser.DefinitionList;
import org.sweble.wikitext.lazy.parser.DefinitionTerm;
import org.sweble.wikitext.lazy.parser.Enumeration;
import org.sweble.wikitext.lazy.parser.EnumerationItem;
import org.sweble.wikitext.lazy.parser.ExternalLink;
import org.sweble.wikitext.lazy.parser.HorizontalRule;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.sweble.wikitext.lazy.parser.Italics;
import org.sweble.wikitext.lazy.parser.Itemization;
import org.sweble.wikitext.lazy.parser.ItemizationItem;
import org.sweble.wikitext.lazy.parser.MagicWord;
import org.sweble.wikitext.lazy.parser.Paragraph;
import org.sweble.wikitext.lazy.parser.Section;
import org.sweble.wikitext.lazy.parser.SemiPre;
import org.sweble.wikitext.lazy.parser.SemiPreLine;
import org.sweble.wikitext.lazy.parser.Signature;
import org.sweble.wikitext.lazy.parser.Table;
import org.sweble.wikitext.lazy.parser.TableCaption;
import org.sweble.wikitext.lazy.parser.TableCell;
import org.sweble.wikitext.lazy.parser.TableHeader;
import org.sweble.wikitext.lazy.parser.TableRow;
import org.sweble.wikitext.lazy.parser.Url;
import org.sweble.wikitext.lazy.parser.Whitespace;
import org.sweble.wikitext.lazy.parser.XmlElement;
import org.sweble.wikitext.lazy.parser.XmlElementClose;
import org.sweble.wikitext.lazy.parser.XmlElementEmpty;
import org.sweble.wikitext.lazy.parser.XmlElementOpen;
import org.sweble.wikitext.lazy.preprocessor.Redirect;
import org.sweble.wikitext.lazy.preprocessor.TagExtension;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;
import org.sweble.wikitext.lazy.preprocessor.TemplateParameter;
import org.sweble.wikitext.lazy.preprocessor.XmlComment;
import org.sweble.wikitext.lazy.utils.XmlAttribute;
import org.sweble.wikitext.lazy.utils.XmlAttributeGarbage;
import org.sweble.wikitext.lazy.utils.XmlCharRef;
import org.sweble.wikitext.lazy.utils.XmlEntityRef;

import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.Text;


/* This class implements Visitor design pattern which customizes XML printing
 * according to the Class types of the AST nodes of the wikitext. 
 * 
 * Improper nested Tags cannot be handled very well using visit methods of 
 * types XMLElementOpen and XMLElementClose. For this reason, it's pre-processed 
 * using TagSoup parser.
 * 
 * <!-- usercomments --> is not printed because Sweble parser turn on the option 
 * "trim" in the PreprocessorToParserTransformer class.
 * */
public class WikiVisitor  extends de.fau.cs.osr.ptk.common.PrinterBase{
		
	private boolean renderTemplates = false;
	private boolean renderTagExtensions = false;
	private List<ExternalLink> numberedLinks = new ArrayList<ExternalLink>();
		
	protected WikiVisitor(Writer writer) {
		super(writer);
		// TODO Auto-generated constructor stub
	}	

	// Visit methods
	public void visit(AstNode astNode) throws IOException{
		print("<span name=\""+astNode.getClass().getSimpleName()+"\" ");
		print("class=\"unknown-node\"");
		print("/>");
		
		/*print("<gap desc=\""+astNode.getClass().getSimpleName()+"\" ");
		print("reason=\"unknown-node\"");
		print("/>");*/
	}
	public void visit(Page page) throws IOException{
		iterate(page.getContent());
	}
	public void visit(Text text) throws IOException	{	
			
		if(!text.getContent().equals("\n")){
			//print(StringEscapeUtils.escapeXml(text.getContent()));
/*			temp = StringEscapeUtils.escapeXml(text.getContent());
			print (	StringUtils.replaceEach(temp, 
				new String[]{"&amp;nbsp;"}, 
				new String[]{"&#160;"}));
*/			
		/*	print (	StringUtils.replaceEach(text.getContent(), 
					new String[]{"&", "\"", "<", ">"}, 
					new String[]{"&amp;", "&quot;", "&lt;", "&gt;"}));*/
			print(escHtml(text.getContent()));
		}			
	}	
	public void visit(Italics n) throws IOException	{
		print("<i>");
		iterate(n.getContent());
		print("</i>");
	}
	public void visit(Bold n) throws IOException	{	
		print("<b>");
		iterate(n.getContent());
		print("</b>");	
	}
	public void visit(Whitespace n) throws IOException	{
		//iterate(n.getContent());
	}
	public void visit(Paragraph p) throws IOException	{
		renderBlockLevelElementsFirst(p);
		if (!isParagraphEmpty(p)) {
			incIndent("\t");
			print("<p>");			
			iterate(p.getContent());						
			print("</p>");
			decIndent();
		}
		printNewline(false);
	}
	public void visit(SemiPre sp) throws IOException	{
		printNewline(false);
		print("<pre>");
		iterate(sp.getContent());
		print("</pre>");
		printNewline(false);
	}
	public void visit(SemiPreLine line) throws IOException	{
		//System.out.println(line.getContent());
		iterate(line.getContent());
		print("\n");
	}
	public void visit(Section s) throws IOException	{				
		incIndent("\t");
		print("<head");
		print(s.getLevel());
		print(">");
		iterate(s.getTitle());
		print("</head");
		print(s.getLevel());
		print(">");		
		decIndent();
		printNewline(false);
		iterate(s.getBody());					
	}
	public void visit(XmlComment e) throws IOException	{
		// trim must be false first
		// otherwise this will never be visited
		print("<!--");
		print(e.getContent());
		print("-->");
	}
	public void visit(XmlElement e) throws IOException	{
		//printNewline(false);
		print("<");
		print(e.getName());
		iterate(e.getXmlAttributes());
		if (e.getEmpty()) {
			print(" />");
		} else {
			print(">");			
			iterate(e.getBody());
			print("</");
			print(e.getName());
			print(">");
			//printNewline(false);
		}
	}
	public void visit(XmlAttribute a) throws IOException	{
		print(" ");
		print(a.getName());
		print("=\"");
		iterate(a.getValue());
		print("\"");
	}
	public void visit(XmlAttributeGarbage g) throws IOException
	{

	}
	public void visit(XmlCharRef ref) throws IOException	{		
		print("&#");
		print(ref.getCodePoint());
		print(";");
		System.out.println("XmlCharRef " + ref.getCodePoint());
	}
	public void visit(XmlEntityRef ref) throws IOException	{
		print("&amp;");
		print(ref.getName());
		print(";");		
	}
	public void visit(DefinitionList n) throws IOException	{
		printNewline(false);
		incIndent("\t");
		print("<dl>");
		printNewline(false);				
		iterate(n.getContent());		
		printNewline(false);
		print("</dl>");
		decIndent();
		printNewline(false);
	}
	public void visit(DefinitionTerm n) throws IOException	{
		// It is not parsed when it occurs alone and is not nested by <dl> or <dd>
		printNewline(false);
		incIndent("    ");
		print("<dt>");
		iterate(n.getContent());
		print("</dt>");
		decIndent();
		printNewline(false);
	}
	public void visit(DefinitionDefinition n) throws IOException	{
		printNewline(false);
		incIndent("    ");
		print("<dd>");
		iterate(n.getContent());
		print("</dd>");
		decIndent();
		printNewline(false);
	}
	public void visit(Enumeration n) throws IOException	{	
		printNewline(false);
		incIndent("\t");
		print("<ol>");		
		printNewline(false);
		iterate(n.getContent());
		printNewline(false);
		print("</ol>");
		decIndent();
		printNewline(false);
	}
	public void visit(EnumerationItem n) throws IOException	{
		printNewline(false);
		incIndent("    ");
		print("<li>");
		iterate(n.getContent());
		print("</li>");
		decIndent();
		printNewline(false);
	}
	public void visit(Itemization n) throws IOException	{
		printNewline(false);
		incIndent("\t");
		print("<ul>");	
		printNewline(false);
		iterate(n.getContent());
		printNewline(false);
		print("</ul>");		
		decIndent();
		printNewline(false);
	}
	public void visit(ItemizationItem n) throws IOException	{
		printNewline(false);
		incIndent("    ");
		print("<li>");
		iterate(n.getContent());
		print("</li>");
		decIndent();
		printNewline(false);
	}
	public void visit(ExternalLink link) throws IOException	{
		
		print("<a href=\"");
		print(link.getTarget().getProtocol());
		print(":");
		print(escHtml(link.getTarget().getPath()));		
		print("\">");
		if (!link.getTitle().isEmpty()) {
			iterate(link.getTitle());
		} else {
			printExternalLinkNumber(link);
		}
		print("</a>");

	}
	public void visit(Url url) throws IOException
	{
		print("<a href=\"");
		print(url.getProtocol());
		print(":");
		print(escHtml(url.getPath()));
		print("\">");
		print(url.getProtocol());
		print(":");
		print(escHtml(url.getPath()));
		print("</a>");

	}
	public void visit(InternalLink n) throws IOException	{
		print("<a href=\"");
		print(makeLinkTarget(n));
		print("\">");
		print(n.getPrefix());
		if (n.getTitle().getContent().isEmpty()) {
			print(makeLinkTitle(n));
		} else {
			iterate(n.getTitle().getContent());
		}
		print(n.getPostfix());
		print("</a>");		

	}
	public void visit(Table table) throws IOException	{		
		printNewline(false);
		incIndent("\t");
		print("<table");
		//iterate(table.getXmlAttributes());
		print(">");
		printNewline(false);
		incIndent("   ");		
		iterate(table.getBody());
		decIndent();
		printNewline(false);		
		print("</table>");
		decIndent();
		printNewline(false);
	}
	public void visit(TableCaption caption) throws IOException
	{
		printNewline(false);
		print("<caption");
		iterate(caption.getXmlAttributes());
		print(">");
		printNewline(false);
		incIndent("\t");
		iterate(caption.getBody());
		decIndent();
		printNewline(false);
		print("</caption>");
		printNewline(false);

	}
	public void visit(TableRow row) throws IOException
	{
		printNewline(false);
		print("<tr");
		iterate(row.getXmlAttributes());
		print(">");
		printNewline(false);		
		incIndent("   ");
		iterate(row.getBody());
		decIndent();
		printNewline(false);
		print("</tr>");
		printNewline(false);
	}
	public void visit(TableHeader header) throws IOException
	{
		printNewline(false);
		print("<th");
		//iterate(header.getXmlAttributes());		
		print(">");
		printNewline(false);		
		iterate(header.getBody());		
		printNewline(false);
		print("</th>");
		printNewline(false);

	}
	public void visit(TableCell cell) throws IOException
	{
		printNewline(false);
		print("<td");
		//iterate(cell.getXmlAttributes());
		print(">");
		printNewline(false);		
		iterate(cell.getBody());		
		printNewline(false);
		print("</td>");
		printNewline(false);

	}
	public void visit(HorizontalRule rule) throws IOException	{
		printNewline(false);
		print("<hr />");
		printNewline(false);
	}
	public void visit(Signature sig) throws IOException	{
		print("<span class=\"");		
		print("signature\">");
		print(makeSignature(sig));
		print("</span>");
	}
	public void visit(Redirect n) throws IOException	{
		print("<span class=\"");
		print("redirect\">&#x21B3; ");		//↳
		print(escHtml(n.getTarget()));
		print("</span>");
	}
	public void visit(IllegalCodePoint n) throws IOException	{
		print("<span class=\"");		
		print("illegal\">");
		print(asXmlCharRefs(n.getCodePoint()));
		print("</span>");
	}
	public void visit(MagicWord n) throws IOException	{
		print("<span class=\"");		
		print("unknown-magic-word\">__");
		print(n.getWord());
		print("__</span>");
	}
	public void visit(TagExtension n) throws IOException	{
		// ref, nowiki
		print("<span name=");		
		print("\""+n.getName()+"\" ");		
		print ("class=\"unknown-tag-extension\"/>");		
	}
	public void visit(XmlElementEmpty e) throws IOException	{
		print("<span class=\"");
		
		print("unknown-node-xml-element-empty\">");
		print("<");
		print(e.getName());
		iterate(e.getXmlAttributes());
		print(" />");
		print("</span>");

	}
	public void visit(XmlElementOpen e) throws IOException	{
		print("<span name=");		
		print("\""+e.getName()+"\" ");		
		print ("class=\"unknown-element\"");
		//iterate(e.getXmlAttributes());
		print("/>");	
		
		/*print("<span class=\"");		
		print("unknown-node-xml-element-open\">");
		print("<");
		print(e.getName());
		iterate(e.getXmlAttributes());
		print(">");
		print("</span>");*/

	}
	public void visit(XmlElementClose e) throws IOException	{		
		if (e.getName().equals("math") || e.getName().equals("references") 
				|| e.getName().equals("div")
				){}
		else if (e.getName().equals("br")) {
			print("<" + e.getName()+" />");
		}
		else {
			print("<span name=");		
			print("\""+e.getName()+"\" ");		
			print ("class=\"unknown-element\"");
			print("/>");			
		}
	}
	public void visit(Template tmpl) throws IOException	{
		print("<span name=");		
		print("\"template\" ");		
		print ("class=\"unknown-template\"/>");
		
		/*
		print("<gap desc=");		
		print("\"template\" ");		
		print ("reason=\"omitted\"/>");
		*/
	}
	public void visit(TemplateParameter param) throws IOException
	{
		print("<span class=\"");
		
		print("unknown-node-template-parameter\">");
		if (renderTemplates) {
			print("{");
			print("{");
			print("{");
			iterate(param.getName());
			dispatch(param.getDefaultValue());
			iterate(param.getGarbage());
			print("}}}");
		} else {
			if (param.getDefaultValue() == null) {
				print("{");
				print("{");
				print("{");
				iterate(param.getName());
				print("}}}");
			} else {
				print("{");
				print("{");
				print("{");
				iterate(param.getName());
				print("|...}}}");
			}
		}
		print("</span>");

	}
	public void visit(TemplateArgument arg) throws IOException	{
		print("|");
		if (arg.getHasName()) {
			iterate(arg.getValue());
		} else {
			iterate(arg.getName());
			print("=");
			iterate(arg.getValue());
		}

	}

	private String asXmlCharRefs(String codePoint)
	{
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < codePoint.length(); ++i)
		{
			b.append("&#");
			b.append((int) codePoint.charAt(i));
			b.append(";");
		}
		return b.toString();
	}

	@SuppressWarnings("unchecked")
	
	
	private void renderBlockLevelElementsFirst(Paragraph p)
	{
		List<AstNode> l = (List<AstNode>) p.getAttribute("blockLevelElements");
		if (l == null)
			return;

		for(AstNode n : l)
			dispatch(n);
	}

	@SuppressWarnings("unchecked")
	private boolean isParagraphEmpty(Paragraph p)
	{
		if (!p.isEmpty())
		{
			List<AstNode> l = (List<AstNode>) p.getAttribute("blockLevelElements");
			if (l == null || p.size() - l.size() > 0)
				return false;
		}
		return true;
	}
	
	private void printExternalLinkNumber(ExternalLink link)	{
		numberedLinks.add(link);
		print(numberedLinks.size());
	}

	private String makeLinkTitle(InternalLink n)	{
		// StringEscapeUtils.escapeXml generate invalid encoding such as &#57361; 
		return escHtml(n.getTarget());
		//return n.getTarget();
	}

	private String makeLinkTarget(InternalLink n)	{		
		return escHtml(n.getTarget());
		//return n.getTarget();
	}

	private String makeSignature(Signature sig)
	{
		return "[SIG]";
	}

}
