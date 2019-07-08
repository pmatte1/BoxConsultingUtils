package com.box.bc.user;

import java.util.Date;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxUser;

/**
 * This class represents an AppUser created via the Box Platform.  This abstraction
 * has access methods to get the API Connection for the specific App User, as well
 * as the Access Token.
 * 
 * @author Peter Matte - Box Sr Solution Architect
 *
 */
public class AppUser extends BoxUser{
	/*
	 * Logging implementation
	 */
	private static Logger logger = Logger.getLogger(AppUser.class);
	private static long refreshInterval = 55*60000L;//3300000L;
	private BoxAPIConnection api;
	private Date apiConnectionCreationDate;

	/**
	 * Constructor for creating an instance of the object.
	 * 
	 * @param api - A BoxAPIConnection to initialize the object
	 * @param id - The Box User's ID to create an instance of
	 */
	public AppUser(BoxAPIConnection api, String id) {
		super(api, id);
	}

	/**
	 * Constructor for creating an instance of the AppUser object
	 * 
	 * @param boxUserInfo - The Info object for the Box User
	 */
	public AppUser(Info boxUserInfo) {
		this(boxUserInfo.getResource().getAPI(), boxUserInfo.getID());
	}

	/**
	 * Method to retrieve the Access Token for this App User
	 * 
	 * @return - String containing the Access Token value from the Box Platform
	 * @throws AuthorizationException 
	 */
	public String getAccessToken() throws AuthorizationException {
		return getAPIConnection().getAccessToken();
	}
	
	/**
	 * Method to retrieve the API Connection for this App User
	 * 
	 * @return - BoxAPIConnection object specific to this App User
	 * @throws AuthorizationException 
	 */
//	public BoxAPIConnection getAPI(){
//		try {
//			return this.getAPIConnection();
//		} catch (AuthorizationException e) {
//			logger.error(e.getMessage(), e);
//		}
//		
//		return null;
//	}
	
	/**
	 * 
	 */
	public void delete(){
		this.delete(true, true);
	}
	
	@Override
	public void delete(boolean notifyUser, boolean force) {
		
		try {
			AppUserManager.getInstance().remove(this);
		} catch (AuthorizationException e) {
			logger.error(e.getMessage());
		}
		super.delete(notifyUser, force);
	}

	/**
	 * Method to retrieve the API Connection for this App User
	 * 
	 * @return - BoxAPIConnection object specific to this App User
	 * @throws AuthorizationException 
	 */
	public BoxAPIConnection getAPIConnection() throws AuthorizationException{
		if(api == null || apiConnectionCreationDate == null){
			refreshAPIConnection();
		}else if((apiConnectionCreationDate.getTime() + refreshInterval)< (new Date().getTime())){
			refreshAPIConnection();
		}else{
			logger.info("Using existing Access Token for user with ID: " + this.getID());
		}

		return api;
	}
	
	/**
	 * This method will generate a new API Connection and store it in the instance level
	 * api variable.
	 * @throws AuthorizationException 
	 */
	protected void refreshAPIConnection() throws AuthorizationException {
		logger.info("Getting New Token or Refreshing Token for user with the ID: " + this.getID());
		api = AuthorizationGenerator.getAPIConnection(this.getID());
		apiConnectionCreationDate = new Date();

	}

}
