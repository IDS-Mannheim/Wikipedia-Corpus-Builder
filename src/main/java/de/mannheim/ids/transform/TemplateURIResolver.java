package de.mannheim.ids.transform;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/** Used to find the XLST templates files in the src/main/resources. 
 * 
 * @author margaretha
 *
 */
public class TemplateURIResolver implements URIResolver{

	@Override
	public Source resolve(String href, String base) throws TransformerException {		 
		return new StreamSource(
				this.getClass().getClassLoader().getResourceAsStream(href)) ;
	}
}
