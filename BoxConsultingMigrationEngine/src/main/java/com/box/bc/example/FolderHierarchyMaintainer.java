package com.box.bc.example;


import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.util.FolderUtil;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;

public class FolderHierarchyMaintainer extends Thread{
	private static Logger logger = Logger.getLogger(FolderHierarchyMaintainer.class);

	private BoxAPIConnection api;
	private long[] folderScale;
	BoxManagedHierarchy boxManagedHierarchy;
//	private List<String> hierarchyIDs = new ArrayList<String>();

	public FolderHierarchyMaintainer(BoxAPIConnection api, long[] folderScale) {
		this.api=api;
		this.folderScale=folderScale;
		this.start();
	}

	long frequency = 10000L;
	public void run(){
		boxManagedHierarchy = new BoxManagedHierarchy(new BoxFolder(api, "0"), folderScale);
		while(true){
			
			boxManagedHierarchy = boxManagedHierarchy.refresh();
			
			try {
				Thread.sleep(frequency);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
			
		}
	}
	
	public BoxFolder getFolder(){
		if(boxManagedHierarchy == null){
			return null;
		}
		return boxManagedHierarchy.getCurrentFolder();
	}
	
	protected class BoxManagedHierarchy{
		private int hierarchyLevel;
		private long[] folderScale;
		private BoxFolder currentFolder;
		private boolean returnFolderValue = false;

		public BoxManagedHierarchy(BoxFolder currentFolder, long[] folderScale){
			this(0,currentFolder,folderScale);
		}
		
		public BoxFolder getCurrentFolder() {
			int iterCount=0;
			while(!returnFolderValue && iterCount<5){
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				iterCount++;
			}
			
			if(!returnFolderValue){
				return null;
			}
			return currentFolder;
			
		}

		public BoxManagedHierarchy(int hierarchyLevel, BoxFolder currentFolder, long[] folderScale){
			this.hierarchyLevel=hierarchyLevel;
			this.folderScale=folderScale;
			this.currentFolder=currentFolder;
		}

		public BoxManagedHierarchy refresh() {
			BoxManagedHierarchy managedFolder = null;

			try {
				if(hierarchyLevel<folderScale.length ){
					returnFolderValue=false;
					BoxFolder folder = FolderUtil.getOrCreateFolder(currentFolder, getFolderName(hierarchyLevel));
					managedFolder = new BoxManagedHierarchy(hierarchyLevel + 1, folder, folderScale);
					managedFolder = managedFolder.refresh();
					currentFolder = managedFolder.getCurrentFolder();
				}else if(new BoxItemCountRetriever(currentFolder.getAPI()).getItemCount(currentFolder)>=folderScale[hierarchyLevel-1]){
					returnFolderValue=false;

					managedFolder = new BoxManagedHierarchy(hierarchyLevel -1, currentFolder.getInfo().getParent().getResource(), folderScale);
					managedFolder = managedFolder.refresh();
					currentFolder = managedFolder.getCurrentFolder();
				}
				returnFolderValue=true;
			} catch (AuthorizationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(managedFolder == null){
				return this;
			}
			return managedFolder;
			
		}

		
		protected String getFolderName(int hierarchyLevel2) {
			String name=null;
			switch(hierarchyLevel2){
			case 0: name="BOX MANAGED HIERARCHY - APP USER " + FolderNumberHolder.getInstance().getAndIncrementAppUserNumber();
			break;
			case 1: name="BOX MANAGED HIERARCHY - FOLDER " + FolderNumberHolder.getInstance().getAndIncrementFolderNumber();
			break;
			default: name="OUTSIDE THE SCOPE OF THIS HIERARCHY";
			break;
			}
				
			return name;
		}
		
		
	}
	
	public static class FolderNumberHolder{
		private static FolderNumberHolder fnh = null;
		
		private int appUserNumber=0;
		private int folderNumber=0;
		
		private FolderNumberHolder(){
			
		}
		
		public static FolderNumberHolder getInstance(){
			if(fnh==null){
				fnh=new FolderNumberHolder();
			}
			return fnh;
			
		}
		
		public int getAndIncrementAppUserNumber(){
			int ret = appUserNumber;
			appUserNumber++;
			
			return ret;
		}
		
		public int getAndIncrementFolderNumber(){
			int ret = folderNumber;
			folderNumber++;
			
			return ret;
		}
		
	}

}

