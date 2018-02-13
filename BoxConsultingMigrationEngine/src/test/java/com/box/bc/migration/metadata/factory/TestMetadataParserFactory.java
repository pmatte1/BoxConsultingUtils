package com.box.bc.migration.metadata.factory;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.box.bc.migration.metadata.IMetadataParser;


public class TestMetadataParserFactory {
	private static Logger logger = Logger.getLogger(TestMetadataParserFactory.class);

	@Test
	public void test1NoFile() {
		IMetadataParser metaDataParser = MetadataParserFactory.getMetadataParser();
		logger.info("Parser Type: " + metaDataParser.getClass());
		assertTrue(metaDataParser instanceof com.box.bc.migration.metadata.NoMetadataAppliedParser);
	}

	@Test
	public void test2FileNoProperty() {
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL[] urls = classLoader.getURLs();
		File file = null;
		for(URL url : urls){
			logger.info(url.getPath());
			if(!url.getPath().endsWith(".jar") && !url.getPath().contains("test-classes")){
				logger.debug("Creating properties file");
				file = new File(url.getPath() + "migrationEngine.properties");
				if(!file.exists()){
					try {
						logger.debug("Creating NEW properties file");

						file.createNewFile();
						logger.debug("Created NEW properties file");
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}
				break;
			}
		}
		

		IMetadataParser metaDataParser = MetadataParserFactory.getMetadataParser();
		logger.info("Parser Type: " + metaDataParser.getClass());
		file = new File(file.toURI());
		
		if(file != null){
			logger.info("Cleaning File");
			logger.info("Deleted Successfully: " + file.delete());
			logger.info("File Cleaned");
		}

		assertTrue(metaDataParser instanceof com.box.bc.migration.metadata.NoMetadataAppliedParser);
	}

	@Test
	public void test3FileHasProperty() {
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL[] urls = classLoader.getURLs();
		File file = null;
		for(URL url : urls){
			logger.info(url.getPath());
			if(!url.getPath().endsWith(".jar") && !url.getPath().contains("test-classes")){
				logger.debug("Creating properties file");
				file = new File(url.getPath() + "migrationEngine.properties");
				if(!file.exists()){
					try {
						logger.debug("Creating NEW properties file");

						file.createNewFile();
						logger.debug("Created NEW properties file");
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}
				break;
			}
		}
		
		if(file != null){
			try {
				FileWriter fw = new FileWriter(file);
				fw.write("metadata.parser.class=com.box.bc.migration.metadata.TestMetadataParser");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		IMetadataParser metaDataParser = MetadataParserFactory.getMetadataParser();
		logger.info("Parser Type: " + metaDataParser.getClass());
		file = new File(file.toURI());
		
		if(file != null){
			logger.info("Cleaning File");
			file.delete();
			logger.info("File Cleaned");
		}
		assertTrue(metaDataParser instanceof com.box.bc.migration.metadata.TestMetadataParser);
	}
}
