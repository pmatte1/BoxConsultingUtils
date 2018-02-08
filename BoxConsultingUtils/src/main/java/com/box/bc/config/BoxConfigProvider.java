package com.box.bc.config;

import com.box.bc.exception.AuthorizationException;
import com.box.sdk.BoxConfig;

public interface BoxConfigProvider {
	public BoxConfig getBoxConfig() throws AuthorizationException;
}
