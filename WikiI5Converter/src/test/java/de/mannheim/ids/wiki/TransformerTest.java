package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import de.mannheim.ids.transform.Transformer;
import de.mannheim.ids.transform.WikiI5Part;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;

public class TransformerTest {

	@Test
	public void testXsltResult() throws Exception {
		WikiI5Converter converter = new WikiI5Converter();
		Configuration config = converter.createConfig(
				new String[]{"-prop", "dewiki-talk.properties"});
		Statistics statistics = new Statistics();
		I5ErrorHandler errorHandler = new I5ErrorHandler(config);
		Builder builder = new Builder();

		String idx = "B";
		String pageId = "8568531";
		String xmlPath = idx + "/" + pageId + ".xml";

		Transformer t = new Transformer(config, statistics, errorHandler,
				xmlPath, idx, pageId);

		WikiI5Part wikipart = t.call();
		assertEquals(true, wikipart.isIDSText());
		InputStream is = wikipart.getInputStream();

//		String line="";
//		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {	
//			while ((line = bufferedReader.readLine()) != null) {
//				System.out.println(line);
//			}
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
		
		Document doc = builder.build(is);
		Node idsText = doc.query("/idsText").get(0);
		assertEquals("WDD17.B0085.68531",
				idsText.query("@id").get(0).getValue());
		assertEquals("WDD17/B0085.68531",
				idsText.query("idsHeader/fileDesc/titleStmt/textSigle").get(0)
						.getValue());
	}
}
