package com.box.bc.util;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.user.AppUserManager;
import com.box.bc.util.StopWatch.StopWatchException;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxCollaboration;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

public class FolderUtil {
	public static Logger logger = Logger.getLogger(FolderUtil.class);
	public static BoxFolder getOrCreateFolderAtRoot(String folderName) throws AuthorizationException{
		return getOrCreateFolder("0", folderName);
	}

	public static BoxFolder getOrCreateFolder(String boxFolderId, String folderName) throws AuthorizationException{
		BoxAPIConnection api = AppUserManager.getInstance().getAppUser().getAPIConnection();
		if(boxFolderId == null){
			boxFolderId = "0";
		}

		return getOrCreateFolder(new BoxFolder(api, boxFolderId),  folderName);


	}

	public static BoxFolder createFolder(String boxFolderId, String folderName) throws AuthorizationException{
		BoxAPIConnection api = AppUserManager.getInstance().getAppUser().getAPIConnection();
		boolean addCollab = false;
		if(boxFolderId == null){
			boxFolderId = "0";
		}

		if(boxFolderId.equals("0")){
			addCollab = true;
		}

		return createFolder(new BoxFolder(api, boxFolderId),  folderName, addCollab);


	}

	public static BoxFolder createFolder(BoxFolder boxFolder, String folderName) throws AuthorizationException{
		return createFolder(boxFolder, folderName, false);
	}

	public static BoxFolder createFolder(BoxFolder boxFolder, String folderName, boolean addCollab) throws AuthorizationException{
		BoxFolder retBoxFolder = null;
		StopWatch sw = new StopWatch();
		try{
			sw.start();
			retBoxFolder = boxFolder.createFolder(folderName).getResource(); 
			sw.stop();
			logger.info("BoxFolder.createFolder() elapsed time: " + sw.getElapsedTime());

			if(addCollab){
				//Add the App User group as a Collaborator On the Folder
				sw.start();
				retBoxFolder.collaborate(AppUserManager.getInstance().getBoxGroup().getResource(), BoxCollaboration.Role.CO_OWNER);
				sw.stop();
				logger.info("BoxFolder.collaborate() elapsed time: " + sw.getElapsedTime());
			}

		}catch(BoxAPIException e){
			if(e.getResponseCode()==409){
				//TODO Get the ID from the response body and return instance of BoxFolder
				logger.warn("Error creating " + folderName + ": " + e.getResponseCode() + ": " + e.getResponse());
			}
		} catch (StopWatchException e) {
			logger.error(e.getMessage(), e);
		}
		return retBoxFolder;

	}


	public static BoxFolder getOrCreateFolder(BoxFolder boxFolder, String folderName) throws AuthorizationException{
		BoxFolder retBoxFolder = null;
		try{
			retBoxFolder = getFolder(boxFolder, folderName);

			if(retBoxFolder == null){
				retBoxFolder = createFolder(boxFolder, folderName, boxFolder.getID().equals("0"));
			}

		}catch(BoxAPIException e){
			if(e.getResponseCode()==409){
				//TODO Get the ID from the response body and return instance of BoxFolder
			}
		}
		return retBoxFolder;

	}

	public static BoxFolder getFolder(BoxFolder boxFolder, String folderName){
		Iterator<BoxItem.Info> iterFolderInfo = boxFolder.getChildren().iterator();
		while(iterFolderInfo.hasNext()){
			BoxItem.Info boxItemInfo = iterFolderInfo.next();
			if(boxItemInfo.getName().equals(folderName)){
				try{
					return (BoxFolder)boxItemInfo.getResource();
				}catch(ClassCastException e){}
			}
		}

		return null;
	}

	public static String applyCascadingMetadataPolicy(BoxFolder boxFolder, String templateName){
		
		return null;
	}
}
