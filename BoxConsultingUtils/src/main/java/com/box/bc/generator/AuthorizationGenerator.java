package com.box.bc.generator;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.box.bc.config.BoxConfigProviderFactory;
import com.box.bc.exception.AuthorizationException;
import com.box.bc.util.AppUserUtil;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxUser;
import com.box.sdk.DeveloperEditionEntityType;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;


public class AuthorizationGenerator {
	private static final int MAX_CACHE_ENTRIES = 15;
	private static Logger logger = Logger.getLogger(AuthorizationGenerator.class);

	public static String getAuthorizationToken() throws AuthorizationException{
		return getAuthorizationToken(null);
	}

	public static String getAuthorizationToken(String userId) throws AuthorizationException {
		return getAPIConnection(userId).getAccessToken();


	}

	/*
	 * Interval To Do A Refresh of the cached Enterprise Connection
	 */
	private static long refreshInterval = 3300000L;

	/*
	 * When the last refresh of the Enterprise Connection was done
	 */
	protected static Date lastEnterpriseRefresh = null;

	/*
	 * Cached instance of the Enterprise Connection to avoid roundtrips
	 * to Box
	 */
	protected static BoxAPIConnection enterpriseConnection = null;

	/**
	 * Retrieves an App Enterprise connection to Box
	 * 
	 * @return BoxAPIConnection for the configured App
	 * @throws AuthorizationException 
	 */
	public static BoxAPIConnection getAppEnterpriseAPIConnection() throws AuthorizationException{
		if(enterpriseConnection == null || lastEnterpriseRefresh == null || (new Date().getTime())-lastEnterpriseRefresh.getTime()>refreshInterval){
			enterpriseConnection = getAPIConnection(null);
			lastEnterpriseRefresh = new Date();
		}

		return enterpriseConnection;
	}

	//It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
	//For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
	//implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
	//private static IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);


	/**
	 * Retrieves a Connection to Box for the Configured user.
	 * 
	 * Acceptable User ID values are email address, user name or Box User ID
	 * 
	 * App configuration must allow for generating connection for users in order to use any 
	 * value other than null as the userId input.
	 * 
	 * @param userId - The email or Box User ID.  Will generate an Enterprise App connection if null
	 * @return BoxAPIConnection object for the user specified in the input parameter userId
	 * @throws AuthorizationException 
	 */
	protected static IAccessTokenCache accessTokenCache = null;
	protected static BoxConfig boxConfig = null;
	public static BoxAPIConnection getAPIConnection(String userId) throws AuthorizationException {
		//It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
		//For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
		//implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
		if(accessTokenCache == null){
			accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);
		}

		BoxAPIConnection api = null;
		try {
			if(boxConfig == null){
				boxConfig = BoxConfigProviderFactory.getBoxConfigProvider().getBoxConfig();
			}

			String apiType = "";
			if(userId == null || userId.trim().length()==0){
				api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);
				apiType = "APPENTERPRISECONNECTION";
			}
			else{
				String boxUserId=userId;

				if(isEmailAddress(userId)){
					boxUserId = getBoxUserIdFromEmail(userId);
				}else if(!isNumber(userId)){
					boxUserId = getBoxUserFromUserName(userId);
				}

				try{
					api = new BoxDeveloperEditionAPIConnection(boxUserId, DeveloperEditionEntityType.USER, boxConfig,
							accessTokenCache);
					apiType = "APPUSER - " + userId;
				}catch(BoxAPIException e){
					api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);
					api.asUser(boxUserId);
					apiType = "APPUSER - " + userId;
				}
			}
			logger.info("API: " + api.getAccessToken() + " API TYPE: " + apiType);

		} catch(BoxAPIException e){
			logger.error(e.getResponseCode() + ": " + e.getResponse(),e);
		} catch (InstantiationException e) {
			logger.error(e.getMessage(),e);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(),e);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(),e);
		}

		return api;	
	}

	protected static String getConfigFile() {
		String configFile = "config.json";
		if(System.getProperty("boxconfigfile")!= null && System.getProperty("boxconfigfile").trim().length()>0){
			configFile = System.getProperty("boxconfigfile");
		}else if(System.getenv("boxconfigfile")!= null  && System.getenv("boxconfigfile").trim().length()>0){
			configFile = System.getenv("boxconfigfile");
		}

		return configFile;
	}

	private static boolean isNumber(String userId) {
		try{
			Long.parseLong(userId);
			return true;
		}catch(Exception e){
			logger.debug("Value " + userId + " is not a number - " + e.getMessage());
		}
		return false;
	}

	private static String getBoxUserFromUserName(String userId) throws AuthorizationException {
		return AppUserUtil.getOrCreateUser(userId).getID();
	}

	private static String getBoxUserIdFromEmail(String userId) throws AuthorizationException {
		//Get the Enterprise Connection
		BoxAPIConnection api = getAPIConnection(null);
		Iterator<BoxUser.Info> iterBoxUserInfo = BoxUser.getAllEnterpriseUsers(api, userId).iterator();

		if(iterBoxUserInfo.hasNext()){
			return iterBoxUserInfo.next().getID();
		}
		return null;
	}

	private static boolean isEmailAddress(String userId) {
		// Check if the address has an @ and a . after it

		int atLoc = userId.indexOf("@");
		int lastPeriodLoc = userId.lastIndexOf(".");

		if(atLoc > -1 && lastPeriodLoc>atLoc){
			return true;
		}
		return false;
	}
}
