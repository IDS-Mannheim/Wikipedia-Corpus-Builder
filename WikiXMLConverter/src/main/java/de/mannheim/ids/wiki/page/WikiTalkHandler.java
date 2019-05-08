package de.mannheim.ids.wiki.page;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mannheim.ids.wiki.Configuration;
import de.mannheim.ids.writer.WikiErrorWriter;
import de.mannheim.ids.writer.WikiPostTime;
import de.mannheim.ids.writer.WikiPostUser;

/**
 * This class implements methods for handling talk page content, including
 * segmenting wikitext into postings, parsing the wikitext and generating
 * posting structures in XML.
 * 
 * @author margaretha
 * 
 */

public class WikiTalkHandler extends WikiPageHandler {

	public enum SignatureType {
		SIGNED, UNSIGNED, SPECIAL_CONTRIBUTION;
		public String toString() {
			return name().toLowerCase();
		}
	}

	public static final String USER_PAGE_EN = "User|user";
	public static final String USER_TALK_EN = "User talk|User_talk|User "
			+ "Talk|User_Talk|user talk |user_talk";
	public static final String SPECIAL_CONTRIBUTIONS_EN = "Special:"
			+ "Contributions|special:contributions";
	public static final String SIGNATURE_EN = "wikipedia:signatures";

	public static final Pattern levelPattern = Pattern.compile("^(:+)");

	public static final Pattern headingPattern = Pattern
			.compile("^\'*(=+[^=]+=+)");
	public static final Pattern headingPattern2 = Pattern
			.compile("^\'*(&lt;h[0-9]&gt;.*&lt;/h[0-9]&gt;)");

	public static final Pattern unsignedPattern = Pattern
			.compile(
					"(.*)\\{\\{[uU]nsigned\\|([^\\|\\}]+)\\|?(.*)\\}\\}(.*)");

	public static final Pattern spanPattern = Pattern
			.compile("(.*)(&lt;span style.*&gt;)(-{0,2})");

	public static final Pattern inTemplatePattern = Pattern
			.compile("([^}]*)}}(.*)");

	private Pattern signaturePattern, signaturePattern2, userTalkPattern,
			specialContribution, unsignedPattern2;

	public WikiPostUser postUser;
	public WikiPostTime postTime;

	private boolean baselineMode = false;
	private String post;
	private StringBuilder wikiXMLBuilder;
	private String unsignedSentenceCase;

	private String userTalkUnderscore;

	/**
	 * Constructs a WikiPostHandler and compiles some regex patterns used in
	 * posting segmentation.
	 * 
	 * @param config
	 *            the conversion configuration
	 * @param wikipage
	 *            a wikipage to be processed
	 * @param wikiStatistics
	 *            the wikistatistics counter
	 * @param errorWriter
	 *            the writer for logging errors
	 * @param postUser
	 *            a WikiPostUser listing the post users
	 * @param postTime
	 *            a WikiPostTime listing the post time
	 * @throws IOException
	 *             an IOException
	 */
	public WikiTalkHandler(Configuration config, WikiPage wikipage,
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

		createSignaturePattern();
		createUserTalkPattern();
		createSpecialContribution();

		String keyword = config.getUnsigned();
		unsignedSentenceCase = keyword.substring(0, 1).toUpperCase()
				+ keyword.substring(1);
		keyword = "[" + keyword.substring(0, 1).toUpperCase() + "|"
				+ keyword.substring(0, 1).toLowerCase() + "]"
				+ keyword.substring(1);

		unsignedPattern2 = Pattern.compile("(.*)\\{\\{" + keyword
				+ "\\|([^\\|\\}]+)\\|?(.*)\\}\\}(.*)");
	}

