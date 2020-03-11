package com.box.bc.migration.metadata;

import org.apache.log4j.Logger;

import com.box.bc.migration.metadata.parser.GenericNameValueParser;

public class CSVParserAllValues extends CSVParser {
	private static Logger logger = Logger.getLogger(CSVParserAllValues.class);

	@Override
	protected void loadHeaderValues(String[] line){
		for(int j=0; j<line.length; j++){
			GenericNameValueParser cm = new GenericNameValueParser(line[j]);
			metadataNameValueParserList.add(cm);
		}
		
	}
	

}
