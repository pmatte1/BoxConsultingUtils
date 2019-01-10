package com.box.bc.migration;

import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.box.bc.generator.AuthorizationGenerator;
import com.box.bc.util.PropertiesUtil;
import com.box.bc.util.StopWatch;
import com.box.bc.util.StopWatch.StopWatchNeverStartedException;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxCollaboration;
import com.box.sdk.BoxCollaboration.Role;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

public class UpdateCollaborationsWithViewPathCollab {
	private static Logger logger = Logger.getLogger(UpdateCollaborationsWithViewPathCollab.class);
	private static String propertiesFile = "applyviewpathcollab.properties";
	private static boolean setTo=Boolean.parseBoolean(getProperties().getProperty("setto","true"));


	public static void main(String[] args) {
		StopWatch sw = new StopWatch();
		try{
			Properties props = getProperties(); 
			String user = props.getProperty("user");
			String folderId = props.getProperty("folderId");
			if(user != null && user.trim().length()>0 && folderId != null && folderId.trim().length()>0){

				BoxAPIConnection api = AuthorizationGenerator.getAPIConnection(user);
				BoxFolder folder = new BoxFolder(api, folderId);

				sw.start();
				updateCollaborations(folder);
				sw.stop();
			}else{
				logger.error("No User and/or Folder Information provided in " + propertiesFile + ".  Please ensure this file is on the classpath.");
				logger.error("User     : " + user);
				logger.error("Folder ID: " + folderId);
			}
		}catch(BoxAPIException e){
			logger.error(e.getResponseCode() + ":" + e.getResponse());
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}

		try {

			logger.warn("Processing Took " + sw.getElapsedTime()/1000L + " seconds");
		} catch (StopWatchNeverStartedException e) {
		}
	}

	protected static Properties getProperties() {
		Properties props = PropertiesUtil.getPropertiesFromFile(propertiesFile); 
		return props;
	}

	private static void updateCollaborations(BoxFolder folder) {
		Collection<BoxCollaboration.Info> colFolderCollabs = folder.getCollaborations();
		logger.info("Updating " + folder.getInfo().getName() + "...");
		int collabCount = 0;
		for(BoxCollaboration.Info folderCollab : colFolderCollabs){
			if(folderCollab.getItem().getID().equals(folder.getID())){

				Role role = folderCollab.getRole();
				folderCollab.setCanViewPath(setTo);
				folderCollab.setRole(role);
				try{
					StopWatch sw = new StopWatch();
					sw.start();
					folderCollab.getResource().updateInfo(folderCollab);
					collabCount++;
					sw.stop();
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}

			}
		}
		logger.info("Set " + collabCount + " collaborations to " + setTo + " on " + folder.getInfo().getName());

		Iterable<BoxItem.Info> folderInfos = folder.getChildren();
		for(BoxItem.Info folderInfo : folderInfos){
			if(folderInfo.getResource() instanceof BoxFolder){
				updateCollaborations((BoxFolder)folderInfo.getResource());
			}
		}

	}

}
