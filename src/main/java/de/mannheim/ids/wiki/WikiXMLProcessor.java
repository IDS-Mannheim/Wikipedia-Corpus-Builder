package de.mannheim.ids.wiki;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

import de.mannheim.ids.wiki.page.WikiArticleHandler;
import de.mannheim.ids.wiki.page.WikiPage;
import de.mannheim.ids.wiki.page.WikiPageHandler;
import de.mannheim.ids.wiki.page.WikiPageReader;
import de.mannheim.ids.wiki.page.WikiPostHandler;
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
	private BlockingQueue<WikiPage> wikipages;
	private WikiPostUser postUser;
	private WikiPostTime postTime;

	public static final WikiPage endPage = new WikiPage();
	public static String Wikipedia_URI;

	public static final WikiConfig wikiconfig = DefaultConfigEnWp.generate();

	public WikiXMLProcessor(Configuration config) throws IOException {
		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		this.config = config;
		this.wikiStatistics = new WikiStatistics();
		this.errorWriter = new WikiErrorWriter(config);
		this.wikipages = new ArrayBlockingQueue<WikiPage>(
				config.getMaxThreads());

		if (config.isDiscussion()) {
			String prefix = Paths.get(config.getWikidump()).getFileName()
					.toString().substring(0, 15);
			postUser = new WikiPostUser(prefix, config);
			postTime = new WikiPostTime(prefix, config.getPageType());
		}

		Wikipedia_URI = "https://" + config.getLanguageCode()
				+ ".wikipedia.org/wiki/";
	}

	public void run() throws IOException {

		long startTime = System.currentTimeMillis();

		WikiPageReader wikiReader = new WikiPageReader(config, wikipages,
				endPage, wikiStatistics);
		Thread wikiReaderThread = new Thread(wikiReader, "wikiReader");
		ExecutorService pool = Executors.newFixedThreadPool(config
				.getMaxThreads());

		try {
			wikiReaderThread.start();
			for (WikiPage wikiPage = wikipages.take(); !wikiPage
					.equals(endPage); wikiPage = wikipages.take()) {
				WikiPageHandler ph;
				if (config.isDiscussion()) {
					ph = new WikiPostHandler(config, wikiPage, wikiStatistics,
							errorWriter, postUser, postTime);
				}
				else {
					ph = new WikiArticleHandler(config, wikiPage,
							wikiStatistics, errorWriter);
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
			}
			catch (InterruptedException e1) {
				System.err.println("Pool termination was interrupted.");
			}
			Thread.currentThread().interrupt();
		}

		if (config.isDiscussion()) {
			postUser.close();
			postTime.close();
		}
		errorWriter.close();
		wikiStatistics.print();

		long endTime = System.currentTimeMillis();
		String duration = DurationFormatUtils.formatDuration(
				(endTime - startTime), "H:mm:ss");
		System.out.println("WikiXMLConverter execution time "
		// + (endTime - startTime));
				+ duration);
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
