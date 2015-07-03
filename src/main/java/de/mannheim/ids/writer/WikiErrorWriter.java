package de.mannheim.ids.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.Utilities;

/**
 * Class for logging errors occurring during the wikitext to wikiXML conversion
 * process.
 * 
 * @author margaretha
 * */
public class WikiErrorWriter {

	public OutputStreamWriter errorWriter;
	private int errorCounter;

	/**
	 * Constructs a WikiErrorWriter. The errors are logged in the given input
	 * file by using the given encoding.
	 * 
	 * @param inputFile
	 * @param encoding
	 * @throws IOException
	 */
	public WikiErrorWriter(Configuration config) throws IOException {
		String filename = Paths.get(config.getWikidump()).getFileName()
				.toString();

		errorWriter = Utilities.createWriter(
				"logs",
				"wikiXML-" + filename.substring(0, 15) + "-"
						+ config.getPageType() + "-errors.log",
				config.getOutputEncoding());
		errorCounter = 1;
	}

	/**
	 * Logs the error described with the given parameters
	 * 
	 * @param type the type of the error
	 * @param pagetitle the wiki page title where the error has occurred
	 * @param cause the cause of the error
	 */
	public void logErrorPage(String type, String pagetitle, String pageId,
			Throwable cause, String wikitext) {
		synchronized (errorWriter) {
			try {
				errorWriter.append(String.valueOf(errorCounter++));
				errorWriter.append(" ");
				errorWriter.append(type);
				errorWriter.append(": ");
				errorWriter.append(pagetitle);
				errorWriter.append(" #");
				errorWriter.append(pageId);
				errorWriter.append(", cause: ");
				errorWriter.append(cause.toString());
				if (!wikitext.isEmpty()) {
					errorWriter.append("\n");
					errorWriter.append(wikitext);
				}
				errorWriter.append("\n\n");
				errorWriter.flush();
			}
			catch (IOException e) {
				System.out.println("Failed writing error.");
			}
		}
	}

	/**
	 * Closes the error writer.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		try {
			if (errorWriter != null) {
				errorWriter.close();
			}
		}
		catch (IOException e) {
			System.out.println("Failed closing the error writer.");
			throw new IOException(e);
		}
	}
}
