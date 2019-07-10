package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.mannheim.ids.wiki.page.WikiTimestamp;

public class WikiTimestampTest {

	@Test
	public void testGermanTimestamp() {
		String language = "de";
		String text = " 21:08, 27. Feb. 2017 (CET)";
		WikiTimestamp t = new WikiTimestamp(text, language);
		assertEquals("2017-02-27T21:08+01", t.getIsoTimestamp());

		text = " 23:41, 31. Mär 2004 (CEST)";
		t = new WikiTimestamp(text, language);
		assertEquals("2004-03-31T23:41+02", t.getIsoTimestamp());

		text = " 01:42, 4. Sep 2002 (PDT)";
		t = new WikiTimestamp(text, language);
		assertEquals("2002-09-04T01:42-07", t.getIsoTimestamp());
	}

	@Test
	public void testEnglishTimestamp() {
		String text = " 05:20, 30 April 2010 (CEST)";
		WikiTimestamp t = new WikiTimestamp(text, "en");
		assertEquals("2010-04-30T05:20+02", t.getIsoTimestamp());

		text = " 05:20, 30 April 2010 (UTC)";
		t = new WikiTimestamp(text, "en");
		assertEquals("2010-04-30T05:20", t.getIsoTimestamp());

		text = " 09:06, Sep 1, 2002 (PDT)";
		t = new WikiTimestamp(text, "en");
		assertEquals("2002-09-01T09:06-07", t.getIsoTimestamp());

		text = " 16:51, 15 Apr 2005 (BST)";
		t = new WikiTimestamp(text, "en");
		assertEquals("2005-04-15T16:51+10", t.getIsoTimestamp());
	}

	@Test
	public void testFrenchTimestamp() {
		// String text = " 15 octobre 2006 à 14:25 (CEST)";
		// WikiTimestamp t = new WikiTimestamp(text, "fr");
		// assertEquals("2006-10-15T14:25+02", t.getIsoTimestamp());

		// doesn't work
		// java short month format is févr.
		// text = "16 fév 2004 à 21:51 (CET)";
		// t = new WikiTimestamp(text, "fr");
		// assertEquals("2004-02-16T21:51+01", t.getIsoTimestamp());
	}

	@Test
	public void testItalianTimestamp() {
		String text = " 23:48, 22 giu 2008 (CEST)";
		WikiTimestamp t = new WikiTimestamp(text, "it");
		assertEquals("2008-06-22T23:48+02", t.getIsoTimestamp());
	}

	@Test
	public void testSpanishTimestamp() {
		String text = " 05:56 21 dic 2005 (CET)";
		WikiTimestamp t = new WikiTimestamp(text, "es");
		assertEquals("2005-12-21T05:56+01", t.getIsoTimestamp());

		text = " 04:14 25 jul, 2005 (CEST)";
		t = new WikiTimestamp(text, "es");
		assertEquals("2005-07-25T04:14+02", t.getIsoTimestamp());

		// without hour
		// text = "25 oct, 2005";
		// t = new WikiTimestamp(text, "es");
		// assertEquals("2005-07-25T04:14+02", t.getIsoTimestamp());

		// wrong format
		// text = "22:34 16 sep 2006 (CEST";
		// t = new WikiTimestamp(text, "es");
		// assertEquals("2005-12-21T05:56+01", t.getIsoTimestamp());

		// free text
		// text = "Saludos. H.R. 14 feb. 2006";
		// t = new WikiTimestamp(text, "es");
		// assertEquals("2005-12-21T05:56+01", t.getIsoTimestamp());

	}

	@Test
	public void testPolishTimestamp() {
		String text = " 00:17, 11 wrz 2004 (CEST)";
		WikiTimestamp t = new WikiTimestamp(text, "pl");
		assertEquals("2004-09-11T00:17+02", t.getIsoTimestamp());

		text = " 21:48, 8 maja 2007 (CEST)";
		t = new WikiTimestamp(text, "pl");
		assertEquals("2007-05-08T21:48+02", t.getIsoTimestamp());

		// 3 luty 2006
	}

	@Test
	public void testCroatianTimestamp() {
		String text = " 22:03, 9. prosinca 2013. (CET)";
		WikiTimestamp t = new WikiTimestamp(text, "hr");
		assertEquals("2013-12-09T22:03+01", t.getIsoTimestamp());

		text = " 16:40, 26 Aug 2004 (CEST)";
		t = new WikiTimestamp(text, "hr");
		assertEquals("2004-08-26T16:40+02", t.getIsoTimestamp());
	}

	@Test
	public void testHungarianTimestamp() {
		String text = " 2006. október 17., 00:30 (CEST)";
		WikiTimestamp t = new WikiTimestamp(text, "hu");
		assertEquals("2006-10-17T00:30+02", t.getIsoTimestamp());

		text = " 2003 szeptember 12 12:21 (UTC)";
		t = new WikiTimestamp(text, "hu");
		assertEquals("2003-09-12T12:21", t.getIsoTimestamp());
	}

	@Test
	public void testNorwegianTimestamp() {
		String text = " 11. feb 2008 kl. 02:27 (CET)";
		WikiTimestamp t = new WikiTimestamp(text, "no");
		assertEquals("2008-02-11T02:27+01", t.getIsoTimestamp());

		text = " 15. des 2003 kl.14:07 (UTC)";
		t = new WikiTimestamp(text, "no");
		assertEquals("2003-12-15T14:07", t.getIsoTimestamp());

		// no time zone
		// 21. mai 2004
	}

	@Test
	public void testRomanianTimestamp() {
		String text = " 8 septembrie 2017 16:40 (EEST)";
		WikiTimestamp t = new WikiTimestamp(text, "ro");
		assertEquals("2017-09-08T16:40+03", t.getIsoTimestamp());

		text = " 27 noiembrie 2011 20:59 (EET)";
		t = new WikiTimestamp(text, "ro");
		assertEquals("2011-11-27T20:59+02", t.getIsoTimestamp());

		text = " 16:35 2 Aug 2003 (UTC)";
		t = new WikiTimestamp(text, "ro");
		assertEquals("2003-08-02T16:35", t.getIsoTimestamp());

		text = " 22 Aug 2003 11:08 (UTC)";
		t = new WikiTimestamp(text, "ro");
		assertEquals("2003-08-22T11:08", t.getIsoTimestamp());
	}

}
