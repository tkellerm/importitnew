package de.abaspro.infosystem.importit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.abas.erp.common.type.AbasDate;

/**
 * @author tkellermann
 * 
 *In der Klasse ExcelProcessing soll die ExcelDatei ausgelesen 
 *und in das Object Datensatz geschrieben werden.
 *
 *
 */

public class ExcelImportProcessing {

	private org.apache.poi.ss.usermodel.Workbook importWorkbook;
	private org.apache.poi.ss.usermodel.Workbook errorWorkbook;
	private org.apache.poi.ss.usermodel.Sheet importSheet;
	private org.apache.poi.ss.usermodel.Sheet errorSheet;
	
	private ArrayList<Datensatz> datensatzList;
	private List<Feld> kopfFelder ;
	private DatensatzTabelle tabellenFelder;
	
	private String importFilename; 
	private String errorFilename;
	private Integer db;
	private Integer gruppe;
	private String dbString;
	private String dbgroupString;
	private Integer tippkommando;
	private String tippkommandoString;
	private Integer tabelleAbFeld; 
	private int anzahlDatensaetze;
	private OptionCode optionCode;
	private Logger logger = Logger.getLogger(Importit21.class);
	
	public ExcelImportProcessing(String importFilename) throws ImportitException {
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
	 * �ber die Funktion readAllData werden die Daten aus dem Excelzeilen in die Datensatzstruktur eingelesen.
	 * �ber das Keyfeld wird gesteuert, ob ein neuer Datensatz angelegt
	 *  
	 * 
	 */
	private ArrayList<Datensatz> readAllData(Sheet importSheet2) throws ImportitException {
	
		ArrayList<Datensatz> datensatzListtemp = new ArrayList<Datensatz>();
		Integer rowstart = 2;
		Integer col = 0;
		Datensatz datensatz = null;
		for (Integer row = rowstart ; row <= getMaxRow(importSheet2); row++) {
//			pr�fen, ob noch gleicher Datensatz normal Col = 0, wenn key - Feldnummer aus kopfffelder holen angegeben dann
//			Wenn das erste Feld in der Zeile leer ist wird die Zeile ignoriert
			if (!getZellenInhaltString(importSheet2, 0, row).isEmpty()) {
				if (datensatz==null) {
					
//					Es wird die erste Datenzeile in der ExcelTabelle gelesen
					
					datensatz = fuellValueInKopfdatensatz(importSheet2 , row);
					
					datensatzListtemp.add(datensatz);		
					
				}else {
					
//					Es wird eine weitere Zeile gelesen
//					pr�fen ob noch gleicher Kopfdatensatz Pr�fung �ber keyfeld(ColNumber)
					String valueKeyfield = getZellenInhaltString(importSheet2, datensatz.getKeyfield(), row);
					if ((!datensatz.getValueOfKeyfield().equals(valueKeyfield)) || this.tabellenFelder == null) {
							//Es f�ngt ein neuer Datensatz an
							datensatz = null;
							datensatz = fuellValueInKopfdatensatz(importSheet2 , row);
							datensatzListtemp.add(datensatz);
					}

				}
//				falls es eine Tabelle gibt muss Sie in jeder Zeile ausgelesen werden
				readTableData(importSheet2 ,row , datensatz);	
			}
						
		}
		
		return datensatzListtemp;
	}



	private void readTableData(Sheet importSheet2, Integer row, Datensatz datensatz) throws ImportitException {
		
//		Wenn keine tabellenfelder definiert sind, d�rfen auch keine Tabellen gef�llt werden.
		
		if (this.tabellenFelder !=null) {
				
			List<DatensatzTabelle> tabelle = datensatz.getTabellenzeilen();	
				DatensatzTabelle datensatzTabelle = new DatensatzTabelle(this.tabellenFelder);
				ArrayList<Feld> tabrow = datensatzTabelle.getTabellenFelder();
//				tabrow = this.tabellenFelder.getTabellenFelder();
				
				for (Feld feld : tabrow) {
					feld.setValue(getZellenInhaltString(importSheet2, feld.getColNumber(), row));
				}
				/**
				 * row ist der Zeilenz�hler in der ExcelTabelle und die Daten beginnen in row 2
				 * um die Pr�fung der Inhalte durchzuf�hren muss eine Zeile in dem Datensatz mit den Tabellenfeldern vorhanden sein
				*/
				if(!datensatzTabelle.isEmpty() || row == 2 ){
//					Datensatz an Tabelle anf�gen
					
					tabelle.add(datensatzTabelle);						
				}

			}
				
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
		
		datensatz.setKopfFelder(getCopyOfKopffelder());
		datensatz.setDatenbank(this.db);
		datensatz.setDbString(this.dbString);
		datensatz.setGruppe(this.gruppe);
		datensatz.setDbGroupString(this.dbgroupString);
		datensatz.setTippkommando(this.tippkommando);
		datensatz.setTippcommandString(this.tippkommandoString);
		datensatz.setOptionCode(this.optionCode);
		datensatz.setTableStartsAtField(this.tabelleAbFeld);
		
		return datensatz;
		
	}



	private List<Feld> getCopyOfKopffelder() throws ImportitException {
		
		
		List<Feld> kopfFelderNeu = new ArrayList<Feld>();
		Feld feldneu;
		for (Feld feld : this.kopfFelder) {
			feldneu = new Feld(feld.getCompleteContent(),feld);
			kopfFelderNeu.add(feldneu);
		}
		
		return kopfFelderNeu;
		
	}
	
	
	public ArrayList<Datensatz> getDatensatzList() {
		return datensatzList;
	}



	private  List<Feld> readFieldinHead(Sheet importSheet2, Integer tabelleAbFeld2) throws ImportitException {

//			alle Felder in der Zeile2 durchlaufen, ob Sie in der Datenbank vorhanden sind
//			Falls ja, dann in Struktur einf�gen
			List<Feld> kopfFelder2 = new ArrayList<Feld>();
			String variable;
			
//			Tabellefelder  vorhanden
				Integer row = 1;
				for (int col = 0; (col < getMaxCol(importSheet2)) && (col < (this.tabelleAbFeld) || this.tabelleAbFeld == 0) ; col++) {
					
//					Zelleninhalt auslesen und Feld Konstruktor �bergeben
					String feldInhalt = getZellenInhaltString(importSheet2, col, row);
//					Falls das Feld komplettleer ist dann nicht in Feldliste aufnehmen
					
					if (feldInhalt !=null) {
						if (!feldInhalt.isEmpty()) {
							Feld feld = new Feld(feldInhalt, true, col);

							//						h�nge das Feld an die Kopffelder an

							kopfFelder2.add(feld);
						}
					}
					
					
				}
				
			return kopfFelder2;	
	}
		

	private DatensatzTabelle readFieldInTable(Sheet importSheet2,	Integer tabelleAbFeld2) throws ImportitException {
//		alle Felder in der Zeile2 durchlaufen, ob Sie in der Datenbank vorhanden sind
//		Falls ja, dann in Struktur einf�gen
		DatensatzTabelle datensatzTabelle = new DatensatzTabelle();
		
		List<Feld> tabellenFelder2 = datensatzTabelle.getTabellenFelder();
		String variable;
//		Tabellenfelder  vorhanden
		Integer row = 1;
			if (tabelleAbFeld2 > 0) {
				for (int col = this.tabelleAbFeld; col < getMaxCol(importSheet2) ; col++) {
					
//					Zelleninhalt auslesen und Feld Konstruktor �bergeben
					
					String feldInhalt;
					
						feldInhalt = getZellenInhaltString(importSheet2, col, row);
						if (!feldInhalt.isEmpty()) {
							Feld feld = new Feld(feldInhalt, true , col );

//							h�nge das Feld an die Tabellenfelder an
							
							tabellenFelder2.add(feld);	
						}
					
					
					
					
					
				}
				return datensatzTabelle;
			}else {
				return null;
			}
			
			
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
			this.dbString          = getdbString(sheet); 
			this.dbgroupString     = getdbgroupString(sheet);
			this.tippkommandoString = getTippkomanndoString(sheet);
			this.db                = getdb(sheet);
			this.gruppe            = getdbgroup(sheet);
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
	
	private String getTippkomanndoString(Sheet sheet) throws ImportitException {
//		Es ist ein Tipkommdo, wenn es keinen Doppelpunkt besitzt 		
		String dbgroup = getDbGroupComplete(sheet);
		
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt == 0 || doppelpunkt == -1){
        	return dbgroup;
        }else {
			return null;
		}
	}




	private Integer getTippkomanndo(Sheet sheet) throws ImportitException {
		
			String tippString = getTippkomanndoString(sheet);
			
        	if (tippString != null) {
				if (!tippString.isEmpty()) {

					try {
						return Integer.parseInt(tippString);

					} catch (NumberFormatException e) {

						//					throw new ImportitException("Das Tippkommando wurde nicht richtig angegeben, so dass die Umformatierung in einen Integer-Wert nicht funktioniert!");
						return null;
					}

				} else {

					return null;
				}
			}else {
				return null;
			}
        	
        }   	
	
	private Boolean isZelleleer(Sheet sheet, int x, int y) throws ImportitException {
		// Pr�ft ob die Zelle leer ist
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
			
			throw new ImportitException("Es war in dem Feld Optionscode ein �ng�ltiger Integerwert eingetragen !");
			
		}
		
	}
	
private Integer getdbgroup(Sheet sheet) throws ImportitException {
		
//		nach dem Doppelpunkt steht die Datenbankgruppe
//		leerer String wenn es ein Tippkommando ist
		
		String dbgroup = getDbGroupComplete(sheet);
		String groupString;
		int doppelpunkt =dbgroup.indexOf(":") + 1;
        
        if (doppelpunkt>0)
     
        {

        	groupString = dbgroup.substring(doppelpunkt,dbgroup.length());
        	
        	if (!groupString.isEmpty() && groupString != null ){
        		
        		try {
        			return Integer.parseInt(groupString);	
        			
				} catch (NumberFormatException e) {
					return null;
//					throw new ImportitException("Die Gruppe wurde nicht richtig angegeben, so dass die Umformatierung in einen Integer-Wert nicht funktioniert!");
				
				}
        		
        		
        		
        	}else {
				return null;
			}
        	 	
        	
        }else{
            return null;
        }
    }

