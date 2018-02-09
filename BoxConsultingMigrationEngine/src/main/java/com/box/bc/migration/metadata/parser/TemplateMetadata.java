package com.box.bc.migration.metadata.parser;

public class TemplateMetadata extends HeaderParser{

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
