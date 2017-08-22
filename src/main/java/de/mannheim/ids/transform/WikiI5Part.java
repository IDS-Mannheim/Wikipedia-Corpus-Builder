package de.mannheim.ids.transform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.mannheim.ids.wiki.I5Exception;

/**
 * Represents a part of the complete WikiI5 corpus, i.e. a start document, an
 * end document, or an idsText. The objects of this class are used to create
 * Future objects as the results coming from the processing threads.
 * 
 * @author margaretha
 *
 */
public class WikiI5Part {

	private ByteArrayOutputStream bos;
	private String wikiPath;

	private boolean idsText;
	private boolean startDoc;

	private String index;
	private String pageId;
	private int docId;

	/**
	 * Constructs a WikiI5Part containing idsText.
	 * 
	 * @param bos
	 *            transformed wikitext in ByteArrayOutputStream
	 * @param wikiXML
	 *            the XML output file
	 * @param pageId
	 *            wikipage id
	 */
	public WikiI5Part(ByteArrayOutputStream bos, File wikiXML, String pageId) {
		setBos(bos);
		setWikiPath(wikiXML.getPath());
		setIDSText(true);
		setPageId(pageId);
	}

	/**
	 * Constructs a WikiI5Part as a startDoc.
	 * 
	 * @param index
	 *            a document index
	 * @param docNr
	 *            a document number
	 * @param isStartDoc
	 *            a boolean determining if the WikiI5Part is a start doc or not.
	 */
	public WikiI5Part(String index, int docNr, boolean isStartDoc) {
		setIndex(index);
		setDocNr(docNr);
		setStartDoc(isStartDoc);
		setIDSText(false);
	}

	/**
	 * Constructs a WikiI5Part as an endDoc.
	 * 
	 */
	public WikiI5Part() {
		setIDSText(false);
		setStartDoc(false);
	}

	public ByteArrayOutputStream getBos() {
		return bos;
	}

	public void setBos(ByteArrayOutputStream bos) {
		// if (bos == null) {
		// throw new IllegalArgumentException(
		// "ByteArrayOutputStream cannot be null.");
		// }
		this.bos = bos;
	}

	/**
	 * Gets the XML outfile path
	 * 
	 * @return the XML outfile path
	 */
	public String getWikiPath() {
		return wikiPath;
	}

	/**
	 * Sets the XML output file path
	 * 
	 * @param wikiPath
	 *            the XML output file path
	 */
	public void setWikiPath(String wikiPath) {
		if (wikiPath == null || wikiPath.isEmpty()) {
			throw new IllegalArgumentException(
					"WikiXML path cannot be null or empty.");
		}
		this.wikiPath = wikiPath;
	}

	/**
	 * Tells if the WikiI5Part is an idsText element or not.
	 * 
	 * @return true if WikiI5Part is an idsText element, false otherwise.
	 */
	public boolean isIDSText() {
		return idsText;
	}

	/**
	 * Sets the WikiI5Part of type idsText.
	 * 
	 * @param isIDSText
	 *            a boolean determining if the WikiI5Part is an IDSText or not.
	 */
	public void setIDSText(boolean isIDSText) {
		this.idsText = isIDSText;
	}

	/**
	 * Returns the document index of the WikiI5Part
	 * 
	 * @return document index of the WikiI5Part
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * Sets the document index of the WikiI5Part
	 * 
	 * @param index
	 *            the document index of the WikiI5Part
	 */
	public void setIndex(String index) {
		if (index == null || index.isEmpty()) {
			throw new IllegalArgumentException(
					"Index cannot be null or empty.");
		}
		this.index = index;
	}

	/**
	 * Gets the document number of the WikiI5Part
	 * 
	 * @return the document number of the WikiI5Part
	 */
	public int getDocNr() {
		return docId;
	}

	/**
	 * Sets the document number of the WikiI5Part
	 * 
	 * @param docNr
	 *            the document number of the WikiI5Part
	 */
	public void setDocNr(int docNr) {
		this.docId = docNr;
	}

	/**
	 * Tells if the WikiI5Part is a start doc or not.
	 * 
	 * @return true if the WikiI5Part is a start doc, false otherwise.
	 */
	public boolean isStartDoc() {
		return startDoc;
	}

	/**
	 * Sets the WikiI5Part as a startDoc
	 * 
	 * @param isStartDoc
	 *            a boolean determining if the WikiI5Part is a start doc or not.
	 */
	public void setStartDoc(boolean isStartDoc) {
		this.startDoc = isStartDoc;
	}

	/**
	 * Returns the wikipage id of the WikiI5Part
	 * 
	 * @return the wikipage id of the WikiI5Part
	 */
	public String getPageId() {
		return pageId;
	}

	/**
	 * Sets the wikipage id of the WikiI5Part
	 * 
	 * @param pageId
	 *            the wikipage id of the WikiI5Part
	 */
	public void setPageId(String pageId) {
		if (pageId == null || pageId.isEmpty()) {
			throw new IllegalArgumentException(
					"PageId cannot be null or empty.");
		}
		this.pageId = pageId;
	}

	public void close() throws I5Exception {
		try {
			bos.close();
		}
		catch (IOException e) {
			throw new I5Exception("Failed closing outputstream.", e);
		}
	}

}
