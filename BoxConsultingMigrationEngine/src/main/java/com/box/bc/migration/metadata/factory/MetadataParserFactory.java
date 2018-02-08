package com.box.bc.migration.metadata.factory;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.NoMetadataAppliedParser;
import com.box.bc.util.PropertiesUtil;

public class MetadataParserFactory {
	private static Logger logger = Logger.getLogger(MetadataParserFactory.class);

	public static IMetadataParser getMetadataParser() {
		Properties props = PropertiesUtil.getPropertiesFromFile("migrationEngine.properties");
		if(props != null){
			String metadataParserClass = props.getProperty("metadata.parser.class");
			if(metadataParserClass != null){
				try{
					return (IMetadataParser)Class.forName(metadataParserClass).newInstance();
				}catch(ClassNotFoundException e){
					logger.error("Unable to find the class " + metadataParserClass + " found in the migrationEngine.properties file.  Please verify the configuration.", e);
				} catch (InstantiationException e) {
					logger.error("Unable to instantiate the class " + metadataParserClass + " found in the migrationEngine.properties file.  Please verify the configuration.", e);
				} catch (IllegalAccessException e) {
					logger.error("Unable to access the class " + metadataParserClass + " found in the migrationEngine.properties file.  Please verify the configuration.", e);
				}
			}else{
				logger.warn("There is no metadata.parser.class property in the migrationEngine.properties file.  Will not apply metadata.");
			}
		}else{
			logger.warn("There is no migrationEngine.properties file.  Will not apply metadata.");
		}
		
		//Given no other metadataParser was defined, we will default to
		//the NoMetadataAppliedParser() which will never provide metadata
		return new NoMetadataAppliedParser();
		
	}
}
