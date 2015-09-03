package de.mannheim.ids.wiki.page;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;

/**
 * This class implements methods for handling a talk page content, including
 * posting segmentation, parsing and posting generation.
 * 
 * @author margaretha
 * 
 */

public class WikiPostHandler extends WikiPageHandler {

	public enum SignatureType {
		SIGNED, UNSIGNED, USER_CONTRIBUTION;
		public String toString() {
			return name().toLowerCase();
		}
	}

	private static final Pattern levelPattern = Pattern.compile("^(:+)");

	private static final Pattern headingPattern = Pattern
			.compile("^\'*(=+[^=]+=+)");
	private static final Pattern headingPattern2 = Pattern
			.compile("^\'*(&lt;h[0-9]&gt;.*&lt;/h[0-9]&gt;)");

	private static final Pattern unsignedPattern = Pattern
			.compile("(.*)\\{\\{unsigned\\|([^\\|\\}]+)\\|?(.*)\\}\\}(.*)");

	private static Pattern signaturePattern, userContribution,
			unsignedPattern2;

	public WikiPostUser postUser;
	public WikiPostTime postTime;

	private boolean baselineMode = false;
	private String post;
	private StringBuilder wikiXMLBuilder;

	public WikiPostHandler(Configuration config, WikiPage wikipage,
			WikiStatistics wikiStatistics, WikiErrorWriter errorWriter,
			WikiPostUser postUser, WikiPostTime postTime) throws IOException {

		super(config, wikipage, wikiStatistics, errorWriter);

		if (postUser == null) {
			throw new IllegalArgumentException("Post user cannot be null.");
		}
		if (postTime == null) {
			throw new IllegalArgumentException("Post time cannot be null.");
		}

		this.postUser = postUser;
		this.postTime = postTime;

		post = "";

		signaturePattern = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?("
				+ config.getUserPage() + ":[^\\]]+)\\]\\](.*)");

		userContribution = Pattern.compile("(.*)\\[\\[("
				+ config.getUserContribution()
				+ "/[^\\|]+)\\|([^\\]]+)\\]\\](.*)");

		unsignedPattern2 = Pattern.compile("(.*)\\{\\{" + config.getUnsigned()
				+ "\\|([^\\|\\}]+)\\|?(.*)\\}\\}(.*)");
	}

	@Override
	public void run() {
		try {
			if (config.isWikitextToGenerate()) {
				writeWikitext();
			}

			wikiXMLBuilder = new StringBuilder();
			for (String text : wikiPage.textSegments) {
				segmentPosting(text);
			}

			// last posting
			if (!post.trim().isEmpty()) {
				writePost(null, null, null, null);
			}
			wikiPage.setWikiXML(wikiXMLBuilder.toString());
			writeWikiXML();
			wikiPage = null;
			post = null;
		}
		catch (Exception e) {
			wikiStatistics.addUnknownErrors();
			e.printStackTrace();

			errorWriter.logErrorPage("HANDLER", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e, "");
		}
	}

	private void addSignature(SignatureType sigType, String timestamp) {
		post += "<autoSignature @type=";
		post += sigType.toString();
		post += ">";
		if (timestamp != null && !timestamp.isEmpty()) {
			post += " <timestamp>";
			post += timestamp;
			post += "</timestamp>";
		}
		post += "</autoSignature>";
	}

	private void segmentPosting(String text) throws Exception {

		if (text == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		String trimmedText = text.trim();

		// Posting before a level marker
		if (!baselineMode && trimmedText.startsWith(":")
				&& !post.trim().isEmpty()) {
			writePost(null, null, null, null);
		}

		// User signature
		if (trimmedText.contains(config.getUserPage() + ":")) {
			if (handleSignature(trimmedText)) return;
		}

		if (!baselineMode) {

			// Special contribution
			if (trimmedText.contains(config.getUserContribution())) {
				if (handleUserContribution(trimmedText)) return;
			}

			// Unsigned
			if (trimmedText.contains("unsigned")) {
				if (handleUnsigned(trimmedText, "unsigned")) return;
			}
			if (trimmedText.contains(config.getUnsigned())) {
				if (handleUnsigned(trimmedText, config.getUnsigned())) return;
			}

			// Timestamp only
			if (trimmedText.endsWith(")")) {
				if (handleTimestampOnly(trimmedText)) return;
			}

			// Level Marker
			if (trimmedText.startsWith(":")) {
				post += trimmedText;
				writePost(null, null, null, null);
				return;
			}

			// Line Marker
			if (trimmedText.startsWith("---")) {
				if (!post.trim().isEmpty()) {
					writePost(null, null, null, null);
				}
				return;
			}

			// Heading
			if (trimmedText.contains("==")) {
				Matcher matcher = headingPattern.matcher(trimmedText);
				if (headerHandler(matcher)) return;
			}

			if (trimmedText.contains("&lt;h")) {
				Matcher matcher = headingPattern2.matcher(trimmedText);
				if (headerHandler(matcher)) return;
			}
		}

		post += trimmedText + "\n";
	}

	private boolean handleTimestampOnly(String trimmedText) throws IOException {

		WikiTimestamp t = new WikiTimestamp(trimmedText);
		if (t.getPretext() != null) {
			post += t.getPretext();
		}
		String timestamp = t.getTimestamp();
		String rest = t.getPostscript();

		if (timestamp != null) {
			addSignature(SignatureType.SIGNED, timestamp);
			writePost("", null, timestamp, rest);
			return true;
		}
		return false;
	}

	private boolean handleSignature(String trimmedText) throws Exception {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = signaturePattern.matcher(trimmedText);

		if (matcher.find()) {
			post += matcher.group(1);

			String userLink, userLinkText;
			String mg = matcher.group(2);
			if (mg.contains("|")) {
				String[] s = mg.split("\\|");
				if (s.length < 2) {
					return false;
				}
				userLink = s[0];
				userLinkText = s[1];
			}
			else {
				userLink = mg;
				userLinkText = userLink;
			}

			WikiTimestamp t = new WikiTimestamp(matcher.group(3));
			String timestamp = t.getTimestamp();
			String rest = t.getPostscript();

			addSignature(chooseSignatureType(SignatureType.SIGNED, rest),
					timestamp);
			writePost(userLinkText, userLink, timestamp, rest);

			matcher.reset();
			return true;
		}
		return false;
	}

	private SignatureType chooseSignatureType(SignatureType type, String rest) {
		if (baselineMode) {
			return SignatureType.SIGNED;
		}
		else if (post.contains(config.getSignature())) {
			return SignatureType.UNSIGNED;
		}
		else if (rest != null && !rest.isEmpty()
				&& rest.contains(config.getSignature())) {
			return SignatureType.UNSIGNED;
		}
		return type;
	}

	private boolean handleUserContribution(String trimmedText)
			throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = userContribution.matcher(trimmedText);
		if (matcher.find()) {
			WikiTimestamp t = new WikiTimestamp(matcher.group(4));
			post += matcher.group(1);
			addSignature(
					chooseSignatureType(SignatureType.USER_CONTRIBUTION,
							t.getPostscript()),
					t.getTimestamp());
			writePost(matcher.group(3), matcher.group(2), t.getTimestamp(),
					t.getPostscript());
			matcher.reset();
			return true;
		}
		return false;
	}

