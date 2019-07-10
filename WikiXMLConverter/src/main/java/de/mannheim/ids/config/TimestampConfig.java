package de.mannheim.ids.config;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class TimestampConfig {

	// german, italian, croatian, polish, spanish
	public static final Pattern timePattern = Pattern.compile("(.*[^0-9])"
			+ "([0-9]{1,2}:[0-9]{2},? [0-9]{1,2}\\.? [^\\d]{3,10},?\\.? [0-9]{4}\\.?\\s?\\([A-Z]{1,4}\\))"
			+ "(.*)");

	// hungarian, norwegian, french, romanian
	public static final Pattern timePattern2 = Pattern.compile("(.*[^0-9])"
			+ "([0-9]{1,4}\\.? [^\\d]{3,10}\\.? [0-9]{1,4}[^\\d]{1,5}[0-9]{1,2}:[0-9]{2}\\s?\\([A-Z]{1,4}\\))"
			+ "(.*)");

	// " 09:06, Sep 1, 2002 (PDT)"
	public static final Pattern timePattern3 = Pattern.compile("(.*[^0-9])"
			+ "([0-9]{1,2}:[0-9]{2},? [^\\d]{3,10}\\.?[0-9]{1,2},? [0-9]{4}\\s?\\([A-Z]+\\))"
			+ "(.*)");

	// German timezone patterns
	public static final Pattern cestPattern = Pattern.compile("CEST",
			Pattern.LITERAL);
	public static final Pattern westPattern = Pattern.compile("WEST",
			Pattern.LITERAL);
	public static final Pattern eestPattern = Pattern.compile("OEST",
			Pattern.LITERAL);

	// EN 08:09, 1 Sep 2004 (UTC)
	public static DateTimeFormatter genericEnFormat = new DateTimeFormatterBuilder()
			.appendPattern("HH:mm")
			.optionalStart().appendPattern(",").optionalEnd()
			.appendPattern(" d MMM yyyy (z)")
			.toFormatter(Locale.ENGLISH);

	// EN 16:35 2 Aug 2003 (UTC)
	public static DateTimeFormatter genericEnFormat2 = DateTimeFormatter
			.ofPattern("d MMM yyyy HH:mm (z)", Locale.ENGLISH);

	// EN 05:20, 1 December 2010 (UTC)
	// EN 09:06, Sep 1, 2002 (PDT)
	public static final DateTimeFormatter enFormat = new DateTimeFormatterBuilder()
			.appendOptional(
					DateTimeFormatter.ofPattern("HH:mm, d MMMM yyyy (z)"))
			.appendOptional(
					DateTimeFormatter.ofPattern("HH:mm, MMM d, yyyy (z)"))
			.toFormatter(Locale.ENGLISH);

	// DE 21:08, 27. Feb. 2017 (CET)
	// DE 23:41, 23. Mär 2004 (CET)
	public static final DateTimeFormatter deFormat = new DateTimeFormatterBuilder()
			.appendPattern("HH:mm, d. MMM")
			.optionalStart().appendPattern(".").optionalEnd()
			.appendPattern(" yyyy (z)")
			.toFormatter(Locale.GERMAN);

	// FR 10 décembre 2007 à 12:02 (CET)
	public static DateTimeFormatter frFormat = DateTimeFormatter.ofPattern(
			"d MMMM yyyy 'à' HH:mm (z)", Locale.FRENCH);
	// 16 fév 2004 à 21:51 (CET)
	public static DateTimeFormatter frFormat2 = DateTimeFormatter.ofPattern(
			"d MMM yyyy 'à' HH:mm (z)", Locale.FRENCH);

	// IT 23:48, 22 giu 2008 (CEST)
	public static DateTimeFormatter itFormat = DateTimeFormatter
			.ofPattern("HH:mm, d MMM yyyy (z)", Locale.ITALIAN);

	// ES 05:56 21 dic 2005 (CET)
	// ES 04:14 25 jul, 2005 (CEST)
	public static DateTimeFormatter esFormat = new DateTimeFormatterBuilder()
			.appendPattern("HH:mm d MMM")
			.optionalStart().appendPattern(",").optionalEnd()
			.appendPattern(" yyyy (z)")
			.toFormatter(Locale.forLanguageTag("es"));

	// PL 00:17, 11 wrz 2004 (CEST)
	// PL 21:48, 8 maja 2007 (CEST)
	public static DateTimeFormatter plFormat = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ofPattern("HH:mm, d MMM yyyy (z)"))
			.appendOptional(DateTimeFormatter.ofPattern("HH:mm, d MMMM yyyy (z)"))
			.toFormatter(Locale.forLanguageTag("pl"));

	// HR 22:03, 9. prosinca 2013. (CET) 
	// 16:40, 26 Aug 2004 (CEST)
	public static DateTimeFormatter hrFormat = DateTimeFormatter.ofPattern(
			"HH:mm, d. MMMM yyyy. (z)", Locale.forLanguageTag("hr"));

	// HU 2006. október 17., 00:30 (CEST)
	//    2003 szeptember 12 12:21 (UTC)
	public static DateTimeFormatter huFormat = new DateTimeFormatterBuilder() 
			.appendOptional(DateTimeFormatter.ofPattern("yyyy. MMMM d., HH:mm (z)"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy MMMM d HH:mm (z)"))
			.toFormatter(Locale.forLanguageTag("hu"));
			
	// NO 11. feb 2008 kl. 02:27 (CET)
	//    15. des 2003 kl.14:07 (UTC)
	public static DateTimeFormatter noFormat = new DateTimeFormatterBuilder()
			.appendPattern("d. MMM yyyy 'kl.'")
			.optionalStart().appendPattern(" ").optionalEnd()
			.appendPattern("HH:mm (z)")
			.toFormatter(Locale.forLanguageTag("no"));
			
			
	// RO 8 septembrie 2017 16:40 (EEST)
	public static DateTimeFormatter roFormat = DateTimeFormatter.ofPattern(
			"d MMMM yyyy HH:mm (z)", Locale.forLanguageTag("ro"));

	public static ArrayList<DateTimeFormatter> getFormats(String language) {
		language = language.toLowerCase();
		ArrayList<DateTimeFormatter> formats = new ArrayList<>();
		if (language.equals("de")) {
			formats.add(deFormat);
		}
		else if (language.equals("en")) {
			formats.add(enFormat);
		}
		else if (language.equals("fr")) {
			formats.add(frFormat2);
			formats.add(frFormat);
		}
		else if (language.equals("it")) {
			formats.add(itFormat);
		}
		else if (language.equals("es")) {
			formats.add(esFormat);
		}
		else if (language.equals("pl")) {
			formats.add(plFormat);
		}
		else if (language.equals("hr")) {
			formats.add(hrFormat);
		}
		else if (language.equals("hu")) {
			formats.add(huFormat);
		}
		else if (language.equals("no")) {
			formats.add(noFormat);
		}
		else if (language.equals("ro")) {
			formats.add(roFormat);
			formats.add(genericEnFormat2);
		}
		formats.add(genericEnFormat);
		return formats;
	}
}
