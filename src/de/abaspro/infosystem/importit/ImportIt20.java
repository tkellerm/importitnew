package de.abaspro.infosystem.importit;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;






















import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPRuntimeException;
import de.abas.ceks.jedp.EDPSelection;
//JEDP API
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.ceks.jedp.TransactionException;
import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.EKS;

//JFOP-API (basic classes FO, FOe, EKS, EKSe and others)
import de.abas.eks.jfop.remote.FOPRunnable;

public class ImportIt20 implements FOPRunnable {
/** 
 * "main"-method, called from abas-ERP 
 *  @param arg arg[0]="Test_fop" (name of this class); 
 *             arg[1],arg[2],...: given paramters
 *  @return exitcode (0 = ok)
 *      Versionshistorie
 *             1.0 Erste Veröffentlichte Version
 *             1.1 
 *             1. Bugfix: Wenn Option "Datensätze anzeigen" gewählt wurde, so wurde bei einem erfolgreichen Import die Fehlerinfo in der Zeile nicht gelöscht. 
 *                        Somit war ein Fehler eines Imports zuvor immer noch sichtbar
 *             2. @skip in feldnamen eingeführt -> Übergehen der Spalte
 *             3. Versionsfeld eingeführt -  Ruhe mit "Welche Version kann was?"
 *             4.  erste Version für subeditor.-> klappt aber noch nicht
 *             1.2
 *              1. Bugfix - > Importzähler korrigiert
 *              2.  Es wird nicht mehr nach Import jeder Tabellenzeile gespeichert. Erst beim nächsten Datensatz wird gespeichert!
 *                 => Fibu Buchungen sind möglich zu importieren 
 *             1.3 
 *              1.  Bugfix -> "Datensätze immer neu Anlegen" funktionierte nicht - korrigiert!
 *             1.4
 *              1. Option Modifiable eingeführt - Alle Felder werden optional auf "änderbar" geprüft
 *              2. Optionen in Excel vorbelegbar über Zelle A2
 *                 Binärcode wird angegeben und wird auch in IS angezeigt in neuem Feld!
 *              3. Auch ohne die Option "Datensätze anzeigen" wird im Fehlerfall die Fehlerzeile mit Selektion und DB ID dargestellt
 *              4. Bugfix -> Durch die Änderung des Speicherverhaltens in Version 1.2 wurde bei jedem Importfehler eine Java
 *                 Exception ausgelöst. Dadurch wurden ab dieser Excelzeile keine weiteren Daten mehr importiert.
 *              5. TABABSPALTE wird nun mit 0 vorbelegt, wenn in Excel File als Leere Zelle vorhanden
 *              6. @modifiable als Spalten Option
 *              7. @notempty als Spalten Option
 *             1.5 
 *
 *              1.Bugfix beim EInlesen von tabellendaten
 *              2. Löschen von tabellendaten wird nun nicht mehr spearat gemacht sondern direkt beim editieren des Datensatzes vor dem Import der weiteren Daten
 *              3.  Bugfix bei Modifizierbarkeit in Tabellen (moifiable benötigt Tabellenzeilenangabe)
 *              4. Bugfix Anzeige Anzahl Datensätze korrigiert( war immer eins zu hoch)
 *              1.6 EDP Vesion reduziert auf 3.25 => 2008 auch lauffähig (alte Libs eingelinkt)
 *              2.0 Umstellung auf Poi
 *              2.1 Umstellung auf Klassen zur Abarbeitung
 * 
 */
//	Variablen deklarieren
	
	private String importFilename; 
	private String errorFilename;
	
	private String checkSumImportFile;
	
	private org.apache.poi.ss.usermodel.Workbook importWorkbook;
	private org.apache.poi.ss.usermodel.Workbook errorWorkbook;
	private org.apache.poi.ss.usermodel.Sheet importSheet;
	private org.apache.poi.ss.usermodel.Sheet errorSheet;
	
	private Integer optionsCode;
	private Boolean optImmmerneu;
	private Boolean optNofop;
	private Boolean optTransaction;
	private Boolean optLoescheTabelle;
	private Boolean optFeldModifiable;
	
	private Integer datenbank;
	private Integer gruppe;
	private Integer tippkommando;
	private Integer tabelleAbFeld;
	private Integer anzahlDatensaetze;
	private Datensatz datensatz;
	private EDPSession edpSession;
	
