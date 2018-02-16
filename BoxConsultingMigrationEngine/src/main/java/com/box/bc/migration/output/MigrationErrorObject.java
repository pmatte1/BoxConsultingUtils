package com.box.bc.migration.output;

import java.io.File;
import java.util.List;

import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.google.gson.Gson;

public class MigrationErrorObject implements OutputWriter{
	
	private File file;
	private String folderId;
	private List<MetadataTemplateAndValues> listMetdataTemplateAndVals;
	private String errorMessage;
	
	public MigrationErrorObject(){
		
	}
	
	public MigrationErrorObject(File file, String folderId, List<MetadataTemplateAndValues> listMetdataTemplateAndVals, String errorMessage){
		this.file=file;
		this.folderId=folderId;
		this.listMetdataTemplateAndVals=listMetdataTemplateAndVals;
		this.errorMessage=errorMessage;
	}
	
	public synchronized String getErrorMessage() {
		return errorMessage;
	}
	public synchronized void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getFolder() {
		return folderId;
	}
	public void setFolder(String folderId) {
		this.folderId = folderId;
	}
	public List<MetadataTemplateAndValues> getListMetdataTemplateAndVals() {
		return listMetdataTemplateAndVals;
	}
	public void setListMetdataTemplateAndVals(
			List<MetadataTemplateAndValues> listMetdataTemplateAndVals) {
		this.listMetdataTemplateAndVals = listMetdataTemplateAndVals;
	}
	public String getHeader() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getLine() {

		return "\"" + file.getPath() + "\",\"" + 
				folderId + "\",\"" + 
				new Gson().toJson(listMetdataTemplateAndVals) + "\",\"" + 
				purgeCarriageReturns(errorMessage) + "\"";
	}

	private String purgeCarriageReturns(String errorMessage2) {
		StringBuffer sb = new StringBuffer();
		String returnVal = errorMessage2.replaceAll("\\n", "");
		returnVal = returnVal.replaceAll("\\r", "");
		
		return returnVal;
	}
	
	

}
