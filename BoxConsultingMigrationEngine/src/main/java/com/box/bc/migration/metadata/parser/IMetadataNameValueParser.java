package com.box.bc.migration.metadata.parser;

import com.box.sdk.Metadata;

/**
 * This interface is to define the header parsing logic to allow
 * flexibility in how we determine templates vs. custom metadata,
 * and derive the attribute names associated with each.
 * 
 * All logic to determine which to use needs to be implemented in the
 * MetadataNameValueParserFactory
 * 
 * @author pmatte
 *
 */
public interface IMetadataNameValueParser {
	
	/**
	 * Returns the template name to use for adding the metadata.  Must
	 * match the Metadata Template's ID (not the name)
	 * 
	 * @return - String containing the template name.
	 */
	public String getTemplateName();
	
	/**
	 * This returns a Metadata object with the attribute set to the
	 * name derived from the header name, and the value set to the 
	 * String value passed in
	 *  
	 * @param value - String value representing the value to set the attribute to
	 * @return Metadata object with the name and value set
	 */
	public Metadata getMetaData(String value);
	
	/**
	 * This returns the name of the Attribute to add a value to.
	 *  
	 * @return String object with the name of the attribute
	 */
	public String getAttributeName();
	
	/**
	 * This controls whether the value should be treated as Metadata
	 * 
	 * Some examples of items that would not be treated as Metadata are:
	 * - The file path to the file to apply metadata to
	 * - A SHA hash that we would use to validate the uploaded content matches the source
	 * 
	 * @return True if the object represents metadata, False if not.
	 */
	public boolean isMetadata();

}
