package com.box.bc.migration.metadata.parser;

import com.box.sdk.Metadata;

public abstract class HeaderParser implements IMetadataNameValueParser {

	protected String headerValue;

	public HeaderParser(String headerValue){
		this.headerValue = headerValue;
	}

	public String getTemplateName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isMetadata() {
		// TODO Auto-generated method stub
		return false;
	}

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
