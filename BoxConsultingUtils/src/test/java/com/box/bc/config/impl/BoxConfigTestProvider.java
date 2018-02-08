package com.box.bc.config.impl;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.sdk.BoxConfig;

public class BoxConfigTestProvider extends BoxConfigJsonProvider {
	private static Logger logger = Logger.getLogger(BoxConfigTestProvider.class);

	public BoxConfig getBoxConfig() throws AuthorizationException {
		logger.info("IN " + this.getClass());
		
		return super.getBoxConfig();
	}


}
