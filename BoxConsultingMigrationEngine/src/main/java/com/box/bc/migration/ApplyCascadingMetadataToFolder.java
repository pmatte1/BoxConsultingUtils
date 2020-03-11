package com.box.bc.migration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.util.FolderUtil;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
//import com.box.sdk.BoxAPIResponseException;
import com.box.sdk.BoxFolder;
import com.box.sdk.Metadata;
import com.box.sdk.MetadataTemplate;
import com.box.sdk.MetadataTemplate.Field;
import com.box.sdk.http.HttpMethod;

public class ApplyCascadingMetadataToFolder {
	private static Logger logger = Logger.getLogger(ApplyCascadingMetadataToFolder.class);

	public static void main(String[] args) {
		try {
			BoxFolder baseFolder = FolderUtil.createFolder("0", "TEST CASCADE CLASSIFICATION");
			Iterable<MetadataTemplate> templates = MetadataTemplate.getEnterpriseMetadataTemplates(baseFolder.getAPI(), new String[0]);
			MetadataTemplate classificationTemplate = null;
			for(MetadataTemplate template : templates){
				logger.info("*************");
				logger.info("template name: " + template.getDisplayName());
				//logger.info("template id  : " + template.getID());
				logger.info("template scope: " + template.getScope());
				logger.info("template key: " + template.getTemplateKey());
				List<Field> fields = template.getFields();
				for(Field field : fields){
					logger.info("*************");
					logger.info("**" + field.getKey() + " - " + field.getOptions());
					logger.info("*************");
				}
				logger.info("*************");
				if(template.getTemplateKey().startsWith("securityClassification")){
					classificationTemplate = template;
				}

			}
			if(classificationTemplate != null){
			Metadata metadata = new Metadata();
			metadata.add("/Box__Security__Classification__Key", "US Persons Only");
			Metadata appliedMetadata = baseFolder.createMetadata(classificationTemplate.getTemplateKey(), classificationTemplate.getScope(), metadata);
			logger.info("Metadata Applied: " + appliedMetadata.getID());

//			String templateKey = appliedMetadata.getTemplateName();
//			logger.info("templateKey: " + templateKey);


			String requestBody = "{";
			requestBody += "\"folder_id\":\"" + baseFolder.getID() + "\",";
			requestBody += "\"scope\":\""+classificationTemplate.getScope()+"\",";
			requestBody += "\"templateKey\":\"" + classificationTemplate.getTemplateKey() + "\"";
			requestBody += "}";

			BoxAPIRequest apiRequest = new BoxAPIRequest(baseFolder.getAPI(), new URL("https://api.box.com/2.0/metadata_cascade_policies"), HttpMethod.POST);
			apiRequest.setBody(requestBody);
			BoxAPIResponse apiResponse = apiRequest.send();
			logger.info(apiResponse.getBody());
			}else{
				logger.error("Could not find the Classification template");
			}
		} catch (BoxAPIException e) {
			logger.error(e.getResponseCode() + ": " + e.getResponse(), e);
		} catch (AuthorizationException e) {
			logger.error(e.getMessage(), e);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage(), e);
		}

	}

}