	private String server;
	private Integer port;
	private String mandant;
	private String passwort;
	
	private static String VERSION = "2.1.0";
	
	
    public int runFop(String[] arg) throws FOPException
{
    	//Alle Felder abfangen 
    	String evtvar = EKS.Tvar("evtvar");

    	
    	
//    	Die ImportDatei wurde eingetragen
    	if (evtvar.equals ("ydatafile"))
        {
     	  try {
			
			holeInfosAusImportDatei(EKS.Mvar("ydatafile"));
			
			fuellAbasMaskeKopffelder();
			
			
     	  	} catch (ImportitException e) {
     	  		EKS.fehler(e.getMessage());
     	  		EKS.formel("M|ydatafile = \"\" ");
     	  		}
    		    		
        }
    	
    	
    	// Tabelle erzeugen wenn Häkchen gesetzt
       if (evtvar.equals("ytabelle"))
       {
            
            
       }
         
       // Struktur anlegen und prüfen
       if (evtvar.equals ("ypruefstrukt"))
       {
//    	   da leider die Daten nicht im Speicher gehalten werden, muss bei jeder Aktion alles wieder neu gefüllt werden-
    	   try {
    	   holeInfosAusImportDatei(EKS.Mvar("ydatafile"));
    	   
    	   
    	   server = EKS.Mvar("yserver");
    	   String stringport = EKS.Mvar("yport");
    	   port = Integer.decode(stringport);
   	   
    	   mandant = EKS.Mvar("ymandant");
    	   passwort = EKS.Mvar("ypasswort");
    	   
    	   if (this.importSheet != null) {
			
		   
    		   this.edpSession = SessionAufbauen(server, port , mandant, passwort);
		
	    	   if (this.edpSession !=null) {
	    		   	this.datensatz = new Datensatz();
	    		   	List<Feld> kopfFelder = datensatz.getKopfFelder();
	    		       		  
					legeKopfFelderListe( importSheet , kopfFelder , edpSession);
					
					if (this.tabelleAbFeld > 0) {
						
						List<DatensatzTabelle> tabellenZeilen = datensatz.getTabellenzeilen();
						
						legeTabellenFelderListe( importSheet , tabellenZeilen , edpSession );
						
						}

					
					EKS.box("\n Prüfung erfolgreich");
					
					}
    	   		}
    	   
    	   }catch (ImportitException e) {
    		   
				EKS.fehler(e.getMessage());
				
//     	  		EKS.formel("M|ydatafile = \"\" ");
				
			}finally{
				if (this.edpSession != null) {
					
					this.edpSession.endSession();
					
					
				}
			}
			
    		   
    		   
    	   	   
		}
    	   
    	   
           
        
       if (evtvar.equals ("ypruefdat"))
       {
    	   
       }
      
       if (evtvar.equals ("yintabladen"))
       {
    	   
       }     
       
       if (evtvar.equals ("yimport"))
       {
    	   
       }
       
       if (evtvar.equals ("yloetab")         ||
    		  evtvar.equals ("ynofop")       ||   
    		  evtvar.equals ("yimmerneu")    ||
    		  evtvar.equals ("ytransaction") ||
    		  evtvar.equals ("ymodifiable") 
   		   )  {
    	   
    	   setOptionCode(EKS.Mvar("yimmerneu").equals(EKS.Gvar("true")), EKS.Mvar("ynofop").equals(EKS.Gvar("true")), EKS.Mvar("ytransaction").equals(EKS.Gvar("true")) , EKS.Mvar("yloetab").equals(EKS.Gvar("true")), EKS.Mvar("ymodifiable").equals(EKS.Gvar("true")));
    	   
       }
       
    return 0;
}

   
	private void legeTabellenFelderListe(Sheet importSheet2,
			List<DatensatzTabelle> tabellenZeilen, EDPSession edpSession2) throws ImportitException {


		
		prüfeDatenbankVorhanden( this.datenbank , this.gruppe , this.tippkommando , edpSession2 );
		
//		alle Felder in der Zeile2 durchlaufen, ob Sie in der Datenbank vorhanden sind
//		Falls ja, dann in Struktur einfügen

		String variable;
		
//		Tabellefelder  vorhanden
			Integer row = 1;
			for (int col = this.tabelleAbFeld; (col < getMaxCol(importSheet2)) && (col >= (this.tabelleAbFeld) || this.tabelleAbFeld != 0) ; col++) {
				
				String zelleninhalt = getZellenInhaltString(importSheet2, col, row);
				
				int index = zelleninhalt.indexOf("@");
				if (index != -1 ) {
					
					variable = zelleninhalt.substring(0, index );
				
				}else {
					
					variable = zelleninhalt;
				}
				try {
//				Wenn der Variablenname leer dann skip option setzen
					Feld feld = new Feld();
					DatensatzTabelle datensatzTabelle = new DatensatzTabelle(); 
				if (!variable.isEmpty()) {
				
					String selectionString = "vdn=" + this.datenbank.toString() + ";vgr=" + this.gruppe.toString() + ";vname=`" + variable ;
					
					EDPQuery edpQ1 = edpSession2.createQuery();
					
						edpQ1.startQuery("12:26" , "Variablentabelle" , selectionString , false , EDPConstants.ALIVEFLAG_ALIVE  , true , true , "vdn,vgr,such,nummer,vname");
						
						if (!edpQ1.getFirstRecord()) {
							
							throw new ImportitException("Das Feld " + variable + " wurde in der Variablentabelle nicht gefunden");
							
						}else {
							
							feld.setName(variable);
							
//							Die Optionen auslesen
							
							feld.setOption_notEmpty(zelleninhalt.contains(ImportOptionen.NOTEMPTY.getSearchstring()));
							feld.setOption_modifiable(zelleninhalt.contains(ImportOptionen.MODIFIABLE.getSearchstring()));
							feld.setOption_skip(zelleninhalt.contains(ImportOptionen.SKIP.getSearchstring()));
							
//							Falls die Option Alle Felder auf modifiable Prüfen gesetzt wird, dann Option in jedem Feld setzen
							
							if (this.optFeldModifiable) {
								
								feld.setOption_modifiable(true);
								
							}
							
//							
						}
				
						edpQ1.breakQuery();		
						
					}else {
						
//						wenn der Variablenname leer ist wird dieses Feld ignoriert
						
						feld.setOption_skip(true);
						
					}

//				hänge das Feld an die Kopffelder an
				
				datensatzTabelle.addTabellenFeld(feld);
				
				
				} catch (InvalidQueryException e) {
					
					throw new ImportitException("Der Selektion ist fehlgeschlagen!\n" + e.getMessage());
								
					
				}
				
			}
			

		
	}


