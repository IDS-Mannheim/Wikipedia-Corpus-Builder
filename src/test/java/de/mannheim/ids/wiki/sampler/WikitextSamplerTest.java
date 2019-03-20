package de.ids_mannheim.korap.sampler;

import java.io.IOException;

import org.apache.commons.cli.ParseException;

import junit.framework.TestCase;

/**
 * Unit test for WikitextSampler
 */
public class WikitextSamplerTest extends TestCase {

	private String wikidump = "data/dewiki-20170701-pages.xml";
	private String output = "dewiki-20170701-sample.xml";

	public void testSamplingWithDefaultFactor()
			throws ParseException, IOException {
		WikitextSampler sampler = new WikitextSampler();
		sampler.parseArguments(new String[]{"-i", wikidump, "-o", output});
		sampler.createSample();
	}

	public void testSamplingWithSpecificFactorAndWeight()
			throws ParseException, IOException {
		WikitextSampler sampler = new WikitextSampler();
		sampler.parseArguments(new String[]{"-i", wikidump, "-f",
				"0.00001", "-rw", "0.05", "-lw", "0.001"});
		sampler.createSample();
	}
}
