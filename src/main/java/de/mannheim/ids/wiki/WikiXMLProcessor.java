package de.mannheim.ids.wiki;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiPageHandler;
import de.mannheim.ids.wiki.page.WikiPageReader;
import de.mannheim.ids.wiki.page.WikiStatistics;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;

/**
 * 
 * 
 * @author margaretha
 * 
 * */
public class WikiXMLProcessor {

	private Configuration config;
	private WikiStatistics wikiStatistics;
	private WikiErrorWriter errorWriter;
	private LinkedBlockingQueue<WikiPage> wikipages;
	private WikiPostUser postUser;
	private WikiPostTime postTime;
	
	public static final WikiPage endPage = new WikiPage();

	public WikiXMLProcessor(Configuration config) throws IOException {
		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		this.config = config;
		this.wikiStatistics = new WikiStatistics();
		this.errorWriter = new WikiErrorWriter(config);
		this.wikipages = new LinkedBlockingQueue<WikiPage>();

		if (config.isDiscussion()) {
			String prefix = Paths.get(config.getWikidump()).getFileName()
					.toString().substring(0, 15);
			postUser = new WikiPostUser(prefix, config.getLanguageCode()
					+ ".wikipedia.org/wiki/" + config.getUserPage() + ":");
			postTime = new WikiPostTime(prefix);
		}
	}

	public void run() throws IOException {

		// createOutputDirectories();

		WikiPageReader wikiReader = new WikiPageReader(config, wikipages, endPage,
				wikiStatistics);
		Thread wikiReaderThread = new Thread(wikiReader, "wikiReader");
		ExecutorService pool = Executors.newFixedThreadPool(config.getMaxThreads());
		
		try {
			wikiReaderThread.start();
			for (WikiPage wp = wikipages.take(); 
					!wp.equals(endPage); 
					wp = wikipages.take()){
				WikiPageHandler ph = new WikiPageHandler(config,
						wp, wikiStatistics, errorWriter);
				if (config.isDiscussion()) {
					ph.setPostTime(postTime);
					ph.setPostUser(postUser);
				}
				pool.execute(ph);
			}
			pool.shutdown();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			pool.shutdownNow();
		}
		
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
		} 
		catch (InterruptedException e) {
			pool.shutdownNow();
			try {
				pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e1) {
				System.err.println("Pool termination was interrupted.");
			}
			Thread.currentThread().interrupt();
		}
		
		errorWriter.close();
		postUser.close();
		postTime.close();
		wikiStatistics.print();
	} 

	@Deprecated
	private void createOutputDirectories() {
		String xmlOutputDir = config.getOutputFolder();
		Utilities.createDirectory(xmlOutputDir);

		for (String i : WikiPage.indexList) {
			Utilities.createDirectory(xmlOutputDir + "/" + i);
		}
	}
}
