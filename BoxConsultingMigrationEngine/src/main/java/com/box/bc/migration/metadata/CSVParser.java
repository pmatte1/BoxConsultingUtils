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

public class CSVParser implements IMetadataParser {
	private static Logger logger = Logger.getLogger(CSVParser.class);

	protected Map<String, List<MetadataTemplateAndValues>> theMap = new HashMap<String, List<MetadataTemplateAndValues>>();

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

	public void load(File metadataFile) {

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(metadataFile));

			List<IMetadataNameValueParser> metadataNameValueParserList = new ArrayList<IMetadataNameValueParser>();

			String[] line;
			for (int i=0; (line = reader.readNext()) != null; i++) {
				if(i==0){
					for(int j=0; j<line.length; j++){
						metadataNameValueParserList.add(MetadataNameValueParserFactory.getParser(line[j]));
					}

				}else{


					String keyValue = null;
					List<MetadataTemplateAndValues> mtavs = new ArrayList<MetadataTemplateAndValues>();
					for(int j=0; j<line.length; j++){
						IMetadataNameValueParser mnvp = metadataNameValueParserList.get(j);
						if(mnvp.isMetadata()){
							String templateName = mnvp.getTemplateName();
							Metadata md = new Metadata();
							boolean isInList = false;
							for(MetadataTemplateAndValues mtav : mtavs){
								if(mtav.getMetadataTemplateName().equals(templateName)){
									md=mtav.getMetadataValues();
									isInList = true;
								}
							}

							Metadata parsedMd = mnvp.getMetaData(line[j]);

							if(!isInList){
								MetadataTemplateAndValues mtav = new MetadataTemplateAndValues();
								mtav.setMetadataTemplateName(templateName);
								mtav.setMetadataValues(parsedMd);
								mtavs.add(mtav);
							}else{
								for(String propertyPath : parsedMd.getPropertyPaths()){
									md.add(propertyPath, parsedMd.get(propertyPath));
								}

							}
						}else{
							if(mnvp.getAttributeName().equals("file_path")){

								keyValue = new File(line[j]).getPath();
							}
						}

					}
					theMap.put(keyValue, mtavs);
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

}
