package com.box.bc.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class allows for a standard implementation of loading a Properties file
 * that resides on the application's classpath
 * 
 * @author Peter Matte - Box Sr Solution Architect
 *
 */
public class PropertiesUtil {
	private static Logger logger = Logger.getLogger(PropertiesUtil.class);

	/**
	 * This method gets an instance of a java.util.Properties from a file that exists on
	 * the classpath of the application.
	 * 
	 * @param fileName - file name or path and file name to the desired property file to be loaded
	 * @return Instance of java.util.Properties with the properties from the specified file loaded
	 */
	public static Properties getPropertiesFromFile(String fileName){
		Properties properties = new Properties();
		try {
			logger.info("Properties File Name: " + fileName);
			InputStream stream = (new PropertiesUtil()).getClass().getClassLoader().getResourceAsStream(fileName);//.getResourceAsStream(fileName);
			properties.load(stream);
		} catch (IOException e) {
			logger.error("ERROR: " + e.getMessage());
		}finally{
		
		}
		return properties;
	}
}
