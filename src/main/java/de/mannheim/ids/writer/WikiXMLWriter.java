package de.mannheim.ids.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.Utilities;
import de.mannheim.ids.wiki.page.WikiPage;

/**
 * This class writes an XML file for each XML-ized wiki page.
 * 
 * @author margaretha
 * 
 */
public class WikiXMLWriter {

	private Configuration config;

	public WikiXMLWriter(Configuration config) {

		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}

		this.config = config;
	}

	public void write(WikiPage wikiPage, String content, String outputFolder)
			throws IOException {

		if (wikiPage == null) {
			throw new IllegalArgumentException("WikiPage cannot be null.");
		}
		if (outputFolder == null || outputFolder.isEmpty()) {
			throw new IllegalArgumentException(
					"Output folder cannot be null or empty.");
		}

		if (content != null && !content.isEmpty()) {

			String path = outputFolder + "/" + wikiPage.getPageIndex() + "/";
			System.out.println(path + wikiPage.getPageId() + ".xml");

			OutputStreamWriter writer = Utilities.createWriter(path,
					wikiPage.getPageId() + ".xml", config.getOutputEncoding());

			writer.append("<?xml version=\"1.0\" encoding=\"");
			writer.append(config.getOutputEncoding());
			writer.append("\"?>\n");

			String[] arr = wikiPage.getPageStructure().split("<text></text>");
			writer.append(arr[0]);
			writer.append("<text lang=\"" + config.getLanguageCode() + "\">\n");
			writer.append(content);
			if (!config.isDiscussion()) {
				writer.append("\n");
			}
			writer.append("      </text>");
			writer.append(arr[1]);
			writer.close();
		}
	}
}
