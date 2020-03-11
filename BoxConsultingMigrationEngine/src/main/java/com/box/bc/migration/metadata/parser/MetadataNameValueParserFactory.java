package com.box.bc.migration.metadata.parser;

import java.util.ArrayList;
import java.util.List;

import com.box.bc.util.PropertiesUtil;

/**
 * This class implements the business logic to determine which metadata parser implentation
 * should be used for a specific header.
 * 
 * @author pmatte
 *
 */
public class MetadataNameValueParserFactory {

	/* List containing any value that is listed as a Reserved Word*/
	protected static List<String> reservedWordsList = new ArrayList<String>();
	protected static String currentPropFile = null;

	static{
		loadReservedWordsList("metadata.properties");
	}
	
	protected static void loadReservedWordsList(String propertiesFileName){
		if(propertiesFileName != null && !propertiesFileName.equals(currentPropFile)){
		boolean keepChecking = true;
		reservedWordsList.add("file_path");
		for(int i=0; keepChecking; i++){
			currentPropFile = propertiesFileName;
			String reservedWord = PropertiesUtil.getPropertiesFromFile(propertiesFileName).getProperty("reservedword." + i, null);
			if(reservedWord != null){
				reservedWordsList.add(reservedWord.trim().toLowerCase());
			}else{
				keepChecking=false;
			}
		}
		}
		
	}
	
	/**
	 * This method will use the header name to identify which implementation
	 * class to use based on the naming convention of the header name passed
	 * in
	 * 
	 * Current Rules:
	 * - If the name is file_path use the ReservedName class
	 * - If the name contains a . then this assumes a template is defined and 
	 * 		returns an instance of the TemplateMetadata class
	 * - Otherwise, assume it is custom metadata and return the CustomMetadata class
	 * 
	 * @param headerName - Name from the metadata file to identify which attribute to use
	 * @return Instance of an IMetadataNameValueParser class based on the rules
	 */
	public static IMetadataNameValueParser getParser(String headerName){
		if(headerName.contains(".")){
			return new TemplateMetadata(headerName);
		}else if(reservedWordsList.contains(headerName.toLowerCase().trim())){
			return new ReservedName(headerName);
		}else{
			return new CustomMetadata(headerName);
		}
	}
	
	public static IMetadataNameValueParser getParser(String headerName, String propertiesFileName){
		
		loadReservedWordsList(propertiesFileName);
		
		if(headerName.contains(".")){
			return new TemplateMetadata(headerName);
		}else if(reservedWordsList.contains(headerName.toLowerCase().trim())){
			return new ReservedName(headerName);
		}else{
			return new CustomMetadata(headerName);
		}
	}

}
