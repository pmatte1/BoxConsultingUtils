package com.box.bc.migration.metadata;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.box.bc.migration.metadata.parser.HeaderParser;
import com.box.bc.migration.metadata.parser.IMetadataNameValueParser;
import com.box.bc.migration.metadata.parser.MetadataNameValueParserFactory;
import com.box.bc.util.PropertiesUtil;
import com.box.sdk.Metadata;
import com.opencsv.CSVReader;

/**
 * This parses a CSV file that has the template and or attributes in the first row
 * and the corresponding values in the subsequent rows.
 *  
 * @author pmatte
 *
 */
public class CSVParser implements IMetadataParser {
	private static Logger logger = Logger.getLogger(CSVParser.class);

	protected Map<String, List<MetadataTemplateAndValues>> theMap = new HashMap<String, List<MetadataTemplateAndValues>>();
	protected List<IMetadataNameValueParser> metadataNameValueParserList = new ArrayList<IMetadataNameValueParser>();

	/**
	 * Will return true if the name ends in .csv
	 */
	public boolean isMetadataFile(File potentialMetadataFile) {
		if(potentialMetadataFile.getName().toLowerCase().endsWith(".csv")){
			load(potentialMetadataFile);
			return true;
		}
		return false;
	}

	public List<MetadataTemplateAndValues> getMetadata(
			File fileToApplyMetadataTo) {
		logger.info("MAP: " + theMap);
		logger.info("File Path: " + fileToApplyMetadataTo.getPath());
		if(theMap != null){
			return theMap.get(fileToApplyMetadataTo.getPath());
		}
		return null;
	}
	
	public Map<String, List<MetadataTemplateAndValues>> getAllMetadata(){
		return theMap;
	}

	/**
	 * Will parse the file and use the file_path from the metadata file to identify
	 * what line applies to which file/folder
	 */
	public void load(File metadataFile) {

		CSVReader reader = null;
		try {
			logger.debug("Loading CSV Reader with Separator " + getSeparator());
			reader = new CSVReader(new FileReader(metadataFile), getSeparator());
			logger.debug("Loaded CSV Reader");


			String[] line;
			for (int i=0; (line = reader.readNext()) != null; i++) {

				if(i==0){
					logger.debug("Parsing Header Line");
					loadHeaderValues(line);
					logger.debug("Parsed Header Line");

				}else{
					loadBody(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(reader != null)
					reader.close();
			} catch (IOException e) {

			}
		}


	}

	/**
	 * Loads the internal Map to hold the Name - Value pairs
	 * 
	 * @param line
	 */
	protected void loadBody(String[] line) {
		String keyValue = null;
		List<MetadataTemplateAndValues> mtavs = new ArrayList<MetadataTemplateAndValues>();
		for(int j=0; j<line.length; j++){
			//Get the parser created for the header in the same index
			IMetadataNameValueParser mnvp = metadataNameValueParserList.get(j);
			
			//If it is metadata, load it into the list of Metadata Templates and Values
			if(mnvp.isMetadata()){
				
				String templateName = mnvp.getTemplateName();
				Metadata md = new Metadata();
				boolean isInList = false;
				
				//Check if the template already exists in the list, and get
				//the metadata values if it does
				for(MetadataTemplateAndValues mtav : mtavs){
					if(mtav.getMetadataTemplateName().equals(templateName)){
						md=mtav.getMetadataValues();
						isInList = true;
					}
				}

				//Get the metadata from the parser, based on the value from 
				//the parsed line of the CSV
				Metadata parsedMd = mnvp.getMetaData(line[j]);

				//If the template does not exist, create a new instance of the
				//MetadataTemplateAndValues object, and add to the list
				if(!isInList){
					MetadataTemplateAndValues mtav = new MetadataTemplateAndValues();
					//Set template name
					mtav.setMetadataTemplateName(templateName);
					
					//Set the metadata values
					mtav.setMetadataValues(parsedMd);
					
					//Add it to the list of MetadataTemplateAndValues
					mtavs.add(mtav);
				}else{
					//Add the metadata information to the existing Metadata object
					for(String propertyPath : parsedMd.getPropertyPaths()){
						md.add(propertyPath, parsedMd.get(propertyPath));
					}

				}
				if(isKey(mnvp.getAttributeName())){
					keyValue = line[j];
				}
			}else{
				//If this is the file_path column, add the value as the key, so it can
				//retrieve the list based on the file or folder's path later
				if(mnvp.getAttributeName().equals("file_path")){

					keyValue = new File(line[j]).getPath();
				}
			}

		}
		if(keyValue == null){
			
		}
		theMap.put(keyValue, mtavs);
		
	}

	String[] allKeys = new String[]{"file_path","transaction#"};
	protected boolean isKey(String string) {
		for(String key: allKeys){
			if(key.toLowerCase().equals(string.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Parses the Header Line to retrieve the parsers for the individual cells
	 * 
	 * @param line
	 * @return List of MetadataNameValueParsers for the cells, based on the header line
	 */
	protected void loadHeaderValues(String[] line) {
		for(int j=0; j<line.length; j++){
			logger.debug("Loading Parser for Column " + j + " (" + line[j] + ")");
			metadataNameValueParserList.add(MetadataNameValueParserFactory.getParser(line[j], propertiesFileName));
			logger.debug("Loaded Parser for Column " + j + " (" + line[j] + ")");
		}
		
	}

	protected char getSeparator() {
		return ',';
	}

	/**
	 * Will return a FilenameFilter that checks if the file ends with .csv
	 */
	public FilenameFilter getFileNameFilter() {
		FilenameFilter fnf = new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				if(name.endsWith(".csv")){
					return true;
				}
				return false;
			}
		};
		
		return fnf;
	}

	String propertiesFileName = null;
	public void load(File metadataFile, String propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
		load(metadataFile);
		
	}

}
