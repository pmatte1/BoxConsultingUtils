package com.box.bc.migration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.metadata.application.MetadataApplicationThread;
import com.box.bc.migration.metadata.factory.MetadataParserFactory;
import com.box.bc.migration.metrics.ThreadMetrics;
import com.box.bc.migration.util.MemoryMonitor;
import com.box.bc.user.AppUserManager;
import com.box.bc.util.FolderUtil;
import com.box.bc.util.PropertiesUtil;
import com.box.bc.util.StopWatch;
import com.box.bc.util.StopWatch.StopWatchException;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxCollaboration;
import com.box.sdk.BoxGlobalSettings;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxCollaboration.Role;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxUser;
import com.box.sdk.MetadataTemplate;
import com.box.sdk.MetadataTemplate.Field;

public class ApplyMetadata extends Thread {
	private static Logger logger = Logger.getLogger(ApplyMetadata.class);

	//Configurations per execution
	protected static final int NUM_CONCURRENT_PROCESSORS = Integer.parseInt(getProperties().getProperty("NUM_CONCURRENT_PROCESSORS", "10"));
	protected static double MAX_QUEUE_SIZE = NUM_CONCURRENT_PROCESSORS*1;

	//END - Configurations per execution

	//Runtime Variables
	protected static ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(NUM_CONCURRENT_PROCESSORS);
	private boolean isRunning = false;

	protected ThreadMetrics threadMetrics = new ThreadMetrics();
	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();
	protected static List<String> folderIdsWithCollabAdded = new ArrayList<String>();
	//END - Runtime Variables


