package com.box.bc.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.user.AppUserManager;
import com.box.sdk.BoxFolder;

public class FolderUtilTests {

	@Test
	public void test() {
		try {
			BoxFolder baseFolder = FolderUtil.createFolder("0", "SS-Top-Level-Folder");

			for(int i=2; i<16; i++){
				baseFolder = new BoxFolder(AppUserManager.getInstance().getAppUser().getAPIConnection(), baseFolder.getID());
				baseFolder = FolderUtil.createFolder(baseFolder, "Folder " + i);
//				try {
//					Thread.sleep(5000L);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}

		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
