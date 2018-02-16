package com.box.bc.migration.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

public class MigrationOutputter {
	private static Logger logger = Logger.getLogger(MigrationOutputter.class);

	private static MigrationOutputter outputter = null;
	private static String errorOutputFile = "C:/temp/errorOutput.csv";
	private String successOutputFile = "C:/temp/successOutput.xml";

	private MigrationOutputter() {

	}

	public static MigrationOutputter getInstance(){
		if(outputter  == null){
			outputter = new MigrationOutputter();
			File errorFile =new File(errorOutputFile); 
			if(errorFile.exists()){
				errorFile.delete();
			}else{

				errorFile.getParentFile().mkdirs();
			}
		}

		return outputter;
	}

	public synchronized void write(MigrationErrorObject meo){
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(errorOutputFile),true);
			fw.write(meo.getLine());
			fw.write('\r');
			fw.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if(fw != null){
			try {
				fw.close();
			} catch (IOException e) {}
		}
	}

	public synchronized void write(MigrationSuccessObject mso){

	}

}
