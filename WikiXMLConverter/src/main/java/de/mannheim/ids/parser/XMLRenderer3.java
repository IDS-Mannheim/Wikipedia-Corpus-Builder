package de.mannheim.ids.parser;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngine;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.output.HtmlRenderer;
import org.sweble.wikitext.engine.output.HtmlRendererCallback;
import org.sweble.wikitext.engine.utils.UrlEncoding;
import org.sweble.wikitext.parser.nodes.WtDefinitionList;
import org.sweble.wikitext.parser.nodes.WtDefinitionListDef;
import org.sweble.wikitext.parser.nodes.WtDefinitionListTerm;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtListItem;
import org.sweble.wikitext.parser.nodes.WtName;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtOrderedList;
import org.sweble.wikitext.parser.nodes.WtPageName;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtSemiPre;
import org.sweble.wikitext.parser.nodes.WtSignature;
import org.sweble.wikitext.parser.nodes.WtTable;
import org.sweble.wikitext.parser.nodes.WtTableCaption;
import org.sweble.wikitext.parser.nodes.WtTableCell;
import org.sweble.wikitext.parser.nodes.WtTableHeader;
import org.sweble.wikitext.parser.nodes.WtTableRow;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateArguments;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtXmlAttribute;
import org.sweble.wikitext.parser.nodes.WtXmlCharRef;
import org.sweble.wikitext.parser.nodes.WtXmlElement;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.sweble.wikitext.parser.utils.WtRtDataPrinter;

import de.fau.cs.osr.utils.visitor.VisitingException;

/**
 * Customized XML renderer based on {@link HtmlRenderer} of 
 * Sweble version 3.
 * 
 * Notes from previous implementation:
 * 
 * in visit(WtImageLink n)
 * aTitle = esc(strCaption);
 * 
 * @author margaretha
 */
public class XMLRenderer3 extends HtmlRenderer {

	public static final Set<String> inlineElements = new HashSet<String>();
	static {
		inlineElements.add("small");
		inlineElements.add("big");
		inlineElements.add("sup");
		inlineElements.add("sub");
		inlineElements.add("u");
	}
	
	public static final Set<String> smileys = new HashSet<String>();
	static{
		// german
		smileys.add("smiley");
		smileys.add("s");
		
		// english
		smileys.add("smiley2");
		smileys.add("=2");
		smileys.add("oldsmiley");
		smileys.add("smiley3");
		smileys.add("=3");
		smileys.add("sert");
		smileys.add("emoji");
		smileys.add("emote");
		
		smileys.add("(-:");
		smileys.add("(:");
		smileys.add("-)");
		smileys.add("=)");
		smileys.add("):");
		smileys.add("=(");
		smileys.add("frown");
		smileys.add("wink");
		smileys.add(";)");
		smileys.add("blush");
		smileys.add("=D");
		smileys.add("=P");
		smileys.add("=S");
		smileys.add("shades");
		smileys.add(")':");
		smileys.add("awesome");
		smileys.add("kitty");
		smileys.add("meh");
	}
	
	private PageId pageId;
	private WtEngine engine;
	
	protected XMLRenderer3(HtmlRendererCallback callback,
			WikiConfig wikiConfig, PageTitle pageTitle, Writer w) {
		super(callback, wikiConfig, pageTitle, w);
		this.engine = new WtEngineImpl(wikiConfig);
		p.incIndent();
	}

	public void setPageId(PageId pageId) {
		this.pageId = pageId;
	}	
	
	@Override
	public void visit(WtDefinitionList n) {
		p.indent("<dl>");
		p.incIndent();
		iterate(n);
		p.decIndent();
		p.indent("</dl>");
	}

	@Override
	public void visit(WtDefinitionListDef n) {
		p.indent("<dd>");
		p.incIndent();
		iterate(n);
		p.decIndent();
		p.print("</dd>");
	}

	@Override
	public void visit(WtDefinitionListTerm n) {
		p.indent("<dt>");
		p.incIndent();
		iterate(n);
		p.decIndent();
		p.print("</dt>");
	}
	
