package com.box.bc.example;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONRequest;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.http.HttpMethod;

public class BoxItemCountRetriever {
	private static Logger logger = Logger.getLogger(BoxItemCountRetriever.class);

	private BoxAPIConnection api;

	public BoxItemCountRetriever(BoxAPIConnection api) {
		this.api=api;
	}

	public long getItemCount(BoxFolder folder){
		long itemCount = 0L;


		try {
			BoxJSONRequest request = new BoxJSONRequest(this.api,new URL("https://api.box.com/2.0/folders/"+folder.getID()+"/items?fields=id"), HttpMethod.GET);
			BoxJSONResponse response = (BoxJSONResponse)request.send();
			logger.info("Box Response: " + response.getJSON());

			itemCount = response.getJsonObject().get("total_count").asLong();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return itemCount;
	}

}
