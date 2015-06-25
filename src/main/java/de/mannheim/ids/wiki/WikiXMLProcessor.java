package de.mannheim.ids.wiki;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import de.mannheim.ids.util.Utilities;
import de.mannheim.ids.util.WikiErrorWriter;
import de.mannheim.ids.util.WikiStatistics;

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
	private WikiTalkUser postUser;
	private WikiTalkTime postTime;

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
			postUser = new WikiTalkUser(prefix, config.getLanguageCode()
					+ ".wikipedia.org/wiki/" + config.getUserPage() + ":");
			postTime = new WikiTalkTime(prefix);
		}
	}

	public void run() throws IOException {

		// createOutputDirectories();

		WikiPageReader wikiReader = new WikiPageReader(config, wikipages,
				wikiStatistics);
		Thread wikiReaderThread = new Thread(wikiReader, "wikiReader");
		ExecutorService pool = Executors.newFixedThreadPool(200);

		synchronized (wikipages) {
			try {
				wikiReaderThread.start();
				while (wikiReaderThread.isAlive() || wikipages.size() > 0) {
					if (wikipages.size() > 0) {

						WikiPageHandler ph = new WikiPageHandler(config,
								wikipages.take(), wikiStatistics, errorWriter);
						if (config.isDiscussion()) {
							ph.setPostTime(postTime);
							ph.setPostUser(postUser);
						}
						pool.execute(ph);
					}
					else {
						System.out.println("Wait for some Wikipages ...");
						wikipages.wait(1000);
					}
				}
				pool.shutdown();
			}
			catch (Exception e) {
				pool.shutdownNow();
			}
		}

		while (!pool.isTerminated()) {
			System.out.println("Waiting for the thread pool to terminate ...");
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		wikiStatistics.print();
		errorWriter.close();
		postUser.close();
		postTime.close();
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