	private String getdbString(Sheet sheet) throws ImportitException{
		//		vor dem Doppelpunkt steht die Datenbank
		//		leerer String wenn es ein Tippkommando ist
				
				String dbgroup = getDbGroupComplete(sheet);
				String dbString;
				
				int doppelpunkt =dbgroup.indexOf(":");
		        
		        if (doppelpunkt>0){
		        		return dbgroup.substring(0,doppelpunkt);
		        	}else {
		        		return null;
					}
		
	}
	
	private String getdbgroupString(Sheet sheet) throws ImportitException{
//		nach dem Doppelpunkt steht die Datenbankgruppe
//		leerer String wenn es ein Tippkommando ist
		
		String dbgroup = getDbGroupComplete(sheet);
		String groupString;
		int doppelpunkt =dbgroup.indexOf(":") + 1;
        
        if (doppelpunkt>0)
     
        {

        	return dbgroup.substring(doppelpunkt,dbgroup.length());
        }else {
        	
			return  null;
		}
	}

	private Integer getdb(Sheet sheet) throws ImportitException {
		
	//		vor dem Doppelpunkt steht die Datenbank
	//		leerer String wenn es ein Tippkommando ist
			
				
	        	String databaseString = this.dbString;
	        	if (databaseString != null ){
		        	if (!databaseString.isEmpty() ){
		        		
		        		try {
		        			return Integer.parseInt(databaseString);	
		        			
						} catch (NumberFormatException e) {
							
							return null;
	//						throw new ImportitException("Die Gruppe wurde nicht richtig angegeben, so dass die Umformatierung in einen Integer-Wert nicht funktioniert!");
						
						}
		        		
		        		
		        	}else {
						return null;
					}
	        	}else {
					return null;
				}
	        	 
	        
	        	
	        }
	
