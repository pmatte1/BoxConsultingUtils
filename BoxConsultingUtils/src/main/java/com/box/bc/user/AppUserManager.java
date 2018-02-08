package com.box.bc.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.util.AppUserUtil;
import com.box.bc.util.GroupUtil;
import com.box.bc.util.PropertiesUtil;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxGroup;

/**
 * This class abstracts the implementation that rotates which App User is being used.  It 
 * also adds each App user to a specific group which can be added as a Collaborator to ensure
 * access to all the users in that group.
 * 
 * @author Peter Matte - Box Sr Solution Architect
 *
 */
public class AppUserManager {
	/*
	 * Logging Implementation
	 */
	private static Logger logger = Logger.getLogger(AppUserManager.class);
	
	/*
	 * Singleton Instance of the AppUserManager class
	 */
	private static AppUserManager appUserManager;

	/*
	 * List of App Users created to support the implementation
	 */
	private List<AppUser> appUserList = new ArrayList<AppUser>();
	
	/*
	 * Tracks which App User was the most recently issued by th AppUserManager class
	 */
	private int lastIndexUsed = 0;
	
	/*
	 * The BoxGroup that all the App Users have been assigned to
	 */
	private BoxGroup.Info boxGroup = null;

	/**
	 * Singleton Pattern constructor for AppUserManager
	 * @throws AuthorizationException 
	 */
	private AppUserManager() throws AuthorizationException{
		loadAppUserMap();
	}

	/**
	 * Does a Pre-Load of the App Users into the AppUserManager instance
	 * for use when requested by the encapsulating application
	 * @throws AuthorizationException 
	 */
	protected void loadAppUserMap() throws AuthorizationException {
		//TODO Move to an object based implementation 
		Properties props = PropertiesUtil.getPropertiesFromFile("appUserManager.properties");
		String prefix = props.getProperty("appuser.prefix");
		String groupName = props.getProperty("groupname");
		
		int countAppUsers = 1;
		int currentIndex = 0;
		try{
			countAppUsers = Integer.parseInt(props.getProperty("appuser.count"));
			lastIndexUsed = new Random().nextInt(countAppUsers);
		}catch(Exception e){
			logger.warn("Issue getting App User Count, Defaulting to 1 App User");
		}
		try{
			currentIndex = Integer.parseInt(props.getProperty("appuser.startindex"));
		}catch(Exception e){
			logger.warn("Issue getting Current Index, Defaulting to 0");
		}
		//END TODO Move to an object based implementation 
		
		//Create the BoxGroup to add users to, if not created already
		if(boxGroup == null){
			if( groupName != null){
				boxGroup = GroupUtil.getOrCreateGroup(groupName);
			}
		}

		//Create each App User, and add them to the BoxGroup
		for(int i=0; i<countAppUsers; i++){
			String appUserName = prefix + "-" + currentIndex;
			logger.info("Getting or Creating App User with the Name: " + appUserName);
			AppUser appUser = AppUserUtil.getOrCreateAppUser(appUserName);
			try{
				boxGroup.getResource().addMembership(appUser);
				appUserList.add(appUser);
			}catch(BoxAPIException e){
				//If Response Code is 409, then the user was already added to the group, so it is safe
				//to add them to the user list
				if(e.getResponseCode() == 409){
					appUserList.add(appUser);
				}else{
					logger.error("Error: " + e.getResponseCode() + " - "+ e.getResponse(), e);
				}
			}
			
			currentIndex++;
		}


	}

	/**
	 * Retrieves the BoxGroup.Info object that represents the Group that 
	 * the App Users were added to.  This allows the group to be added to the 
	 * security model
	 * 
	 * @return - BoxGroup.Info object representing admin group for App Users
	 */
	public BoxGroup.Info getBoxGroup() {
		return boxGroup;
	}

	/**
	 * Singleton Pattern implementation to retrieve an instance of the AppUserManager
	 * 
	 * @return Instance of AppUserManager with pre-loaded App User and Group information
	 * @throws AuthorizationException 
	 */
	public synchronized static AppUserManager getInstance() throws AuthorizationException{
		if(appUserManager == null){
			appUserManager = new AppUserManager();
		}

		return appUserManager;
	}

	/**
	 * Method to retrieve an App User that was configured to support the integration.
	 * This will iterate through a list of App Users to combat Rate Limiting and scaling
	 * limitations
	 * 
	 * @return Instance of an App User
	 */
	public synchronized AppUser getAppUser(){
		if((lastIndexUsed) + 1 < appUserList.size()){
			lastIndexUsed++;
		}else{
			lastIndexUsed = 0;
		}

		if(appUserList.size()==0){
			logger.error("There are no App Users available in the App Manager");
			return null;
		}
		
		return appUserList.get(lastIndexUsed);
	}

	public synchronized void remove(AppUser appUser) {
		if(appUserList.contains(appUser)){
			appUserList.remove(appUser);
		}
		
	}



}
