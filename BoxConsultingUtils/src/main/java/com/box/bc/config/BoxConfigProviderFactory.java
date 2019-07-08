package com.box.bc.config;

import java.util.Properties;

import com.box.bc.config.impl.BoxConfigJsonProvider;
import com.box.bc.util.PropertiesUtil;

public class BoxConfigProviderFactory {
	
	public static BoxConfigProvider getBoxConfigProvider() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		Properties appUserManagerProps = PropertiesUtil.getPropertiesFromFile("appUserManager.properties");
		if(appUserManagerProps.containsKey("boxconfigprovider")){
			return (BoxConfigProvider)(Class.forName(appUserManagerProps.getProperty("boxconfigprovider")).newInstance());
		}else{
			
		}
		
		return new BoxConfigJsonProvider();
	}

}
