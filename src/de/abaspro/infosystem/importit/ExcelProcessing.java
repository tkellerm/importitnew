package de.abaspro.infosystem.importit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author tkellermann
 * 
 *In der Klasse ExcelProcessing soll die ExcelDatei ausgelesen 
 *und in das Object Datensatz geschrieben werden.
 *
 *
 */

public class ExcelProcessing {

	private org.apache.poi.ss.usermodel.Workbook importWorkbook;
	private org.apache.poi.ss.usermodel.Workbook errorWorkbook;
	private org.apache.poi.ss.usermodel.Sheet importSheet;
	private org.apache.poi.ss.usermodel.Sheet errorSheet;
	
	private ArrayList<Datensatz> datensatzList;
	private List<Feld> kopfFelder ;
	private DatensatzTabelle tabellenFelder;
	
	private String importFilename; 
	private String errorFilename;
	private Integer datenbank;
	private Integer gruppe;
	private Integer tippkommando;
	private Integer tabelleAbFeld; 
	private int anzahlDatensaetze;
	private OptionCode optionCode;
	
	public ExcelProcessing(String importFilename) throws ImportitException {
		super();
		this.kopfFelder = new ArrayList<Feld>();
		this.tabellenFelder = new DatensatzTabelle();
		this.datensatzList = new ArrayList<Datensatz>();
		this.importFilename = importFilename;
		checkImportFile(importFilename);
		this.importWorkbook = initWorkbook(importFilename);
		this.importSheet = importWorkbook.getSheetAt(0);
		pruefeUndHoleInfoAusSheet(this.importSheet);
		this.kopfFelder = readFieldinHead(this.importSheet , this.tabelleAbFeld);
		this.tabellenFelder = readFieldInTable(this.importSheet , this.tabelleAbFeld);
		this.datensatzList = readAllData(this.importSheet);
	}
	
	
	
	
	/**
	 * @param importSheet2
	 * @return
	 * @throws ImportitException
	 * 
	 * Über die Funktion readAllData werden die Daten aus dem Excelzeilen in die Datensatzstruktur eingelesen.
	 * Über das Keyfeld wird gesteuert, ob ein neuer Datensatz angelegt 
	 * 
	 */
	private ArrayList<Datensatz> readAllData(Sheet importSheet2) throws ImportitException {
	
		ArrayList<Datensatz> datensatzListtemp = new ArrayList<Datensatz>();
		Integer row = 2;
		Integer col = 0;
		Datensatz datensatz = null;
		for (row = row ; row < getMaxRow(importSheet2); row++) {
//			prüfen, ob noch gleicher Datensatz normal Col = 0, wenn key - Feldnummer aus kopfffelder holen angegeben dann
			
			if (datensatz==null) {
				
//				Es wird die erste Datenzeile in der ExcelTabelle gelesen
				
				datensatz = fuellValueInKopfdatensatz(importSheet2 , row); 
				datensatzListtemp.add(datensatz);		
				
			}else {
				
//				Es wird eine weitere Zeile gelesen
//				prüfen ob noch gleicher Kopfdatensatz Prüfung über keyfeld(ColNumber)
				
				if (datensatz.getValueOfKeyfield().equals(getZellenInhaltString(importSheet2, datensatz.getKeyfield(), row))) {
					//Es ist der gleiche Datensatz aber neue Tabellenzeile
						List<DatensatzTabelle> tabelle = datensatz.getTabellenzeilen();
						
						DatensatzTabelle datensatzTabelle = new DatensatzTabelle(this.tabellenFelder);
						ArrayList<Feld> tabrow = datensatzTabelle.getTabellenFelder();
						 tabrow = this.tabellenFelder.getTabellenFelder();
						for (Feld feld : tabrow) {
							feld.setValue(getZellenInhaltString(importSheet2, feld.getColNumber(), row));
						}
//						Datensatz an Tabelle anfügen
						tabelle.add(datensatzTabelle);						
					
				}else {
					
					//Es fängt ein neuer Datensatz an
					
					datensatz = fuellValueInKopfdatensatz(importSheet2 , row);
					datensatzListtemp.add(datensatz);
				}
			}
			
		}
		
		return datensatzListtemp;
	}



