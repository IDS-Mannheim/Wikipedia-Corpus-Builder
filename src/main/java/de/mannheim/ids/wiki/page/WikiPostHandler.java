package de.mannheim.ids.wiki.page;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
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

public class WikiPostHandler {

	public enum SignatureType {
		SIGNED, UNSIGNED, USER_CONSTRIBUTION, HEURISTIC;
		public String toString() {
			return name().toLowerCase();
		}
	}

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

	private TagSoupParser tagSoupParser;

	private WikiStatistics wikiStatistics;
	private WikiErrorWriter errorWriter;
	private WikiPage wikiPage;

	public WikiPostUser postUser;
	public WikiPostTime postTime;

	private String posting;

	private final Configuration config;
	private boolean sigFlag, baselineMode = false;

	public WikiPostHandler(Configuration config, WikiPage wikipage,
			WikiStatistics wikiStatistics, WikiErrorWriter errorWriter,
			WikiPostUser postUser, WikiPostTime postTime,
			TagSoupParser tagSoupParser) throws IOException {

		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException("WikiStatistics cannot be null.");
		}

		this.tagSoupParser = tagSoupParser;
		this.config = config;

		this.postUser = postUser;
		this.postTime = postTime;
		this.wikiStatistics = wikiStatistics;
		this.wikiPage = wikipage;
		this.errorWriter = errorWriter;
		this.posting = "";

