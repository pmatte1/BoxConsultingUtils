package com.box.bc.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.factory.MetadataParserFactory;
import com.box.bc.migration.metrics.ThreadMetrics;
import com.box.bc.migration.util.MemoryMonitor;
import com.box.bc.user.AppUserManager;
import com.box.bc.util.FolderUtil;
import com.box.bc.util.StopWatch;
import com.box.bc.util.StopWatch.StopWatchException;

public class MultiThreadedUpload extends Thread {
	private static Logger logger = Logger.getLogger(MultiThreadedUpload.class);

	//Configurations per execution
	//TODO: Move to Configurations
	protected static final int NUM_CONCURRENT_PROCESSORS = 100;
	protected static double MAX_QUEUE_SIZE = NUM_CONCURRENT_PROCESSORS*1;

	//protected static File baseFolder = new File("C:/demo/TEST-PARSING");
	//protected static File baseFolder = new File("C:/demo");
	protected static File baseFolder = new File("C:/Varonis");
	protected static String topLevelFolder = "Varonis Data";
	//END - Configurations per execution

	//Runtime Variables
	protected static ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(NUM_CONCURRENT_PROCESSORS);
	protected String baseBoxFolderId = null;
	private boolean isRunning = false;
	private File baseFile;

	protected ThreadMetrics threadMetrics = new ThreadMetrics();
	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();
	//END - Runtime Variables


	public static void main(String args[]){

		try {
			logger.warn("START - Setting up App Users");
			AppUserManager.getInstance();
			logger.warn("END - Setting up App Users");
		} catch (AuthorizationException e1) {
			logger.error(e1.getMessage(), e1);
		}
		List<MultiThreadedUpload> mtuList = new ArrayList<MultiThreadedUpload>();
		File[] topLevelFolders = baseFolder.listFiles();
		int lengthOfLargestName = 0;
		for(File topLevelFolder : topLevelFolders){
			if(topLevelFolder.getName().length()>lengthOfLargestName){
				lengthOfLargestName=topLevelFolder.getName().length();
			}
		}

		for(File topLevelFolder : topLevelFolders){
			if(topLevelFolder.isDirectory()){
				String threadName = topLevelFolder.getName();
				while(threadName.length()<lengthOfLargestName){
					threadName += " ";
				}

				MultiThreadedUpload mtu = new MultiThreadedUpload(threadName);
				mtu.setBaseFile(topLevelFolder);
				mtuList.add(mtu);
				mtu.setName(threadName);
				mtu.start();
			}
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
			logger.warn("*************************************************************************************************");

			for(MultiThreadedUpload mtu : mtuList){

				String lastAction = "";
				if(mtu.getState().equals(State.TERMINATED)){
					lastAction = " DURATION: " + (mtu.threadMetrics.getProcessingTime()/1000) + " seconds (" + (mtu.threadMetrics.getProcessingTime()/1000/60) + " minutes " + mtu.threadMetrics.getProcessingTime()/1000%60 + " seconds)";
					lastAction += " FILE SIZE: " + getFileSizeOutput(mtu.threadMetrics.getBytesUploaded());
				}else{
					keepRunning = true;
					lastAction = " LAST ACTION: " + mtu.threadMetrics.getCurrentAction();
				}
				logger.warn("Thread " + mtu.getName() + " - STATE: " +mtu.getState().name() + " - uploaded " + mtu.threadMetrics.getFilesUploaded() + " files at " + mtu.getRateOfUpload() + lastAction);
			}
			logger.warn("*************************************************************************************************");
			logger.warn("*************************************************************************************************");
			logger.warn("Active Thread Count : " + ((ThreadPoolExecutor)executor).getActiveCount());
			logger.warn("Queue Size          : " + ((ThreadPoolExecutor)executor).getQueue().size());
			logger.warn("Pool Size           : " + ((ThreadPoolExecutor)executor).getPoolSize());
			logger.warn("Task Count          : " + ((ThreadPoolExecutor)executor).getTaskCount());
			logger.warn("Completed Task Count: " + ((ThreadPoolExecutor)executor).getCompletedTaskCount());
			logger.warn("*************************************************************************************************");
		}

		memMon.stopRunning();
		executor.shutdown();

	}

	public MultiThreadedUpload(String string) {
		super(string);
	}

	public void run(){
		isRunning=true;
		StopWatch sw = new StopWatch();
		sw.start();

		try {
			uploadFiles(getBaseBoxFolderId(),this.baseFile);
			sw.stop();
			this.threadMetrics.setProcessingTime(sw.getElapsedTime());
			logger.warn("Uploaded " + this.getName().trim() + " in " + (sw.getElapsedTime()/1000) + " seconds (" + (sw.getElapsedTime()/1000/60) + " minutes)");
		} catch (StopWatchException e) {

		} catch (AuthorizationException e) {
			logger.error(e.getMessage());
		}

		String bytesUploadedOutput = (this.threadMetrics.getBytesUploaded()/1024L/1024L > 1) ? "" + this.threadMetrics.getBytesUploaded()/1024L/1024L + " MB" : "" + this.threadMetrics.getBytesUploaded()/1024L + " KB";
		logger.warn("Uploaded " + this.threadMetrics.getFilesUploaded() + " files totalling " + bytesUploadedOutput);
		logger.warn("Rate of Upload: " + getRateOfUpload());
		logger.warn("Created " + this.threadMetrics.getFoldersCreated() + " folders in " + (this.threadMetrics.getMsSpentCreatingFolders()/1000) + " seconds (average of " + ((this.threadMetrics.getMsSpentCreatingFolders()/1000)/this.threadMetrics.getFoldersCreated()) + " sec/folder)");
		isRunning=false;

	}

	protected void uploadFiles(String baseBoxFolderId, File baseFile2) {

		Thread thread = new UploadFilesAndFolders(this.getUploadThreadName(),baseBoxFolderId, baseFile2, this.threadMetrics, executor, metadataParser);
		executor.execute(thread);

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
	
	protected void setBaseBoxFolderId(String folderId){
		baseBoxFolderId=folderId;
	}

	protected synchronized String getBaseBoxFolderId() throws AuthorizationException {
		if(baseBoxFolderId == null){
			baseBoxFolderId =FolderUtil.getOrCreateFolder("0", topLevelFolder).getID(); 
		}
		logger.info("Base Box Folder ID: " + baseBoxFolderId);
		return baseBoxFolderId;
	}

	protected void setBaseFile(File topLevelFolder) {
		this.baseFile = topLevelFolder;

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

	protected static String getFileSizeOutput(long bytesUploaded2) {
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

	protected String getUploadThreadName() {
		return super.getName();
	}

}
