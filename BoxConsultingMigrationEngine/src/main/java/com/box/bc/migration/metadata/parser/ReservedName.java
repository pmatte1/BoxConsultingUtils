package com.box.bc.migration.metadata.parser;

/**
 * This class will hold any values that are identified as 
 * Reserved words in the implementation.  This is to allow
 * for items other than metadata to be included in the metadata
 * file in order to properly apply metadata to the correct
 * item
 *  
 * @author pmatte
 *
 */
public class ReservedName extends HeaderParser {

	/**
	 * Constructor
	 *  
	 * @param headerName - Name of the Reserved Word from the metadata file
	 */
	public ReservedName(String headerName) {
		super(headerName);
	}

	/**
	 * 
	 */
	public String getTemplateName() {
		return null;
	}

	/**
	 * 
	 */
	public String getAttributeName() {
		return headerValue;
	}

	/**
	 * 
	 */
	public boolean isMetadata() {
		return false;
	}

}
