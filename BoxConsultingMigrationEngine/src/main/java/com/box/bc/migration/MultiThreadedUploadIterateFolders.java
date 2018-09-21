package com.box.bc.migration;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLContext;

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

public class MultiThreadedUploadIterateFolders extends MultiThreadedUpload {
	private static Logger logger = Logger.getLogger(MultiThreadedUploadIterateFolders.class);

	protected static File baseFolder = new File("C:/Varonis/Box Sync");
	protected static String topLevelFolderRoot = "Varonis Data";
	protected static int iterationCount = 0;
	//END - Configurations per execution

	//	//Runtime Variables
	//	protected static ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(NUM_CONCURRENT_PROCESSORS);
	//	protected String baseBoxFolderId = null;
	//
	//	protected ThreadMetrics threadMetrics = new ThreadMetrics();
	//	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();
	//	//END - Runtime Variables


	public static void main(String args[]){

		try {
			logger.warn("START - Setting up App Users");
			AppUserManager.getInstance();
			logger.warn("END - Setting up App Users");
		} catch (AuthorizationException e1) {
			logger.error(e1.getMessage(), e1);
		}
		
		int maxValue = 100;
		for(int i=79; i<maxValue; i++){
			topLevelFolder = topLevelFolderRoot + " - " + bufferValue(maxValue, i); 
			String baseFolderId = null;
			try {
				baseFolderId = FolderUtil.getOrCreateFolder("0", topLevelFolder).getID();
			} catch (AuthorizationException e1) {
				logger.error(e1.getMessage(), e1);
			}
			
			List<MultiThreadedUploadIterateFolders> mtuList = new ArrayList<MultiThreadedUploadIterateFolders>();
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

					MultiThreadedUploadIterateFolders mtu = new MultiThreadedUploadIterateFolders(threadName);
					mtu.setBaseFile(topLevelFolder);
					mtu.setBaseBoxFolderId(baseFolderId);
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

				for(MultiThreadedUploadIterateFolders mtu : mtuList){

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

		}
		executor.shutdown();

	}

	protected static String bufferValue(int maxValue, int i) {
		String maxValueAsStr = "" + maxValue;
		String retVal = "" + i;
		
		while(retVal.length()<maxValueAsStr.length()){
			retVal = "0" + retVal;
		}
		
		return retVal;
	}

	public MultiThreadedUploadIterateFolders(String string) {
		super(string);
	}


	protected synchronized String getBaseBoxFolderId() throws AuthorizationException {
		if(baseBoxFolderId == null){
			baseBoxFolderId =FolderUtil.getOrCreateFolder("0", topLevelFolder).getID(); 
		}
		logger.info("Base Box Folder ID: " + baseBoxFolderId);
		return baseBoxFolderId;
	}
	
	protected String getUploadThreadName() {
		return super.getName() + " - " + System.currentTimeMillis();
	}

	//	private static String getFileSizeOutput(long bytesUploaded2) {
	//		int i=0;
	//		String units = "";
	//		while(bytesUploaded2>1024){
	//			bytesUploaded2 = bytesUploaded2/1024;
	//			i++;
	//		}
	//
	//		switch (i){
	//		case 0: units="bytes";
	//		break;
	//		case 1: units="KB";
	//		break;
	//		case 2: units="MB";
	//		break;
	//		case 3: units="GB";
	//		break;
	//		case 4: units="TB";
	//		break;
	//		}
	//
	//		return bytesUploaded2 + " " + units;
	//	}


}
