package com.box.bc.migration;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.box.bc.generator.AuthorizationGenerator;
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


	public static void main(String[] args) {
		StopWatch sw = new StopWatch();
		try{
			BoxAPIConnection api = AuthorizationGenerator.getAPIConnection("pmatte+demo@box.com");
			BoxFolder folder = new BoxFolder(api, "62934970138");

			sw.start();
			updateCollaborations(folder);
			sw.stop();
		}catch(BoxAPIException e){
			logger.error(e.getResponseCode() + ":" + e.getResponse());
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}

		try {
			logger.warn("Processing Took " + sw.getElapsedTime()/1000L + " seconds");
		} catch (StopWatchNeverStartedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static boolean setTo=true;
	private static void updateCollaborations(BoxFolder folder) {
		Collection<BoxCollaboration.Info> colFolderCollabs = folder.getCollaborations();
		for(BoxCollaboration.Info folderCollab : colFolderCollabs){
			//if(folderCollab.getCanViewPath() != setTo){
				Role role = folderCollab.getRole();
				folderCollab.setCanViewPath(setTo);
				folderCollab.setRole(role);
				try{
					StopWatch sw = new StopWatch();
					sw.start();
					folderCollab.getResource().updateInfo(folderCollab);
					sw.stop();
					logger.warn("Call to update " + folder.getInfo().getName() + " took " + sw.getElapsedTime() + " ms");
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}

			//}
		}

		Iterable<BoxItem.Info> folderInfos = folder.getChildren();
		for(BoxItem.Info folderInfo : folderInfos){
			if(folderInfo.getResource() instanceof BoxFolder){
				updateCollaborations((BoxFolder)folderInfo.getResource());
			}
		}

	}

}
