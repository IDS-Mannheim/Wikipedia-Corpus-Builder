package de.mannheim.ids.wiki.sampler;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class WikitextSampleBuilderTest {

	private String wikidump = "../WikiXMLConverter/data/dewiki-20170701-sample.xml";
	private String output = "dewiki-20170701-sample.xml";

	@Test
	public void testSamplingWithDefaultFactor()
			throws ParseException, IOException {
		WikitextSampler sampler = new WikitextSampler();
		sampler.parseArguments(new String[]{"-i", wikidump, "-o", output});
		sampler.createSample();
		
		File f = new File(output);
		assertTrue(f.exists());
	}

	@Test
	public void testSamplingWithSpecificFactorAndWeight()
			throws ParseException, IOException {
		WikitextSampler sampler = new WikitextSampler();
		sampler.parseArguments(new String[]{"-i", wikidump, "-f",
				"0.1", "-rw", "0.5", "-lw", "0.5"});
		sampler.createSample();
		
		File f = new File("output.xml");
		assertTrue(f.exists());
	}
}
