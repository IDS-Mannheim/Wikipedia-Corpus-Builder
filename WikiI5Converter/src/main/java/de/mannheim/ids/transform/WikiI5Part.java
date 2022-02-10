package de.mannheim.ids.transform;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cocoon.xml.sax.SAXBuffer;

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

	private InputStream inputStream;
	private String wikiPath;

	private boolean idsText = false;
	private boolean startDoc = false;
	private boolean endDoc = false;

	private String index;
	private String pageId;
	private int docId;

	private SAXBuffer idsTextBuffer;
	/**
	 * Constructs a WikiI5Part containing idsText.
	 * 
	 * @param wikiXML
	 *            the XML output file
	 * @param pageId
	 *            wikipage id
	 * @param is
	 *            wikitext/IdsText InputStream (which was the output of XSLT
	 *            transformation).
	 */
	public WikiI5Part(InputStream is, String wikiXMLPath,
			String pageId) {
		setInputStream(is);
		setWikiPath(wikiXMLPath);
		setIDSText(true);
		setPageId(pageId);
	}
	
	   public WikiI5Part (String wikiXMLPath, String pageId,
               SAXBuffer extendedIdsTextBuffer) {
        setWikiPath(wikiXMLPath);
        setIDSText(true);
        setPageId(pageId);
        setIdsTextBuffer(extendedIdsTextBuffer);
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
	}

	/**
	 * Constructs a WikiI5Part as an endDoc.
	 * @param isEndDoc 
	 * 
	 */
	public WikiI5Part(boolean isEndDoc) {
	    setEndDoc(true);
	}

    public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
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
			if (inputStream != null) {
				inputStream.close();
			}
		}
		catch (IOException e) {
			throw new I5Exception("Failed closing outputstream.", e);
		}
	}

    public SAXBuffer getIdsTextBuffer () {
        return idsTextBuffer;
    }

    public void setIdsTextBuffer (SAXBuffer idsTextBuffer) {
        this.idsTextBuffer = idsTextBuffer;
    }

    public boolean isEndDoc () {
        return endDoc;
    }

    public void setEndDoc (boolean endDoc) {
        this.endDoc = endDoc;
    }

}