		signaturePattern = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?"
				+ config.getUserPage() + ":([^\\]]+)\\]\\](.*[0-9]{2}:.*)");

		userContribution = Pattern.compile("(.*)\\[\\["
				+ config.getUserContribution()
				+ "/([^\\|]+)\\|[^\\]]+\\]\\](.*)");
	}

	public void handlePosts() throws IOException {

		for (String text : wikiPage.textSegments) {
			segmentPosting(text);
		}

		if (!posting.trim().isEmpty()) {
			writePosting(SignatureType.HEURISTIC, "unknown", "", "",
					posting.trim(), "");
			posting = "";
		}
	}

	private void segmentPosting(String text) throws IOException {

		if (text == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		String trimmedText = text.trim();
		sigFlag = false;

		// Posting before a level marker
		if (!baselineMode && trimmedText.startsWith(":")
				&& !posting.trim().isEmpty()) {
			writePosting(SignatureType.HEURISTIC, "unknown", "", "",
					posting.trim(), "");
			posting = "";
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
				writePosting(SignatureType.HEURISTIC, "unknown", "", "",
						trimmedText, "");
				return;
			}

			// Line Marker
			if (trimmedText.startsWith("---")) {
				if (!posting.trim().isEmpty()) {
					writePosting(SignatureType.HEURISTIC, "unknown", "", "",
							posting.trim(), "");
					posting = "";
				}
				return;
			}

			// Heading
			if (trimmedText.contains("=")) {
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

		posting += text;

	}

	private boolean handleTimestampOnly(String trimmedText) throws IOException {

		String[] a = matchTimeStamp(trimmedText);
		posting = a[0];
		String timestamp = a[1];
		String rest = a[2];

		writePosting(SignatureType.HEURISTIC, "", "", timestamp,
				posting.trim(), rest);
		if (timestamp != null) {
			posting = "";
			return true;
		}
		return false;
	}

	private boolean handleSignature(String trimmedText) throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = signaturePattern.matcher(trimmedText);

		if (matcher.find()) {
			String[] a = matchTimeStamp(matcher.group(3));
			String timestamp = a[1];
			String rest = a[2];
			sigFlag = true;
			posting += matcher.group(1);

			String userLink, userLinkText;
			String mg = matcher.group(2);
			if (mg.contains("|")) {
				String[] s = mg.split("\\|");
				userLink = s[0];
				userLinkText = s[1];
			}
			else {
				userLink = mg;
				userLinkText = userLink;
			}

			writePosting(chooseSignatureType(SignatureType.SIGNED, rest),
					userLinkText, userLink, timestamp, posting.trim(),
					rest.trim());

			matcher.reset();
			posting = "";
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
				strArr[2] = matcher2.group(2);
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
		else if (posting.contains(config.getSignature())) {
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

			String temp = matcher.group(1);
			posting += temp;
			writePosting(
					chooseSignatureType(SignatureType.USER_CONSTRIBUTION, rest),
					matcher.group(2), "", timestamp, posting.trim(), rest);

			matcher.reset();
			posting = "";
			return true;
		}
		return false;
	}

	private boolean handleUnsigned(String trimmedText) throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = unsignedPattern.matcher(trimmedText);
		if (matcher.find()) {
			String timestamp = "", rest = "";
			if (matcher.group(3) != null) {
				String[] a = matchTimeStamp(matcher.group(3));
				timestamp = a[1];
				rest = a[2];
			}
			rest += matcher.group(4);
			posting += matcher.group(1);
			writePosting(SignatureType.UNSIGNED, matcher.group(2), "",
					timestamp, posting.trim(), rest);

			matcher.reset();
			posting = "";
			return true;
		}
		return false;
	}

	private boolean headerHandler(Matcher matcher) throws IOException {

		if (matcher == null) {
			throw new IllegalArgumentException("Matcher cannot be null.");
		}

		if (matcher.find()) {
			if (!posting.trim().isEmpty()) {
				writePosting(SignatureType.HEURISTIC, "unknown", "", "",
						posting.trim(), "");
				posting = "";
			}

			String text = WikiPageHandler.cleanTextStart(matcher.group(1));
			wikiPage.wikitext += parseToXML(text.trim());
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

	@SuppressWarnings("deprecation")
	private String parseToXML(String posting) {

		if (posting == null) {
			throw new IllegalArgumentException("Posting cannot be null.");
		}

		posting = StringEscapeUtils.unescapeXml(posting); // unescape XML tags
		posting = WikiPageHandler.cleanPattern(posting);

		try {
			posting = tagSoupParser.generate(posting, true);
			Sweble2Parser swebleParser = new Sweble2Parser(posting,
					wikiPage.getPageTitle(), config.getLanguageCode(),
					wikiStatistics, errorWriter);
			Thread swebleThread = new Thread(swebleParser);
			swebleThread.start();
			swebleThread.join(1000 * 60);
			if (swebleThread.isAlive()) {
				swebleThread.stop();
			}
			posting = swebleParser.getWikiXML();
		}
		catch (Exception e) {
			wikiStatistics.addSwebleErrors();
			errorWriter.logErrorPage("SWEBLE", wikiPage.getPageTitle(),
					e.getCause());
			posting = "";
		}
		return posting;
	}

	private void writePosting(SignatureType signatureType, String username,
			String userLink, String timestamp, String posting, String postscript)
			throws IOException {

		if (posting == null) {
			throw new IllegalArgumentException("Posting cannot be null.");
		}
		if (username == null) {
			throw new IllegalArgumentException("Speaker cannot be null.");
		}

		if (posting.isEmpty())
			return;
		else
			wikiStatistics.addTotalPostings();

		int level = identifyLevel(posting.trim());
		if (level > 0) {
			posting = posting.substring(level, posting.length());
		}

		StringBuilder sb = new StringBuilder();
		sb.append("        <posting indentLevel=\"" + level + "\"");

		if (!username.isEmpty()) {
			postUser.createPostUser(username, userLink, sigFlag);
			sb.append(" who=\"" + postUser.getUserId(username) + "\"");
		}

		// if (!username.equals("unknown")) {
		posting += "<autoSignature @type=" + signatureType.toString() + "/>";
		// }

		if (timestamp != null && !timestamp.isEmpty()) {
			sb.append(" synch=\"" + postTime.createTimestamp(timestamp) + "\"");
			posting += " <timestamp>" + timestamp + "</timestamp>";
		}
		sb.append(">");

		sb.append(parseToXML(posting));

		if (postscript != null) {
			if (postscript.toLowerCase().startsWith("ps")
					|| postscript.toLowerCase().startsWith("p.s")) {
				sb.append("<seg type=\"postscript\">");
				sb.append(parseToXML(postscript));
				sb.append("</seg>");
			}
			else {
				sb.append(parseToXML(postscript));
			}
		}

		sb.append("        </posting>");
		wikiPage.wikitext += sb.toString();
	}
}
