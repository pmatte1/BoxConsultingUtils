package com.box.bc.migration.metadata.parser;

import com.box.sdk.Metadata;

/**
 * This class allows for a standard implementation of the getMetaData method,
 * by using the getAttributeName method to identify the attribute to set
 * the metadata value to.
 * 
 * All sub-classes will need to implement the getAttributeName method
 * 
 * @author pmatte
 *
 */
public abstract class HeaderParser implements IMetadataNameValueParser {

	protected String headerValue;

	/**
	 * Constructor for the abstract HeaderParser class
	 * 
	 * @param headerValue - Value that will represent the template and/or attribute
	 * to assign the value to
	 */
	public HeaderParser(String headerValue){
		this.headerValue = headerValue;
	}

	/**
	 * Abstract method to make it compliant to the IMetadataNameValueParser interface
	 */
	public abstract String getTemplateName();

	/**
	 * Will return the Attribute Name (without a /) to set the value to
	 * 
	 * @return String containing the name of the attribute
	 */
	protected abstract String getAttributeName();

	/**
	 * Abstract method to make it compliant to the IMetadataNameValueParser interface
	 */
	public abstract boolean isMetadata();

	public Metadata getMetaData(String value) {
		if(isMetadata()){
			Metadata md = new Metadata();
			if(getAttributeName() != null){
				md.add("/" + getAttributeName(), value);
				return md;
			}
		}
		return null;
	}

}