	private String getDbGroupComplete(Sheet sheet) throws ImportitException {
	 		
		
		return getZellenInhaltString(sheet, 0, 0);
        
	}
	
	private Integer getTabelleab(Sheet sheet) throws ImportitException {
		Integer tabAbFeld = 0;
		try{
			String zelleninhalt = getZellenInhaltString(sheet, 1, 0);
			if (zelleninhalt.length() > 0) {
				tabAbFeld = Integer.parseInt(zelleninhalt);
//				Da in Poi die erste Spalte mit 0 z�hlt muss der Wert 1 abgezogen werden
				if (tabAbFeld > 1) {
					tabAbFeld = tabAbFeld - 1;
				}else {
					tabAbFeld = 0;
				}
			}
		
		 
		return tabAbFeld;
		}catch (NumberFormatException e) {
			
			throw new ImportitException("Es war in dem Feld TabelleAbFeld ein �ng�ltiger Integerwert eingetragen !");
			
		}
		
		
	}

	private String getZellenInhaltString(Sheet sheet, int x, int y) throws ImportitException {
//		Hier werden alle Inhaltsm�glichkeiten einer Celle in einen String umgewandelt
		Cell cell = null;
		try {
			Row row = sheet.getRow(y);
			if (row != null) {
				cell = row.getCell(x);
			}
		} catch (NullPointerException e) {
			logger.error("Bei der Zelle Zeile " + y + " Zelle " + x + " tritt eine Nullpointer Exeption auf", e);
			throw new ImportitException("Bei der Zelle Zeile " + y + " Zelle " + x + " tritt eine Nullpointer Exeption auf"  , e);
		}
		
		if (cell != null) {
			if ( cell.getCellType() == Cell.CELL_TYPE_STRING)  {
				return cell.getStringCellValue();
			}else {
		
				if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC)  {
//					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (DateUtil.isCellDateFormatted(cell)) {
						Date date = cell.getDateCellValue();
						AbasDate abasDate = new AbasDate(date);
						return abasDate.toString();
					}else {
						Double nummericvalue = cell.getNumericCellValue();
						Integer intvalue = nummericvalue.intValue();
						NumberFormat numberformat = new DecimalFormat("#.#########");
						String fnummericValue = numberformat.format(nummericvalue);
						if 	(intvalue.doubleValue()  == nummericvalue){
							return intvalue.toString();
						}else {
							return fnummericValue;	
						}	
					}
					
					
//				return cell.getStringCellValue();
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
													int fehlerzeile = y +1;
													int fehlerspalte = x+1;
													throw new ImportitException("Der Zelleninhalt in der Zeile " + fehlerzeile + " Spalte " + fehlerspalte + " ist vom Typ ERROR!Der Feldwert ist ung�ltig! Das k�nnen wir nicht verabeiten!");
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
										int fehlerzeile = y +1;
										int fehlerspalte = x+1;
										throw new ImportitException("Der Zelleninhalt in der Zeile " + fehlerzeile + " Spalte " + fehlerspalte + " ist vom Typ ERROR!Der Feldwert ist ung�ltig! Das k�nnen wir nicht verabeiten!");
									}
									
								}
		
	
							}	
	
					}
				}
			}
		
		}else {
//			Falls die Zelle null ist soll ein leer-String "" zur�ckgeben werden.
			return "";
//			throw new ImportitException("Der Zelleninhalt in der Zeile " + x + " Spalte " + y + " ist null");
		}
	// Falls irgendein Fall vergessen wurde wird null �bertragen
		return null;
	
	}
	
	private int getAnzDatenzeilen(Sheet poisheet) {
		// holt alle gef�llten Zeilen aus dem sheet
		// laut Definition sind die ersten beidenZeilen nur f�r die Konfiguration 
			return poisheet.getPhysicalNumberOfRows()-2;
	}
}