	public static void main(String args[]){

		try {
			logger.info("Setting up App Users...");
			AppUserManager.getInstance();
			logger.info("Setting up App Users Complete");
		} catch (AuthorizationException e1) {
			logger.error(e1.getMessage(), e1);
		}

		//This will add the necessary permissions for the App User group

		setPermissions();

		List<ApplyMetadata> mtuList = new ArrayList<ApplyMetadata>();

		try{
			ApplyMetadata mtu = new ApplyMetadata("Metadata Application");
			mtuList.add(mtu);
			mtu.start();
		}catch(Exception e){

		}

		MemoryMonitor memMon = new MemoryMonitor(1000);
		memMon.start();

		//This section simply monitors the threads, and periodically outputs statistics for each thread
		boolean keepRunning = true;
		while(keepRunning){
			try {
				Thread.sleep(60000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			keepRunning = false;

			for(ApplyMetadata mtu : mtuList){

				if(!mtu.getState().equals(State.TERMINATED)){
					keepRunning = true;
				}
			}
			logger.warn("*************************************************************************************************");
			logger.warn("Active Thread Count : " + ((ThreadPoolExecutor)executor).getActiveCount());
			logger.warn("Queue Size          : " + ((ThreadPoolExecutor)executor).getQueue().size());
			logger.warn("Pool Size           : " + ((ThreadPoolExecutor)executor).getPoolSize());
			logger.warn("Task Count          : " + ((ThreadPoolExecutor)executor).getTaskCount());
			logger.warn("Completed Task Count: " + ((ThreadPoolExecutor)executor).getCompletedTaskCount());
			logger.warn("*************************************************************************************************");
		}

		//This will remove the added permissions for the App User group
		removePermissions();

		memMon.stopRunning();
		executor.shutdown();

	}

	protected static void setPermissions() {
		//Check if there is a Migration User Specified
		String[] migrationUsers = getMigrationUsers();
		logger.info("Checking for Migration User...");
		if(migrationUsers != null && migrationUsers.length>0){
			for(String migrationUser : migrationUsers){
				logger.info("Migration User found.  Updating permissions for " + migrationUsers);
				//If there is, then add the AppUserManager group to all top level folders that the user owns
				try {

					BoxAPIConnection migrationUserApi = AuthorizationGenerator.getAPIConnection(migrationUser);
					List<BoxFolder> ownedFolders = getOwnedFolders(migrationUserApi);

					for(BoxFolder folder: ownedFolders){
						try{
							folder.collaborate(AppUserManager.getInstance().getBoxGroup().getResource(), Role.CO_OWNER, false, false);
							folderIdsWithCollabAdded.add(folder.getID());
							logger.info("Permissions Updated for " + folder.getInfo().getName());
						}catch(BoxAPIException e){
							if(e.getResponseCode() != 409){
								logger.warn(e.getResponseCode() + "-" + e.getResponse());
							}else{
								logger.info("Permissions Updated for " + folder.getInfo().getName());
							}
						}
					}

				} catch (AuthorizationException e) {
					logger.error(e.getMessage(), e);			
				}
			}
		}else{
			try {
				logger.info("No Migration User Specified.  Permission will need to be granted manually for the group " + AppUserManager.getInstance().getBoxGroup().getName());
			} catch (AuthorizationException e) {
				logger.error(e.getMessage());
			}

		}

	}

	protected static List<BoxFolder> getOwnedFolders(
			BoxAPIConnection migrationUserApi) {

		logger.info("Getting Owned Folders");
		List<BoxFolder> ownedFolders = new ArrayList<BoxFolder>();

		BoxFolder baseFolder = new BoxFolder(migrationUserApi, "0");
		Iterable<BoxItem.Info> children = baseFolder.getChildren(new String[]{"id","name","owned_by"});
		BoxUser bu = BoxUser.getCurrentUser(migrationUserApi);

		for(BoxItem.Info child:children){
			if(child.getResource() instanceof BoxFolder){
				//BoxFolder updatedChild = (BoxFolder) child.getResource();//new BoxFolder(migrationUserApi, child.getID());
				logger.debug("Owner: " + child.getOwnedBy().getName() + " ID: " + child.getOwnedBy().getID());
				if(child.getOwnedBy()!=null && child.getOwnedBy().getID().equals(bu.getInfo().getID())){
					ownedFolders.add((BoxFolder)child.getResource());
				}
			}
		}


		logger.info("Found " + ownedFolders.size() + " folders owned by " + bu.getInfo(new String[0]).getName());
		return ownedFolders;
	}

	protected static void removePermissions(){
		if(getMigrationUsers()!=null){

			try {
				BoxAPIConnection appApi = AuthorizationGenerator.getAppEnterpriseAPIConnection();
				
				for(String folderId : folderIdsWithCollabAdded){
					BoxFolder folder = new BoxFolder(appApi, folderId);
					String folderOwnerId = folder.getInfo().getOwnedBy().getID();
					
					BoxAPIConnection migrationUserApi = AuthorizationGenerator.getAPIConnection(folderOwnerId);
					folder = new BoxFolder(migrationUserApi, folderId);
					Collection<BoxCollaboration.Info> colCollabInfo = folder.getCollaborations();
					for(BoxCollaboration.Info collabInfo: colCollabInfo){
						if(collabInfo.getAccessibleBy().getName().equals(AppUserManager.getInstance().getBoxGroup().getName())){
							collabInfo.getResource().delete();
						}
					}

				}
			} catch (AuthorizationException e) {
				logger.error(e.getMessage(), e);			
			}
		}

	}


	protected static String[] getMigrationUsers() {
		String[] returnVals = new String[0];
		if(getProperties().getProperty("migrationuser")!=null){
			return new String[]{getProperties().getProperty("migrationuser")};
		}else{		
			List<String> usersNamesList = new ArrayList<String>();
			for(int i=0; getProperties().getProperty("migrationuser." + i) != null; i++){
				usersNamesList.add(getProperties().getProperty("migrationuser." + i));
			}
			returnVals = usersNamesList.toArray(new String[usersNamesList.size()]);
		}

		return returnVals;
	}

	public ApplyMetadata(String string) {
		super(string);
	}

	public void run(){
		isRunning=true;
		StopWatch sw = new StopWatch();
		sw.start();

		try {
			verifyMetadataTemplate();
			logger.debug("Metadata File: " + getMetadataFile());
			applyMetadata(getMetadataFile());
			sw.stop();
			this.threadMetrics.setProcessingTime(sw.getElapsedTime());
			logger.info("Uploaded " + this.getName().trim() + " in " + (sw.getElapsedTime()/1000) + " seconds (" + (sw.getElapsedTime()/1000/60) + " minutes)");
		} catch (StopWatchException e) {

		}

		isRunning=false;

	}

	protected void verifyMetadataTemplate() {
		boolean moreTemplates = true;
		for(int i=0; moreTemplates; i++){
			String templateName = getProperties().getProperty("template."+i+".name", null);

			if(templateName != null){
				String templateKey = getProperties().getProperty("template."+i+".templatekey", templateName.toLowerCase());
				try {
					//Create the template if it does not exist
					BoxAPIConnection api = AuthorizationGenerator.getAppEnterpriseAPIConnection();
					Iterable<MetadataTemplate> templates = MetadataTemplate.getEnterpriseMetadataTemplates(api, new String[0]);

					MetadataTemplate template = null;
					for(MetadataTemplate currentTemplate: templates){
						if(currentTemplate.getTemplateKey().equals(templateKey)){
							template = currentTemplate;
							if(!template.getDisplayName().equals(templateName)){
								logger.error("Name is not correct for the template with key " + templateKey + " .  The name is '" + template.getDisplayName() + "' but has '" + templateName + "' in the configuration file.  Please update manually.");
							}
							break;
						}
					}

					if(template == null){
						List<Field> fields = new ArrayList<Field>();
						boolean moreAttributes = true;
						for(int j=0; moreAttributes; j++){
							Field fieldDef = new Field();
							fieldDef.setKey(getProperties().getProperty("template."+ i +".attribute." + j + ".key", null));
							fieldDef.setDisplayName(getProperties().getProperty("template."+ i +".attribute." + j + ".name", null));
							fieldDef.setType(getProperties().getProperty("template."+ i +".attribute." + j + ".type", null));
							fieldDef.setDescription(getProperties().getProperty("template."+ i +".attribute." + j + ".desc", ""));
							if(fieldDef.getKey()!= null && fieldDef.getDisplayName()!=null){
								fields.add(fieldDef);
							}else{
								moreAttributes=false;
							}

						}
						template = MetadataTemplate.createMetadataTemplate(api, "enterprise", templateKey, templateName, false, fields);
					}else{
						logger.info("Template already exists, skipped creation");
					}
				} catch (AuthorizationException e) {
					logger.error(e.getMessage(),e);
				}
			}else{
				moreTemplates=false;
			}
		}

	}

	protected String getMetadataFile() {
		// Pull this from a properties file
		Properties applyMetadataProps = getProperties();

		return applyMetadataProps.getProperty("metadatafile",null);
	}

	protected static Properties getProperties(){
		return PropertiesUtil.getPropertiesFromFile("metadata.properties");
	}

	private void applyMetadata(String metadataFile) {

		while(((ThreadPoolExecutor)executor).getQueue().size()>= MAX_QUEUE_SIZE){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
			}
		}

		metadataParser.load(new File(metadataFile));
		Map<String, List<MetadataTemplateAndValues>> theMap = metadataParser.getAllMetadata();
		Set<String> keySet = theMap.keySet();
		for(String key : keySet){
			threadMetrics.addFuture(startApplyMetadataThread(key, theMap.get(key)));
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {}

		boolean keepLooping = true;
		while(keepLooping){
			if(threadMetrics.getAllFutures().size()>0){
				keepLooping=false;
				for(int i=0; i<threadMetrics.getAllFutures().size(); i++){
					logger.debug("threadMetrics.getFuture("+i+").isDone(): " + threadMetrics.getFuture(i).isDone());
					if(!threadMetrics.getFuture(i).isDone()){
						keepLooping = true;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
						break;
					}
				}
			}
		}		

	}


	private int myBackoff = 1;
	private Future<?> startApplyMetadataThread(String key,
			List<MetadataTemplateAndValues> list) {
		double maxPoolSize =((ThreadPoolExecutor)executor).getMaximumPoolSize();
		double maxQueueSize = maxPoolSize*1;

		if(((ThreadPoolExecutor)executor).getQueue().size()>= maxQueueSize){
			try {
				logger.debug("Max Queue Size Reached.  Pausing before retry.");
				Thread.sleep(myBackoff*1000);
			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
			}
			myBackoff=myBackoff*2;
		}else{
			myBackoff=1;
		}
		Thread thread = new MetadataApplicationThread(this.getName() + "-" + System.currentTimeMillis(), key, list);
		thread.setName(this.getName() + "-" + System.currentTimeMillis());
		return executor.submit(thread);

	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String getRateOfUpload() {
		if(threadMetrics.getMsSpentUploading() == 0L){
			return "Nothing Uploaded Yet";
		}
		return ((threadMetrics.getBytesUploaded()/1024)/(threadMetrics.getMsSpentUploading()/1000)) + "kb per second";
	}

	private static String getFileSizeOutput(long bytesUploaded2) {
		int i=0;
		String units = "";
		while(bytesUploaded2>1024){
			bytesUploaded2 = bytesUploaded2/1024;
			i++;
		}

		switch (i){
		case 0: units="bytes";
		break;
		case 1: units="KB";
		break;
		case 2: units="MB";
		break;
		case 3: units="GB";
		break;
		case 4: units="TB";
		break;
		}

		return bytesUploaded2 + " " + units;
	}


}