	private void holeInfosAusImportDatei(String mvar) throws ImportitException {
		
		setzeImportFile(mvar);
		setErrorFilename(this.importFilename);
		setCheckSumImportFile();
		this.importWorkbook = initWorkbook(this.importFilename);
		this.errorWorkbook = initWorkbook(this.importFilename);
		this.importSheet = importWorkbook.getSheetAt(0);
		this.errorSheet = errorWorkbook.getSheetAt(0);
		
		pruefeUndHoleInfoAusSheet(this.importSheet);
		
	}


	private void legeKopfFelderListe(Sheet importSheet2, List<Feld> kopfFelder,
			EDPSession edpSession2) throws ImportitException {
		
			prüfeDatenbankVorhanden( this.datenbank , this.gruppe , this.tippkommando , edpSession2 );
			
//			alle Felder in der Zeile2 durchlaufen, ob Sie in der Datenbank vorhanden sind
//			Falls ja, dann in Struktur einfügen

			String variable;
			
//			Tabellefelder  vorhanden
				Integer row = 1;
				for (int col = 0; (col < getMaxCol(importSheet2)) && (col < (this.tabelleAbFeld) || this.tabelleAbFeld == 0) ; col++) {
					
					String zelleninhalt = getZellenInhaltString(importSheet2, col, row);
					
					int index = zelleninhalt.indexOf("@");
					if (index != -1 ) {
						
						variable = zelleninhalt.substring(0, index );
					
					}else {
						
						variable = zelleninhalt;
					}
					try {
//					Wenn der Variablenname leer dann skip option setzen
						Feld feld = new Feld();
					if (!variable.isEmpty()) {
					
						String selectionString = "vdn=" + this.datenbank.toString() + ";vgr=" + this.gruppe.toString() + ";vname=`" + variable ;
						
						EDPQuery edpQ1 = edpSession2.createQuery();
						
							edpQ1.startQuery("12:26" , "Variablentabelle" , selectionString , false , EDPConstants.ALIVEFLAG_ALIVE  , true , true , "vdn,vgr,such,nummer,vname");
							
							if (!edpQ1.getFirstRecord()) {
								
								throw new ImportitException("Das Feld " + variable + " wurde in der Variablentabelle nicht gefunden");
								
							}else {
								
								feld.setName(variable);
								
//								Die Optionen auslesen
								
								feld.setOption_notEmpty(zelleninhalt.contains(ImportOptionen.NOTEMPTY.getSearchstring()));
								feld.setOption_modifiable(zelleninhalt.contains(ImportOptionen.MODIFIABLE.getSearchstring()));
								feld.setOption_skip(zelleninhalt.contains(ImportOptionen.SKIP.getSearchstring()));
								
//								Falls die Option Alle Felder auf modifiable Prüfen gesetzt wird, dann Option in jedem Feld setzen
								
								if (this.optFeldModifiable) {
									
									feld.setOption_modifiable(true);
									
								}
								
//								Den Schlüssel auslesen, wenn es das erste Feld ist.
								
								if ( zelleninhalt.contains("@") && 
										!(zelleninhalt.contains(ImportOptionen.NOTEMPTY.getSearchstring())) &&
										!(zelleninhalt.contains(ImportOptionen.MODIFIABLE.getSearchstring())) &&
										!(zelleninhalt.contains(ImportOptionen.SKIP.getSearchstring())) &&
										col == 0)	{
														String schluessel = zelleninhalt.substring(index+1); 	
														
														edpQ1.getKeyNames(this.datenbank.toString() , schluessel);
														
														if (!edpQ1.getFirstRecord()) {
															throw new ImportitException("Der Schlüssel " + schluessel + " wurde für die Datenbank " + this.datenbank.toString() + " nicht gefunden");
														}else {
															feld.setKey(schluessel);	
														}
									
										 			}
						
								}
					
					
							edpQ1.breakQuery();		
							
						}else {
							
//							wenn der Variablenname leer ist wird dieses Feld ignoriert
							
							feld.setOption_skip(true);
							
						}

//					hänge das Feld an die Kopffelder an
					
					kopfFelder.add(feld);
					
					
					} catch (InvalidQueryException e) {
						
						throw new ImportitException("Der Selektion ist fehlgeschlagen!\n" + e.getMessage());
									
						
					}
					
				}
				
		
	}


