package com.box.bc.migration.metadata.parser;

import com.box.sdk.Metadata;

public interface IMetadataNameValueParser {
	
	public String getTemplateName();
	public String getAttributeName();
	public Metadata getMetaData(String value);
	public boolean isMetadata();

}
