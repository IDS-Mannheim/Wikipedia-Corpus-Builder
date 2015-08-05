package de.mannheim.ids.transform;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class WikiI5Part {

	private ByteArrayOutputStream bos;
	private String wikiPath;

	private boolean idsText;
	private boolean startDoc;

	private String index;
	private String pageId;
	private int docNr;

	public WikiI5Part(ByteArrayOutputStream bos, File wikiXML, String pageId) {
		setBos(bos);
		setWikiPath(wikiXML.getPath());
		setIDSText(true);
		setPageId(pageId);
	}

	public WikiI5Part(String index, int docNr, boolean isStartDoc) {
		setIndex(index);
		setDocNr(docNr);
		setStartDoc(isStartDoc);
		setIDSText(false);
	}

	public WikiI5Part(boolean isStartDoc) {
		setIDSText(false);
		setStartDoc(isStartDoc);
	}

	public ByteArrayOutputStream getBos() {
		return bos;
	}

	public void setBos(ByteArrayOutputStream bos) {
		if (bos == null) {
			throw new IllegalArgumentException(
					"ByteArrayOutputStream cannot be null.");
		}
		this.bos = bos;
	}

	public String getWikiPath() {
		return wikiPath;
	}

	public void setWikiPath(String wikiPath) {
		if (wikiPath == null || wikiPath.isEmpty()) {
			throw new IllegalArgumentException(
					"WikiXML path cannot be null or empty.");
		}
		this.wikiPath = wikiPath;
	}

	public boolean isIDSText() {
		return idsText;
	}

	public void setIDSText(boolean isIDSText) {
		this.idsText = isIDSText;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		if (index == null || index.isEmpty()) {
			throw new IllegalArgumentException("Index cannot be null or empty.");
		}
		this.index = index;
	}

	public int getDocNr() {
		return docNr;
	}

	public void setDocNr(int docNr) {
		this.docNr = docNr;
	}

	public boolean isStartDoc() {
		return startDoc;
	}

	public void setStartDoc(boolean isStartDoc) {
		this.startDoc = isStartDoc;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		if (pageId == null || pageId.isEmpty()) {
			throw new IllegalArgumentException(
					"PageId cannot be null or empty.");
		}
		this.pageId = pageId;
	}

}
