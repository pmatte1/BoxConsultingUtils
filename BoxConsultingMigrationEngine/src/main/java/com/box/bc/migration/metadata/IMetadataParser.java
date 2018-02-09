package com.box.bc.migration.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.box.sdk.Metadata;

public interface IMetadataParser {
	public boolean isMetadataFile(File potentialMetadataFile);
	
	public List<MetadataTemplateAndValues> getMetadata(File fileToApplyMetadataTo);
	
	public void load(File metadataFile);

	public FilenameFilter getFileNameFilter();
}
