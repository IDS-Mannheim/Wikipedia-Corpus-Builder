package de.mannheim.ids.parser;

import java.io.Writer;

import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.output.SafeLinkTitlePrinter;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtRedirect;
import org.sweble.wikitext.parser.nodes.WtTableCaption;
import org.sweble.wikitext.parser.nodes.WtTableCell;
import org.sweble.wikitext.parser.nodes.WtTableHeader;
import org.sweble.wikitext.parser.nodes.WtTableRow;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtXmlAttribute;
import org.sweble.wikitext.parser.utils.WtRtDataPrinter;

public class LinkTitlePrinter extends SafeLinkTitlePrinter {

	public LinkTitlePrinter(Writer writer, WikiConfig wikiConfig) {
		super(writer, wikiConfig);
	}

	@Override
	public void visit(WtImageLink n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtExternalLink n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtRedirect n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtTableCaption n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtTableCell n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtTableHeader n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtTableRow n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtUrl n) {
		printAsWikitext(n);
	}

	@Override
	public void visit(WtXmlAttribute n) {
		printAsWikitext(n);
	}

	private void printAsWikitext(WtNode n) {
		p.indentAtBol(esc(WtRtDataPrinter.print(n)));
	}
}
