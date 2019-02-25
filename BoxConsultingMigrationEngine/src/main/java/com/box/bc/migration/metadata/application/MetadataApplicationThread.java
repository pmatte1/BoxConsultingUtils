package com.box.bc.migration.metadata.application;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.util.MetadataUtil;
import com.box.bc.user.AppUserManager;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;

public class MetadataApplicationThread extends Thread {
	private static Logger logger = Logger.getLogger(MetadataApplicationThread.class);

	private String key;

	private List<MetadataTemplateAndValues> listTemplateAndValues;

	public MetadataApplicationThread(String name,
			String key, List<MetadataTemplateAndValues> listTemplateAndValues) {
		this.setName(name);
		this.key=key;
		this.listTemplateAndValues=listTemplateAndValues;


	}

	public void run(){

		BoxFile boxFile = getFile(key);
		if(boxFile != null){
			try{
				new MetadataUtil().applyMetadataTemplateAndVals(boxFile, listTemplateAndValues);
				logger.debug("Added Metadata to " + key);
			}catch(BoxAPIException e){
				logger.error(e.getResponse(), e);
			}
		}


	}

	private BoxFile getFile(String key2) {
		//Check if the key is a Box File ID
		if(StringUtils.isNumeric(key2)){
			try {
				return new BoxFile(AppUserManager.getInstance().getAppUser().getAPIConnection(), key2);
			} catch (AuthorizationException e) {
				logger.error("Could Not Retrieve Authorization Info for Box File ID: " + key2 + " (" + e.getMessage() + ")");			
			} catch (BoxAPIException e){
				logger.error("Error Retrieving File: " + e.getResponseCode() + "-" + e.getResponse());
			} catch (Exception e){
				logger.error(e.getMessage(), e);
			}
		}else if(key2.contains("/") || key2.contains("\\")){
			//TODO: Get by Path
		}

		logger.warn("The value " + key2 + " is neither a path, nor an ID.  Please be sure the format of the entry is correct.");
		return null;
	}




}
