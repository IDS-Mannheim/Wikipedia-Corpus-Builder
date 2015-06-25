package de.mannheim.ids.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.mannheim.ids.wiki.Utilities;


/**
 * Time information of postings. <br/>
 * Generates a list of timestamps in XML.
 * 
 * @author margaretha
 * 
 */

public class WikiPostTime {

	private OutputStreamWriter timeWriter;
	private int counter;

	public WikiPostTime(String prefixFileName) throws IOException {

		if (prefixFileName == null || prefixFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"prefixFileName cannot be null or empty.");
		}

		timeWriter = Utilities.createWriter("post", prefixFileName
				+ "-post-timeline.xml", "utf-8");
		counter = 0;

		timeWriter.append("<timeline>\n");
	}

	public String createTimestamp(String timeline) throws IOException {

		if (timeline == null || timeline.isEmpty()) {
			throw new IllegalArgumentException(
					"Timeline cannot be null or empty.");
		}

		String timeId = generateTimeId();
		synchronized (timeWriter) {
			timeWriter.append("   <when xml:id=\"" + timeId + "\"");
			timeWriter.append(" absolute=\"" + timeline + "\"/>\n");
		}
		return timeId;
	}

	private String generateTimeId() {
		String timeId = "t" + String.format("%08d", counter);
		counter++;
		return timeId;
	}

	public void close() throws IOException {
		timeWriter.append("</timeline>\n");
		timeWriter.close();
	}

}
