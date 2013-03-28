package de.mannheim.ids.wiki;

import java.util.ArrayList;
import java.util.List;

/** Set the language properties of a wikidump
 * 
 * @author margaretha
 * @version 1.0 Build Mar 2013
 */
public class LanguageSetter {

	private List<String> metapages = new ArrayList<String>();
	private String talk, language;
	
	public LanguageSetter() {
		// TODO Auto-generated constructor stub
	}

	/** Set the language of the wikidump
	 * 
	 *  If the language of the input wikidump is defined in the LanguageSetter 
	 * 	class, instantiate the language. Otherwise, create an empty instance 
	 * 	and set its properties.
	 * 
	 * @param language
	 */	
	public LanguageSetter(String language) {		
		this.setLanguage(language);
		initLanguageProperties(language);
	}
	
	/** Define the title indicators for skipping metapages
	 * 
	 * @param language
	 */
	private void initLanguageProperties(String language){
		if (language.equals("de")){			
			metapages.add("Media:");
			metapages.add("Spezial:");
			metapages.add("Benutzer:");
			metapages.add("Benutzer Diskussion:");
			metapages.add("Wikipedia:");
			metapages.add("Wikipedia Diskussion:");
			metapages.add("Datei:");
			metapages.add("Datei Diskussion:");
			metapages.add("MediaWiki:");
			metapages.add("MediaWiki Diskussion:");
			metapages.add("Vorlage:");
			metapages.add("Vorlage Diskussion:");
			metapages.add("Hilfe:");
			metapages.add("Hilfe Diskussion:");
			metapages.add("Kategorie:");
			metapages.add("Kategorie Diskussion:");
			metapages.add("Portal:");
			metapages.add("Portal Diskussion:");
			
			setTalk("Diskussion");
		}
		else if (language.equals("fr")){    	    		
			metapages.add("Média:");
			metapages.add("Spécial:");
			metapages.add("Utilisateur:");
			metapages.add("Discussion utilisateur:");
			metapages.add("Wikipédia:");
			metapages.add("Discussion Wikipédia:");
			metapages.add("Fichier:");
			metapages.add("Discussion fichier:");
			metapages.add("MediaWiki:");
			metapages.add("Discussion MediaWiki:");
			metapages.add("Modèle:");
			metapages.add("Discussion modèle:");
			metapages.add("Aide:");
			metapages.add("Discussion aide:");
			metapages.add("Catégorie:");
			metapages.add("Discussion catégorie:");
			metapages.add("Portail:");
			metapages.add("Discussion Portail:");
			metapages.add("Projet:");
			metapages.add("Discussion Projet:");
			metapages.add("Référence:");
			metapages.add("Discussion Référence:");
			
			setTalk("Discussion");
		}
	}

	public String getTalk() {
		return talk;
	}

	public void setTalk(String talk) {
		this.talk = talk;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public void setMetapages(List<String> metapages) {
		this.metapages = metapages;
	}
	
	public List<String> getMetapages() {
		return metapages;
	}
}
