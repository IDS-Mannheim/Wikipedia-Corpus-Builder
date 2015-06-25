package de.mannheim.ids.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import de.mannheim.ids.util.Utilities;

/**
 * Class implementation for handling posting authors. <br/>
 * Generates a list of post authors / wikipedia users in XML.
 * 
 * @author margaretha
 * 
 */

public class WikiPostUser {

	private Map<String, String> userMap; // username, userid
	private OutputStreamWriter userWriter;
	private int counter;
	private String userUri;

	public WikiPostUser(String prefixFileName, String userUri)
			throws IOException {

		if (prefixFileName == null || prefixFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"prefixFileName cannot be null or empty.");
		}
		if (userUri == null || userUri.isEmpty()) {
			throw new IllegalArgumentException(
					"UserUri cannot be null or empty.");
		}

		userMap = new HashMap<String, String>();
		userWriter = Utilities.createWriter("post", prefixFileName
				+ "-post-user.xml", "utf-8");
		userWriter.append("<listPerson>\n");
		counter = 0;
		this.userUri = userUri;
		getTalkUser("unknown", "", false);
	}

	public String getTalkUser(String username, String speaker, boolean sigFlag)
			throws IOException {
		if (username == null) {
			throw new IllegalArgumentException("Username cannot be null.");
		}

		String user = null;
		synchronized (userMap) {
			if (!userMap.containsKey(username)) {
				String userId = generateUserId();
				userMap.put(username, userId);
				createPerson(username, userId, speaker, sigFlag);
			}
			user = userMap.get(username);
		}

		return user;
	}

	private String generateUserId() {
		String userId = "WU" + String.format("%08d", counter);
		counter++;
		return userId;
	}

	private void createPerson(String username, String userId, String speaker,
			boolean sigFlag) throws IOException {

		if (speaker == null) {
			throw new IllegalArgumentException("Speaker cannot be null.");
		}
		synchronized (userWriter) {
			userWriter.append("   <person xml:id=\"" + userId + "\">\n");
			userWriter.append("      <persName>" + username + "</persName>\n");

			if (sigFlag) {
				userWriter.append("      <signatureContent>\n");
				userWriter.append("         <ref target=\"" + userUri);
				userWriter.append(speaker.replaceAll("\\s", "_") + "\">");
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
