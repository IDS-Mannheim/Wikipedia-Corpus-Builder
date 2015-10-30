package de.mannheim.ids.db;

import java.util.HashMap;
import java.util.Map;

/** LanguageLinks of a wikipage. It contains a map of the titles of 
 * 	the wikipage analogs in different languages and their links in 
 * 	the other wikipedias.  
 * 
 * @author margaretha
 *
 */
public class LanguageLinks {

	private String pageId;
	private Map<String, String> titleMap;
	
	public LanguageLinks(String pageId) {
		this.pageId = pageId;
		this.titleMap = (Map<String, String>) new HashMap<String, String>();
	}
	
	public String getPageId() {
		return pageId;
	}
	public void setPageId(String pageId) {
		this.pageId = pageId;
	}
	public Map<String, String> getTitleMap() {
		return titleMap;
	}
	public void setTitleMap(Map<String, String> titleMap) {
		this.titleMap = titleMap;
	}
}
