package com.box.bc.util;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.box.sdk.BoxAPIException;

public class Utilities {
	public static Logger logger = Logger.getLogger(Utilities.class);

	public static String getIdFrom409Exception(BoxAPIException e){
		if(e.getResponseCode() == 409){
			//Get the ID from the response and get folder
			logger.debug("Conflict Error: " + e.getResponse());
			JSONObject responseJson = new JSONObject(e.getResponse());
			return responseJson.getJSONObject("context_info").getJSONObject("conflicts").getString("id");
		}
		
		return null;
	}
}
