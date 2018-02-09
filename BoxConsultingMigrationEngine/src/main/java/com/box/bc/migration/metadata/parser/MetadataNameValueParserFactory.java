package com.box.bc.migration.metadata.parser;

import java.util.ArrayList;
import java.util.List;

public class MetadataNameValueParserFactory {

	protected static List<String> reservedWordsList = new ArrayList<String>();
	static{
		reservedWordsList.add("file_path");
	}
	
	public static IMetadataNameValueParser getParser(String headerName){
		if(headerName.contains(".")){
			return new TemplateMetadata(headerName);
		}else if(reservedWordsList.contains(headerName.toLowerCase())){
			return new ReservedName(headerName);
		}else{
			return new CustomMetadata(headerName);
		}
	}
}
