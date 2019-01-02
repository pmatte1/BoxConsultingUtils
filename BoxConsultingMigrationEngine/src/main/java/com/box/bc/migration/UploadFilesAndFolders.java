package com.box.bc.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.migration.metadata.IMetadataParser;
import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.metadata.factory.MetadataParserFactory;
import com.box.bc.migration.metadata.parser.CustomMetadata;
import com.box.bc.migration.metrics.ThreadMetrics;
import com.box.bc.migration.util.MetadataUtil;
import com.box.bc.user.AppUserManager;
import com.box.bc.util.FolderUtil;
import com.box.bc.util.StopWatch;
import com.box.bc.util.StopWatch.StopWatchException;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFile.Info;

public class UploadFilesAndFolders extends Thread {
	private static Logger logger = Logger.getLogger(UploadFilesAndFolders.class);

	private String baseBoxFolderId;
	private File baseFile2;
	private ThreadMetrics threadMetrics;
	protected IMetadataParser metadataParser;
	protected ExecutorService executor;
	
	private static final long MINIMUM_LARGE_UPLOAD_SIZE = (30L*1024L*1024L); //30 MB
	private static int NUM_CONCURRENT_LARGE_UPLOAD_THREADS=20;
	
	private static boolean doNotUploadMetadataFile=true;

	public UploadFilesAndFolders(String threadName, String baseBoxFolderId, File baseFile2, ThreadMetrics threadMetrics, ExecutorService executor, IMetadataParser metadataParser){
		super(threadName);
		this.baseBoxFolderId=baseBoxFolderId;
		this.baseFile2=baseFile2;
		this.threadMetrics=threadMetrics;
		this.executor=executor;
		this.metadataParser=metadataParser;
	}

	public UploadFilesAndFolders(String threadName, String baseBoxFolderId, File baseFile2, ExecutorService executor, IMetadataParser metadataParser){
		this(threadName, baseBoxFolderId, baseFile2, new ThreadMetrics(), executor, metadataParser);
	}

	public void run(){
		uploadFiles(this.baseBoxFolderId, baseFile2);
	}