	private void createSignaturePattern() {
		String userPage = config.getUserPage();
		if (!config.getLanguageCode().equals("en")) {
			StringBuilder sb = new StringBuilder(userPage);
			sb.append("|");
			sb.append(userPage.toLowerCase());
			sb.append("|");
			sb.append(USER_PAGE_EN);
			userPage = sb.toString();
		}
		else {
			userPage = USER_PAGE_EN;
		}
		signaturePattern = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?(("
				+ userPage + "):[^\\]]+)\\]\\](.*)");
		signaturePattern2 = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?(("
				+ userPage + "):[^/]+\\|[^\\]]+)\\]\\](.*)");
	}

	private void createUserTalkPattern() {
		String userTalk = config.getUserTalk();
		userTalkUnderscore = userTalk.replace(" ", "_");

		if (!config.getLanguageCode().equals("en")) {
			StringBuilder sb = new StringBuilder(userTalk);
			sb.append("|");
			sb.append(userTalk.toLowerCase());
			sb.append("|");
			sb.append(userTalkUnderscore);
			sb.append("|");
			sb.append(userTalkUnderscore.toLowerCase());
			sb.append("|");
			sb.append(USER_TALK_EN);
			userTalk = sb.toString();
		}
		else {
			userTalk = USER_TALK_EN;
		}
		userTalkPattern = Pattern.compile("(.*)\\[\\[((" + userTalk
				+ "):[^\\]]+)\\]\\](.*)");
	}

	private void createSpecialContribution() {
		String contributions = config.getSpecialContribution();

		if (!config.getLanguageCode().equals("en")) {
			StringBuilder sb = new StringBuilder(contributions);
			sb.append("|");
			sb.append(contributions.toLowerCase());
			sb.append("|");
			sb.append(SPECIAL_CONTRIBUTIONS_EN);
			contributions = sb.toString();
		}
		else {
			contributions = SPECIAL_CONTRIBUTIONS_EN;
		}

		specialContribution = Pattern.compile("(.*)\\[\\[((" + contributions
				+ ")/[^\\|]+)\\|([^\\]]+)\\]\\](.*)");
	}

	@Override
	public void run() {
		try {
			if (config.isWikitextToGenerate()) {
				writeWikitext();
			}
			System.out.println(wikiPage.getPageIndex() + "/"
					+ wikiPage.getPageId() + ".xml");

			// creating postings by gradually checking small portions of
			// wikitext (segments of wikitext)
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
			// e.printStackTrace();

			errorWriter.logErrorPage("HANDLER", wikiPage.getPageTitle(),
					wikiPage.getPageId(), e, "");
		}
	}

	/**
	 * Adds a signature element to the current post.
	 * 
	 * @param sigType
	 *            the signature type of the post
	 * @param timestamp
	 *            the post timestamp
	 */
	private void addSignature(SignatureType sigType, String timestamp) {
		post += "<autoSignature @type=";
		post += sigType.toString();
		post += ">";
		if (timestamp != null && !timestamp.isEmpty()) {
			post += "<timestamp>";
			post += timestamp;
			post += "</timestamp>";
		}
		post += "</autoSignature>";
	}

	/**
	 * Heuristically filters out the given wikitext to determine posting
	 * boundaries and eventually creates posting elements.
	 * 
	 * @param text
	 *            wikitext
	 * @throws IOException
	 *             an IOException
	 */
	private void segmentPosting(String text) throws IOException {

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
		if (trimmedText.contains("[[")) {
			if (handleSignature(trimmedText)) {
				return;
			}
		}

		if (!baselineMode) {

			// Special contribution
			// if (trimmedText.contains(config.getUserContribution())) {
			if (trimmedText.contains("[[")) {
				if (handleSpecialContribution(trimmedText))
					return;
			}

			// Unsigned
			if (trimmedText.contains("unsigned")) {
				if (handleUnsigned(trimmedText, "unsigned"))
					return;
			}
			else if (trimmedText.contains("Unsigned")) {
				if (handleUnsigned(trimmedText, "Unsigned"))
					return;
			}
			if (trimmedText.contains(config.getUnsigned())) {
				if (handleUnsigned(trimmedText, config.getUnsigned()))
					return;
			}
			else if (trimmedText.contains(unsignedSentenceCase)) {
				if (handleUnsigned(trimmedText, unsignedSentenceCase))
					return;
			}

			// User talk
			// Timestamp only
			if (trimmedText.endsWith(")")) {
				if (handleUserTalk(trimmedText))
					return;
				if (handleTimestampOnly(trimmedText))
					return;
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
				if (headerHandler(matcher))
					return;
			}

			if (trimmedText.contains("&lt;h")) {
				Matcher matcher = headingPattern2.matcher(trimmedText);
				if (headerHandler(matcher))
					return;
			}
		}

		post += trimmedText + "\n";
	}

	private void addPostscript(String postscript) {
		StringBuilder sb = new StringBuilder();
		String trimmedPostscript = postscript.toLowerCase().trim();
		if (trimmedPostscript.startsWith("ps")
				|| trimmedPostscript.startsWith("p.s")) {
			sb.append("<seg type=\"postscript\">");
			sb.append(postscript);
			sb.append("</seg>");
		}
		else {
			sb.append(postscript);
		}
		post += sb.toString();
	}
	/**
	 * Recognizes timestamp mark-ups in user signature mark-ups that do not
	 * appear together with user links.
	 * 
	 * 
	 * @param trimmedText
	 * @return true if a timestamp is recognized, false otherwise.
	 * @throws IOException
	 *             an IOException
	 */
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

	private boolean handleUserTalk(String trimmedText) throws IOException {
		Matcher matcher = userTalkPattern.matcher(trimmedText);

		if (matcher.find()) {
			if (isSignatureInTemplate(matcher.group(4))) {
				return false;
			}

			String userLink, userLinkText;
			String mg = matcher.group(2);
			if (mg.contains("|")) {
				String[] s = mg.split("\\|");
				if (s.length < 2) {
					return false;
				}
				userLink = s[0].replaceAll(" ", "_");
				userLinkText = s[1];
			}
			else {
				userLink = mg;
				userLinkText = userLink;
			}

			// must have a timestamp
			WikiTimestamp t = new WikiTimestamp(matcher.group(4));
			String timestamp = t.getTimestamp();
			if (timestamp == null) {
				return false;
			}
			String rest = t.getPostscript();

			addSignature(chooseSignatureType(SignatureType.SIGNED, rest),
					timestamp);
			writePost(userLinkText, userLink, timestamp, rest);

			matcher.reset();
			return true;
		}
		return false;
	}

	/**
	 * Identifies signature mark-ups, extracts signature information from them,
	 * and creates an XML signature structure for them.
	 * 
	 * @param trimmedText
	 *            trimmed wikitext
	 * @return true if the given wikitext contains a signature markup, false
	 *         otherwise.
	 * @throws IOException
	 *             an IOException
	 */
	private boolean handleSignature(String trimmedText) throws IOException {
		Matcher matcher;
		if (trimmedText.contains("|")) {
			matcher = signaturePattern2.matcher(trimmedText);
		}
		else {
			matcher = signaturePattern.matcher(trimmedText);
		}

		if (matcher.find()) {
			if (isSignatureInTemplate(matcher.group(4))) {
				return false;
			}

			post += cleanSpanBeforeSignature(matcher.group(1));

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

			WikiTimestamp t = new WikiTimestamp(matcher.group(4));
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

	private boolean isSignatureInTemplate(String signature) {
		Matcher templateMatcher = inTemplatePattern
				.matcher(signature);
		if (templateMatcher.find()) {
			if (!templateMatcher.group(1).contains("{{")) {
				return true;
			}
		}
		return false;
	}

	private String cleanSpanBeforeSignature(String text) {
		Matcher spanMatcher = spanPattern.matcher(text);
		if (spanMatcher.find()) {
			text = spanMatcher.group(1) + spanMatcher.group(3);
		}
		return text;
	}

	/**
	 * Re-checks and determines the signature type.
	 * 
	 * @param type
	 *            the initial possible signature type
	 * @param rest
	 *            tailing text after a signature markup
	 * @return a signature type
	 */
	private SignatureType chooseSignatureType(SignatureType type, String rest) {
		String lowercasePost = post.toLowerCase();
		if (baselineMode) {
			return SignatureType.SIGNED;
		}
		else if (lowercasePost.contains(config.getSignature())
				|| lowercasePost.contains(SIGNATURE_EN)) {
			return SignatureType.UNSIGNED;
		}
		else if (rest != null && !rest.isEmpty()) {
			String lowercaseRest = rest.toLowerCase();
			if (lowercaseRest.contains(config.getSignature())
					|| lowercaseRest.contains(SIGNATURE_EN)) {
				return SignatureType.UNSIGNED;
			}
		}
		return type;
	}

	/**
	 * Identifies user contribution mark-ups in the given trimmed wikitext,
	 * creates a corresponding XML signature structure, and eventually a posting
	 * for the collected post text until now.
	 * 
	 * @param trimmedText
	 *            trimmed wikitext
	 * @return true if the trimmed wikitext contains a user contribution markup,
	 *         false otherwise.
	 * @throws IOException
	 *             an IOException
	 */
	private boolean handleSpecialContribution(String trimmedText)
			throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = specialContribution.matcher(trimmedText);
		if (matcher.find()) {
			WikiTimestamp t = new WikiTimestamp(matcher.group(5));
			post += matcher.group(1);
			addSignature(chooseSignatureType(SignatureType.SPECIAL_CONTRIBUTION,
					t.getPostscript()), t.getTimestamp());
			writePost(matcher.group(3), matcher.group(2), t.getTimestamp(),
					t.getPostscript());
			matcher.reset();
			return true;
		}
		return false;
	}

	/**
	 * Identifies unsigned templates in the given trimmed wikitext by using the
	 * unsigned keywords. The keywords are generally varied for different
	 * languages. Nevertheless, the english keyword are often used in wikipedias
	 * of other languages.
	 * 
	 * @param trimmedText
	 *            trimmed wikitext
	 * @param unsigned
	 *            unsigned template keywords
	 * @return true if a unsigned template is found, false otherwise.
	 * @throws IOException
	 *             an IOException
	 */
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
				if (a.length > 1)
					rest = a[1];
			}

			addSignature(SignatureType.UNSIGNED, "");
			writePost("", null, null, rest);
			return true;
		}
		else {
			Matcher matcher;
			if (unsigned.toLowerCase().equals("unsigned")) {
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
				if (matcher.group(1) != null) {
					post += matcher.group(1);
				}
				addSignature(SignatureType.UNSIGNED, timestamp);
				writePost(matcher.group(2), null, timestamp, rest);

				matcher.reset();
				return true;
			}
		}

		return false;
	}

