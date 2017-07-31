package de.ids_mannheim.korap.sampler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Creates a small sample of Wikipedia dump.
 * 
 * For German Wikipedia, the probability of sampling pages with titles starting
 * with Wikipedia:Redundanz and Wikipedia:Löschkandidaten can be increased by
 * specifying weights. The smaller the weights, the smaller the number of the
 * pages included in the the sample.
 * 
 * @author margaretha
 * 
 */
public class WikitextSampler {
	private Options options;
	private String filePath;
	private static String outputPath;
	private BufferedWriter writer;
	private double factor = 0.00001;
	// Recommended redundanzWeight = 0.05
	private double redundanzWeight = 0;
	// Recommended löschkandidatenWeight = 0.001
	private double löschkandidatenWeight = 0;

	public WikitextSampler() {
		options = new Options();
		options.addOption("f", true, "factor between 0 and 1."
				+ "Smaller factor means less number of pages. ");
		options.addOption("i", true, "wikipedia input file");
		options.addOption("o", true, "output file");
		options.addOption("rw", true,
				"weight for sampling Wikipedia:Redundanz pages. "
						+ "Smaller weight means less number of pages. ");
		options.addOption("lw", true,
				"weight for sampling Wikipedia:Löschkandidaten pages."
						+ "Smaller weight means less number of pages. ");
	}

	public static void main(String[] args) throws ParseException, IOException {
		long start = System.currentTimeMillis();
		WikitextSampler sampler = new WikitextSampler();
		sampler.parseArguments(args);
		sampler.createSample();
		long end = System.currentTimeMillis();
		System.out.println("Run time: " + (end - start) + " ms.");

		File outputFile = new File(outputPath);
		double kb = (outputFile.length() / 1024);
		System.out.println("Output file size " + kb + "KB.");
	}

	public void parseArguments(String[] args)
			throws ParseException, FileNotFoundException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		String file = cmd.getOptionValue("i");
		if (file != null) {
			this.filePath = file;
		}
		else {
			throw new IllegalArgumentException(
					"Please specify the location of the wikipedia file.");
		}

		String factor = cmd.getOptionValue("f");
		if (factor != null) {
			this.factor = Double.valueOf(factor);
		}

		String redundanzWeight = cmd.getOptionValue("rw");
		if (redundanzWeight != null) {
			this.redundanzWeight = Double.valueOf(redundanzWeight);
		}

		String löschkandidatenWeight = cmd.getOptionValue("lw");
		if (löschkandidatenWeight != null) {
			this.löschkandidatenWeight = Double.valueOf(löschkandidatenWeight);
		}

		outputPath = cmd.getOptionValue("o");
		if (outputPath == null) {
			outputPath = "output.xml";
			System.out.println("Creating output file with name output.xml");
		}

		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(outputPath))), 2048);
	}

	public void createSample() throws IOException {
		File file = new File(filePath);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)), 2048);

		String normalizedLöschkandidaten = Normalizer.normalize(
				"<title>Wikipedia:Löschkandidaten", Normalizer.Form.NFD);

		String line;
		String trimmedLine;
		String page = null;
		boolean read = false;

		while ((line = br.readLine()) != null) {
			trimmedLine = line.trim();
			if (trimmedLine.startsWith("<mediawiki")) {
				read = true;
			}
			else if (trimmedLine.startsWith("<title>")) {
				String normalizedTitle = Normalizer.normalize(trimmedLine,
						Form.NFD);
				if (normalizedTitle.startsWith(normalizedLöschkandidaten)) {
					if (Math.random() - löschkandidatenWeight < factor) {
						writer.append(page);
						writer.append("\n");
						read = true;
						System.out.println(trimmedLine);
					}
				}
				else if (trimmedLine.startsWith("<title>Wikipedia:Redundanz")) {
					if (Math.random() - redundanzWeight < factor) {
						writer.append(page);
						writer.append("\n");
						read = true;
						System.out.println(trimmedLine);
					}
				}
				else if (Math.random() < factor) {
					writer.append(page);
					writer.append("\n");
					read = true;
					System.out.println(trimmedLine);
				}
			}
			else if (trimmedLine.startsWith("<page")) {
				page = line;
				read = false;
			}
			else if (trimmedLine.startsWith("</mediawiki")) {
				read = true;
			}

			if (read) {
				writer.append(line);
				writer.append("\n");
			}
		}
		writer.close();
		br.close();
	}

}
