package com.box.bc.migration.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class NoMetadataAppliedParser implements IMetadataParser {

	/**
	 * This method will always return false, as there will not be any 
	 * metadata applied with this implementation
	 */
	public boolean isMetadataFile(File file) {
		return false;
	}

	/**
	 * This method will always return null, as there will not be any
	 * metadata applied with this implementation
	 */
	public List<MetadataTemplateAndValues> getMetadata(File file) {
		return new ArrayList<MetadataTemplateAndValues>();
	}

	/**
	 * This is an empty method as no parsing is required
	 */
	public void load(File metadataFile) {
		// TODO Auto-generated method stub
		
	}

	public FilenameFilter getFileNameFilter() {
		// TODO Auto-generated method stub
		return null;
	}

}
