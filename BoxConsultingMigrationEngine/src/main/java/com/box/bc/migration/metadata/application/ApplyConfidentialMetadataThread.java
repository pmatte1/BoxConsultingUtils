package com.box.bc.migration.metadata.application;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.bc.migration.FolderCreationFromFile;
import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.metadata.factory.MetadataParserFactory;
import com.box.bc.migration.util.WalkerDunlopSingletonUtil;
import com.box.bc.user.AppUserManager;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxMetadataCascadePolicy;
import com.box.sdk.BoxUser;
import com.box.sdk.Metadata;

public class ApplyConfidentialMetadataThread {
	private static Logger logger = Logger.getLogger(ApplyConfidentialMetadataThread.class);

	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();

	public ApplyConfidentialMetadataThread(){

	}

	public static void main(String[] args) {
		FolderCreationFromFile.setPermissions();
		ApplyConfidentialMetadataThread acmt = new ApplyConfidentialMetadataThread();
		acmt.runApplicationLogic();
		FolderCreationFromFile.removePermissions();
	}

	public void runApplicationLogic(){
		String[] alphabet = new String[]{"0-9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		for(String alphaFolderName : alphabet){
			String folderId = WalkerDunlopSingletonUtil.getInstance().getConfidentialFolderID(alphaFolderName);
			try {
				BoxFolder confAlphaFolder = new BoxFolder(AppUserManager.getInstance().getAppUser().getAPIConnection(), folderId);
				Iterable<BoxItem.Info> listConfFolders = confAlphaFolder.getChildren();
				String[] fieldList = new String[]{"id","name","metadata.enterprise." + WalkerDunlopSingletonUtil.getInstance().getFOLDER_METADATA_TEMPLATE_NAME()};
				for(BoxItem.Info itemInfo : listConfFolders){
					try{
						if(itemInfo.getType().equals("folder")){
							BoxFolder folder = new BoxFolder(AppUserManager.getInstance().getAppUser().getAPIConnection(), itemInfo.getID());
							BoxFolder.Info folderInfo = folder.getInfo(fieldList);
							//						BoxUser user = BoxUser.getCurrentUser(folder.getAPI());
							//						logger.info("User: " + user.getID() + ": " + user.getInfo(new String[0]).getName());
							logger.info("Setting for: " + folderInfo.getName());
							String structureId = null;
							try{
							Metadata md = folderInfo.getMetadata(WalkerDunlopSingletonUtil.getInstance().getFOLDER_METADATA_TEMPLATE_NAME(), "enterprise");
							structureId = md.getString("/" + WalkerDunlopSingletonUtil.getInstance().getStructureIdAttributeName());
							}catch(NullPointerException npe){
								logger.error("Null Pointer on getting metadata.  Will attempt to apply.");
							}
							
							if(structureId == null || !structureId.trim().equals("00")){
								FolderCreationApplicationThread fcat = new FolderCreationApplicationThread("dummy", "dummy", new ArrayList<MetadataTemplateAndValues>());
								fcat.updateConfidentialFolderIds(folderInfo);
								logger.info("Successfully processed: " + folderInfo.getID() + ": " + folderInfo.getName());
								
								
								try{
									BoxMetadataCascadePolicy.Info info = folder.addMetadataCascadePolicy("enterprise", WalkerDunlopSingletonUtil.getInstance().getMETADATA_TEMPLATE_NAME());
									info.getResource().forceApply("none");
								}catch(BoxAPIException e){
									Iterable<BoxMetadataCascadePolicy.Info> iterableInfo = folder.getMetadataCascadePolicies(new String[0]);
									for(BoxMetadataCascadePolicy.Info info : iterableInfo){
										if(info.getTemplateKey().equals(WalkerDunlopSingletonUtil.getInstance().getMETADATA_TEMPLATE_NAME())){
											try{
												info.getResource().forceApply("none");
											}catch(BoxAPIException ex){
												
											}
										}
									}
								}

							}
						}
					}catch(Exception e){
						logger.error(e.getMessage(), e);
					}
				}

			} catch (AuthorizationException e) {
				logger.error(e.getMessage(), e);
			}


		}

	}


}
