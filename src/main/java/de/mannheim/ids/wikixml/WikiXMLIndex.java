package de.mannheim.ids.wikixml;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class WikiXMLIndex {

	private DocumentBuilder xmlBuilder;
	private Document indexDoc;

	public WikiXMLIndex(String indexURI) {
		setXMLBuilder();
		setIndexDoc(indexURI);
	}

	private void setXMLBuilder() {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();

		try {
			builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,
					false);
			xmlBuilder = builderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new RuntimeException("Failed setting a document builder.", e);
		}
	}

	public Document getIndexDoc() {
		return indexDoc;
	}

	public void setIndexDoc(String indexURI) {
		if (indexURI == null || indexURI.isEmpty()) {
			throw new IllegalArgumentException("Index cannot be null or empty.");
		}

		try {
			indexDoc = xmlBuilder.parse(indexURI);
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
