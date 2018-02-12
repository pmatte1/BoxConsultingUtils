package com.box.bc.migration.metadata.parser;

/**
 * Class to hold the Template Metadata.  It will get the
 * Template Name from the header value with the naming convention
 * <templatename>.<attributename>
 * 
 * @author pmatte
 *
 */
public class TemplateMetadata extends HeaderParser{

	/**
	 * Constructor
	 * 
	 * @param headerName
	 */
	public TemplateMetadata(String headerName) {
		super(headerName);
	}

	public String getTemplateName() {
		if(this.headerValue.contains(".")){
			return headerValue.substring(0, headerValue.indexOf("."));
		}
		return null;
	}

	public String getAttributeName() {
		return headerValue.substring(getTemplateName().length()+1);
	}

	public boolean isMetadata() {
		return true;
	}

}
