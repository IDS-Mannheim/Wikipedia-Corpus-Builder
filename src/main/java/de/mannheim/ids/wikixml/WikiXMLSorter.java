package de.mannheim.ids.wikixml;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.mannheim.ids.transform.Transformer;
import de.mannheim.ids.transform.WikiI5Part;
import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.WikiI5Processor;

public class WikiXMLSorter extends Thread {

	private final XPathFactory xPathFactory;
	private final XPath xPath;
	private final Configuration config;
	private final Document wikiPageIndexes;
	private ExecutorService pool;
	private Future<WikiI5Part> endFuture;

	public WikiXMLSorter(Configuration config, Future<WikiI5Part> endFuture,
			ExecutorService pool) {
		this.config = config;
		this.xPathFactory = XPathFactory.newInstance();
		this.xPath = xPathFactory.newXPath();
		this.endFuture = endFuture;
		this.pool = pool;

		WikiXMLIndex xmlIndex = new WikiXMLIndex(config.getWikiXMLIndex());
		wikiPageIndexes = xmlIndex.getIndexDoc();

	}

	@Override
	public void run() {

		int n = 0;
		XPathExpression lastId;
		// group pages by index
		for (String idx : Configuration.indexes) {
			try {
				lastId = xPath.compile(config.getPageType() + "/index[@value='"
						+ idx + "']/id[last()]");
				n = (int) (double) lastId.evaluate(wikiPageIndexes,
						XPathConstants.NUMBER);
			}
			catch (XPathExpressionException e) {
				throw new RuntimeException(
						"Failed acquiring the last id from index " + idx, e);
			}
			if (n < 1) continue;

			try {
				groupPagesbyDoc(idx, n);
			}
			catch (I5Exception e) {
				throw new RuntimeException(e);
			}
		}
		// end
		try {
			WikiI5Processor.wikiI5Queue.put(endFuture);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err
					.println("Failed putting endFuture to the blocking queue.");
		}
	}

	// Group per 100000 pages into a doc
	private void groupPagesbyDoc(String idx, int n) throws I5Exception {
		XPathExpression group;
		String docSigle;
		int docNr;

		for (int i = 0; i < n / 100000 + 1; i++) {
			docNr = i;
			docSigle = idx + String.format("%02d", docNr);
			System.out.println("DocId " + docSigle);

			NodeList pagegroup = null;
			try {
				group = xPath.compile(config.getPageType() + "/index[@value='"
						+ idx + "']/id[xs:integer"
						+ "(xs:integer(.) div 100000) = " + docNr + "]");
				pagegroup = (NodeList) group.evaluate(wikiPageIndexes,
						XPathConstants.NODESET);
			}
			catch (XPathExpressionException e) {
				throw new I5Exception(
						"Failed acquiring the pagegroup for doc sigle "
								+ docSigle, e);
			}
			if (pagegroup.getLength() < 1) {
				continue;
			}

			// Start document;
			WikiI5Part startDoc = new WikiI5Part(idx, docNr, true);
			try {
				WikiI5Processor.wikiI5Queue
						.put(createFutureFromWikiI5Part(startDoc));
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new I5Exception(
						"Failed putting startDoc to the blocking queue.");
			}

			createFilesFromGroup(pagegroup, idx);

			// End document
			WikiI5Part endDoc = new WikiI5Part(false);
			try {
				WikiI5Processor.wikiI5Queue
						.put(createFutureFromWikiI5Part(endDoc));
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new I5Exception(
						"Failed putting endDoc to the blocking queue.");
			}
		}

	}

	private Future<WikiI5Part> createFutureFromWikiI5Part(final WikiI5Part w) {
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
				return w;
			}

			@Override
			public WikiI5Part get() throws InterruptedException,
					ExecutionException {
				return w;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}
		};
	}

	private void createFilesFromGroup(NodeList pagegroup, String idx)
			throws I5Exception {

		// Do transformation and validation for each page in the group
		for (int j = 0; j < pagegroup.getLength(); j++) {
			String pageId = pagegroup.item(j).getTextContent();
			String xmlPath = idx + "/" + pageId + ".xml";
			System.out.println(xmlPath);

			Transformer t = new Transformer(config, new File(xmlPath), idx,
					pageId);
			try {
				WikiI5Processor.wikiI5Queue.put(pool.submit(t));
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err
						.println("Thread was interrupted while putting "
								+ "a transformation future result to the blocking queue. ");
			}
		}
	}
}
