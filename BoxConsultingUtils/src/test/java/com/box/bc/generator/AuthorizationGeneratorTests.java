package com.box.bc.generator;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;

public class AuthorizationGeneratorTests {
	public static Logger logger = Logger.getLogger(AuthorizationGeneratorTests.class); 
	
	@Test
	public void testDefaultBoxConfigFile(){
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		System.setProperty("boxconfigfile", "");
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));

		try {
			AuthorizationGenerator.getAppEnterpriseAPIConnection();
			fail("Should not have found a file because the default does not exist");
		} catch (AuthorizationException e) {
			logger.error(e.getMessage());
		}
	}

	@Test
	public void testBoxConfigFileSystemProperty() {
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		System.setProperty("boxconfigfile", "./config/bcu_config.json");
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		
		try {
			AuthorizationGenerator.getAppEnterpriseAPIConnection();
		} catch (AuthorizationException e) {
			logger.error(e.getMessage(),e);
			fail("Should have found a file named bcu_config.json");

		}

		
	}

	@Test
	public void testBoxConfigFileSystemPropertyWrongName() {
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		System.setProperty("boxconfigfile", "notthere.json");
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		
		try {
			AuthorizationGenerator.getAppEnterpriseAPIConnection();
			fail("Should not have found a file because the file named notthere.json does not exist");

		} catch (AuthorizationException e) {
			logger.error(e.getMessage());

		}

		
	}


	@Test
	public void testBoxConfigFileValidateName() {
		String intendedFileName = "isthere.json";
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		System.setProperty("boxconfigfile", intendedFileName);
		logger.info("boxconfigfile=" + System.getProperty("boxconfigfile"));
		
			String configFile = AuthorizationGenerator.getConfigFile();
			assertTrue(configFile.equals(intendedFileName));

		
	}
}
