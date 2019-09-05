package com.box.bc.util;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class Utilities {
	public static Logger logger = Logger.getLogger(Utilities.class);

	public static String getIdFrom409Exception(BoxAPIException e){
		if(e.getResponseCode() == 409){
			//Get the ID from the response and get folder
			logger.info("Folder with the name " + getFolderName() + " already exists.  Configuring existing folder...");
			JSONObject responseJson = new JSONObject(e.getResponse());
			String id = responseJson.getJSONObject("context_info").getJSONArray("conflicts").getJSONObject(0).getString("id");
		}
	}
}
