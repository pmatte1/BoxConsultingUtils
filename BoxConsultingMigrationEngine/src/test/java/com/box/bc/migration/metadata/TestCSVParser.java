package com.box.bc.migration.metadata;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.box.bc.util.PropertiesUtil;

public class TestCSVParser {
	private static Logger logger = Logger.getLogger(TestCSVParser.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		IMetadataParser csvParser = new com.box.bc.migration.metadata.CSVParser();
		URL fileUri = ClassLoader.getSystemClassLoader().getResource("testMigrationParser.csv");
		assertTrue(csvParser.isMetadataFile(new File(fileUri.getPath())));
		
		//Verify there are 2 rows
		assertTrue(((CSVParser)csvParser).theMap.size()==2);
		
		List<MetadataTemplateAndValues> mdTemplateAndValues = csvParser.getMetadata(new File("C:/TEST/rowA.doc"));
		
		//Should contain custom metadata and a demoTestTemplate
		assertTrue(mdTemplateAndValues.size() == 2);
		
		
		//fail("Not yet implemented");
	}

}
