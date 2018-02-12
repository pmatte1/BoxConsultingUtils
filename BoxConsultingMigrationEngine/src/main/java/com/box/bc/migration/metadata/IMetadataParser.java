package com.box.bc.migration.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.box.sdk.Metadata;

/**
 * This is the interface to allow for parsing a metadata file and providing
 * the values in a consistent fashion to the executing logic
 * 
 * @author pmatte
 *
 */
public interface IMetadataParser {
	/**
	 * Identifies if the file is a metadata file or not.  Allows
	 * for different types of metadata files to be supported.
	 * 
	 * @param potentialMetadataFile - The File object representing a file that may be
	 * a metadata file
	 * @return True if the file is a metadata file, false if not
	 */
	public boolean isMetadataFile(File potentialMetadataFile);
	
	/**
	 * Since there could be lists of items to apply metadata to in
	 * 1 metadata file, this allows for implementing the correct logic
	 * for mapping the File or Folder to the metadata from the file
	 * 
	 * @param fileToApplyMetadataTo - The File object representing the file or folder
	 * to add metadata to after upload
	 * @return List containing the metadata template(s), attribute(s) and values for the attribute(s)
	 */
	public List<MetadataTemplateAndValues> getMetadata(File fileToApplyMetadataTo);
	
	/**
	 * This allows for the file to be parsed in whatever way is required to meet the
	 * requirements based on the metadata file standards
	 * 
	 * @param metadataFile - The file containing the metadata
	 */
	public void load(File metadataFile);

	/**
	 * This is create a FilenameFilter that can be used to identify any
	 * Metadata files ahead of time, so they can be loaded prior to iterating over
	 * the rest of the results.  
	 * 
	 * This ensures that if a metadata file and the file that needs metadata both reside
	 * in the same directory, we can identify the metadata file first.
	 * 
	 * @return FilenameFilter object that will retrieve all the metadata files in a directory
	 */
	public FilenameFilter getFileNameFilter();
}