	@Override
	public void visit(WtInternalLink n)
	{
//		if (!n.getTarget().isResolved())
//		{
//			printAsWikitext(n);
//			return;
//		}

		p.indentAtBol();

		PageTitle target;
		try
		{
			target = PageTitle.make(wikiConfig, n.getTarget().getAsString());
		}
		catch (LinkTargetException e)
		{
			throw new VisitingException(e);
		}

		// FIXME: I think these should be removed in the parser already?!
//		if (target.getNamespace() == wikiConfig.getNamespace("Category"))
//			return;

		if (!callback.resourceExists(target))
		{
			String title = target.getDenormalizedFullTitle();

			String path = UrlEncoding.WIKI.encode(target.getNormalizedFullTitle());

			if (n.hasTitle())
			{
				pt("<a href=\"%s\" class=\"new\" title=\"%s (page does not exist)\">%=%!%=</a>",
						callback.makeUrlMissingTarget(path),
						title,
						n.getPrefix(),
						n.getTitle(),
						n.getPostfix());
			}
			else
			{
				String linkText = makeTitleFromTarget(n, target);

				pt("<a href=\"%s\" class=\"new\" title=\"%s (page does not exist)\">%=%=%=</a>",
						callback.makeUrlMissingTarget(path),
						title,
						n.getPrefix(),
						linkText,
						n.getPostfix());
			}
		}
		else
		{
			if (!target.equals(pageTitle))
			{
				// EM: fix link title with empty text (e.g. space)
				if (n.hasTitle()
						&& !isWtTextEmpty(n.getTitle().get(0))) {
					pt("<a href=\"%s\" title=\"%s\">%=%!%=</a>",
							callback.makeUrl(target),
							makeLinkTitle(n, target),
							n.getPrefix(),
							n.getTitle(),
							n.getPostfix());
				}
				else
				{
					pt("<a href=\"%s\" title=\"%s\">%=%=%=</a>",
							callback.makeUrl(target),
							makeLinkTitle(n, target),
							n.getPrefix(),
							makeTitleFromTarget(n, target),
							n.getPostfix());
				}
			}
			else
			{
				if (n.hasTitle())
				{
					pt("<strong class=\"selflink\">%=%!%=</strong>",
							n.getPrefix(),
							n.getTitle(),
							n.getPostfix());
				}
				else
				{
					pt("<strong class=\"selflink\">%=%=%=</strong>",
							n.getPrefix(),
							makeTitleFromTarget(n, target),
							n.getPostfix());
				}
			}
		}
	}
	
