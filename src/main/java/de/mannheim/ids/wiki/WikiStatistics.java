package de.mannheim.ids.wiki;

public class WikiStatistics {	
	
	private int swebleErrors;
	private int parsingErrors;
	private int outerErrors;

	private int emptyArticles;
	private int emptyDiscussions;
	private int emptyParsedArticles;
	private int emptyParsedDiscussions;
	
	private int totalDiscussions;
	private int totalArticles;
	private int totalMetapages;

	public WikiStatistics() {
		this.swebleErrors=0;
		this.parsingErrors=0;
		this.outerErrors=0;		
		this.emptyArticles=0; 
		this.emptyDiscussions=0;
		this.emptyParsedDiscussions=0; 
		this.emptyParsedArticles=0;		
		this.totalMetapages=0;
		this.totalDiscussions=0;
		this.totalArticles=0;		
	} 
	
	public int getSwebleErrors() {
		return swebleErrors;
	}

	public void addSwebleErrors() {
		this.swebleErrors ++;
	}

	public int getParsingErrors() {
		return parsingErrors;
	}

	public void addParsingErrors() {
		this.parsingErrors ++;
	}

	public int getOuterErrors() {
		return outerErrors;
	}

	public void addOuterErrors() {
		this.outerErrors ++;
	}

	public int getTotalDiscussions() {
		return totalDiscussions;
	}

	public void addTotalDiscussions() {
		this.totalDiscussions ++;
	}	

	public int getTotalArticles() {
		return totalArticles;
	}

	public void addTotalArticles() {
		this.totalArticles ++;
	}

	public int getEmptyArticles() {
		return emptyArticles;
	}

	public void addEmptyArticles() {
		this.emptyArticles ++;
	}

	public int getEmptyDiscussions() {
		return emptyDiscussions;
	}

	public void addEmptyDiscussions() {
		this.emptyDiscussions ++;
	}

	public int getEmptyParsedArticles() {
		return emptyParsedArticles;
	}

	public void addEmptyParsedArticles() {
		this.emptyParsedArticles ++;
	}

	public int getEmptyParsedDiscussions() {
		return emptyParsedDiscussions;
	}

	public void addEmptyParsedDiscussions() {
		this.emptyParsedDiscussions ++;
	}

	public int getTotalMetapages() {
		return totalMetapages;
	}

	public void addTotalMetapages() {
		this.totalMetapages ++;
	}
}
