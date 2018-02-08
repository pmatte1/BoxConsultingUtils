package com.box.bc.config.impl;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.apache.log4j.Logger;

import com.box.bc.config.BoxConfigProvider;
import com.box.bc.exception.AuthorizationException;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxConfig;

public class BoxConfigJsonProvider implements BoxConfigProvider {
	private static Logger logger = Logger.getLogger(BoxConfigJsonProvider.class);

	public BoxConfig getBoxConfig() throws AuthorizationException {
		String configFile = getConfigFile();


		Reader reader = null;
		BoxConfig boxConfig = null;
		try {
			URL url = this.getClass().getClassLoader().getResource(configFile);//ClassLoader.getSystemClassLoader().getSystemResource("config.json");
			logger.debug("URL: " + url);
			if(url != null){
				reader = new FileReader(url.getFile());
				boxConfig = BoxConfig.readFrom(reader);
			}else{
				throw new AuthorizationException("Cannot Find the file named " + configFile);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch(BoxAPIException e){
			logger.error(e.getResponseCode() + ": " + e.getResponse(),e);

		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					
				}
			}
		}

		return boxConfig;
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


}
