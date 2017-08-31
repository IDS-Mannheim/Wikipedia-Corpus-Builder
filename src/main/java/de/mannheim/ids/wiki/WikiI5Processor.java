package de.mannheim.ids.wiki;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.time.DurationFormatUtils;

import de.mannheim.ids.transform.WikiI5Part;
import de.mannheim.ids.transform.WikiXMLSorter;

/**
 * Manages the overall WikiXML to I5 conversion process.
 * 
 * @author margaretha
 *
 */
public class WikiI5Processor {

	private final Configuration config;

	private Statistics statistics;
	private I5ErrorHandler errorHandler;

	public static BlockingQueue<Future<WikiI5Part>> wikiI5Queue;

	/**
	 * Constructs a WikiI5Processor.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @throws I5Exception
	 *             an {@link I5Exception}
	 */
	public WikiI5Processor(Configuration config) throws I5Exception {

		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null.");
		}

		this.config = config;
		wikiI5Queue = new ArrayBlockingQueue<Future<WikiI5Part>>(
				config.getMaxThreads());
		errorHandler = new I5ErrorHandler(config);
		statistics = new Statistics();
	}

	/**
	 * Starts the conversion process with creating I5Writing and writing the
	 * start document element. Then, sorts the WikiXML files by using
	 * WikiXMLSorter, runs the XSLT transformations for the sorted Wikipages and
	 * put the Future results in a BlockingQueue limiting the number of threads.
	 * Writes each future results and eventually builds the whole WikiI5 corpus.
	 * 
	 * 
	 * @throws I5Exception an {@link I5Exception}
	 */
	public void run() throws I5Exception {

		long start = System.currentTimeMillis();

		I5Writer wikiI5Writer = new I5Writer(config, errorHandler, statistics);
		wikiI5Writer.writeStartDocument();

		Future<WikiI5Part> endFuture = createEndFuture();
		ExecutorService pool = Executors
				.newFixedThreadPool(config.getMaxThreads());
		WikiXMLSorter sorter = new WikiXMLSorter(config, endFuture, pool,
				errorHandler, statistics);
		pool.execute(sorter);

		try {
			for (Future<WikiI5Part> future = wikiI5Queue.take(); !future
					.equals(endFuture); future = wikiI5Queue.take()) {
				try {
					WikiI5Part w = future.get();
					wikiI5Writer.write(w);
				}
				catch (ExecutionException e) {
					System.err.println("Future execution throws an exception: "
							+ e.getCause());
				}
			}
			pool.shutdown();
		}
		catch (InterruptedException e) {
			System.err.println(
					"Blocking queue was interrupted, cause: " + e.getCause());
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

		statistics.printStats();
		wikiI5Writer.close();
		errorHandler.close();

		long end = System.currentTimeMillis();
		String duration = DurationFormatUtils.formatDuration((end - start),
				"H:mm:ss");
		System.out.println("WikiI5Converter execution time "
				// + (end - start));
				+ duration);
	}

	/**
	 * Creates an end Future as a sign (the last Future) to stop the writing
	 * process.
	 * 
	 * @return the end Future
	 */
	private Future<WikiI5Part> createEndFuture() {
		return new Future<WikiI5Part>() {
			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public WikiI5Part get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return null;
			}

			@Override
			public WikiI5Part get()
					throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}
		};
	}

}
