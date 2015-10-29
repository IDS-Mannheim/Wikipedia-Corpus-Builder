package de.mannheim.ids.wiki;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;

/**
 * Class Utilities contains utility methods used in some other classes.
 * 
 * @author margaretha
 * 
 */
public class Utilities {

	/**
	 * Creates the directory according to the given path including non-existent
	 * parent directories.
	 * 
	 * @param path
	 */
	public static void createDirectory(String path) {

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException(
					"Directory cannot be null or empty.");
		}

		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * Creates an OutputStreamWriter according to the given outputFile and the
	 * encoding parameters. This method also creates any necessary non-existent
	 * directories where the outputFile should be written on.
	 * 
	 * @param directory a directory path to the output file
	 * @param outputFile output filename
	 * @param encoding encoding
	 * @return an OutputStreamWriter
	 * @throws IOException
	 */
	public static OutputStreamWriter createWriter(String directory,
			String outputFile, String encoding) throws IOException {

		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException(
					"Encoding cannot be null or empty.");
		}

		createDirectory(directory);

		File file = new File(directory + "/" + outputFile);
		if (!file.exists()) {
			file.createNewFile();
		}
		return new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(file)), encoding);
	}

	/**
	 * Normalized the given input (a letter)
	 * 
	 * @param input a letter
	 * @param indexList a list letters and numbers
	 * @return a normalized String
	 * @throws IOException
	 */
	public static String normalizeIndex(String input, String[] indexList)
			throws IOException {
		String normalizedStr = Normalizer.normalize(input, Form.NFKD)
				.toUpperCase();
		normalizedStr = normalizedStr.substring(0, 1);

		if (Arrays.asList(indexList).contains(normalizedStr)) {
			return normalizedStr;
		}
		else {
			return "Char";
		}
	}
}
