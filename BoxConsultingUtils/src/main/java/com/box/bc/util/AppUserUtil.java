package com.box.bc.util;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.bc.user.AppUser;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxUser;
import com.box.sdk.CreateUserParams;

/**
 * This class is responsible for the implementation of creating or retrieving
 * App Users for the encapsulating App
 * 
 * It relies upon proper function of the AuthorizationTokenGenerator class, which has 
 * dependencies on the proper configuration of the App credentials via config.json
 * 
 * @author Peter Matte - Box Sr Solution Architect
 *
 */
public class AppUserUtil {
	private static Logger logger = Logger.getLogger(AuthorizationGenerator.class);

	/**
	 * 
	 * @param name - Name of the App User to be created
	 * @param email - Email address of the App User to be created
	 * @return BoxUser.Info object representing the newly created user
	 * @throws AuthorizationException 
	 */
	public static BoxUser.Info createUser(String name) throws AuthorizationException{
		BoxDeveloperEditionAPIConnection api =  (BoxDeveloperEditionAPIConnection)AuthorizationGenerator.getAPIConnection(null);
		logger.debug("Acquired API Connection");
		CreateUserParams cup = new CreateUserParams();
		cup.setExternalAppUserId(name);
		cup.setSpaceAmount(1000000000000000L);
		return BoxUser.createAppUser(api, name, cup);

	}

	public static BoxUser.Info getOrCreateUser(String name) throws AuthorizationException{

		BoxUser.Info boxUserInfo = getUser(name);
		if(boxUserInfo != null){
			logger.info("Returning existing user");
			return boxUserInfo;
		}else{
			logger.info("Returning new user");
			return createUser(name);
		}
	}

	public static AppUser getOrCreateAppUser(String name) throws AuthorizationException{

		return new AppUser(getOrCreateUser(name));
	}

	public static BoxUser.Info getUser(String name) throws AuthorizationException{
		BoxDeveloperEditionAPIConnection api =  (BoxDeveloperEditionAPIConnection)AuthorizationGenerator.getAPIConnection(null);
		logger.debug("Acquired API Connection");
		
//		Iterable<BoxUser.Info> iterableBoxUsers = BoxUser.getAllEnterpriseUsers(api, name, new String[0]);
		
		Iterable<BoxUser.Info> iterableBoxUsers = BoxUser.getAppUsersByExternalAppUserID(api, name, new String[0]);
		if(iterableBoxUsers != null){
			Iterator<BoxUser.Info> iterBoxUsers = iterableBoxUsers.iterator();
			while(iterBoxUsers.hasNext()){
				BoxUser.Info boxUserInfo = iterBoxUsers.next();
				if(boxUserInfo.getName().equals(name)){
					return boxUserInfo;
				}
			}
		}

		return null;
	}
}