	private boolean isWtTextEmpty(WtNode node) {
		if (node instanceof WtText) {
			WtText text = (WtText) node;
			if (text.getContent().trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void visit(WtItalics n) {
		p.print("<i>");
		iterate(n);
		p.print("</i>");
	}

	@Override
	public void visit(WtListItem n) {
		p.indent("<li>");
		p.incIndent();
		iterate(n);
		p.decIndent();
		p.print("</li>");
	}

	@Override
	public void visit(WtOrderedList n) {
		p.indent("<ol>");
		p.incIndent();
		iterate(n);
		p.decIndent();
		p.indent("</ol>");
	}

	@Override
	public void visit(WtParagraph n) {
		if (!n.isEmpty()) {
			p.indent("<p>");
			iterate(n);
			p.print("</p>");
		}
	}

	@Override
	public void visit(WtSection n) {
		p.indent();
		pt("<h%d>%!</h%d>", n.getLevel(), n.getHeading(), n.getLevel());
		dispatch(n.getBody());
	}

	@Override
	public void visit(WtSemiPre n) {
		p.indent();
		++inPre;
		pt("<pre>%!</pre>", n);
		--inPre;
		// p.println();
	}
	
	@Override
	public void visit(WtSignature n) {
		p.print("<span class=\"");
		p.print("signature\"/>");
	}
	
	// EM: what is the problem with cleanAttribs?
	
	@Override
	public void visit(WtTable n) {
		p.indent();
		// pt("<table%!>", cleanAttribs(n.getXmlAttributes()));
		// p.println();
		p.print("<table>");
		p.incIndent();
		fixTableBody(n.getBody()); // private
		p.decIndent();
		p.indent("</table>");
	}
	
	@Override
	public void visit(WtTableCaption n) {
		// p.indent();
		// pt("<caption%!>", cleanAttribs(n.getXmlAttributes()));
		p.indent("<caption>");
		// p.println();
		p.incIndent();
		dispatch(getCellContent(n.getBody()));
		p.decIndent();
		p.indent("</caption>");
	}
	
	@Override
	public void visit(WtTableCell n) {
		// p.indent();
		// pt("<td%!>", cleanAttribs(n.getXmlAttributes()));
		p.indent("<td>");
		// p.println();
		p.incIndent();
		dispatch(getCellContent(n.getBody()));
		p.decIndent();
		p.indent("</td>");
	}
	
	@Override
	public void visit(WtTableHeader n) {
		// p.indent();
		// pt("<th%!>", cleanAttribs(n.getXmlAttributes()));
		p.indent("<th>");
		// p.println();
		p.incIndent();
		dispatch(getCellContent(n.getBody()));
		p.decIndent();
		p.indent("</th>");
	}
	
	@Override
	public void visit(WtTableRow n) {
		boolean cellsDefined = false;
		for (WtNode cell : n.getBody()) {
			switch (cell.getNodeType()) {
				case WtNode.NT_TABLE_CELL :
				case WtNode.NT_TABLE_HEADER :
					cellsDefined = true;
					break;
			}
		}

		if (cellsDefined) {
			// p.indent();
			// pt("<tr%!>", cleanAttribs(n.getXmlAttributes()));
			p.indent("<tr>");
			// p.println();
			p.incIndent();
			dispatch(getCellContent(n.getBody()));
			p.decIndent();
			p.indent("</tr>");
		}
		else {
			iterate(n.getBody());
		}
	}
	
	@Override
	public void visit(WtTagExtension n) {
		// EM: TagExtension parse tree for ref within a template might be
		// incorrect.
		if (n.getName().equals("ref")) {
			pt("<%s%!>", n.getName(), n.getXmlAttributes());
			try {
				EngProcessedPage cp = engine.postprocess(pageId,
						n.getBody().getContent(), null);
				iterate(cp.getPage());
			}
			catch (EngineException e) {
				throw new RuntimeException(e);
			}
			p.print("</" + n.getName() + ">");
			// pt("<%s%!>%=</%s>", n.getName(), n.getXmlAttributes(),
			// n.getBody().getContent(), n.getName());
			// pt("&lt;%s%!&gt;%=&lt;/%s&gt;", n.getName(),
			// n.getXmlAttributes(),
			// n.getBody().getContent(), n.getName());
		}
		// nowiki, math
		else {
			p.print("<span id=\"" + n.getName()
					+ "\" class=\"tag-extension\"/>");
		}
	}
	
	@Override
	public void visit(WtTemplate n) {
		// e.g. info box
//		System.out.println(n);
		WtName wtName = n.getName();
		if (wtName != null && !wtName.isEmpty()) {
			String name = wtName.getAsString().toLowerCase();
			if (smileys.contains(name)) {
				p.print("<figure type=\"emoji\" creation=\"template\">");
				p.print("<desc type=\"template\">[_EMOJI:");
				printAsWikitext(n);
				p.print("_]</desc>");
				p.print("</figure>");
			}
			else {
				p.print("<span class=\"template\"/>");
			}
		}
	}
	
	@Override
	public void visit(WtTemplateArgument n) {
		try {
			Object obj = n.getValue().get(0);
			if (obj instanceof WtText) {
				// System.out.print(" "+((WtText) obj).getContent());
				p.print(" " + ((WtText) obj).getContent());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(WtTemplateArguments n) {
		iterate(n);
	}
	
	@Override
	public void visit(WtUnorderedList n) {
		p.indent("<ul>");
		p.incIndent();
		iterate(n);
		p.decIndent();
		p.indent("</ul>");
	}
	
	@Override
	public void visit(WtXmlCharRef n) {
		p.indentAtBol();
		pf("&amp;#%d;", n.getCodePoint());
	}
	
	@Override
	public void visit(WtXmlElement n) {
		if (n.hasBody()) {
			if (blockElements.contains(n.getName().toLowerCase())) {
				p.indent();
				pt("<%s%!>", n.getName(), cleanAttribs(n.getXmlAttributes()));
				// p.println();
				p.incIndent();
				dispatch(n.getBody());
				p.decIndent();
				// p.indent();
				pf("</%s>", n.getName());
				// p.println();
			}
			else if (n.getName().contains(":")) {
				p.indentAtBol();
				pt("&lt;%s%!&gt;", n.getName(),
						cleanAttribs(n.getXmlAttributes()));
				p.incIndent();
				dispatch(n.getBody());
				p.decIndent();
				// p.indentAtBol();
				pf("&lt;/%s&gt;", n.getName());
			}
			else // if (inlineElements.contains(n.getName().toLowerCase())){
			{
				p.indentAtBol();
				pt("<%s%!>", n.getName(), cleanAttribs(n.getXmlAttributes()));
				p.incIndent();
				dispatch(n.getBody());
				p.decIndent();
				// p.indentAtBol();
				pf("</%s>", n.getName());
			}
		}
		else {
			p.indentAtBol();
			pt("<%s%! />", n.getName(), cleanAttribs(n.getXmlAttributes()));
		}
		// System.out.println(n.getName());
	}
	
//	@Override
//	public void visit(WtXmlEntityRef n) {
//		p.indentAtBol();
//		pf("&amp;%s;", n.getName());
//	}
	
	static String makeLinkTitle(WtInternalLink n, PageTitle target) {
		return esc(target.getDenormalizedFullTitle());
	}
	
	@Override
	protected String makeImageCaption(WtImageLink n) {
		StringWriter w = new StringWriter();
		LinkTitlePrinter p = new LinkTitlePrinter(w, wikiConfig);
		p.go(n.getTitle());
		return w.toString();
	}
	
	@Override
	protected String makeImageTitle(WtImageLink n, PageTitle target) {
		return esc(target.getDenormalizedFullTitle());
	}

	private String makeTitleFromTarget(WtInternalLink n, PageTitle target) {
		return makeTitleFromTarget(target, n.getTarget());
	}
	
	private String makeTitleFromTarget(PageTitle target, WtPageName title) {
		String targetStr = title.getAsString();
		if (target.hasInitialColon() && !targetStr.isEmpty()
				&& targetStr.charAt(0) == ':')
			targetStr = targetStr.substring(1);
		return targetStr;
	}
	
	@Override
	protected WtNodeList cleanAttribs(WtNodeList xmlAttributes) {
		ArrayList<WtXmlAttribute> clean = null;

		WtXmlAttribute style = null;
		for (WtNode a : xmlAttributes) {
			if (a instanceof WtXmlAttribute) {
				WtXmlAttribute attr = (WtXmlAttribute) a;
				if (!attr.getName().isResolved())
					continue;

				String name = attr.getName().getAsString().toLowerCase();
				if (name.equals("style")) {
					style = attr;
				}
				else if (name.equals("width")) {
					if (clean == null)
						clean = new ArrayList<WtXmlAttribute>();
					clean.add(attr);
				}
				else if (name.equals("align")) {
					if (clean == null)
						clean = new ArrayList<WtXmlAttribute>();
					clean.add(attr);
				}
			}
		}

		if (clean == null || clean.isEmpty()) {
			// EM: added
			ArrayList<String> names = new ArrayList<>();
			for (WtNode a : xmlAttributes) {
				if (a instanceof WtXmlAttribute) {
					WtXmlAttribute attr = (WtXmlAttribute) a;
					String name = attr.getName().getAsString().toLowerCase();

					if (!names.contains(name)) {
						names.add(name);
					}
					else { // remove duplicate attributes
						xmlAttributes.remove(a);
					}
				}
			}
			// --

			return xmlAttributes;
		}

		String newStyle = "";
		if (style != null)
			newStyle = cleanAttribValue(style.getValue());

		for (WtXmlAttribute a : clean) {
			if (!a.getName().isResolved())
				continue;

			String name = a.getName().getAsString().toLowerCase();
			if (name.equals("align")) {
				newStyle = String.format(
						// "text-align: %s; ",
						"align: %s; ", cleanAttribValue(a.getValue()))
						+ newStyle;
			}
			else {
				newStyle = String.format("%s: %s; ", name,
						cleanAttribValue(a.getValue())) + newStyle;
			}
		}

		WtXmlAttribute newStyleAttrib = nf.attr(
				nf.name(nf.list(nf.text("style"))),
				nf.value(nf.list(nf.text(newStyle))));

		WtNodeList newAttribs = nf.attrs(nf.list());

		ArrayList<String> names = new ArrayList<>();
		for (WtNode a : xmlAttributes) {
			// EM: modified
			WtXmlAttribute attr = (WtXmlAttribute) a;
			String name = attr.getName().getAsString().toLowerCase();

			if (!names.contains(name)) {
				names.add(name);

				if (a == style) {
					newAttribs.add(newStyleAttrib);
				}
				else if (clean.contains(a)) {
					// Remove
				}
				else {
					// Copy the rest
					newAttribs.add(a);
				}
			}
			// --
		}

		if (style == null)
			newAttribs.add(newStyleAttrib);

		return newAttribs;
	}
	
	// EM: simply copied from HTMLRenderer due to inaccessibility
	
	private void fixTableBody(WtNodeList body) {
		boolean hadRow = false;
		WtTableRow implicitRow = null;
		for (WtNode c : body) {
			switch (c.getNodeType()) {
				case WtNode.NT_TABLE_HEADER : // fall through!
				case WtNode.NT_TABLE_CELL : {
					if (hadRow) {
						dispatch(c);
					}
					else {
						if (implicitRow == null)
							implicitRow = nf.tr(nf.emptyAttrs(),
									nf.body(nf.list()));
						implicitRow.getBody().add(c);
					}
					break;
				}

				case WtNode.NT_TABLE_CAPTION : {
					if (!hadRow && implicitRow != null)
						dispatch(implicitRow);
					implicitRow = null;
					dispatch(c);
					break;
				}

				case WtNode.NT_TABLE_ROW : {
					if (!hadRow && implicitRow != null)
						dispatch(implicitRow);
					hadRow = true;
					dispatch(c);
					break;
				}

				default : {
					if (!hadRow && implicitRow != null)
						implicitRow.getBody().add(c);
					else
						dispatch(c);
					break;
				}
			}
		}
	}
	
	private void printAsWikitext(WtNode n) {
		p.indentAtBol(esc(WtRtDataPrinter.print(n)));
	}
}
