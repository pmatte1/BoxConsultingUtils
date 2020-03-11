package com.box.bc.example;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.user.AppUser;
import com.box.bc.user.AppUserManager;
import com.box.sdk.BoxFolder;

public class FolderHierarchyExample {
	private static Logger logger = Logger.getLogger(FolderHierarchyExample.class);

	public FolderHierarchyExample() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		try {
			AppUserManager appUserManager = AppUserManager.getInstance();
			AppUser appUser = appUserManager.getAppUser();
			
			FolderHierarchyMaintainer fhm = new FolderHierarchyMaintainer(appUser.getAPI(), new long[]{17L,10L});
			
//			try {
//				Thread.sleep(60000L);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			for(int i=0; i<10000; i++){
				BoxFolder boxFolder = fhm.getFolder();
				
				if(boxFolder != null){
					logger.info("Current Folder ID: " + boxFolder.getID());
					boxFolder.uploadFile(fhm.getClass().getClassLoader().getResourceAsStream("log4j.properties"), "file - " + System.currentTimeMillis() + ".txt");
					
				}
				
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
