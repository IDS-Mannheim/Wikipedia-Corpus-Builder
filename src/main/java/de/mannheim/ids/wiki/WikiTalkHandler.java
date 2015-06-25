package de.mannheim.ids.wiki;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import de.mannheim.ids.parser.Sweble2Parser;
import de.mannheim.ids.parser.TagSoupParser;
import de.mannheim.ids.util.WikiErrorWriter;
import de.mannheim.ids.util.WikiStatistics;

/**
 * This class implements methods for handling a talk page content, including
 * posting segmentation, parsing and posting generation.
 * 
 * @author margaretha
 * 
 */

public class WikiTalkHandler {

	private Pattern levelPattern = Pattern.compile("^(:+)");
	private Pattern headingPattern = Pattern.compile("^\'*(=+[^=]+=+)");
	private Pattern headingPattern2 = Pattern
			.compile("^\'*(&lt;h[0-9]&gt;.*&lt;/h[0-9]&gt;)");
	private Pattern timePattern = Pattern
			.compile("\\s*([0-9]{2}:[^\\)]*\\))(.*)");
	private Pattern unsignedPattern = Pattern
			.compile("(.*)\\{\\{unsigned\\|([^\\|\\}]+)\\|?(.*)\\}\\}");
	private Pattern signaturePattern, specialContribution;

	private TagSoupParser tagSoupParser;

	private WikiStatistics wikiStatistics;
	private WikiErrorWriter errorWriter;
	private WikiPage wikiPage;

	public WikiTalkUser postUser;
	public WikiTalkTime postTime;

	private String userLabel, contributionLabel;
	private String posting = "", language;

	private boolean sigFlag, baselineMode = false;

	public WikiTalkHandler(Configuration config, WikiPage wikipage,
			WikiStatistics wikiStatistics, WikiErrorWriter errorWriter,
			WikiTalkUser postUser, WikiTalkTime postTime,
			TagSoupParser tagSoupParser) throws IOException {

		if (config == null) {
			throw new IllegalArgumentException("Configuration cannot be null.");
		}
		if (wikiStatistics == null) {
			throw new IllegalArgumentException("WikiStatistics cannot be null.");
		}

		this.language = config.getLanguageCode();
		this.userLabel = config.getUserPage();
		this.contributionLabel = config.getUserContribution();
		this.tagSoupParser = tagSoupParser;

		this.postUser = postUser;
		this.postTime = postTime;
		this.wikiStatistics = wikiStatistics;
		this.wikiPage = wikipage;
		this.errorWriter = errorWriter;

		signaturePattern = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?" + userLabel
				+ ":([^\\|]+)\\|([^\\]]+)\\]\\](.*)");
		specialContribution = Pattern.compile("(.*)\\[\\[" + contributionLabel
				+ "/([^\\|]+)\\|[^\\]]+\\]\\](.*)");
	}

	public void handlePosts() throws IOException {

		for (String text : wikiPage.textSegments) {
			segmentPosting(text);
		}

		if (!posting.trim().isEmpty()) {
			writePosting("unknown", "", "", posting.trim(), "");
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
			writePosting("unknown", "", "", posting.trim(), "");
			posting = "";
		}

		// User signature
		if (trimmedText.contains(this.userLabel)) {
			if (handleSignature(trimmedText))
				return;
		}

		if (!baselineMode) {

			// Special contribution and help signature
			if (trimmedText.contains(this.contributionLabel)) {
				if (handleHelp(trimmedText))
					return;
			}

			// Unsigned
			if (trimmedText.contains("unsigned")) {
				if (handleUnsigned(trimmedText))
					return;
			}

			// Level Marker
			if (trimmedText.startsWith(":")) {
				writePosting("unknown", "", "", trimmedText, "");
				return;
			}

			// Line Marker
			if (trimmedText.startsWith("---")) {
				if (!posting.trim().isEmpty()) {
					writePosting("unknown", "", "", posting.trim(), "");
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

		// else posting+=trimmedText+"\n";
		posting += text + "\n";

	}

	private boolean handleSignature(String trimmedText) throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = signaturePattern.matcher(trimmedText);
		if (matcher.find()) {
			String rest = "", timestamp = "";
			Matcher matcher2 = timePattern.matcher(matcher.group(4));
			if (matcher2.find()) {
				timestamp = matcher2.group(1);
				rest = matcher2.group(2);
			}
			sigFlag = true;
			posting += matcher.group(1) + "\n";

			writePosting(matcher.group(3), matcher.group(2), timestamp,
					posting.trim(), rest.trim());

			matcher.reset();
			posting = "";
			return true;
		}
		return false;
	}

	private boolean handleHelp(String trimmedText) throws IOException {
		if (trimmedText == null) {
			throw new IllegalArgumentException("Text cannot be null.");
		}

		Matcher matcher = specialContribution.matcher(trimmedText);
		if (matcher.find()) {
			String timestamp = "";
			Matcher matcher2 = timePattern.matcher(matcher.group(3));
			if (matcher2.find()) {
				timestamp = matcher2.group(1);
			}

			String temp = matcher.group(1);
			temp = temp
					.replace(
							"&lt;small&gt;(''nicht [[Hilfe:Signatur|signierter]] Beitrag von''",
							"");
			posting += temp + "\n";
			writePosting(matcher.group(2), "", timestamp, posting.trim(), "");

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
			String timestamp = "";
			if (matcher.group(3) != null) {
				Matcher matcher2 = timePattern.matcher(matcher.group(3));
				if (matcher2.find()) {
					timestamp = matcher2.group(1);
				}
			}
			posting += matcher.group(1) + "\n";
			writePosting(matcher.group(2), "", timestamp, posting.trim(), "");

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
				writePosting("unknown", "", "", posting.trim(), "");
				posting = "";
			}

			String text = WikiPageHandler.cleanTextStart(matcher.group(1));
			wikiPage.wikitext += parseToXML(text.trim()) + "\n";
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
					wikiPage.getPageTitle(), language, wikiStatistics,
					errorWriter);
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

	private void writePosting(String speaker, String speakerLabel,
			String timestamp, String posting, String postscript)
			throws IOException {

		if (posting == null) {
			throw new IllegalArgumentException("Posting cannot be null.");
		}
		if (speaker == null) {
			throw new IllegalArgumentException("Speaker cannot be null.");
		}
		if (timestamp == null) {
			throw new IllegalArgumentException("Timestamp cannot be null.");
		}
		if (postscript == null) {
			throw new IllegalArgumentException("Postscript cannot be null.");
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

		if (!speaker.isEmpty()) {
			sb.append(" who=\""
					+ postUser.getTalkUser(speaker, speakerLabel, sigFlag)
					+ "\"");
			if (!speaker.equals("unknown"))
				posting += "<autoSignature/>";
		}

		if (!timestamp.isEmpty()) {
			sb.append(" synch=\"" + postTime.createTimestamp(timestamp)
					+ "\"");
			posting += " <timestamp>" + timestamp + "</timestamp>";
			// System.out.println(posting+"\n");
		}
		sb.append(">\n");

		sb.append(parseToXML(posting) + "\n");

		if (postscript.toLowerCase().startsWith("ps")
				|| postscript.toLowerCase().startsWith("p.s")) {
			sb.append("<seg type=\"postscript\">");
			sb.append(postscript);
			sb.append("</seg>\n");
		}
		else {
			sb.append(postscript);
		}

		sb.append("        </posting>\n");
		wikiPage.wikitext += sb.toString();
	}

}