	/**
	 * @param importSheet2
	 * @param tableBeginAtRow
	 * @return
	 * @throws ImportitException
	 * 
	 * Liest die Werte aus bis zur Spalte @tableBeginAtRow aus und schreibt sie in das Value der einzeln Kopffeldern
	 * 
	 */
	/**
	 * @param importSheet2
	 * @param tableBeginAtRow
	 * @return
	 * @throws ImportitException
	 */
	private Datensatz fuellValueInKopfdatensatz(Sheet importSheet2, Integer tableBeginAtRow) throws ImportitException {

		Datensatz datensatz = initNewDatensatz();
		List<Feld> kopffelder = datensatz.getKopfFelder();
		for (Feld feld : kopffelder) {
			feld.setValue(getZellenInhaltString(importSheet2, feld.getColNumber(), tableBeginAtRow));

		}

		return datensatz;
		
	}




	private Datensatz initNewDatensatz() throws ImportitException {

		Datensatz datensatz = new Datensatz();
		datensatz.setKopfFelder(this.kopfFelder);
		datensatz.setDatenbank(this.datenbank);
		datensatz.setGruppe(this.gruppe);
		datensatz.setTippkommando(this.tippkommando);
		datensatz.setOptionCode(this.optionCode);
		datensatz.setTableStartsAtField(this.tabelleAbFeld);
		List<DatensatzTabelle> tabelle = datensatz.getTabellenzeilen();
		return datensatz;
	}




	private List<Feld> leseDataKopffelder(List<Feld> kopfFelder2) {
		// TODO Auto-generated method stub
		return null;
	}




	public ArrayList<Datensatz> getDatensatzList() {
		return datensatzList;
	}



	private  List<Feld> readFieldinHead(Sheet importSheet2, Integer tabelleAbFeld2) throws ImportitException {

//			alle Felder in der Zeile2 durchlaufen, ob Sie in der Datenbank vorhanden sind
//			Falls ja, dann in Struktur einfügen
			List<Feld> kopfFelder2 = new ArrayList<Feld>();
			String variable;
			
//			Tabellefelder  vorhanden
				Integer row = 1;
				for (int col = 0; (col < getMaxCol(importSheet2)) && (col < (this.tabelleAbFeld) || this.tabelleAbFeld == 0) ; col++) {
					
//					Zelleninhalt auslesen und Feld Konstruktor übergeben

					Feld feld = new Feld(getZellenInhaltString(importSheet2, col, row), true , col );

//					hänge das Feld an die Kopffelder an
					
					kopfFelder2.add(feld);
					
				}
				
			return kopfFelder2;	
	}
		

	private DatensatzTabelle readFieldInTable(Sheet importSheet2,	Integer tabelleAbFeld2) {
//		alle Felder in der Zeile2 durchlaufen, ob Sie in der Datenbank vorhanden sind
//		Falls ja, dann in Struktur einfügen
		DatensatzTabelle datensatzTabelle = new DatensatzTabelle();
		
		List<Feld> tabellenFelder2 = datensatzTabelle.getTabellenFelder();
		String variable;
//		Tabellenfelder  vorhanden
			Integer row = 1;
			if (tabelleAbFeld2 > 0) {
				for (int col = this.tabelleAbFeld; col < getMaxCol(importSheet2) ; col++) {
					
//					Zelleninhalt auslesen und Feld Konstruktor übergeben

					Feld feld = new Feld(getZellenInhaltString(importSheet2, col, row), true , col );

//					hänge das Feld an die Kopffelder an
					
					tabellenFelder2.add(feld);
					
				}
			}
		return datensatzTabelle;	
	}
	
	
	
	private void checkImportFile(String filename) throws ImportitException{
    	File file = new File(filename);
    	if (file.exists() & file.isFile() ) {
			if (file.canRead()) {
				if (filename.contains(".xlsx") || filename.contains(".xls")) {
					this.importFilename = file.getAbsolutePath();	
				}else {
					throw new ImportitException("Die Datei " + filename + " ist keine Excel-Datei");
				}

								
			}else {
		
				throw new ImportitException("Die Importdatei " + filename + "ist nicht mit Lesen-Rechte versehen!");
			}
			
		}else {
			
			throw new ImportitException("Die Importdatei " + filename + " ist nicht vorhanden bzw keine Datei");
			
		}

    }

