package com.box.bc.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.metadata.factory.MetadataParserFactory;
import com.box.bc.migration.util.MemoryMonitor;
import com.box.bc.util.FolderUtil;
import com.box.bc.util.StopWatch;
import com.box.bc.util.StopWatch.StopWatchException;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFile.Info;
import com.box.sdk.BoxFolder;

public class MultiThreadedUpload extends Thread {
	private static final long MINIMUM_LARGE_UPLOAD_SIZE = (30L*1024L*1024L); //30 MB

	protected static File baseFolder = new File("C:/demo/large-upload");
	//protected static File baseFolder = new File("C:/Users/pmatte/Box Sync");
	protected String topLevelFolder = "PM TEST FOLDER - Memory Eval";

	private static Logger logger = Logger.getLogger(MultiThreadedUpload.class);
	private boolean isRunning = false;
	private File baseFile;

	protected long filesUploaded = 0L;
	protected long bytesUploaded = 0L;
	protected long msSpentUploading = 0L;
	protected long foldersCreated = 0L;
	protected long msSpentCreatingFolders = 0L;

	protected String currentAction = "";
	//	protected Metadata meta = new Metadata();
	protected IMetadataParser metadataParser = MetadataParserFactory.getMetadataParser();

	protected long processingTime = 0L;

	private int NUM_CONCURRENT_UPLOAD_THREADS=20;



	public MultiThreadedUpload(String string) {
		super(string);
		//meta.add("/test1", "Test Value 1");
	}

