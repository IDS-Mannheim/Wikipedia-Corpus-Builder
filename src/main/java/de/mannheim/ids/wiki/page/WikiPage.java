package de.mannheim.ids.wiki.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.mannheim.ids.wiki.Utilities;

/**
 * Wikipage Class
 * 
 * @author margaretha
 * 
 */
public class WikiPage {

	private String pageStructure;
	private String wikitext;
	private String wikiXML;
	private String pageTitle;
	private String pageIndex;
	private String pageId;
	private String pageIndent;

	private boolean isRedirect;

	public List<String> textSegments;

	public static final String[] indexList = { "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "Char" };

	public WikiPage() {
		setWikitext("");
		wikiXML = "";
		textSegments = new ArrayList<String>();
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(boolean isDiscussion) throws IOException {
		String firstLetter = null;
		if (isDiscussion) {
			String[] a = pageTitle.split(":");
			firstLetter = a[1].substring(0, 1);
		}
		else {
			firstLetter = this.pageTitle.substring(0, 1);
		}
		pageIndex = Utilities.normalizeIndex(firstLetter, indexList);
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public boolean isRedirect() {
		return isRedirect;
	}

	public void setRedirect(boolean isRedirect) {
		this.isRedirect = isRedirect;
	}

	public String getPageIndent() {
		return pageIndent;
	}

	public void setPageIndent(String pageIndent) {
		this.pageIndent = pageIndent;
	}

	public String getWikiXML() {
		return wikiXML;
	}

	public void setWikiXML(String wikiXML) {
		this.wikiXML = wikiXML;
	}

	public String getWikitext() {
		return wikitext;
	}

	public void setWikitext(String wikitext) {
		this.wikitext = wikitext;
	}

	public String getPageStructure() {
		return pageStructure;
	}

	public void setPageStructure(String pageStructure) {
		this.pageStructure = pageStructure;
	}
}
