package de.mannheim.ids.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.wiki.Utilities;
import de.mannheim.ids.wiki.WikiXMLProcessor;

/**
 * Class implementation for handling posting authors. <br/>
 * Generates a list of post authors (e.g. wikipedia users) in XML.
 * 
 * @author margaretha
 * 
 */

public class WikiPostUser {

	private Map<String, String> userMap; // username, userid
	private OutputStreamWriter userWriter;
	private int counter;

	public WikiPostUser(String prefixFileName, Configuration config)
			throws IOException {

		if (prefixFileName == null || prefixFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"prefixFileName cannot be null or empty.");
		}

		userMap = new HashMap<String, String>();
		userWriter = Utilities.createWriter("post", prefixFileName + "-post-"
				+ config.getPageType() + "-user.xml", "utf-8");
		userWriter.append("<listPerson>\n");
		counter = 1;
	}

	public void createPostUser(String username, String userLink)
			throws IOException {
		if (username == null) {
			throw new IllegalArgumentException("Username cannot be null.");
		}

		synchronized (userMap) {
			if (!userMap.containsKey(username)) {
				String userId = generateUserId();
				userMap.put(username, userId);
				createPerson(username, userId, userLink);
			}
		}
	}

	public String getUserId(String username) {
		return userMap.get(username);
	}

	private String generateUserId() {
		String userId = "WU" + String.format("%08d", counter);
		counter++;
		return userId;
	}

	private void createPerson(String username, String userId, String userLink)
			throws IOException {

		synchronized (userWriter) {
			userWriter.append("   <person xml:id=\"" + userId + "\">\n");
			userWriter.append("      <persName>" + username + "</persName>\n");

			if (userLink != null && !userLink.isEmpty()) {
				userWriter.append("      <signatureContent>\n");
				userWriter.append("         <ref target=\"");
				userWriter.append(WikiXMLProcessor.Wikipedia_URI);
				userWriter.append(userLink.replaceAll("\\s", "_") + "\">");
				userWriter.append(username + "</ref>\n");
				userWriter.append("      </signatureContent>\n");
			}

			userWriter.append("   </person>\n");
			userWriter.flush();
		}
	}

	public void close() throws IOException {
		userWriter.append("</listPerson>");
		userWriter.close();
	}

}
