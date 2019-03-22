package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class PropertiesTest {

	@Test
	public void testPolskiConfiguration() throws ParseException, IOException {
		WikiXMLConverter converter = new WikiXMLConverter();
		Configuration config = converter
				.createConfig(new String[]{"-prop", "plwiki-löschkandidaten.properties"});
		
		assertEquals("Specjalna:Wkład",config.getUserContribution());
	}
	
}
