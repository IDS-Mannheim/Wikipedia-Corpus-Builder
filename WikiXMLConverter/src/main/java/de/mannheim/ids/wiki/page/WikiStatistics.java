package de.mannheim.ids.wiki.page;

/**
 * Collects statistical information about Wikipages and the errors found in the
 * conversion process.
 * 
 * @author margaretha
 * 
 */
public class WikiStatistics {

	private int swebleErrors;
	private int xmlWellnessErrors;
	private int pageStructureErrors;
	private int unknownErrors;
	private int rendererErrors;
	private int numOfThreadDeaths;

	private int emptyPages;
	private int emptyParsedPages;
	private int redirectPages;
	private int skippedPages;
	
	private int noId;

	private int totalNonEmptyPages;
	private int totalPages;
	private int totalPostings;

	public WikiStatistics() {
		swebleErrors = 0;
		xmlWellnessErrors = 0;
		pageStructureErrors = 0;
		emptyPages = 0;
		emptyParsedPages = 0;
		redirectPages = 0;
		totalPostings = 0;
		totalPages = 0;
		totalNonEmptyPages = 0;
		noId = 0;
		rendererErrors = 0;
		numOfThreadDeaths = 0;
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
		System.out.println("Total skipped pages " + getSkippedPages());
		
		if (totalPostings > 0) {
			System.out.println("Total postings " + getTotalPostings() + "\n");
		}

		System.out.println("Total Sweble exceptions " + getSwebleErrors());
		System.out.println("Total Renderer exceptions " + getRendererErrors());
		System.out.println("Total XML Wellness exceptions " + getXMLWellnessErrors());
		System.out.println("Total XML Page structure exceptions "
				+ getPageStructureErrors());
		System.out.println("Total thread deaths: " + getNumOfThreadDeaths());
		System.out.println("Total unknown errors: " + getUnknownErrors());

		System.out.println("===============================================");
	}

	public int getSwebleErrors() {
		return swebleErrors;
	}

	public synchronized void addSwebleErrors() {
		swebleErrors++;
	}

	public int getXMLWellnessErrors() {
		return xmlWellnessErrors;
	}

	public synchronized void addXMLWellnessErrors() {
		xmlWellnessErrors++;
	}

	public int getPageStructureErrors() {
		return pageStructureErrors;
	}

	public synchronized void addPageStructureErrors() {
		pageStructureErrors++;
	}

	public int getTotalPostings() {
		return totalPostings;
	}

	public synchronized void addTotalPostings() {
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

	public synchronized void addEmptyParsedPages() {
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

	public synchronized void addTotalNonEmptyPages() {
		totalNonEmptyPages++;
	}

	public int getNoId() {
		return noId;
	}

	public void addNoId() {
		this.noId++;
	}

	public int getUnknownErrors() {
		return unknownErrors;
	}

	public synchronized void addUnknownErrors() {
		this.unknownErrors++;
	}

	public int getRendererErrors() {
		return rendererErrors;
	}

	public synchronized void addRendererErrors() {
		this.rendererErrors++;
	}

	public int getNumOfThreadDeaths() {
		return numOfThreadDeaths;
	}

	public synchronized void addNumOfThreadDeaths() {
		this.numOfThreadDeaths++;
	}

	public int getSkippedPages() {
		return skippedPages;
	}

	public void addSkippedPages() {
		this.skippedPages++;
	}
}
