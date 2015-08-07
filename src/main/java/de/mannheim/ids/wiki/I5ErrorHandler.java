package de.mannheim.ids.wiki;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class handles the validation error and writes the error messages to a
 * text file with name format: i5-[language]wiki-[published
 * date]-[pagetype]-error.txt.
 * 
 * @author margaretha
 * 
 */

public class I5ErrorHandler implements ErrorHandler {

	private OutputStreamWriter errorWriter;
	private int numOfInvalidText = 0;

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

	private String createFileName(Configuration config) {
		StringBuilder sb = new StringBuilder();
		sb.append("logs/wikiI5-");
		sb.append(config.getDumpFilename().substring(0, 16));
		sb.append(config.getPageType());
		sb.append("-error.txt");
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
			throw new I5Exception("Failed writing DVD validation error.", e);
		}
	}

	void close() throws I5Exception {
		System.out.println("Number Of invalid text: " + numOfInvalidText);
		try {
			errorWriter.close();
		}
		catch (IOException e) {
			throw new I5Exception("Failed closing the error writer.", e);
		}
	}

}
