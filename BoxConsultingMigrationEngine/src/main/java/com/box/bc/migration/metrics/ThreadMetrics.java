package com.box.bc.migration.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class ThreadMetrics {
	protected long filesUploaded = 0L;
	protected long bytesUploaded = 0L;
	protected long msSpentUploading = 0L;
	protected long foldersCreated = 0L;
	protected long msSpentCreatingFolders = 0L;
	protected long processingTime = 0L;
	private int backoffInSeconds = 1;
	
	protected List<Future> futures = new ArrayList<Future>();

	protected String currentAction = "";

	public synchronized void addFuture(Future future){
		futures.add(future);
	}
	
	public synchronized Future getFuture(int index){
		return futures.get(index);
	}
	
	public synchronized List<Future> getAllFutures(){
		return futures;
	}

	public synchronized long getFilesUploaded() {
		return filesUploaded;
	}




	public synchronized void setFilesUploaded(long filesUploaded) {
		this.filesUploaded = filesUploaded;
	}




	public synchronized long getBytesUploaded() {
		return bytesUploaded;
	}




	public synchronized void setBytesUploaded(long bytesUploaded) {
		this.bytesUploaded = bytesUploaded;
	}




	public synchronized long getMsSpentUploading() {
		return msSpentUploading;
	}




	public synchronized void setMsSpentUploading(long msSpentUploading) {
		this.msSpentUploading = msSpentUploading;
	}




	public synchronized long getFoldersCreated() {
		return foldersCreated;
	}




	public synchronized void setFoldersCreated(long foldersCreated) {
		this.foldersCreated = foldersCreated;
	}




	public synchronized long getMsSpentCreatingFolders() {
		return msSpentCreatingFolders;
	}




	public synchronized void setMsSpentCreatingFolders(long msSpentCreatingFolders) {
		this.msSpentCreatingFolders = msSpentCreatingFolders;
	}




	public synchronized long getProcessingTime() {
		return processingTime;
	}




	public synchronized void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}




	public synchronized String getCurrentAction() {
		return currentAction;
	}




	public synchronized void setCurrentAction(String currentAction) {
		this.currentAction = currentAction;
	}




	public ThreadMetrics() {
		// TODO Auto-generated constructor stub
	}

	public synchronized int getBackoffInSeconds() {
		return backoffInSeconds;
	}

	public synchronized void setBackoffInSeconds(int backoffInSeconds) {
		this.backoffInSeconds = backoffInSeconds;
	}

}