	private void prüfeDatenbankVorhanden(Integer datenbank2, Integer gruppe2,
			Integer tippkommando2, EDPSession edpSession2) throws ImportitException {
		
		if (datenbank2 !=null && gruppe2!=null) {
			EDPQuery edpQ1 = edpSession2.createQuery();
			try {
				edpQ1.startQuery("12:26" , "Variablentabelle" , "vdn=" + this.datenbank.toString() + ";vgr=" + this.gruppe.toString() , false , EDPConstants.ALIVEFLAG_ALIVE , true , true , "vdn,vgr,such,nummer");
				
				if (edpQ1.getRecordCount()==0) {
					
					throw new  ImportitException("Die Datenbank mit der Nummer " + this.datenbank + " und der Gruppe " + this.gruppe + "wurde nicht gefunden!");
				}else {
//					Datenbank vorhanden
					edpQ1.breakQuery();
				}
			
			} catch (InvalidQueryException e) {
				
				Logger.getLogger(ImportIt20.class.getName()).log(Level.SEVERE, null, e);
				
				throw new  ImportitException("Bei der Suche nach der Datenbank war die Selektion ungültig \n" + e.getMessage() );
				
			}	
		}else if (tippkommando2 != null) {
			if (Integer.parseInt(EKS.Gvar("maxkkomnam")) < tippkommando2) {
				
				throw new  ImportitException("Es wurde das falsche Tippkommando angegeben!"); 
				
			}
		}
		

		

		
		
	}


	private void pruefeUndHoleInfoAusSheet(
			org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
		
		
			try{
				
			this.datenbank         = getdb(sheet);
			this.gruppe            = getgroup(sheet);
			this.tippkommando      = getTippkomanndo(sheet);
			this.tabelleAbFeld     = getTabelleab(sheet); 
			setOptionCode(getOptionCodeFromSheet(sheet));
			this.anzahlDatensaetze = getAnzDatenzeilen(this.importSheet);
			
			}catch (NumberFormatException e){
				throw new ImportitException("Es war die Datenbank bzw. das Tippkommando im falschen Format angegeben!");
			}
			
		 
		
		
	}


