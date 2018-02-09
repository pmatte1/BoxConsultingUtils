package com.box.bc.migration.metadata.parser;


public class CustomMetadata extends HeaderParser {
	public static String CUSTOM_METADATA_TEMPLATE_NAME = "NO_TEMPLATE";

	public CustomMetadata(String headerValue) {
		super(headerValue);
	}

	public String getTemplateName() {
		return CUSTOM_METADATA_TEMPLATE_NAME;
	}

	public String getAttributeName() {
		return this.headerValue;
	}

	public boolean isMetadata() {
		return true;
	}


}
