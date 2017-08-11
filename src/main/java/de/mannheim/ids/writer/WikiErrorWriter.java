package de.mannheim.ids.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.Utilities;

/**
 * Class for logging errors occurring during the wikitext to wikiXML conversion
 * process.
 * 
 * @author margaretha
 */
public class WikiErrorWriter {

	public OutputStreamWriter writer;
	private int errorCounter;
	private Configuration config;

	/**
	 * Constructs a WikiErrorWriter. The errors are logged in the given input
	 * file by using the given encoding.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @throws IOException
	 *             an IOException
	 */
	public WikiErrorWriter(Configuration config) throws IOException {
		String filename = Paths.get(config.getWikidump()).getFileName()
				.toString();

		writer = Utilities.createWriter("logs",
				"wikiXML-" + filename.substring(0, 15) + "-"
						+ config.getPageType() + "-errors.log",
				config.getOutputEncoding());
		errorCounter = 1;
		this.config = config;
	}

	/**
	 * Logs the error described with the given parameters
	 * 
	 * @param type
	 *            the type of the error
	 * @param pagetitle
	 *            the wiki page title where the error has occurred
	 * @param cause
	 *            the cause of the error
	 * @param wikitext
	 *            the problematic wikitext
	 */
	public synchronized void logErrorPage(String type, String pagetitle,
			String pageId, Throwable cause, String wikitext) {
		try {
			writer.append(String.valueOf(errorCounter++));
			writer.append(" ");
			writer.append(type);
			writer.append(": ");
			writer.append(pagetitle);
			writer.append(" #");
			writer.append(pageId);
			writer.append(", cause: ");
			writer.append(cause.toString());
			if (config.isDiscussion()) {
				writer.append("\n");
				writer.append(wikitext);
			}
			else if (wikitext.isEmpty()) {
				writer.append("\n");
				cause.printStackTrace(new PrintWriter(writer));
			}
			writer.append("\n");
			writer.flush();
		}
		catch (IOException e) {
			System.out.println("Failed writing error.");
		}
	}

	/**
	 * Closes the error writer.
	 * 
	 * @throws IOException
	 *             an IOException
	 */
	public void close() throws IOException {
		try {
			if (writer != null) {
				writer.close();
			}
		}
		catch (IOException e) {
			System.out.println("Failed closing the error writer.");
			throw new IOException(e);
		}
	}
}
