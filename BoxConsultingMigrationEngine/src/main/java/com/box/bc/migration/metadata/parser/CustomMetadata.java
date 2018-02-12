package com.box.bc.migration.metadata.parser;

/**
 * This class identifies a Metadata item that is not to be 
 * added to a template
 * 
 * @author pmatte
 *
 */
public class CustomMetadata extends HeaderParser {
	/* Constant value to identify Custom metadata	 */
	public static String CUSTOM_METADATA_TEMPLATE_NAME = "NO_TEMPLATE";

	/**
	 * Constructor to set the Header Value
	 * 
	 * @param headerValue - The attribute name to add to the Folder/Document
	 */
	public CustomMetadata(String headerValue) {
		super(headerValue);
	}

	/**
	 * This will always return the CUSTOM_METADATA_TEMPLATE_NAME value, 
	 * as this will always be CUSTOM metadata
	 * 
	 * @return String containing the value of the CUSTOM_METADATA_TEMPLATE_NAME constant
	 */
	public String getTemplateName() {
		return CUSTOM_METADATA_TEMPLATE_NAME;
	}

	/**
	 * This will return the stored Header Value passed into the constructor
	 * as that will be the attribute name
	 * 
	 * @return String containing the attribute name to use
	 */
	public String getAttributeName() {
		return this.headerValue;
	}

	/**
	 * Will always return true, as this always represents metadata
	 */
	public boolean isMetadata() {
		return true;
	}


}
