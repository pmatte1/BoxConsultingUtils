package com.box.bc.migration.metadata.parser;

public class ReservedName extends HeaderParser {

	public ReservedName(String headerName) {
		super(headerName);
	}

	public String getTemplateName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeName() {
		// TODO Auto-generated method stub
		return headerValue;
	}

	public boolean isMetadata() {
		// TODO Auto-generated method stub
		return false;
	}

}
