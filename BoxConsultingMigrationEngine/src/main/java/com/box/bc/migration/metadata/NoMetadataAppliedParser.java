package com.box.bc.migration.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This implementation ensures no metadata is applied to any
 * of the files or folders processed in the migration
 * 
 * @author pmatte
 *
 */
public class NoMetadataAppliedParser implements IMetadataParser {

	/**
	 * This method will always return false, as there will not be any 
	 * metadata applied with this implementation
	 */
	public boolean isMetadataFile(File file) {
		return false;
	}

	/**
	 * This method will always return an empty list, as there will not be any
	 * metadata applied with this implementation
	 */
	public List<MetadataTemplateAndValues> getMetadata(File file) {
		return new ArrayList<MetadataTemplateAndValues>();
	}

	/**
	 * This is an empty method as no parsing is required
	 */
	public void load(File metadataFile) {
		
		
	}

	/**
	 * This method always returns NULL since there will not be a 
	 * metadata property file, since there is no metadata.
	 */
	public FilenameFilter getFileNameFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<MetadataTemplateAndValues>> getAllMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

}
