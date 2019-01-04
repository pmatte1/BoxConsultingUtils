package com.box.bc.migration.metadata;

import org.apache.log4j.Logger;

public class PipeDelimitedParser extends CSVParser {
	private static Logger logger = Logger.getLogger(PipeDelimitedParser.class);
	

	public PipeDelimitedParser() {
		logger.info("Using Instance of Pipe Delimited Parser");
	}


	@Override
	protected char getSeparator() {
		// Returns a Pipe Delimited Separator to parse the file
		return '|';
	}

}