	private Workbook initWorkbook(String filename) throws ImportitException {
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
	
	private void pruefeUndHoleInfoAusSheet(Sheet sheet) throws ImportitException {
		
			try{
				
			this.datenbank         = getdb(sheet);
			this.gruppe            = getgroup(sheet);
			this.tippkommando      = getTippkomanndo(sheet);
			this.tabelleAbFeld     = getTabelleab(sheet); 
			this.optionCode        = new OptionCode(getOptionCodeFromSheet(sheet));
			this.anzahlDatensaetze = getAnzDatenzeilen(this.importSheet);
			
			}catch (NumberFormatException e){
				throw new ImportitException("Es war die Datenbank bzw. das Tippkommando im falschen Format angegeben!");
			}
					
	}
	
	private int getMaxCol(Sheet sheet) {
	// Anzahl Spalten(Cols) in der 2. Zeile (entspricht 1) 
		 Integer maxcol = sheet.getRow(1).getPhysicalNumberOfCells();
	return maxcol;
}


	private int getMaxRow(Sheet sheet) {
		// TODO Auto-generated method stub
			
		return sheet.getLastRowNum();
}
	
	private Integer getTippkomanndo(Sheet sheet) throws ImportitException {
		
//		Es ist ein Tipkommdo, wenn es keinen Doppelpunkt besitzt 		
		String dbgroup = getdbgroup(sheet);
		String tippString;
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt == 0){
        	tippString = dbgroup.substring(0,doppelpunkt);
        	
        	if (!tippString.isEmpty() && tippString != null ){
        		
        		try {
        			return Integer.parseInt(tippString);	
        			
				} catch (NumberFormatException e) {
					
					throw new ImportitException("Das Tippkommando wurde nicht richtig angegeben, so dass die Umformatierung in einen Integer-Wert nicht funktioniert!");
				
				}
        		
        	}else {
        		
				return null;
			}
        	
        }else {
			return null;
		}    
	}	
	
	private Boolean isZelleleer(Sheet sheet, int x, int y) {
		// Prüft ob die Zelle leer ist
		Cell cell = sheet.getRow(y).getCell(x);
		
		if (cell == null) {
			return true;
		}else {
			if (sheet.getRow(y).getCell(x).getCellType() == Cell.CELL_TYPE_BLANK){
				return true;
			}else {
				if (getZellenInhaltString(sheet, x, y).equals("")) {
					return true;
				}else {
					if (getZellenInhaltString(sheet, x, y) == null) {
						return null;
					}else {
						return false;
					}
				}
			}
		}					
	}
	
	private Integer getOptionCodeFromSheet(
			Sheet sheet) throws ImportitException {
		try{
		if (isZelleleer(sheet, 2, 0)) {
			return 0;
		}else {
			Integer option=Integer.parseInt(getZellenInhaltString(sheet, 2, 0));
			return option;	
		}	
				
		}catch (NumberFormatException e) {
			
			throw new ImportitException("Es war in dem Feld Optionscode ein üngültiger Integerwert eingetragen !");
			
		}
		
	}
	
private Integer getgroup(Sheet sheet) throws ImportitException {
		
//		nach dem Doppelpunkt steht die Datenbankgruppe
//		leerer String wenn es ein Tippkommando ist
		
		String dbgroup = getdbgroup(sheet);
		String groupString;
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt>0)
     
