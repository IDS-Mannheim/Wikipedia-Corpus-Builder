package de.mannheim.ids.wiki.page;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;

import de.mannheim.ids.config.TimestampConfig;

/**
 * Handles timestamp mark up, also for different languages.
 * 
 * @author margaretha
 * 
 */
public class WikiTimestamp {

	private String pretext;
	private String timestamp;
	private String postscript;
	private String isoTimestamp;

	private String language;
	private boolean DEBUG = false;
	public static ArrayList<DateTimeFormatter> formats;

	public WikiTimestamp(String text, String language) {
		formats = TimestampConfig.getFormats(language);
		this.language = language;
		matchTimeStamp(text);
	}

	public void matchTimeStamp(String text) {
		Matcher matcher = TimestampConfig.timePattern.matcher(text);
		if (matcher.find()) {
			if (DEBUG) System.out.println("timePattern");
			createTimeStamp(matcher);
			return;
		}

		matcher = TimestampConfig.timePattern2.matcher(text);
		if (matcher.find()) {
			if (DEBUG) System.out.println("timePattern2");
			createTimeStamp(matcher);
			return;
		}

		matcher = TimestampConfig.timePattern3.matcher(text);
		if (matcher.find()) {
			if (DEBUG) System.out.println("timePattern3");
			createTimeStamp(matcher);
		}
		else {
			setPostscript(text);
		}
	}

	public void createTimeStamp(Matcher matcher) {
		setPretext(matcher.group(1)); // pretext

		String timestamp = matcher.group(2);
		setTimestamp(timestamp); // timestamp
		
		String isoTimestamp = createIsoTimestamp(timestamp);
		setIsoTimestamp(isoTimestamp);
		
		setPostscript(matcher.group(3));
		matcher.reset();
	}

	private String createIsoTimestamp(String text) {
		if (language.equalsIgnoreCase("de")) {
			text = TimestampConfig.cestPattern.matcher(text)
					.replaceFirst("MESZ");
			text = TimestampConfig.westPattern.matcher(text)
					.replaceFirst("WESZ");
			text = TimestampConfig.eestPattern.matcher(text)
					.replaceFirst("OESZ");
		}
		
		ZonedDateTime zonedDateTime = null;
		for (DateTimeFormatter format : formats){
			try {
				zonedDateTime = ZonedDateTime.parse(text, format);
				if (DEBUG) System.out.println(zonedDateTime);
				break;
			}
			catch (Exception e) {
				continue;
			}
			
		}
		
		String isoTimestamp = null;
		if (zonedDateTime != null) {
			if (zonedDateTime.getZone().getId().equals("UTC")) {
				isoTimestamp = zonedDateTime.toString().substring(0, 16);
			}
			else {
				isoTimestamp = zonedDateTime.toString().substring(0, 19);
			}
		}
		return isoTimestamp;
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

	public String getIsoTimestamp() {
		return isoTimestamp;
	}

	public void setIsoTimestamp(String isoTimestamp) {
		this.isoTimestamp = isoTimestamp;
	}

}