	/**
	 * Identifies headers as post boundaries.
	 * 
	 * @param matcher
	 *            a Matcher
	 * @return
	 * @throws IOException
	 *             an IOException
	 */
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
			wikiXMLBuilder.append(wikiXML);
			wikiXMLBuilder.append("\n");
			matcher.reset();
			return true;
		}
		return false;
	}

	/**
	 * Identifies level mark-ups in the given post. Levels indicates the depth
	 * of a post in a thread. Posts without any depth have level 0.
	 * 
	 * @param post
	 *            a post text
	 * @return the level depth
	 */
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

	/**
	 * Creates a post by using the given variables.
	 * 
	 * @param username
	 *            the post user name
	 * @param userLink
	 *            the post user link
	 * @param timestamp
	 *            the post timestamp
	 * @param postscript
	 *            the tailing text after a post user signature
	 * @throws IOException
	 *             an IOException
	 */
	private void writePost(String username, String userLink, String timestamp,
			String postscript) throws IOException {

		if (postscript != null && !postscript.isEmpty()) {
			addPostscript(postscript);
		}

		String post = this.post.trim();
		this.post = ""; // reset post

		if (post.isEmpty())
			return;
		int level = identifyLevel(post);

		if (level > 0) {
			post = new String(post.substring(level, post.length()).trim());

		}
		String wikiXML = parseToXML(wikiPage.getPageId(),
				wikiPage.getPageTitle(), post);
		if (wikiXML.isEmpty())
			return;

		wikiStatistics.addTotalPostings();

		String postingElement = createPostingElement(level, username, userLink,
				timestamp, wikiXML);
		wikiXMLBuilder.append(postingElement);
	}

	/**
	 * Creates a posting element based on the given variables.
	 * 
	 * @param level
	 *            the depth of the post in a thread
	 * @param username
	 *            the post user name
	 * @param userLink
	 *            the post user link
	 * @param timestamp
	 *            the post timestamp
	 * @param wikiXML
	 *            the post content
	 * @param postscript
	 *            the tailing text after the signature
	 * @return an XML post element
	 * @throws IOException
	 *             an IOException
	 */
	private String createPostingElement(int level, String username,
			String userLink, String timestamp, String wikiXML)
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
		sb.append("        </posting>\n");

		return sb.toString();
	}
}
