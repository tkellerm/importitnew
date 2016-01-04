package de.abaspro.infosystem.importit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExportProcessing {
	
	private ArrayList<Datensatz> datensatzList;
	
	private org.apache.poi.ss.usermodel.Workbook Workbook;
	
	
	/**
	 * @param filename
	 * Der Dateiname für die ExportDatei. Über die Endung wird auch der Dateityp festgelegt. 
	 * 
	 * @param asErrorReport
	 * Wenn gesetzt werden die Fehlerinformationen aus der Datensatzlist mit hitnen angefügt.
	 * @throws ImportitException 
	 * 
	 */
	
	
	public ExcelExportProcessing(String filename , Boolean asErrorReport) throws ImportitException {
		initWorkbook(filename);
	}


	private Workbook initWorkbook(String filename) throws ImportitException {
		if (checkFileName(filename)) {
		
			try {
	    		if (filename.contains(".xlsx")) {
	    			org.apache.poi.ss.usermodel.Workbook workbook;
				
					workbook = new XSSFWorkbook(new FileInputStream(filename));
				
	    		return workbook;
	    		
	    		}else if (filename.contains(".xls")) {
	        	   Workbook workbook = new HSSFWorkbook(new FileInputStream(filename));
	        	   return workbook;
	        	   
	    		} else {
				
	    			throw new ImportitException("Die Datei " + filename + " ist keine Excel-Datei");
	    		}
	    		
	    	} catch (FileNotFoundException e) {
	    		
				throw new ImportitException("Die Datei " + filename + " wurde nicht gefunden;" + e.getMessage());
				
			} catch (IOException e) {
				
				throw new ImportitException("Bei dem Zugriff auf die Datei " + filename + " trat ein Fehler auf;" + e.getMessage());
				
			}
			
			
			
		}
		return Workbook;
		
		
	}


	private boolean checkFileName(String filename) {

//		prüfe ob der Pfad existiert
		
//		prüfe, ob die Endung stimmt 
		
		return false;
	}



	
	
	
}
