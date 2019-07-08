package com.box.bc.util;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxGroup;

/**
 * This is a standard implementation to interface with
 * Groups in Box.
 * 
 * @author Peter Matte - Box Sr Solution Architect
 *
 */
public class GroupUtil {
	private static Logger logger = Logger.getLogger(GroupUtil.class);

	/**
	 * Method to retrieve a Box Group by name. If a group with the name is not created
	 * NULL is returned to the calling class.
	 * 
	 * The group name is case sensitive, so an input of "MyGroup" will not retrieve the 
	 * group named "mygroup"
	 * 
	 * @param groupName - Name of the group to retrieve
	 * @return An instance of BoxGroup.Info that matches the groupName param, or NULL if no match
	 * exists
	 * @throws AuthorizationException 
	 */
	public static BoxGroup.Info getGroup(String groupName) throws AuthorizationException{
		
		Iterable<BoxGroup.Info> iterableBoxGroupInfo = BoxGroup.getAllGroupsByName(AuthorizationGenerator.getAppEnterpriseAPIConnection(), groupName);
		if(iterableBoxGroupInfo != null){
			Iterator<BoxGroup.Info> iterBoxGroupInfo = iterableBoxGroupInfo.iterator();
			while(iterBoxGroupInfo.hasNext()){
				BoxGroup.Info boxGroupInfo = iterBoxGroupInfo.next();
				if(boxGroupInfo.getName().equals(groupName)){
					return boxGroupInfo;
				}
			}
		}
		return null;
	}
	
	/**
	 * This method will retrieve a group based on the supplied name.  If it
	 * does not exist, then it will create the group with that name
	 * 
	 * @param groupName - Name of the group to retrieve
	 * @return An instance of BoxGroup.Info that matches the name in the groupName
	 * parameter
	 * @throws AuthorizationException 
	 */
	public static BoxGroup.Info getOrCreateGroup(String groupName) throws AuthorizationException{
		BoxGroup.Info groupInfo = getGroup(groupName);
		
		if(groupInfo == null){
			try{
				groupInfo = createGroup(groupName);
			}catch(BoxAPIException e){
				logger.info(e.getResponse());
				if(e.getResponseCode()==409){
					Iterable<BoxGroup.Info> groupInfos = BoxGroup.getAllGroups(AuthorizationGenerator.getAppEnterpriseAPIConnection());
					for(BoxGroup.Info localGroupInfo : groupInfos){
						if(localGroupInfo.getName().equals(groupName)){
							groupInfo=localGroupInfo;
							break;
						}
					}
				}
			}
		}
		
		return groupInfo;
	}
	
	/**
	 * This method will create a group based on the supplied name.
	 * 
	 * @param groupName - Name of the group to create
	 * @return An instance of BoxGroup.Info that was created
	 * @throws AuthorizationException 
	 */
	public static BoxGroup.Info createGroup(String groupName) throws AuthorizationException{
		
		return BoxGroup.createGroup(AuthorizationGenerator.getAppEnterpriseAPIConnection(), groupName);
	}
}
