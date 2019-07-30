package de.mannheim.ids.wiki.page;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.mannheim.ids.wiki.Utilities;
import de.mannheim.ids.wiki.WikiXMLProcessor;

/**
 * Wikipage class
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
	private boolean hasTitlePrefix;

	public static final String[] indexList = {"A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9"};

	public WikiPage(boolean hasTitlePrefix) {
		this();
		this.hasTitlePrefix = hasTitlePrefix;
	}

	public WikiPage() {
		setWikitext("");
		wikiXML = "";
		textSegments = new ArrayList<String>();
	}
	
	/**
	 * Determines the index of the given page title.
	 * 
	 * @param pageTitle
	 *            a page title
	 * @return the index of the page title
	 */
	public static String determinePageIndex(String pageTitle) {
		String firstChar = pageTitle.substring(0, 1);

		if (Arrays.asList(indexList).contains(firstChar)) {
			return firstChar;
		}
		else {
			return determinePageIndex(pageTitle.substring(1));
		}
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
		String pageTitle = Normalizer.normalize(this.pageTitle, Form.NFKD)
				.toUpperCase();
		String[] title = null;
		if (isDiscussion) {
			if (hasTitlePrefix && pageTitle.contains("/")) {
				title = pageTitle.split("/");
			}
			else {
				title = pageTitle.split(":");
			}

			try {
				pageIndex = determinePageIndex(title[1]);
			}
			catch (Exception e) {
				System.err.println(title);
				pageIndex = determinePageIndex(pageTitle);
				WikiXMLProcessor.errorWriter.logErrorPage("TITLE ", this.pageTitle,
						pageId, e, "");

			}
		}
		else {
			pageIndex = determinePageIndex(pageTitle);
		}
		
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
