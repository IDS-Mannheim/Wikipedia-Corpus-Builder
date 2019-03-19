package de.mannheim.ids.db;

import java.util.HashMap;
import java.util.Map;

/**
 * LanguageLinks of a wikipage. It contains a map of the titles of the wikipage
 * analogs in different languages and their links in the other wikipedias.
 * 
 * @author margaretha
 *
 */
public class LanguageLinks {

	private String pageId;
	private Map<String, String> titleMap;

	/**
	 * Constructs LanguageLinks for the given wikipage id.
	 * 
	 * @param pageId
	 *            a wikipage id
	 */
	public LanguageLinks(String pageId) {
		this.pageId = pageId;
		this.titleMap = (Map<String, String>) new HashMap<String, String>();
	}

	/**
	 * Gets the wikipage id
	 * 
	 * @return wikipage id
	 */
	public String getPageId() {
		return pageId;
	}

	/**
	 * Sets the wikipage id
	 * 
	 * @param pageId
	 *            a wikipage id
	 */
	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	/**
	 * Gets the language link map for this wikipage.
	 * 
	 * @return the language link map for this wikipage.
	 */
	public Map<String, String> getTitleMap() {
		return titleMap;
	}

	/**
	 * Sets a map containing the titles and links of the wikipage analogs in
	 * different languages.
	 * 
	 * @param titleMap
	 *            a map of titles and links of wikipages in different languages
	 */
	public void setTitleMap(Map<String, String> titleMap) {
		this.titleMap = titleMap;
	}
}
