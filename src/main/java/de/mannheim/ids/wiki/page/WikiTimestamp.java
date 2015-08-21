package de.mannheim.ids.wiki.page;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiTimestamp {

	private static final Pattern timePattern = Pattern
	// .compile("([^0-9]*)([0-9]{1,2}:.+[0-9]{4})(.*)");
			.compile("([^0-9]*)([0-9]{1,2}:[0-9]{2},.+[0-9]{4})(.*)");
	// .compile("([^0-9]*)([0-9]{1,2}:[0-9]{2},.+[0-9]{4})(.*\\([A-Z]+\\))(.*)");
	private static final Pattern timeZone = Pattern
			.compile("(.*\\([A-Z]+\\))(.*)");
	// .compile("(.*\\(\\w+\\b\\))(.*)");

	private String pretext;
	private String timestamp;
	private String postscript;

	public WikiTimestamp(String text) {
		createTimeStamp(text);
	}

	public void createTimeStamp(String text) {
		Matcher matcher = timePattern.matcher(text);
		if (matcher.find()) {
			setPretext(matcher.group(1)); // pretext
			setTimestamp(matcher.group(2)); // timestamp
			
			Matcher matcher2 = timeZone.matcher(matcher.group(3));
			if (matcher2.find()) {
				timestamp += matcher2.group(1); // timezone
				setPostscript(matcher2.group(2).trim()); // rest
			}
			matcher2.reset();
		}
		else {
			setPostscript(text);
		}
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
