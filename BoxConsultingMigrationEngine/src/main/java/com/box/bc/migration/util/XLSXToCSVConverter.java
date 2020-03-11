package com.box.bc.migration.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Hello world!
 *
 */
public class XLSXToCSVConverter {
	private static Logger logger = Logger.getLogger(XLSXToCSVConverter.class);


	/**
	 * Input to method is a 'Loan Batches' XLSX file. The return value for this method is the output CSV file, which has the same file name as the input file except with a 
	 * .csv extension. For example, if the input file is 'Prod_30.xlsx', the generated CSV file is 'Prod_30.csv'. 
	 * 
	 * The following dependencies are required for use: 
	 * 
	 * - https://mvnrepository.com/artifact/org.apache.poi/poi
	 * - https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
	 * - https://mvnrepository.com/artifact/org.apache.commons/commons-text
	 * 
	 * @param xlsxFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static File convertXLSXtoCSV(File xlsxFile) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(xlsxFile);

		// Finds the workbook instance for XLSX file
		@SuppressWarnings("resource")
		XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

		// Return first sheet from the XLSX workbook
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);

		//create stringbuilder to hold CSV content
		StringBuilder sb = new StringBuilder();


		// Traversing over each row of XLSX file
		for(int k = 0; k <= mySheet.getLastRowNum(); k++) {
			XSSFRow row = mySheet.getRow(k);

			String rowString = "";

			// For each row, iterate through each columns
			for(int i = 0; i <= row.getLastCellNum(); i++) {
				XSSFCell cell = row.getCell(i);

				//if the cell is empty (i.e. the Loan column) add an empty column
				if (cell == null || cell.getCellType() == CellType.BLANK) {
					rowString = rowString + ",";
					
				//if the cell is not empty...
				}else {
					cell.setCellType(CellType.STRING);//every cell gets converted to a string before writing to the CSV file

					switch (cell.getCellType()) {
					case STRING:

						String val = cell.getStringCellValue();

						if(val.contains(",")) {
							val = StringEscapeUtils.escapeCsv(val);
						}

						if(rowString.isEmpty()) {
							rowString = val;
						}else {
							rowString = rowString + "," + val;
						}
						break;
					default :
						//WARNING! We should never get here... see action 'cell.setCellType(CellType.STRING)' above
						System.out.println("Whoops...");
					}
				}
			}
			
			//append row from XLSX file to StringBuilder object that is holding CSV content
			sb.append(rowString + "\n");
		}

		//create CSV file object to be returned
		File csvFile = new File(xlsxFile.getName().split("\\.")[0] + ".csv");

		/* This logic will make sure that the file 
		 * gets created if it is not present at the
		 * specified location*/
		if (csvFile.exists()) {
			csvFile.delete();
		}
		csvFile.createNewFile();

		//write to CSV file
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
			bw.append(sb);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return csvFile;
	}
}
