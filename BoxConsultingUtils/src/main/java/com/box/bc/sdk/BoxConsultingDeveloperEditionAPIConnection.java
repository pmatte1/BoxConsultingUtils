package com.box.bc.sdk;

import java.util.Properties;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.DeveloperEditionEntityType;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.JWTEncryptionPreferences;

public class BoxConsultingDeveloperEditionAPIConnection extends
BoxDeveloperEditionAPIConnection {


	public BoxConsultingDeveloperEditionAPIConnection(String entityId,
			DeveloperEditionEntityType entityType, BoxConfig boxConfig,
			IAccessTokenCache accessTokenCache) {
		super(entityId, entityType, boxConfig, accessTokenCache);
	}

	public BoxConsultingDeveloperEditionAPIConnection(String entityId,
			DeveloperEditionEntityType entityType, String clientID,
			String clientSecret, JWTEncryptionPreferences encryptionPref,
			IAccessTokenCache accessTokenCache) {
		super(entityId, entityType, clientID, clientSecret, encryptionPref,
				accessTokenCache);
	}

	/**
	 * This will retrieve the Token URL from an environment variable named boxTokenUrl
	 * or it will retrieve the URL from boxApiConnectionProps.properties from a 
	 * property named boxTokenUrl
	 * 
	 * If neither exists, it will use the default from the BoxDeveloperEditionAPIConnection class
	 */
	@Override
	public String getTokenURL() {
		if(System.getenv("boxTokenUrl") != null){
			return System.getenv("boxTokenUrl");
		}else if(getTokenUrlFromProps() != null){
			return getTokenUrlFromProps();
		}

		return super.getTokenURL();
	}

	protected Properties props = new Properties();
	protected boolean isLoaded = false;
	/**
	 * This retrieves the Token URL from the boApiConnectionProps.properties file's boxTokenUrl key
	 * 
	 * @return String containing the URL for the Auth Token
	 */
	protected String getTokenUrlFromProps() {
		try{
			if(!isLoaded) {
				props.load(this.getClass().getResourceAsStream("boxApiConnectionProps.properties"));
				isLoaded=true;
			}
			return props.getProperty("boxTokenUrl");


		}catch(Exception e){

		}
		return null;
	}



}
