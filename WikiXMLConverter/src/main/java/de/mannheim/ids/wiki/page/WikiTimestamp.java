package de.mannheim.ids.wiki.page;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles timestamp mark up, also for different languages.
 * 
 * @author margaretha
 * 
 */
public class WikiTimestamp {

	// german, italian, croatian, polish, spanish
	public static final Pattern timePattern = Pattern.compile("(.*[^0-9])"
			+ "([0-9]{1,2}:[0-9]{2},? [0-9]{1,2}\\.? [^\\d]{3,10},?\\.? [0-9]{4}\\.?\\s?\\([A-Z]+\\))"
			+ "(.*)");

	// hungarian, norwegian
	public static final Pattern timePattern2 = Pattern.compile("(.*)"
			+ "([0-9]{1,4}\\.? [^\\d]{3,10}\\.? [0-9]{1,4}.{1,5}[0-9]{1,2}:[0-9]{2}\\s?\\([A-Z]+\\))"
			+ "(.*)");

	// french
	public static final Pattern timePattern3 = Pattern.compile("([.*]*)"
			+ "([0-9]{1,2}:[0-9]{2} [^\\d]{3,10}\\.? [0-9]{1,2}, [0-9]{4}\\s?\\([A-Z]+\\))"
			+ "(.*)");

	private String pretext;
	private String timestamp;
	private String postscript;

	public WikiTimestamp(String text) {
		matchTimeStamp(text);
	}

	public void matchTimeStamp(String text) {
		Matcher matcher = timePattern.matcher(text);
		if (matcher.find()) {
			createTimeStamp(matcher);
			return;
		}

		matcher = timePattern2.matcher(text);
		if (matcher.find()) {
			createTimeStamp(matcher);
			return;
		}

		matcher = timePattern3.matcher(text);
		if (matcher.find()) {
			createTimeStamp(matcher);
		}
		else {
			setPostscript(text);
		}
	}

	public void createTimeStamp(Matcher matcher) {
		setPretext(matcher.group(1)); // pretext
		setTimestamp(matcher.group(2)); // timestamp
		setPostscript(matcher.group(3));
		matcher.reset();
	}

	public String getPretext() {
		return pretext;
	}

	public void setPretext(String pretext) {
		this.pretext = pretext;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getPostscript() {
		return postscript;
	}

	public void setPostscript(String postscript) {
		this.postscript = postscript;
	}

}
