package com.box.bc.user;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.box.bc.exception.AuthorizationException;
import com.box.sdk.BoxUser;
import com.box.sdk.BoxUser.Status;

public class AppUserManagerTests {
	private static Logger logger = Logger.getLogger(AppUserManagerTests.class);

	@Test
	@Ignore
	public void testDeleteAppUsers() {
		AppUserManager aum;
		try {
			aum = AppUserManager.getInstance();
			AppUser au = aum.getAppUser();

			while(au != null){
				au.delete();
				au = aum.getAppUser();
			}

		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void testInactivateUsers(){

		changeAppUserStatus(Status.INACTIVE);
	}

	@Test
	public void testActivateUsers(){
		changeAppUserStatus(Status.ACTIVE);
	}

	private void changeAppUserStatus(Status status) {
		List<String> processUserList = new ArrayList<String>();

		try {
			AppUser au = AppUserManager.getInstance().getAppUser();
			while(au != null){
				if(!processUserList.contains(au.getID())){
					BoxUser.Info auInfo = au.getInfo(new String[0]);
					logger.info("Updating Status for " + auInfo.getName() + " from " + auInfo.getStatus());
					auInfo.setStatus(Status.INACTIVE);
					au.updateInfo(auInfo);
					logger.info("Updated  Status for " + au.getInfo(new String[0]).getName() + " to   " + au.getInfo(new String[0]).getStatus());
					processUserList.add(au.getID());
					au = AppUserManager.getInstance().getAppUser();
				}else{
					au = null;
				}
			}
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test
	@Ignore
	public void testGetAppUser() {
		for(int i=0; i<50; i++){
			try {
				AppUserManager.getInstance().getAppUser().getAPIConnection();
			} catch (AuthorizationException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
}
