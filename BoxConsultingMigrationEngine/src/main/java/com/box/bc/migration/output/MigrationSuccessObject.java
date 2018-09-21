package com.box.bc.migration.output;

import java.io.File;

public class MigrationSuccessObject {
	private File file;
	private String sha;
	private String BoxItem;
	public synchronized File getFile() {
		return file;
	}
	public synchronized void setFile(File file) {
		this.file = file;
	}
	public synchronized String getSha() {
		return sha;
	}
	public synchronized void setSha(String sha) {
		this.sha = sha;
	}
	public synchronized String getBoxItem() {
		return BoxItem;
	}
	public synchronized void setBoxItem(String boxItem) {
		BoxItem = boxItem;
	}
	
	

}
