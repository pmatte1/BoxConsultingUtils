package com.box.bc.migration.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.box.bc.migration.util.XLSXToCSVConverter;

public class XlsxToCsvParserAllValues extends CSVParserAllValues {

	public XlsxToCsvParserAllValues() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void load(File metadataFile) {
				try {
					super.load(XLSXToCSVConverter.convertXLSXtoCSV(metadataFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

}
