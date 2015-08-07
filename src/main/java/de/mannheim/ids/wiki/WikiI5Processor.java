package de.mannheim.ids.wiki;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.mannheim.ids.transform.WikiI5Part;
import de.mannheim.ids.transform.WikiXMLSorter;

public class WikiI5Processor {

	private final Configuration config;

	public static BlockingQueue<Future<WikiI5Part>> wikiI5Queue;

	public WikiI5Processor(Configuration config) {

		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null.");
		}

		this.config = config;
		wikiI5Queue = new ArrayBlockingQueue<Future<WikiI5Part>>(
				config.getMaxThreads());

	}

	public void run() throws I5Exception {

		I5Writer wikiI5Writer = new I5Writer(config);
		wikiI5Writer.writeStartDocument();

		Future<WikiI5Part> endFuture = createEndFuture();
		ExecutorService pool = Executors.newFixedThreadPool(config
				.getMaxThreads());
		WikiXMLSorter sorter = new WikiXMLSorter(config, endFuture, pool);
		pool.execute(sorter);

		try {
			for (Future<WikiI5Part> future = wikiI5Queue.take(); !future
					.equals(endFuture); future = wikiI5Queue.take()) {
				wikiI5Writer.write(future.get());
			}
			pool.shutdown();
		}
		catch (InterruptedException | ExecutionException e) {
			System.out.println(e.getCause());
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

		wikiI5Writer.close();
	}

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
			public WikiI5Part get() throws InterruptedException,
					ExecutionException {
				return null;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}
		};
	}

}