	public static void main(String args[]){

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
				String threadNum = topLevelFolder.getName();
				while(threadNum.length()<lengthOfLargestName){
					threadNum += " ";
				}

				MultiThreadedUpload mtu = new MultiThreadedUpload(threadNum);
				mtu.setBaseFile(topLevelFolder);
				mtuList.add(mtu);
				mtu.setName(threadNum);
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
					lastAction = " DURATION: " + (mtu.processingTime/1000) + " seconds (" + (mtu.processingTime/1000/60) + " minutes)";
					lastAction += " FILE SIZE: " + getFileSizeOutput(mtu.bytesUploaded);
				}else{
					keepRunning = true;
					lastAction = " LAST ACTION: " + mtu.currentAction;
				}
				logger.warn("Thread " + mtu.getName() + " - STATE: " +mtu.getState().name() + " - uploaded " + mtu.filesUploaded + " files at " + mtu.getRateOfUpload() + lastAction);
			}
			logger.warn("*************************************************************************************************");
		}

		memMon.stopRunning();

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

	public void run(){
		isRunning=true;
		StopWatch sw = new StopWatch();
		sw.start();

		try {
			uploadFiles(getBaseBoxFolderId(),this.baseFile);
			sw.stop();
			this.processingTime  = sw.getElapsedTime();
			logger.warn("Uploaded " + this.getName().trim() + " in " + (sw.getElapsedTime()/1000) + " seconds (" + (sw.getElapsedTime()/1000/60) + " minutes");
		} catch (StopWatchException e) {

		} catch (AuthorizationException e) {
			logger.error(e.getMessage());
		}

		String bytesUploadedOutput = (this.bytesUploaded/1024L/1024L > 1) ? "" + this.bytesUploaded/1024L/1024L + " MB" : "" + this.bytesUploaded/1024L + " KB";
		logger.warn("Uploaded " + this.filesUploaded + " files totalling " + bytesUploadedOutput);
		logger.warn("Rate of Upload: " + getRateOfUpload());
		logger.warn("Created " + foldersCreated + " folders in " + (msSpentCreatingFolders/1000) + " seconds (average of " + ((msSpentCreatingFolders/1000)/foldersCreated) + " sec/folder)");
		isRunning=false;

	}
	public String getRateOfUpload() {
		if(msSpentUploading == 0L){
			return "Nothing Uploaded Yet";
		}
		return ((bytesUploaded/1024)/(msSpentUploading/1000)) + "kb per second";
	}

	private void uploadFiles(String baseBoxFolderId, File baseFile2) {
		StopWatch swFolderCreate = new StopWatch();
		StopWatch swCreateFile = new StopWatch();
		try {
			currentAction = "Start - " + baseFile2.getAbsolutePath();
			swFolderCreate.start();
			currentAction = "Before FolderUtil.getOrCreateFolder " + baseFile2.getAbsolutePath();
			BoxFolder currentFolder = FolderUtil.getOrCreateFolder(baseBoxFolderId, baseFile2.getName());

			//currentFolder.createMetadata("demoTestTemplate",)
			currentAction = "After FolderUtil.getOrCreateFolder " + baseFile2.getAbsolutePath();
			try{
				swFolderCreate.stop();
				msSpentCreatingFolders = msSpentCreatingFolders + swFolderCreate.getElapsedTime();
				foldersCreated++;
			}catch(Exception e){}
			currentAction = "After Updating Folder Metrics";

			currentAction = "Before List Files " + baseFile2.getAbsolutePath();
			File[] folderContents = baseFile2.listFiles();
			currentAction = "After List Files " + baseFile2.getAbsolutePath();

			for(File fileOrFolder : folderContents){
				currentAction = "Before Directory Check " + baseFile2.getAbsolutePath();
				if(fileOrFolder.isDirectory()){
					currentAction = "Start Recursion for " + fileOrFolder.getAbsolutePath();
					uploadFiles(currentFolder.getID(),fileOrFolder);
					currentAction = "End Recursion for " + fileOrFolder.getAbsolutePath();
				}else if(metadataParser.isMetadataFile(fileOrFolder)){
					metadataParser.load(fileOrFolder);
				}else{

					try {
						swCreateFile.start();
						currentAction = "Start Upload for " + fileOrFolder.getAbsolutePath();

						BoxFile.Info createdFile = null;
						if(fileOrFolder.length()>MINIMUM_LARGE_UPLOAD_SIZE){
							currentAction = "Start Large File Upload for " + fileOrFolder.getAbsolutePath();
							//currentFolder.uploadLargeFile(new FileInputStream(fileOrFolder), fileOrFolder.getName(), fileOrFolder.length());
							createdFile = currentFolder.uploadLargeFile(new FileInputStream(fileOrFolder), fileOrFolder.getName(), fileOrFolder.length(), NUM_CONCURRENT_UPLOAD_THREADS, 5, TimeUnit.MINUTES);
							//							URL url = BoxFolder.UPLOAD_SESSION_URL_TEMPLATE.build(currentFolder.getAPI().getBaseUploadURL());
							//							LargeFileUploadRefactor lfu = new LargeFileUploadRefactor(NUM_CONCURRENT_UPLOAD_THREADS, 5, TimeUnit.MINUTES); 
							//							createdFile = lfu.upload(currentFolder.getAPI(), currentFolder.getID(), new FileInputStream(fileOrFolder), url, fileOrFolder.getName(), fileOrFolder.length());
							currentAction = "End Large File Upload for " + fileOrFolder.getAbsolutePath();
						}else{
							currentAction = "Start Normal File Upload for " + fileOrFolder.getAbsolutePath();
							createdFile = currentFolder.uploadFile(new FileInputStream(fileOrFolder), fileOrFolder.getName());
							currentAction = "End Normal File Upload for " + fileOrFolder.getAbsolutePath();

						}

						if(createdFile != null){
							applyMetadataTemplateAndVals(createdFile, metadataParser.getMetadata(fileOrFolder));
						}

						currentAction = "End Upload for " + fileOrFolder.getAbsolutePath();
						swCreateFile.stop();
						currentAction = "Stopped createFile SW " + fileOrFolder.getAbsolutePath();
						filesUploaded++;
						currentAction = "Incremented Files Uploaded " + fileOrFolder.getAbsolutePath();
						bytesUploaded = bytesUploaded + fileOrFolder.length();
						currentAction = "Added addt'l upload bytes " + fileOrFolder.getAbsolutePath();
						msSpentUploading = msSpentUploading + swCreateFile.getElapsedTime();
						currentAction = "Added addt'l upload time " + fileOrFolder.getAbsolutePath();

						logger.info("Uploaded File in: " + swCreateFile.getElapsedTime() + "ms");
					} catch (FileNotFoundException e) {
						logger.error("Error Uploading " + fileOrFolder.getAbsolutePath() + ": " + e.getMessage());
					} catch (StopWatchException e) {
						logger.error(e.getMessage());
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					} catch (IOException e) {
						logger.error(e.getMessage());
					}catch(BoxAPIException e){
						logger.error(e.getResponseCode() + ": " + e.getResponse());
					}
				}
			}
		} catch (AuthorizationException e) {
			logger.error(e.getMessage());		
		}

	}

	protected void applyMetadataTemplateAndVals(Info createdFile,
			List<MetadataTemplateAndValues> metadataTemplateAndVals) {
		if(metadataTemplateAndVals != null){
			for(MetadataTemplateAndValues templateAndVal : metadataTemplateAndVals){
				//If a template name is specified, then create with the template
				//otherwise, just add as custom metadata
				if(templateAndVal.getMetadataTemplateName() != null && templateAndVal.getMetadataValues() != null){
					createdFile.getResource().createMetadata(templateAndVal.getMetadataTemplateName(),
							templateAndVal.getMetadataValues());
				}else{
					if(templateAndVal.getMetadataValues() != null){
						createdFile.getResource().createMetadata(templateAndVal.getMetadataValues());
					}
				}
			}
		}



	}

	String baseBoxFolderId = null;
	protected synchronized String getBaseBoxFolderId() throws AuthorizationException {
		if(baseBoxFolderId == null){
			baseBoxFolderId =FolderUtil.getOrCreateFolder("0", this.topLevelFolder).getID(); 
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


}