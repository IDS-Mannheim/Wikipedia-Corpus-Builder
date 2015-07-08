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
		SIGNED, UNSIGNED, USER_CONTRIBUTION, HEURISTIC;
		public String toString() {
			return name().toLowerCase();
		}
	}

	// private static final Pattern textPattern = Pattern
	// .compile("<text.*\">(.*)");
	private static final Pattern levelPattern = Pattern.compile("^(:+)");

	private static final Pattern headingPattern = Pattern
			.compile("^\'*(=+[^=]+=+)");
	private static final Pattern headingPattern2 = Pattern
			.compile("^\'*(&lt;h[0-9]&gt;.*&lt;/h[0-9]&gt;)");

	private static final Pattern timePattern = Pattern
			.compile("([^0-9]*)([0-9]{1,2}:.+[0-9]{4})(.*)");
	private static final Pattern timeZone = Pattern
			.compile("(.*\\(\\w+\\b\\))(.*)");

	private static final Pattern unsignedPattern = Pattern
			.compile("(.*)\\{\\{unsigned\\|([^\\|\\}]+)\\|?(.*)\\}\\}(.*)");

	private static Pattern signaturePattern, userContribution;

	public WikiPostUser postUser;
	public WikiPostTime postTime;

	private boolean baselineMode = false;
	private StringBuilder postingBuilder;

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

		postingBuilder = new StringBuilder();

		signaturePattern = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?("
				+ config.getUserPage() + ":[^\\]]+)\\]\\](.*)");

		userContribution = Pattern.compile("(.*)\\[\\[("
				+ config.getUserContribution()
				+ "/[^\\|]+)\\|([^\\]]+)\\]\\](.*)");
	}

	@Override
	public void run() {
		try {
			if (config.isWikitextToGenerate()) {
				writeWikitext();
			}

			for (String text : wikiPage.textSegments) {
				segmentPosting(text);
			}

			// last posting
			if (!postingBuilder.toString().trim().isEmpty()) {
				addSignature(SignatureType.HEURISTIC, null);
				writePosting(null, null, null, null);
			}

			writeWikiXML();
		}
		catch (Exception e) {
			wikiStatistics.addUnknownErrors();
			errorWriter.logErrorPage("HANDLER", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e, "");
		}
	}

	private void addSignature(SignatureType sigType, String timestamp) {
		postingBuilder.append("<autoSignature @type=");
		postingBuilder.append(sigType.toString());
		postingBuilder.append(">");
		if (timestamp != null && !timestamp.isEmpty()) {
			postingBuilder.append(" <timestamp>");
			postingBuilder.append(timestamp);
			postingBuilder.append("</timestamp>");
		}
		postingBuilder.append("</autoSignature>");
	}

	private void segmentPosting(String text) throws Exception {

		if (text == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		String trimmedText = text.trim();

		// Posting before a level marker
		if (!baselineMode && trimmedText.startsWith(":")
				&& !postingBuilder.toString().trim().isEmpty()) {
			addSignature(SignatureType.HEURISTIC, null);
			writePosting(null, null, null, null);
		}

		// User signature
		if (trimmedText.contains(config.getUserPage())) {
			if (handleSignature(trimmedText))
				return;
		}

		if (!baselineMode) {

			// Special contribution
			if (trimmedText.contains(config.getUserContribution())) {
				if (handleUserContribution(trimmedText))
					return;
			}

			// Unsigned
			if (trimmedText.contains("unsigned")) {
				if (handleUnsigned(trimmedText))
					return;
			}

			// Timestamp only
			if (trimmedText.endsWith(")")) {
				if (handleTimestampOnly(trimmedText))
					return;
			}

			// Level Marker
			if (trimmedText.startsWith(":")) {
				postingBuilder.append(trimmedText);
				addSignature(SignatureType.HEURISTIC, null);
				writePosting(null, null, null, null);
				return;
			}

			// Line Marker
			if (trimmedText.startsWith("---")) {
				if (!postingBuilder.toString().trim().isEmpty()) {
					addSignature(SignatureType.HEURISTIC, null);
					writePosting(null, null, null, null);
				}
				return;
			}

			// Heading
			if (trimmedText.contains("==")) {
				Matcher matcher = headingPattern.matcher(trimmedText);
				if (headerHandler(matcher))
					return;
			}

			if (trimmedText.contains("&lt;h")) {
				Matcher matcher = headingPattern2.matcher(trimmedText);
				if (headerHandler(matcher))
					return;
			}
		}

		postingBuilder.append(trimmedText);
		postingBuilder.append("\n");
	}

	private boolean handleTimestampOnly(String trimmedText) throws IOException {

		String[] a = matchTimeStamp(trimmedText);
		if (a[0] != null) {
			postingBuilder.append(a[0]);
		}
		String timestamp = a[1];
		String rest = a[2];

		addSignature(SignatureType.HEURISTIC, timestamp);
		writePosting("", null, timestamp, rest);
		if (timestamp != null) {
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
			postingBuilder.append(matcher.group(1));

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

			String[] a = matchTimeStamp(matcher.group(3));
			String timestamp = a[1];
			String rest = a[2];

			addSignature(chooseSignatureType(SignatureType.SIGNED, rest),
					timestamp);
			writePosting(userLinkText, userLink, timestamp, rest.trim());

			matcher.reset();
			return true;
		}
		return false;
	}

	private String[] matchTimeStamp(String text) {
		String[] strArr = new String[3];
		Matcher matcher2 = timePattern.matcher(text);
		if (matcher2.find()) {
			strArr[0] = matcher2.group(1); // pretext
			strArr[1] = matcher2.group(2); // timestamp
			Matcher matcher3 = timeZone.matcher(matcher2.group(3));
			if (matcher3.find()) {
				strArr[1] += matcher3.group(1); // timezone
				strArr[2] = matcher3.group(2); // rest
			}
			else {
				strArr[2] = "";
			}
		}
		else {
			strArr[2] = text;
		}
		return strArr;
	}

	private SignatureType chooseSignatureType(SignatureType type, String rest) {
		if (baselineMode) {
			return SignatureType.SIGNED;
		}
		else if (postingBuilder.toString().contains(config.getSignature())) {
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
			String[] a = matchTimeStamp(matcher.group(3));
			String timestamp = a[1];
			String rest = a[2];

			postingBuilder.append(matcher.group(1));
			addSignature(
					chooseSignatureType(SignatureType.USER_CONTRIBUTION, rest),
					timestamp);
			writePosting(matcher.group(3), matcher.group(2), timestamp, rest);

			matcher.reset();
			return true;
		}
		return false;
	}

	private boolean handleUnsigned(String trimmedText) throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		if (trimmedText.contains("{{unsigned}}")) {
			String rest = "";
			String[] a = trimmedText.split("\\{\\{unsigned\\}\\}");
			if (a.length > 0) {
				postingBuilder.append(a[0]);
				if (a.length > 1)
					rest = a[1];
			}

			addSignature(SignatureType.UNSIGNED, "");
			writePosting("", null, null, rest);
			return true;
		}
		else {
			Matcher matcher = unsignedPattern.matcher(trimmedText);
			if (matcher.find()) {
				String timestamp = "", rest = "";
				if (matcher.group(3) != null) {
					String[] a = matchTimeStamp(matcher.group(3));
					timestamp = a[1];
					rest = a[2];
				}
				rest += matcher.group(4);
				postingBuilder.append(matcher.group(1));
				addSignature(SignatureType.UNSIGNED, timestamp);
				writePosting(matcher.group(2), null, timestamp, rest);

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
			if (!postingBuilder.toString().trim().isEmpty()) {
				addSignature(SignatureType.HEURISTIC, "");
				writePosting(null, null, null, null);
			}

			String text = WikiPageReader.cleanTextStart(matcher.group(1));
			wikiPage.appendWikiXML(parseToXML(wikiPage.getPageId(),
					wikiPage.getPageTitle(), text.trim()));
			wikiPage.appendWikiXML("\n");
			matcher.reset();
			return true;
		}
		return false;
	}

	private int identifyLevel(String posting) {

		if (posting == null) {
			throw new IllegalArgumentException("Posting cannot be null.");
		}

		Matcher matcher = levelPattern.matcher(posting);
		if (matcher.find()) {
			return matcher.group(1).length();
		}
		return 0;
	}

	private void writePosting(String username, String userLink,
			String timestamp, String postscript) throws IOException {

		String posting = postingBuilder.toString().trim();
		postingBuilder = new StringBuilder(); // reset postingBuilder

		String wikiXML = parseToXML(wikiPage.getPageId(),
				wikiPage.getPageTitle(), posting);

		if (posting.isEmpty() || wikiXML.isEmpty()) {
			return;
		}
		else {
			wikiStatistics.addTotalPostings();
		}

		int level = identifyLevel(posting);

		wikiPage.appendWikiXML(createPostingElement(level, username, userLink,
				timestamp, wikiXML, postscript));
	}

	private String createPostingElement(int level, String username,
			String userLink, String timestamp, String wikiXML, String postscript)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("        <posting indentLevel=\"" + level + "\"");
		if (username != null && !username.isEmpty()) {
			postUser.createPostUser(username, userLink);
			sb.append(" who=\"" + postUser.getUserId(username) + "\"");
		}
		if (timestamp != null && !timestamp.isEmpty()) {
			sb.append(" synch=\"" + postTime.createTimestamp(timestamp) + "\"");

		}
		sb.append(">\n");

		sb.append(wikiXML);
		sb.append("\n");

		if (postscript != null && !postscript.isEmpty()) {
			if (postscript.toLowerCase().startsWith("ps")
					|| postscript.toLowerCase().startsWith("p.s")) {
				sb.append("<seg type=\"postscript\">");
				sb.append(parseToXML(wikiPage.getPageId(),
						wikiPage.getPageTitle(), postscript));
				sb.append("</seg>\n");
			}
			else {
				sb.append(parseToXML(wikiPage.getPageId(),
						wikiPage.getPageTitle(), postscript));
				sb.append("\n");
			}
		}
		sb.append("        </posting>\n");
		return sb.toString();
	}
}
