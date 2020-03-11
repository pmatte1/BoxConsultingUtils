package com.box.bc.migration.metadata.application;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.bc.migration.FolderCreationFromFile;
import com.box.bc.migration.logging.FolderCreationResults;
import com.box.bc.migration.logging.ResultsPublisher;
import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.metadata.factory.MetadataParserFactory;
import com.box.bc.migration.util.WalkerDunlopSingletonUtil;
import com.box.bc.user.AppUserManager;
import com.box.bc.util.FolderUtil;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.Metadata;

public class OutputRun2Info {
	private static Logger logger = Logger.getLogger(OutputRun2Info.class);

	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();

	public OutputRun2Info(){

	}

	public static void main(String[] args) {
		FolderCreationFromFile.setPermissions();
		OutputRun2Info acmt = new OutputRun2Info();
		acmt.runApplicationLogic();
		FolderCreationFromFile.removePermissions();
	}

	public void runApplicationLogic(){
		//TODO Move Output File to configuration
		SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY-MM-dd_HH_mm_ss");
		String timeStamp = dateFormatter.format(new Date());

		String[] alphabet = new String[]{"0-9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		for(String alphaFolderName : alphabet){
			String folderId = WalkerDunlopSingletonUtil.getInstance().getConfidentialFolderID(alphaFolderName);
			String closedAlphaFolderId = WalkerDunlopSingletonUtil.getInstance().getClosedFolderID(alphaFolderName);
			try {
				BoxFolder confAlphaFolder = new BoxFolder(AppUserManager.getInstance().getAppUser().getAPIConnection(), folderId);
				BoxFolder closedAlphaFolder = new BoxFolder(AppUserManager.getInstance().getAppUser().getAPIConnection(), closedAlphaFolderId);

				Iterable<BoxItem.Info> listConfFolders = confAlphaFolder.getChildren();
				for(BoxItem.Info itemInfo : listConfFolders){
					if(itemInfo.getType().equals("folder")){
						logger.info("Outputting folder: " + itemInfo.getID() + ": " + itemInfo.getName());
						FolderCreationResults fcr = new FolderCreationResults();
						BoxFolder.Info folderInfo = (BoxFolder.Info)itemInfo;
						fcr.setNameFromFile(folderInfo.getName());
						fcr.setFolderName(folderInfo.getName());
						fcr.setConfidentialFolderId(folderInfo.getID());

						BoxFolder closedFolder = FolderUtil.getFolder(closedAlphaFolder, folderInfo.getName());
						if(closedFolder == null){
							logger.error("No Closed Folder with the name " + folderInfo.getName());
						}else{
							fcr.setNonConfidentialFolderId(closedFolder.getInfo().getName());
							for(Metadata allMd : closedFolder.getAllMetadata(new String[0])){

								if(allMd.getTemplateName().equalsIgnoreCase(WalkerDunlopSingletonUtil.getInstance().getMETADATA_TEMPLATE_NAME())){
									try{
										logger.debug("Name: " + allMd.getTemplateName() + " - All Metadata: " + allMd);
										fcr.setTransactionId(Double.toString(allMd.getFloat("/" + WalkerDunlopSingletonUtil.getInstance().getTRANSACTION_NUM_ATTR_NAME())));
										fcr.setLoanNumber(Double.toString(allMd.getFloat("/" + WalkerDunlopSingletonUtil.getInstance().getLOAN_NUM_ATTR_NAME())));
										fcr.setLoanMetadataApplied(true);
									}catch(NullPointerException e){
										
									}catch(Exception e){
										
									}
								}
							}
						}

						fcr.setLinkCreatedSuccessfully(true);
						fcr.setProcessingTimeInMillis(35000L);
						ResultsPublisher.getInstance("./outputRun-"+timeStamp+".csv").publish(fcr);

					}
				}

			} catch (AuthorizationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

	}


}
