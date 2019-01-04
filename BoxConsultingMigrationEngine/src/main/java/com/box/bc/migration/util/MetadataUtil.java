package com.box.bc.migration.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.box.bc.migration.metadata.MetadataTemplateAndValues;
import com.box.bc.migration.metadata.parser.CustomMetadata;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFile.Info;
import com.box.sdk.Metadata;

public class MetadataUtil {
	private static Logger logger = Logger.getLogger(MetadataUtil.class);

	public void applyMetadataTemplateAndVals(Info createdFile,
			List<MetadataTemplateAndValues> metadataTemplateAndVals) {
		logger.info("Start applyMetadataTemplateAndVals to " + createdFile.getName());
		applyMetadataTemplateAndVals(createdFile.getResource(), metadataTemplateAndVals);
		logger.info("End applyMetadataTemplateAndVals");



	}

	public void applyMetadataTemplateAndVals(BoxFile createdFile,
			List<MetadataTemplateAndValues> metadataTemplateAndVals) {
		if(metadataTemplateAndVals != null){
			//logger.info("list is not null and is of size " + metadataTemplateAndVals.size());
			for(MetadataTemplateAndValues templateAndVal : metadataTemplateAndVals){
				try{

					//If a template name is specified, then create with the template
					//otherwise, just add as custom metadata
					if(templateAndVal.getMetadataTemplateName() != null && 
							!templateAndVal.getMetadataTemplateName().equals(CustomMetadata.CUSTOM_METADATA_TEMPLATE_NAME) && 
							templateAndVal.getMetadataValues() != null){
						createdFile.createMetadata(templateAndVal.getMetadataTemplateName(),
								templateAndVal.getMetadataValues());
					}else{
						if(templateAndVal.getMetadataValues() != null){
							createdFile.createMetadata(templateAndVal.getMetadataValues());
						}
					}
				}catch(BoxAPIException e){
					if(e.getResponseCode()==409){
						logger.warn("The template " + templateAndVal.getMetadataTemplateName() + " already exists on the object with the ID " + createdFile.getID());
						Metadata existingMd = createdFile.getMetadata(templateAndVal.getMetadataTemplateName());
						logger.warn("Metadata values before: " + existingMd);
						Metadata updatedMd = templateAndVal.getMetadataValues();

						List<String> propPaths = updatedMd.getPropertyPaths();
						for(String path: propPaths){
							existingMd.add(path, updatedMd.get(path));
						}

						createdFile.updateMetadata(existingMd);
					}else{
						logger.error("Error Adding Metadata to " + createdFile.getID() + " ERROR: " + e.getResponseCode() + "-" + e.getResponse());
					}
				}

			}
		}

	}

	public void applyMetadataTemplateAndVals(BoxFolder createdFolder,
			List<MetadataTemplateAndValues> metadataTemplateAndVals) {
		if(metadataTemplateAndVals != null){
			logger.info("list is not null and is of size " + metadataTemplateAndVals.size());
			for(MetadataTemplateAndValues templateAndVal : metadataTemplateAndVals){
				try{
					//If a template name is specified, then create with the template
					//otherwise, just add as custom metadata
					if(templateAndVal.getMetadataTemplateName() != null && 
							!templateAndVal.getMetadataTemplateName().equals(CustomMetadata.CUSTOM_METADATA_TEMPLATE_NAME) && 
							templateAndVal.getMetadataValues() != null){
						createdFolder.createMetadata(templateAndVal.getMetadataTemplateName(),
								templateAndVal.getMetadataValues());
					}else{
						if(templateAndVal.getMetadataValues() != null){
							createdFolder.createMetadata(templateAndVal.getMetadataValues());
						}
					}
				}catch(BoxAPIException e){
					if(e.getResponseCode()==409){
						logger.error("The template " + templateAndVal.getMetadataTemplateName() + " already exists on the object with the ID " + createdFolder.getID() + " we will update the existing template");
						Metadata existingMd = createdFolder.getMetadata(templateAndVal.getMetadataTemplateName());
						logger.error("Metadata values before: " + existingMd);
						Metadata updatedMd = templateAndVal.getMetadataValues();

						List<String> propPaths = updatedMd.getPropertyPaths();
						for(String path: propPaths){
							existingMd.add(path, updatedMd.get(path));
						}

						createdFolder.updateMetadata(existingMd);
					}
				}
			}
		}

	}

}
