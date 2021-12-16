package de.mannheim.ids.transform;

import java.io.File;
import java.util.List;
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
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.xml.sax.InputSource;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.I5ErrorHandler;
import de.mannheim.ids.wiki.I5Exception;
import de.mannheim.ids.wiki.Statistics;
import de.mannheim.ids.wiki.WikiI5Processor;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;

/**
 * TaskManager sorts WikiXML pages alphabetically and groups them in
 * idsDoc elements. Each idsDoc should contain 100000 idsText
 * elements.
 * 
 * It creates a future transformation task for each WikiXML, and other
 * tasks to generate start and end documents.
 * The tasks are added to the {@link WikiI5Processor#taskQueue}.
 * 
 * @author margaretha
 *
 */
public class TaskManager extends Thread {

	private static final int DOCUMENT_SIZE=100000; 
	
	private XPathFactory xPathFactory;
	private final XPath xPath;
	private final Configuration config;
//	private NodeInfo wikiPageIndexes;
	private ExecutorService pool;
	private Future<WikiI5Part> endFuture;
	private I5ErrorHandler errorHandler;
	private Statistics statistics;
	private InputSource is;

	/** Construct a WikiXMLSorter.
	 * @param config the conversion configuration
	 * @param endFuture a dummy Future serving as a sign to end the process.
	 * @param pool an ExecutorService
	 * @param errorHandler an I5ErrorHandler
	 * @param statistics a statistic counter
	 * @throws I5Exception an I5Exception
	 */
	public TaskManager(Configuration config, Future<WikiI5Part> endFuture,
			ExecutorService pool, I5ErrorHandler errorHandler, Statistics statistics) throws I5Exception {
		this.config = config;
		
		System.setProperty("javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON,
				                "net.sf.saxon.xpath.XPathFactoryImpl");
		
		try {
			this.xPathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
		}
		catch (XPathFactoryConfigurationException e) {
			throw new I5Exception("Failed creating a new instance of XPathFactory", e);
		}
		this.xPath = xPathFactory.newXPath();
		this.endFuture = endFuture;
		this.pool = pool;
		this.errorHandler = errorHandler;
		this.statistics = statistics;
		
		is = new InputSource(new File(config.getWikiXMLIndex()).toURI().toString());        
//		SAXSource ss = new SAXSource(is);
//		try {
//			wikiPageIndexes = ((XPathEvaluator) xPath)..setSource(ss);
//		}
//		catch (XPathException e) {
//			throw new I5Exception("Failed setting wikipage index node.", e);
//		}
		
		
	}
	
	@Override
	public void run() {
		groupPagesByIndex();
		try {
			WikiI5Processor.taskQueue.put(endFuture);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed putting endFuture to the blocking queue.",e);
		}
		
		// count number of pages with unsorted chars (excluded / not to be transformed)
		try {
			XPathExpression chars;
			chars = xPath.compile("count("+config.getPageType() + "/index[@value='Char']/id)");
			int m = (int) (double) chars.evaluate(is,
					XPathConstants.NUMBER);
			statistics.setNumOfChar(m);
		}
		catch (XPathExpressionException e) {
			throw new RuntimeException(
					"Failed counting number of pages with unsorted chars.");
		}
		
	}
 
	
	private void groupPagesByIndex() {
		int n = 0;
		XPathExpression lastId;
		for (String idx : Configuration.indexes) {
			try {
				lastId = xPath.compile("wiki/index[@value='"
						+ idx + "']/id[last()]");
				n = (int) (double) lastId.evaluate(is,
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
	}
	
	/** Groups per 100000 pages into an ids document
	 * 
	 * @param idx document index
	 * @param n the last id
	 * @throws I5Exception an I5Exception
	 */
	private void groupPagesbyDoc(String idx, int n) throws I5Exception {
		XPathExpression group;
		String docId;
		int docNr;

		for (int i = 0; i < n / DOCUMENT_SIZE + 1; i++) {
			docNr = i;
			docId = idx + String.format("%04d", docNr);
//			System.out.println("DocId " + docId);

			List pagegroup = null;
			try {
				group = xPath.compile("wiki/index[@value='"
						+ idx + "']/id[xs:integer"
						+ "(xs:integer(.) div 100000) = " + docNr + "]");
				pagegroup = (List) group.evaluate(is,
						XPathConstants.NODESET);
			}
			catch (XPathExpressionException e) {
				throw new I5Exception(
						"Failed acquiring the pagegroup for doc id "
								+ docId, e);
			}
			if (pagegroup.size() < 1) {
				continue;
			}

			addFutureDocPart(new WikiI5Part(idx, docNr, true), "startDoc");
			addFutureTasks(pagegroup, idx);
			addFutureDocPart(new WikiI5Part(), "endDoc");
		}

	}
	
	private void addFutureDocPart(WikiI5Part part, String whichPart) throws I5Exception {
		try {
		    Future<WikiI5Part> task = createTaskFromWikiI5Part(part);
			WikiI5Processor.taskQueue
					.put(task);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new I5Exception(
					"Failed putting "+whichPart+" to the blocking queue.");
		}
	}
	
	/** Creates a Future object from the given WikiI5Part.
	 * @param w a WikiI5Part
	 * @return
	 */
	private Future<WikiI5Part> createTaskFromWikiI5Part(final WikiI5Part w) {
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

	/** Performs transformation and validation for each page in the given pagegroup. 
	 * 	The transformation results are Future objects collected in the wikiI5Queue 
	 * 	belonging to the WikiI5Processor. 
	 * 
	 * @see WikiI5Processor
	 * 
	 * @param pagegroup a list of wikipages
	 * @param idx the index of the wikipages
	 * @throws I5Exception
	 */
	private void addFutureTasks(List pagegroup, String idx)
			throws I5Exception {

		for (int j = 0; j < pagegroup.size(); j++) {
			NodeInfo pg = (NodeInfo) pagegroup.get(j);
			String pageId = pg.getStringValue();
			String xmlPath = idx + "/" + pageId + ".xml";
//			System.out.println(xmlPath);

			Transformer t = new Transformer(config, statistics, errorHandler,
					xmlPath, idx,	pageId);
			try {
				WikiI5Processor.taskQueue.put(pool.submit(t));
			}
			catch (InterruptedException e) {
				System.err
						.println("Thread "+xmlPath+"was interrupted while putting "
								+ "a transformation future result to the blocking queue. ");
			}
		}
	}
}