	private void uploadFiles(String baseBoxFolderId, File baseFile2) {
		if(baseFile2.isDirectory()){
			StopWatch swFolderCreate = new StopWatch();
			try {
				this.threadMetrics.setCurrentAction("Start - " + baseFile2.getAbsolutePath());
				swFolderCreate.start();
				this.threadMetrics.setCurrentAction("Before FolderUtil.getOrCreateFolder " + baseFile2.getAbsolutePath());
				BoxFolder currentFolder = FolderUtil.getOrCreateFolder(baseBoxFolderId, baseFile2.getName());
				this.threadMetrics.setCurrentAction("After FolderUtil.getOrCreateFolder " + baseFile2.getAbsolutePath());
				try{
					swFolderCreate.stop();
					threadMetrics.setMsSpentCreatingFolders(threadMetrics.getMsSpentCreatingFolders() + swFolderCreate.getElapsedTime());
					threadMetrics.setFoldersCreated(threadMetrics.getFoldersCreated()+1);
				}catch(Exception e){}
				this.threadMetrics.setCurrentAction("After Updating Folder Metrics");

				this.threadMetrics.setCurrentAction("Adding Metadata - If Required");
				new MetadataUtil().applyMetadataTemplateAndVals(currentFolder, metadataParser.getMetadata(baseFile2));
				this.threadMetrics.setCurrentAction("Added Metadata - If Required");


				//Logic to get any metadata files first, and load it
				if(metadataParser.getFileNameFilter() != null){
					File[] metadataFiles = baseFile2.listFiles(metadataParser.getFileNameFilter());
					if(metadataFiles.length>1){
						logger.warn("There are more than 1 Metadata Files in this directory.  This is not currently supported.");
					}

					for(File metaFile : metadataFiles){
						if(metadataParser.isMetadataFile(metaFile)){
							metadataParser.load(metaFile);
						}
					}

				}


				this.threadMetrics.setCurrentAction("Before List Files " + baseFile2.getAbsolutePath());
				File[] folderContents = baseFile2.listFiles();
				this.threadMetrics.setCurrentAction("After List Files " + baseFile2.getAbsolutePath());

				for(File fileOrFolder : folderContents){
					if(metadataParser.isMetadataFile(fileOrFolder) && doNotUploadMetadataFile){
						logger.info("Skipping upload of " + fileOrFolder.getName() + " as it is a metadata file, and we are configured not to upload metadata files");;
					}else{
						logger.info("START - Spawning thread for " + fileOrFolder.getAbsolutePath());
						this.threadMetrics.setCurrentAction("Start Thread Launch for " + fileOrFolder.getAbsolutePath());
						this.threadMetrics.addFuture(startNewUploadThread(currentFolder.getID(), fileOrFolder));
						this.threadMetrics.setCurrentAction("End Thread Launch for " + fileOrFolder.getAbsolutePath());
						logger.info("END - Spawning thread for " + fileOrFolder.getAbsolutePath());

					}
				}


			} catch (AuthorizationException e) {
				logger.error(e.getMessage());		
			}
		}else{
			//This is a file, do the upload logic
			StopWatch swCreateFile = new StopWatch();

			try {

				BoxFolder currentFolder = new BoxFolder(AppUserManager.getInstance().getAppUser().getAPIConnection(), baseBoxFolderId);
				swCreateFile.start();
				this.threadMetrics.setCurrentAction("Start Upload for " + baseFile2.getAbsolutePath());

				BoxFile.Info createdFile = null;
				if(baseFile2.length()>MINIMUM_LARGE_UPLOAD_SIZE){
					this.threadMetrics.setCurrentAction("Start Large File Upload for " + baseFile2.getAbsolutePath());
					createdFile = currentFolder.uploadLargeFile(new FileInputStream(baseFile2), baseFile2.getName(), baseFile2.length(), NUM_CONCURRENT_LARGE_UPLOAD_THREADS, 5, TimeUnit.MINUTES);
					this.threadMetrics.setCurrentAction("End Large File Upload for " + baseFile2.getAbsolutePath());
				}else{
					this.threadMetrics.setCurrentAction("Start Normal File Upload for " + baseFile2.getAbsolutePath());
					createdFile = currentFolder.uploadFile(new FileInputStream(baseFile2), baseFile2.getName());
					this.threadMetrics.setCurrentAction("End Normal File Upload for " + baseFile2.getAbsolutePath());
				}

				if(createdFile != null){
					new MetadataUtil().applyMetadataTemplateAndVals(createdFile, metadataParser.getMetadata(baseFile2));
				}

				this.threadMetrics.setCurrentAction("End Upload for " + baseFile2.getAbsolutePath());
				swCreateFile.stop();
				this.threadMetrics.setCurrentAction("Stopped createFile SW " + baseFile2.getAbsolutePath());
				this.threadMetrics.setFilesUploaded(threadMetrics.getFilesUploaded()+1);
				this.threadMetrics.setCurrentAction("Incremented Files Uploaded " + baseFile2.getAbsolutePath());
				this.threadMetrics.setBytesUploaded(this.threadMetrics.getBytesUploaded() + baseFile2.length());
				this.threadMetrics.setCurrentAction("Added addt'l upload bytes " + baseFile2.getAbsolutePath());
				this.threadMetrics.setMsSpentUploading(threadMetrics.getMsSpentUploading() + swCreateFile.getElapsedTime());
				this.threadMetrics.setCurrentAction("Added addt'l upload time " + baseFile2.getAbsolutePath());

				logger.info("Uploaded File in: " + swCreateFile.getElapsedTime() + "ms");
			} catch (FileNotFoundException e) {
				logger.error("Error Uploading " + baseFile2.getAbsolutePath() + ": " + e.getMessage());
			} catch (StopWatchException e) {
				logger.error(e.getMessage());
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}catch(BoxAPIException e){
				logger.error(e.getResponseCode() + ": " + e.getResponse());
			} catch (AuthorizationException e) {
				logger.error(e.getMessage());
			}
		}


	}

	private int myBackoff = 1;
	protected Future<?> startNewUploadThread(String id, File fileOrFolder) {
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
		Thread thread = new UploadFilesAndFolders(this.getName() + "-" + new Date().getTime(),id, fileOrFolder, this.threadMetrics, this.executor, this.metadataParser);
		return executor.submit(thread);

	}


}
