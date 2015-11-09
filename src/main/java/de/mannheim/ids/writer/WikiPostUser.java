package de.mannheim.ids.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

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

	/**
	 * Constructs a WikiPostUser object and the output file in the post folder.
	 * 
	 * @param prefixFileName prefixFileName file identifier (part of the
	 *            wikidump file name)
	 * @param pageType the wikipage type
	 * @throws IOException
	 */
	public WikiPostUser(String prefixFileName, String pageType)
			throws IOException {

		if (prefixFileName == null || prefixFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"prefixFileName cannot be null or empty.");
		}
		if (pageType == null || pageType.isEmpty()) {
			throw new IllegalArgumentException(
					"pageType cannot be null or empty.");
		}

		userMap = new HashMap<String, String>();
		userWriter = Utilities.createWriter("post", prefixFileName + "-post-"
				+ pageType + "-user.xml", "utf-8");
		userWriter.append("<listPerson>\n");
		counter = 1;
	}

	/**
	 * Creates a post user element for the given variables and keep it in the
	 * user map.
	 * 
	 * @param username the post user name
	 * @param userLink the post user link
	 * @throws IOException
	 */
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

	/**
	 * Returns the user id of the given username
	 * 
	 * @param username the post user name
	 * @return
	 */
	public String getUserId(String username) {
		return userMap.get(username);
	}

	/**
	 * Generates an incremental user id.
	 * 
	 * @return
	 */
	private String generateUserId() {
		String userId = "WU" + String.format("%08d", counter);
		counter++;
		return userId;
	}

	/**
	 * Creates a post user element containing information about the user from
	 * the given variables.
	 * 
	 * @param username the post user name
	 * @param userId the post user id
	 * @param userLink the post user link
	 * @throws IOException
	 */
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
