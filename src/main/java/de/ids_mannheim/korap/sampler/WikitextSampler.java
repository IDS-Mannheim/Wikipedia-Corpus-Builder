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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Creates a small sample of Wikipedia dump
 * 
 * @author margaretha
 *
 */
public class WikitextSampler {
	private Options options;
	private String filePath;
	private double factor;
	private BufferedWriter writer;

	public static Pattern redundanzPattern = Pattern.compile("^<title>Wikipedia:Redundanz");  
	public static Pattern löschKandidatenPattern = Pattern.compile("^<title>Wikipedia:Löschkandidaten");
	
	public WikitextSampler() {
		options = new Options();
		options.addOption("f", true, "factor between 0 and 1");
		options.addOption("i", true, "wikipedia input file");
		options.addOption("o", true, "output file");
	}

	public static void main(String[] args) throws ParseException, IOException {
		long start = System.currentTimeMillis();
		WikitextSampler sampler = new WikitextSampler();
		sampler.parseArguments(args);
		sampler.createSample();
		long end = System.currentTimeMillis();
		System.out.println("Run time: "+(end-start) + "ms.");
	}

	private void parseArguments(String[] args)
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
		else {
			this.factor = 0.01;
		}

		String outputPath = cmd.getOptionValue("o");
		if (outputPath != null) {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(outputPath))), 2048);
		}
		else {
			writer = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(new File("output.xml"))),
					2048);
			System.out.println("Creating output file with name output.xml");
		}
	}

	private void createSample() throws IOException {
		File file = new File(filePath);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)), 2048);

		String line;
		String trimmedLine;
		String page = null;
		boolean read = false;
		double weight = 0.3;
		while ((line = br.readLine()) != null) {
			trimmedLine = line.trim();
			if (trimmedLine.startsWith("<mediawiki")) {
				read = true;
			}
			else if (trimmedLine.startsWith("<title>")) {
				if (trimmedLine
						.startsWith("<title>Wikipedia:Löschdiskusionen")) {
					if (Math.random() - weight < factor) {
						writer.append(page );
						writer.append("\n");
						read = true;
					}
					continue;
				}
				else if (trimmedLine.startsWith("<title>Wikipedia:Redundanz")) {
					if (Math.random() - weight < factor) {
						writer.append(page );
						writer.append("\n");
						read = true;
					}
				}
				else if (Math.random() < factor) {
					writer.append(page );
					writer.append("\n");
					read = true;
				}
			}
			else if (trimmedLine.startsWith("<page")) {
				page = line;
				read = false;
			}

			if (read) {
				writer.append(line);
				writer.append("\n");
			}
		}
		writer.close();
		br.close();
	}
	
	private boolean match(String string, Pattern pattern){
		Matcher matcher = pattern.matcher(string);		
		if (matcher.find()){
			System.out.println(matcher.group(1));
			return true;
		}
		return false;
	}
}