        {

        	groupString = dbgroup.substring(0,doppelpunkt);
        	
        	if (!groupString.isEmpty() && groupString != null ){
        		
        		try {
        			return Integer.parseInt(groupString);	
        			
				} catch (NumberFormatException e) {
					
					throw new ImportitException("Die Gruppe wurde nicht richtig angegeben, so dass die Umformatierung in einen Integer-Wert nicht funktioniert!");
				
				}
        		
        		
        		
        	}else {
				return null;
			}
        	 	
        	
        }else{
            return null;
        }
    }

	private Integer getdb(Sheet sheet) throws ImportitException {
		
	//		vor dem Doppelpunkt steht die Datenbank
	//		leerer String wenn es ein Tippkommando ist
			
			String dbgroup = getdbgroup(sheet);
			String dbString;
			
			int doppelpunkt =dbgroup.indexOf(":");
	        
	        if (doppelpunkt>0)
	     
	        	{
	        	dbString = dbgroup.substring(0,doppelpunkt);
	        	
	        	if (!dbString.isEmpty() && dbString != null ){
	        		
	        		try {
	        			return Integer.parseInt(dbString);	
	        			
					} catch (NumberFormatException e) {
						
						throw new ImportitException("Die Gruppe wurde nicht richtig angegeben, so dass die Umformatierung in einen Integer-Wert nicht funktioniert!");
					
					}
	        		
	        		
	        	}else {
					return null;
				}
	        	 
	        
	        	}else {
	        		return null;
	        		}
	        }
	
	private String getdbgroup(Sheet sheet) {
	 		
		
		return getZellenInhaltString(sheet, 0, 0);
        
	}
	
	private Integer getTabelleab(Sheet sheet) throws ImportitException {
		
		try{
		Integer tabAbFeld = Integer.parseInt(getZellenInhaltString(sheet, 1, 0));
//		Da in Poi die erste Spalte mit 0 zählt muss der Wert 1 abgezogen werden
		if (tabAbFeld > 2) {
			tabAbFeld = tabAbFeld - 1;
		}
		 
		return tabAbFeld;
		}catch (NumberFormatException e) {
			
			throw new ImportitException("Es war in dem Feld TabelleAbFeld ein üngültiger Integerwert eingetragen !");
			
		}
		
		
	}

	private String getZellenInhaltString(Sheet sheet, int x, int y) {
//		Hier werden alle Inhaltsmöglichkeiten einer Celle in einen String umgewandelt 
		Cell cell = sheet.getRow(y).getCell(x);
		if (cell != null) {
			
			if ( cell.getCellType() == Cell.CELL_TYPE_STRING)  {
				return cell.getStringCellValue();
			}else {
		
				if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC)  {
			
					Double nummericvalue = cell.getNumericCellValue();
					Integer intvalue = nummericvalue.intValue();
					if 	(intvalue.doubleValue()  == nummericvalue){
						return intvalue.toString();
					}else {
						return nummericvalue.toString();	
				}
				
			}else {
				if ( cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
					if (cell.getBooleanCellValue() == true) {
						return "ja";
					}else {
						return "nein";
					}
				}else {
						if ( cell.getCellType() == Cell.CELL_TYPE_FORMULA){
								if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
										return cell.getStringCellValue();
								}else {
									if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
										Double nummericvalue = cell.getNumericCellValue();
										return nummericvalue.toString();
									}else {
										if ( cell.getCachedFormulaResultType() == Cell.CELL_TYPE_BOOLEAN){
											if (cell.getBooleanCellValue() == true) {
													return "ja";
											}else {
													return "nein";
											}
										}else {
											if ( cell.getCachedFormulaResultType() == Cell.CELL_TYPE_BLANK){
												return "";
											}else {
												if ( cell.getCachedFormulaResultType() == Cell.CELL_TYPE_ERROR){
														return null;
												}
											}
										}
									}
								}
							}else {
								if ( cell.getCellType() == Cell.CELL_TYPE_BLANK){
									return "";
								}else {
									if ( cell.getCellType() == Cell.CELL_TYPE_ERROR){
										return null;
									}
									
								}
		
	
							}	
	
					}
				}
			}
		
		}
	// Falls irgendein Fall vergessen wurde wird null übertragen
		return null;
	
	}
	
	private int getAnzDatenzeilen(Sheet poisheet) {
		// holt alle gefüllten Zeilen aus dem sheet
		// laut Definition sind die ersten beidenZeilen nur für die Konfiguration 
			return poisheet.getPhysicalNumberOfRows()-2;
	}
}