	private boolean handleUnsigned(String trimmedText, String unsigned)
			throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		if (trimmedText.contains("{{" + unsigned + "}}")) {
			String rest = "";
			String[] a = trimmedText.split("\\{\\{" + unsigned + "\\}\\}");
			if (a.length > 0) {
				post += a[0];
				if (a.length > 1) rest = a[1];
			}

			addSignature(SignatureType.UNSIGNED, "");
			writePost("", null, null, rest);
			return true;
		}
		else {
			Matcher matcher;
			if (unsigned.equals("unsigned")) {
				matcher = unsignedPattern.matcher(trimmedText);
			}
			else {
				matcher = unsignedPattern2.matcher(trimmedText);
			}

			if (matcher.find()) {
				String timestamp = "", rest = "";
				if (matcher.group(3) != null) {
					WikiTimestamp t = new WikiTimestamp(matcher.group(3));
					timestamp = t.getTimestamp();
					rest = t.getPostscript();
				}
				rest += matcher.group(4);
				post += matcher.group(1);
				addSignature(SignatureType.UNSIGNED, timestamp);
				writePost(matcher.group(2), null, timestamp, rest);

				matcher.reset();
				return true;
			}
		}

		return false;
	}

	private boolean headerHandler(Matcher matcher) throws IOException {

		if (matcher == null) {
			throw new IllegalArgumentException("Matcher cannot be null.");
		}

		if (matcher.find()) {
			if (!post.trim().isEmpty()) {
				writePost(null, null, null, null);
			}

			String text = WikiPageReader.cleanTextStart(matcher.group(1));
			String wikiXML = parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), text.trim());
			// if (!Thread.interrupted()) {
				wikiXMLBuilder.append(wikiXML);
				wikiXMLBuilder.append("\n");
			// }
			matcher.reset();
			return true;
		}
		return false;
	}

	private int identifyLevel(String post) {

		if (post == null) {
			throw new IllegalArgumentException("Post cannot be null.");
		}

		Matcher matcher = levelPattern.matcher(post);
		if (matcher.find()) {
			return matcher.group(1).length();
		}
		return 0;
	}

	private void writePost(String username, String userLink, String timestamp,
			String postscript) throws IOException {

		String post = this.post.trim();
		this.post = ""; // reset post

		if (post.isEmpty()) return;
		int level = identifyLevel(post);

		if (level > 0) {
			post = new String(post.substring(level, post.length()).trim());

		}

		String wikiXML = parseToXML(wikiPage.getPageId(),
				wikiPage.getPageTitle(), post);
		if (wikiXML.isEmpty()) return;

		wikiStatistics.addTotalPostings();

		String postingElement = createPostingElement(level, username, userLink,
				timestamp, wikiXML, postscript);
		wikiXMLBuilder.append(postingElement);
	}

	private String createPostingElement(int level, String username,
			String userLink, String timestamp, String wikiXML, String postscript)
			throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append("        <posting indentLevel=\"");
		sb.append(level);
		sb.append("\"");
		if (username != null && !username.isEmpty()) {
			postUser.createPostUser(username, userLink);
			sb.append(" who=\"");
			sb.append(postUser.getUserId(username));
			sb.append("\"");
		}
		if (timestamp != null && !timestamp.isEmpty()) {
			sb.append(" synch=\"");
			sb.append(postTime.createTimestamp(timestamp));
			sb.append("\"");

		}
		sb.append(">\n");

		sb.append(wikiXML);
		sb.append("\n");

		if (postscript != null && !postscript.isEmpty()) {
			String ps = parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), postscript);

			// if (!Thread.interrupted()) {
				if (postscript.toLowerCase().startsWith("ps")
						|| postscript.toLowerCase().startsWith("p.s")) {
					sb.append("<seg type=\"postscript\">");
					sb.append(ps);
					sb.append("</seg>\n");
				} else {
					sb.append(ps);
					sb.append("\n");
				}
			// }
		}
		sb.append("        </posting>\n");

		return sb.toString();		
	}
}
