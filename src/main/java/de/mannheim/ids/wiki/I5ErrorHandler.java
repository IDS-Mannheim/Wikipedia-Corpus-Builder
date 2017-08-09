package de.mannheim.ids.wiki;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class handles the validation error and writes the error messages to a
 * text file in logs/ folder with name format: wikii5-[language]wiki-[published
 * date]-[pagetype]-error.log.
 * 
 * @author margaretha
 * 
 */

public class I5ErrorHandler implements ErrorHandler, ErrorListener {

	private OutputStreamWriter errorWriter;
	private int numOfInvalidText = 0;

	/** Constructs I5ErrorHandler.
	 * @param config the conversion configuration
	 * @throws I5Exception an {@link I5Exception}
	 */
	public I5ErrorHandler(Configuration config) throws I5Exception {

		if (config == null) {
			throw new IllegalArgumentException("Config cannot be null");
		}

		try {
			File dir = new File("logs");
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File errorFile = new File(createFileName(config));
			errorFile.createNewFile();

			errorWriter = new OutputStreamWriter(
					new FileOutputStream(errorFile));

		}
		catch (IOException e) {
			throw new I5Exception("Failed creating the error file.", e);
		}
	}

	/** Creates a log file for logging the errors.
	 * @param config the conversion configuration
	 * @return a file name
	 */
	private String createFileName(Configuration config) {
		StringBuilder sb = new StringBuilder();
		sb.append("logs/wikiI5-");
		sb.append(config.getDumpFilename().substring(0, 16));
		sb.append(config.getPageType());
		sb.append("-error.log");
		return sb.toString();
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		throw new SAXException(exception);
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw new SAXException(exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw new SAXException(exception);
	}

	/** Writes information about an error, such where it happens and what type of error has occurred.
	 * @param xmlPath the wikixml file path where the error occurs
	 * @param message an error message
	 * @param t a Throwable
	 * @throws I5Exception an {@link I5Exception}
	 */
	public synchronized void write(String xmlPath, String message, Throwable t)
			throws I5Exception {
		numOfInvalidText++;
		try {
			errorWriter.append(numOfInvalidText + " ");
			errorWriter.append(xmlPath);
			errorWriter.append(": ");
			errorWriter.append(message);			
			errorWriter.append("\n");
			if (t.getCause() != null){
				errorWriter.append(t.getCause().toString());
			}
			else{
				errorWriter.append(t.getMessage());
			}
			errorWriter.append("\n\n");
			errorWriter.flush();
		}
		catch (IOException e) {
			throw new I5Exception("Failed writing error.", e);
		}
	}

	public void close() throws I5Exception {		
		try {
			errorWriter.close();
		}
		catch (IOException e) {
			throw new I5Exception("Failed closing the error writer.", e);
		}
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
		System.err.println("Transformer warning: "+exception.getCause());
		throw exception;		
	}

	@Override
	public void error(TransformerException exception)
			throws TransformerException {
		System.err.println("Transformer error: "+exception.getCause());
		throw exception;
	}

	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		//exception.printStackTrace();
		System.err.println("Transformer fatal error: "+exception.getCause());
		throw exception;		
	}

}
