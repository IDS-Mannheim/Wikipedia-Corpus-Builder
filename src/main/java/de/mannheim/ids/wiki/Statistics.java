package de.mannheim.ids.wiki;

public class Statistics {
	public int transformationError;
	public int dtdValidationError;
	public int saxParserError;
	public int emptyTransformationResult;
	public int transformedPages;
	public int numOfChar;
	
	public Statistics() {
		transformationError=0;
		dtdValidationError=0;
		saxParserError=0;
		emptyTransformationResult=0;
		transformedPages=0;
		numOfChar=0;
	}
	
	public void printStats() {		
		System.out.println("Number of transformed pages: "+transformedPages);
		System.out.println("Number of not-transformed pages (char index): "+numOfChar);
		System.out.println("Number of transformation errors: "+transformationError);
		System.out.println("Number of empty transformation results: "+emptyTransformationResult);
		System.out.println("Number of DTD validation errors: "+dtdValidationError);
		System.out.println("Number of non well-formed XML: "+saxParserError);
		
		System.out.println("Total number valid pages: " + (transformedPages - 
				transformationError - dtdValidationError - saxParserError));
	}
	
	public int getNumOfChar() {
		return numOfChar;
	}
	
	public void setNumOfChar(int numOfChar) {
		this.numOfChar = numOfChar;
	}
	
	public int getTransformedPages() {
		return transformedPages;
	}
	
	public void addTransformedPages() {
		this.transformedPages++;
	}
	
	public int getEmptyTransformationResult() {
		return emptyTransformationResult;
	}
	
	public void addEmptyTransformationResult() {
		this.emptyTransformationResult++;
	}
	
	public int getDtdValidationError() {
		return dtdValidationError;
	}
	
	public synchronized void addDtdValidationError() {
		this.dtdValidationError++;
	}
	
	public int getTransformationError() {
		return transformationError;
	}
	
	public synchronized void addTransformationError() {
		this.transformationError++;
	}
	
	public int getSaxParserError() {
		return saxParserError;
	}
	
	public synchronized void addSaxParserError() {
		this.saxParserError++;
	}
	
}
