package com.box.bc.migration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

public class ApplyMetadata extends Thread {
	private static Logger logger = Logger.getLogger(ApplyMetadata.class);

	//Configurations per execution
	//TODO: Move to Configurations
	protected static final int NUM_CONCURRENT_PROCESSORS = 10;
	protected static double MAX_QUEUE_SIZE = NUM_CONCURRENT_PROCESSORS*1;

	//END - Configurations per execution

	//Runtime Variables
	protected static ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(NUM_CONCURRENT_PROCESSORS);
	//protected String baseBoxFolderId = null;
	private boolean isRunning = false;
	//private File baseFile;

	protected ThreadMetrics threadMetrics = new ThreadMetrics();
	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();
	protected static List<String> folderIdsWithCollabAdded = new ArrayList<String>();
	//END - Runtime Variables


	public static void main(String args[]){
//		BoxGlobalSettings.setConnectTimeout(30000);
//		BoxGlobalSettings.setReadTimeout(120000);
		
		try {
			logger.warn("START - Setting up App Users");
			AppUserManager.getInstance();
			logger.warn("END - Setting up App Users");
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
			//logger.warn("*************************************************************************************************");

			for(ApplyMetadata mtu : mtuList){

				String lastAction = "";
				if(mtu.getState().equals(State.TERMINATED)){
					lastAction = " DURATION: " + (mtu.threadMetrics.getProcessingTime()/1000) + " seconds (" + (mtu.threadMetrics.getProcessingTime()/1000/60) + " minutes " + mtu.threadMetrics.getProcessingTime()/1000%60 + " seconds)";
					//lastAction += " FILE SIZE: " + getFileSizeOutput(mtu.threadMetrics.getBytesUploaded());
				}else{
					keepRunning = true;
					lastAction = " LAST ACTION: " + mtu.threadMetrics.getCurrentAction();
				}
				//logger.warn("Thread " + mtu.getName() + " - STATE: " +mtu.getState().name() + " - uploaded " + mtu.threadMetrics.getFilesUploaded() + " files at " + mtu.getRateOfUpload() + lastAction);
			}
			//logger.warn("*************************************************************************************************");
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
		if(getMigrationUser()!=null){
			//If there is, then add the AppUserManager group to all top level folders that the user owns
			try {
				BoxAPIConnection migrationUserApi = AuthorizationGenerator.getAPIConnection(getMigrationUser());
				List<BoxFolder> ownedFolders = getOwnedFolders(migrationUserApi);

				for(BoxFolder folder: ownedFolders){
					try{
						folder.collaborate(AppUserManager.getInstance().getBoxGroup().getResource(), Role.CO_OWNER, false, false);
						folderIdsWithCollabAdded.add(folder.getID());
					}catch(BoxAPIException e){
						logger.warn(e.getResponseCode() + "-" + e.getResponse());
					}
				}

			} catch (AuthorizationException e) {
				logger.error(e.getMessage(), e);			
			}

		}

	}

	protected static List<BoxFolder> getOwnedFolders(
			BoxAPIConnection migrationUserApi) {

		List<BoxFolder> ownedFolders = new ArrayList<BoxFolder>();

		BoxFolder baseFolder = new BoxFolder(migrationUserApi, "0");
		Iterable<BoxItem.Info> children = baseFolder.getChildren();
		BoxUser bu = BoxUser.getCurrentUser(migrationUserApi);

		for(BoxItem.Info child:children){
			if(child.getResource() instanceof BoxFolder){
				BoxFolder updatedChild = new BoxFolder(migrationUserApi, child.getID());
				logger.info("Owner: " + updatedChild.getInfo().getOwnedBy().getName() + " ID: " + updatedChild.getInfo().getOwnedBy().getID());
				if(updatedChild.getInfo().getOwnedBy()!=null && updatedChild.getInfo().getOwnedBy().getID().equals(bu.getInfo().getID())){
					ownedFolders.add((BoxFolder)updatedChild.getInfo().getResource());
				}
			}
		}


		return ownedFolders;
	}

	protected static void removePermissions(){
		if(getMigrationUser()!=null){

			try {
				BoxAPIConnection migrationUserApi = AuthorizationGenerator.getAPIConnection(getMigrationUser());

				for(String folderId : folderIdsWithCollabAdded){
					BoxFolder folder = new BoxFolder(migrationUserApi, folderId);
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


	protected static String getMigrationUser() {

		return getProperties().getProperty("migrationuser");
	}

	public ApplyMetadata(String string) {
		super(string);
	}

	public void run(){
		isRunning=true;
		StopWatch sw = new StopWatch();
		sw.start();

		try {
			applyMetadata(getMetadataFile());
			sw.stop();
			this.threadMetrics.setProcessingTime(sw.getElapsedTime());
			logger.warn("Uploaded " + this.getName().trim() + " in " + (sw.getElapsedTime()/1000) + " seconds (" + (sw.getElapsedTime()/1000/60) + " minutes)");
		} catch (StopWatchException e) {

		}

		String bytesUploadedOutput = (this.threadMetrics.getBytesUploaded()/1024L/1024L > 1) ? "" + this.threadMetrics.getBytesUploaded()/1024L/1024L + " MB" : "" + this.threadMetrics.getBytesUploaded()/1024L + " KB";
		logger.warn("Uploaded " + this.threadMetrics.getFilesUploaded() + " files totalling " + bytesUploadedOutput);
		logger.warn("Rate of Upload: " + getRateOfUpload());
		if(this.threadMetrics.getFoldersCreated()>0){
			logger.warn("Created " + this.threadMetrics.getFoldersCreated() + " folders in " + (this.threadMetrics.getMsSpentCreatingFolders()/1000) + " seconds (average of " + ((this.threadMetrics.getMsSpentCreatingFolders()/1000)/this.threadMetrics.getFoldersCreated()) + " sec/folder)");
		}
		isRunning=false;

	}

	protected String getMetadataFile() {
		// Pull this from a properties file
		Properties applyMetadataProps = getProperties();

		return applyMetadataProps.getProperty("metadatafile",null);
	}

	protected static Properties getProperties(){
		return PropertiesUtil.getPropertiesFromFile("apply_metadata.properties");
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

		for(String key : theMap.keySet()){
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
				logger.info("Max Queue Size Reached.  Pausing before retry.");
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