	private Integer getOptionCodeFromSheet(
			org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
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


	private Integer getTabelleab(org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
		
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


	private void delRowInSheet(org.apache.poi.ss.usermodel.Sheet sheet,
		int rowNumber) {
	// TODO Auto-generated method stub
		Row row = sheet.getRow(rowNumber);
		if (row != null) {
			sheet.removeRow(row);
		}
	
}


	private int getMaxCol(org.apache.poi.ss.usermodel.Sheet sheet) {
	// Anzahl Spalten(Cols) in der 2. Zeile (entspricht 1) 
		 Integer maxcol = sheet.getRow(1).getPhysicalNumberOfCells();
	return maxcol;
}


	private int getMaxRow(org.apache.poi.ss.usermodel.Sheet sheet) {
		// TODO Auto-generated method stub
			
		return sheet.getLastRowNum();
}

	

	private void TabelleAufbauen(EDPSession session,
		org.apache.poi.ss.usermodel.Sheet sheet, String dbgroup) {
	// TODO Auto-generated method stub
		

        
	    EDPQuery edpQT1 = session.createQuery();
	    String schluessel = "";
	    String xdbgroup="";
	    String selfield=getZellenInhaltString(sheet, 0, 1);
	    int klammeraffe =selfield.indexOf("@");
	    if (klammeraffe>0)
	            {  
	               String dummy=selfield;
	               selfield =dummy.substring(0,klammeraffe);
	               schluessel=";@sort="+dummy.substring(klammeraffe+1);       
	               
	            }
	                        
	    xdbgroup=dbgroup;
	    // Schleife über alle Zeilen
	   
	    for (int y = 2 ; y <= getMaxRow(sheet) ; y++)
	    {	
	    	getZellenInhaltString(sheet, 0, y);
	        EKS.hinweis("-SOFORT \"Lese ZEILE "+y+" "+getZellenInhaltString(sheet, 0, y)+"\"");
	        // Zeile erzeugen
	        EKS.mache("MASKE ZEILE +");
	        EKS.formel("M|ysel=\""+getZellenInhaltString(sheet, 0, y)+"\"");
	        
	        // Selektion aufbauen leere Spalte A übergehen ( Könnte auch ein tabellenimport sein mit leerer Spalte A)
	        if (!(isZelleleer(sheet, 0, y)))
	        {    
	            String selektionT=selfield+"=="+getZellenInhaltString(sheet, 0, y)+";@sort="+schluessel;
	            // Selektion starten
	            try {


	    
	        int doppelpunkt =dbgroup.indexOf(":");
	       if (doppelpunkt>0)
	       {
	    
	       String db =dbgroup.substring(0,doppelpunkt);
	       String gruppe=dbgroup.substring((doppelpunkt+1));
	      

	        if ((db.equals("Firma")) || (db.equals("12"))  && (gruppe.equals("Sachmerkmal-Leiste")))
	              {
	            xdbgroup="Firma:Selektionsrohling";
	              }
	       }

	            edpQT1.startQuery(xdbgroup, "", selektionT, "id");
	                
	                } catch (InvalidQueryException ex) 
	                    {
	                    EKS.box("Fehler\n"+ex.getMessage());
	                    Logger.getLogger(ImportIt20.class.getName()).log(Level.SEVERE, null, ex);
	                    }
	                // Selektion erfolgreich?
	                if (edpQT1.getRecordCount()==1){
	                //Datensatz
	                edpQT1.getNextRecord();
	                // Datensatz eintragen über die id
	                EKS.formel("M|ydatensatz=\""+edpQT1.getField(1)+"\"");    
	                }
	        } 
	     }
	    
	
}


	
//	private Boolean isZelleDatum(org.apache.poi.ss.usermodel.Sheet sheet, int x, int y) {
//		// TODO Auto-generated method stub
//		
//		if (sheet.getRow(x).getCell(y).getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC){
//			if (sheet.getRow(x).getCell(y).getCellStyle() == org.apache.poi.ss.usermodel.CellStyle.)
//		}
//		
//		return (sheet.getRow(x).getCell(y).getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
//	}
	
	private Boolean isZelleleer(org.apache.poi.ss.usermodel.Sheet sheet, int x, int y) {
		// Prüft ob die Zelle leer ist
		org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(y).getCell(x);
		
		if (cell == null) {
			return true;
		}else {
			if (sheet.getRow(y).getCell(x).getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK){
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
	
private String getZellenInhaltString(org.apache.poi.ss.usermodel.Sheet sheet, int x, int y) {
//		Hier werden alle Inhaltsmöglichkeiten einer Celle in einen String umgewandelt 
		org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(y).getCell(x);
		if (cell != null) {
			
			if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING)  {
				return cell.getStringCellValue();
			}else {
		
				if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC)  {
			
					Double nummericvalue = cell.getNumericCellValue();
					Integer intvalue = nummericvalue.intValue();
					if 	(intvalue.doubleValue()  == nummericvalue){
						return intvalue.toString();
					}else {
						return nummericvalue.toString();	
				}
				
			}else {
				if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN){
					if (cell.getBooleanCellValue() == true) {
						return "ja";
					}else {
						return "nein";
					}
				}else {
						if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA){
								if (cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
										return cell.getStringCellValue();
								}else {
									if (cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC) {
										Double nummericvalue = cell.getNumericCellValue();
										return nummericvalue.toString();
									}else {
										if ( cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN){
											if (cell.getBooleanCellValue() == true) {
													return "ja";
											}else {
													return "nein";
											}
										}else {
											if ( cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK){
												return "";
											}else {
												if ( cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR){
														return null;
												}
											}
										}
									}
								}
							}else {
								if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK){
									return "";
								}else {
									if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR){
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
	
	

	private int getAnzDatenzeilen(org.apache.poi.ss.usermodel.Sheet poisheet) {
	// holt alle gefüllten Zeilen aus dem sheet
	// laut Definition sind die ersten beidenZeilen nur für die Konfiguration 
		return poisheet.getPhysicalNumberOfRows()-2;
}



	private String getxdbgroup(org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
	// wenn es Sachmerkmalsleisten sind, dann soll es eine Rückgabe geben
    	
    	Integer db = getdb(sheet);
    	Integer group = getgroup(sheet); 
    	
    	if ((db.equals("Firma")) || (db.equals("12"))  && (group.equals("Sachmerkmal-Leiste")))
        {
         return "Firma:Selektionsrohling";
        
        }else {
			return getdbgroup(sheet);
		}
 }
    	




	private Integer getTippkomanndo(org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
		
//		Es ist ein Tipkommdo, wenn es keinen Doppelpunkt besitzt 		
		String dbgroup = getdbgroup(sheet);
		String tippString;
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt == 0)
     
        {
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



	private Integer getgroup(org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
		
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
	



	private Integer getdb(org.apache.poi.ss.usermodel.Sheet sheet) throws ImportitException {
		
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


	private String getdbgroup(org.apache.poi.ss.usermodel.Sheet sheet) {
	 		
		
		return getZellenInhaltString(sheet, 0, 0);
         
        
}

	 




    private EDPSession SessionAufbauen(String yserver, int yport, String ymandant, String ypasswort) throws ImportitException 
    { 
      EDPSession  session = EDPFactory.createEDPSession ();
      if (yserver != null && 
    		  yport != 0 && 
    		  ymandant != null && 
    		  ypasswort != null) {
    	  
    	  		try {
    	  			session.beginSession(EKS.Mvar("yserver"), yport, EKS.Mvar("ymandant"), EKS.Mvar("ypasswort"), "JEDP_0001");
    	  		} catch (CantBeginSessionException ex) 
    	  			{
    	  			Logger.getLogger(ImportIt20.class.getName()).log(Level.SEVERE, null, ex);
    	  			throw new ImportitException("FEHLER\n EDP Session kann nicht gestartet werden\n"+ex.getMessage());
    	  			}
                 
             // FOP Mode ein /ausschalten
              if (EKS.Mvar("ynofop").equals("ja"))
              {
              session.setFOPMode(false);        
              }
              else
              {
              session.setFOPMode(true);              
              }
              
        return session;
	}else {
		throw new ImportitException("Es wurde leider nicht alle Felder (Server , Port , Mandant , Passwort) gefüllt! Dadurch konnte keine EDP-Session aufgebaut werden" );
	}
	        
    }

    
    private void setErrorFilename(String importFilename){
    	
    	
        if (importFilename.contains(".xlsx")) {
        	this.errorFilename = importFilename.replaceAll(".xlsx", ".error.xlsx" );
		} else {
			this.errorFilename = importFilename.replaceAll(".xls", ".error.xls" );
		}
    	
    }

    private void setzeImportFile(String filename) throws ImportitException{
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
    
    private org.apache.poi.ss.usermodel.Workbook initWorkbook(String filename) throws ImportitException {
    	try {
    		if (filename.contains(".xlsx")) {
    			org.apache.poi.ss.usermodel.Workbook workbook;
			
				workbook = new XSSFWorkbook(new FileInputStream(filename));
			
    		return workbook;
    		
    	}else if (filename.contains(".xls")) {
        	   org.apache.poi.ss.usermodel.Workbook workbook = new HSSFWorkbook(new FileInputStream(filename));
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
    
    private Boolean checkImportFileHasChanged() throws ImportitException{
    	
    	String checksumNew = getCheckSumImportFile(importFilename);
    	   	
		return checksumNew.equals(checkSumImportFile);
	
    	
    }


	private String getCheckSumImportFile(String filename) throws ImportitException {
		FileInputStream fis = null;
		try {
			
			fis = new FileInputStream(new File(filename));
			String checkSum = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			fis.close();
			return checkSum;
		}catch (FileNotFoundException e) {
			
			throw new ImportitException("Die Datei " + filename + " wurde bei dem Checksum-Test nicht gefunden!");
			
		} catch (IOException e) {
			
			throw new ImportitException("Bei dem Zugriff auf die Datei " + filename + " trat ein Fehler bei dem Checksum-Test auf!");
			
		}finally{
			if (fis != null) {
				try {
//					Falls vorhanden den FileInputStream schliessen
					fis.close();
				} catch (IOException e2) {
					
					// Nichts machen weil dann kein FileInputStream offen war
				}
			}
		}
		
		
		
		
		
	}


	private void setCheckSumImportFile() throws ImportitException {
		
			this.checkSumImportFile = getCheckSumImportFile(this.importFilename);
			
	}
    
    private void setOptioncodeToOptionen() {
    	 
    	
    	String binoption="0000"+Integer.toBinaryString(this.optionsCode);
        binoption=binoption.substring ( (binoption.length()-5) );
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
       
        this.optImmmerneu = binoption.substring(4).equals("1");
        this.optNofop = binoption.substring(3,4).equals("1");
        this.optTransaction = binoption.substring(2,3).equals("1");
        this.optLoescheTabelle = binoption.substring(1,2).equals("1");
        this.optFeldModifiable = binoption.substring(0,1).equals("1");
        
    	
    }
    
    private void setOptionCode(Boolean immerneu , Boolean nofop , Boolean transaction , Boolean loeschtab , Boolean checkFeldModifiable){
    	
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
  	
    	Integer optcode=0;   	
    	
    	if (immerneu) { optcode = 1; }
    	if (nofop) { optcode = optcode + 2; }
    	if (transaction) { optcode = optcode + 4; }
    	if (loeschtab) { optcode = optcode + 8; }
    	if (checkFeldModifiable) { optcode = optcode + 16; }
    	
    	setOptionCode(optcode);
    }
    
private void setOptionCode(Integer optcode){
    	
       	this.optionsCode = optcode;
//      jetzt noch die Optionen setzen;  
    	this.setOptioncodeToOptionen();

    }
    
    
   private void fuellAbasMaskeKopffelder(){
	   
	   EKS.formel("M|yerrorfile  = \"" + this.errorFilename + "\"");
	   EKS.formel("M|yoption     = \"" + this.optionsCode + "\"");
	   EKS.formel("M|yimmerneu   = \"" + this.optImmmerneu + "\"");
	   EKS.formel("M|ynofop      = \"" + this.optNofop + "\"");
	   EKS.formel("M|yloetab     = \"" + this.optLoescheTabelle + "\"");
	   EKS.formel("M|ytransaction= \"" + this.optTransaction + "\"");
	   EKS.formel("M|ymodifiable = \"" + this.optFeldModifiable + "\"");
	   EKS.formel("M|yversion    = \"" + ImportIt20.VERSION + "\"");
	   EKS.formel("M|ydb         = \"" + this.datenbank + "\"");
	   EKS.formel("M|ygruppe     = \"" + this.gruppe + "\"");
	   EKS.formel("M|yzeilen     = \"" + this.anzahlDatensaetze + "\"");
	   EKS.formel("M|ytababspalte= \"" + this.tabelleAbFeld + "\"");
   }
    
    
    
    
    
    
    
    
    
}
