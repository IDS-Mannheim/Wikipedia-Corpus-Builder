package de.mannheim.ids.wiki.page;

/**
 * Collect statistical information about Wikipages and the errors found in the
 * conversion process.
 * 
 * @author margaretha
 * 
 */
public class WikiStatistics {

	private int swebleErrors;
	private int parsingErrors;
	private int pageStructureErrors;

	private int emptyPages;
	private int emptyParsedPages;
	private int redirectPages;

	private int noId;

	private int totalNonEmptyPages;
	private int totalPages;
	private int totalPostings;

	public WikiStatistics() {
		swebleErrors = 0;
		parsingErrors = 0;
		pageStructureErrors = 0;
		emptyPages = 0;
		emptyParsedPages = 0;
		redirectPages = 0;
		totalPostings = 0;
		totalPages = 0;
		totalNonEmptyPages = 0;
		noId = 0;
	}

	public void print() {
		System.out.println("\n===============================================");
		System.out.println("Total pages (without redirect pages) "
				+ getTotalPages());
		System.out.println("Total non-empty pages " + getTotalNonEmptyPages());
		System.out.println("Total redirect pages " + getRedirectPages());
		System.out.println("Total empty pages " + getEmptyPages());
		System.out.println("Total empty parsed pages " + getEmptyParsedPages());
		System.out.println("Total pages without id " + getNoId());

		if (totalPostings > 0) {
			System.out.println("Total postings " + getTotalPostings() + "\n");
		}

		System.out.println("Total Sweble exceptions " + getSwebleErrors());
		System.out
				.println("Total XML parsing exceptions " + getParsingErrors());
		System.out.println("Total XML Page structure exceptions "
				+ getPageStructureErrors());
		System.out.println("===============================================");
	}

	public int getSwebleErrors() {
		return swebleErrors;
	}

	public void addSwebleErrors() {
		swebleErrors++;
	}

	public int getParsingErrors() {
		return parsingErrors;
	}

	public void addParsingErrors() {
		parsingErrors++;
	}

	public int getPageStructureErrors() {
		return pageStructureErrors;
	}

	public void addPageStructureErrors() {
		pageStructureErrors++;
	}

	public int getTotalPostings() {
		return totalPostings;
	}

	public void addTotalPostings() {
		totalPostings++;
	}

	public int getEmptyPages() {
		return emptyPages;
	}

	public void addEmptyPages() {
		emptyPages++;
	}

	public int getEmptyParsedPages() {
		return emptyParsedPages;
	}

	public void addEmptyParsedPages() {
		emptyParsedPages++;
	}

	public int getRedirectPages() {
		return redirectPages;
	}

	public void addRedirectPages() {
		redirectPages++;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void addTotalPages() {
		totalPages++;
	}

	public int getTotalNonEmptyPages() {
		return totalNonEmptyPages;
	}

	public void addTotalNonEmptyPages() {
		totalNonEmptyPages++;
	}

	public int getNoId() {
		return noId;
	}

	public void addNoId() {
		this.noId++;
	}

}
