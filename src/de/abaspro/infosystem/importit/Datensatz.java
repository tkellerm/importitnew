package de.abaspro.infosystem.importit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Datensatz {

	 private List<Feld> kopfFelder = new ArrayList<Feld>();

	 private List<DatensatzTabelle> tabellenZeilen = new ArrayList<DatensatzTabelle>();	 
	 private Integer database;
	 private String dbString;
	 
	 
	 private Integer group;
	 private String dbGroupString;
	 private Integer tippcommand;
	 private String tippcommandString;
	 private String  importError = "";
	 private Integer tableStartsAtField;
	 private OptionCode optionCode;
	 private Integer keyfield;
	 private String abasId;
	 private String errorReport = "";
	 private String errordebug = "";
	 private Boolean isimportiert = false;
	 
	 private final static Logger log = Logger.getLogger( Importit21.class);

	public Integer getTableStartsAtField() {
		return tableStartsAtField;
	}


	public void setTableStartsAtField(Integer tableStartsAtField) {
		this.tableStartsAtField = tableStartsAtField;
	}


	public Integer getKeyfield() {
		return keyfield;
	}


	public OptionCode getOptionCode() {
		return optionCode;
	}


	public void setOptionCode(OptionCode optionCode) {
		this.optionCode = optionCode;
	}


	public Integer getGruppe() {
		return group;
	}


	public void setGruppe(Integer gruppe) {
		this.group = gruppe;
	}


	public Integer getTippkommando() {
		return tippcommand;
	}


	public void setTippkommando(Integer tippkommando) {
		this.tippcommand = tippkommando;
	}


	public String getAbasId() {
		return abasId;
	}


	public void setAbasId(String abasId) {
		this.abasId = abasId;
	}


	public List<Feld> getKopfFelder() {
		return kopfFelder;
	}


	public void setKopfFelder(List<Feld> kopfFelder) throws ImportitException {
		this.kopfFelder = kopfFelder;
		this.keyfield = checkKeyField(this.kopfFelder);
	}

	/**
	 * @return InOneTransaction
	 * 
	 * Es wird die zurückgeben ob die Transaktion-Option gesetzt ist.
	 */
	
	public Boolean getOptionTransaction() {
		
			OptionCode options = this.getOptionCode();
	
			return options.getInOneTransaction();
			
	}

	/**
	 * @param kopfFelder2
	 * @return ColNumber
	 * @throws ImportitException
	 * 
	 * In den Kopffeldern nach dem ersten Schlüssel gesucht, falls ein 2. angeben wird, 
	 * wird eine ImportitExeption geworfen.
	 * Als Rückgabewert wird die Spaltenummer zurückgegeben 
	 * 
	 */
	
	private Integer checkKeyField(List<Feld> kopfFelder2) throws ImportitException {
		
		Integer keyfield = 0;
		
		for (Feld feld : kopfFelder2) {
			if (!feld.getKey().isEmpty()) {
				if (keyfield != 0) {
					throw new ImportitException("Es wurden mehrere Schlüssel eingetragen! Es ist nur einer erlaubt");
				}
				keyfield = feld.getColNumber();
			}
			
		}
		
		return keyfield;

	}


	public List<DatensatzTabelle> getTabellenzeilen() {
		return tabellenZeilen;
	}


	public void addKopfFeld(Feld feld) {
		kopfFelder.add(feld);
	}
	
	

	/**
	 * loescht alle Value-Felder in den KopfFeldern 	
	 */
		
	public void initKopfFelder() {
		
		for (int i = 0; i < kopfFelder.size(); i++) {
			Feld feld = kopfFelder.get(i);
			feld.setValue("");
			}
			
	}


	public String getImportError() {
		return importError;
	}


	public void setImportError(String error) {
		this.importError = error;
	}


	public Integer getDatenbank() {
		return database;
	}


	public void setDatenbank(Integer datenbank) {
		this.database = datenbank;
	}
	

	public String toString() {
		
		String string = this.getDatenbank() + ":" +this.getGruppe() + " " + this.getValueOfKeyfieldToString();
		
		return string;
	}

	private String getValueOfKeyfieldToString(){
		if (getKopfFelder()!= null) {
			  
			Feld feld = getKopfFelder().get(this.keyfield);
			if (feld != null) {
				return feld.getValue().toString();
			}
			
		}
		return "";
		
	}
	

	public String getValueOfKeyfield() throws ImportitException {
		if (getKopfFelder()!= null) {
			  
			Feld feld = getKopfFelder().get(this.keyfield);
			if (feld != null) {
				return feld.getValue();
			}else {
				throw new ImportitException("Das Feld an der Stelle " + this.keyfield.toString() + "ist nicht initialisiert");
			}
			
		}else {
			throw new ImportitException("Es waren die Kopffelder noch nicht gesetzt");
		}
		
	}
	
	public String getKeyOfKeyfield() throws ImportitException {
		if (getKopfFelder()!= null) {
			  
			Feld feld = getKopfFelder().get(this.keyfield);
			if (feld != null) {
				return feld.getKey();
			}else {
				throw new ImportitException("Das Feld an der Stelle " + this.keyfield.toString() + "ist nicht initialisiert");
			}
			
		}else {
			throw new ImportitException("Es waren die Kopffelder noch nicht gesetzt");
		}
		
	}
	
	public String getNameOfKeyfield() throws ImportitException {
		if (getKopfFelder()!= null) {
			  
			Feld feld = getKopfFelder().get(this.keyfield);
			if (feld != null) {
				return feld.getName();
			}else {
				throw new ImportitException("Das Feld an der Stelle " + this.keyfield.toString() + "ist nicht initialisiert");
			}
			
		}else {
			throw new ImportitException("Es waren die Kopffelder noch nicht gesetzt");
		}
		
	}


	/**
	 * @return
	 * Liefert die Liste der Felder im ersten Tabellenzeilendatensatz aus
	 * 
	 */
	public List<Feld> getTabellenFelder() {
//		Es wird die erste Zeile der Tabelle geholt und die Felder übergeben
		
		List<DatensatzTabelle> tabZeilen = this.getTabellenzeilen();
		if (tabZeilen.size()> 0) {
			DatensatzTabelle tabErsteZeile = tabZeilen.get(0);
			return tabErsteZeile.getTabellenFelder();	
		}else {
			return null;
		}
		
		
	}


	public void copyAbasTypInDatensatz(Datensatz datensatz) {
		// aus dem übergebenen Datensatz werden die abastypen in alle anghängten Felder kopiert.
				
		for (int i = 0; i < this.kopfFelder.size(); i++) {
			
			if (this.kopfFelder.get(i).getAbasTyp().isEmpty()) {
				this.kopfFelder.get(i).setAbasTyp(datensatz.kopfFelder.get(i).getAbasTyp());	
			}

		}
		List<Feld> tabellenfelder = datensatz.getTabellenFelder();
		for (DatensatzTabelle datensatzTabelle : this.tabellenZeilen) {
			
			datensatzTabelle.copyAbasTypInTable(tabellenfelder);
		}

	}


	public void copyDatabaseinDatensatz(Datensatz datensatz) {
		this.database = datensatz.getDatenbank();
		this.group = datensatz.getGruppe();
		
	}


	/**
	 * @param errorString
	 * 
	 * Es wird der Fehler errorString als neue Zeile an den Fehlerstring angehängt
	 * 
	 */
	
	public void appendError(Exception e) {
		if (this.importError.isEmpty()) {
			this.importError = e.getMessage();
		}else {
			this.importError = this.importError + "\n" + e.getMessage();
		}
		
		if (this.errordebug.isEmpty()) {
			this.errordebug =  e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e);
		}else {
			this.errordebug = this.errordebug + "\n" + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e);
		}
		
		
	}
	
public void appendError(String errorMessage , Exception e) {
		
		appendError(errorMessage);
		appendError(e);
		
	}
	
public void appendError(String errorString) {
		
		this.importError = this.importError + "\n" + errorString ;
		
	}
	

/**
 * @return the errorReport
 */
public String getErrorReport() {
	return errorReport;
}


/**
 * @return the errordebug
 */
public String getErrordebug() {
	return errordebug;
}


public void createErrorReport(){
	
	this.errorReport="";
	
//	Fehlermeldungen im Kopf
	
	if (this.importError != null) {
		text2ErrorReport(this.importError);	
	}
	
		
// Fehlermeldungen aus den Kopf Feldern
	
	String kopftext =  "Fehler aus den Kopffeldern : ";
	
	for (Feld feld : kopfFelder) {
		if (feld.getError() != null) {
			String feldError = feld.getError(); 
			if (!feldError.isEmpty()) {
				if (!kopftext.isEmpty()) {
					text2ErrorReport(kopftext);
					kopftext = "";
				}
				feld2ErrorReport( feld);
			}
			
		}
	}
	
//	Fehlermeldungen aus den Tabellenfeldern
	   
	for ( DatensatzTabelle tabZeile : tabellenZeilen) {
		    int aktZeile = tabellenZeilen.indexOf(tabZeile);
		    String rowText =  "Ausgabe Zeile " + aktZeile;
		    
			ArrayList<Feld> tabellenFelder = tabZeile.getTabellenFelder();
			for (Feld feld : tabellenFelder) {
				if (feld.getError() != null) {
					String feldError = feld.getError(); 
					if (!feldError.isEmpty()) {
						if (!rowText.isEmpty()) {
							text2ErrorReport(rowText);
							rowText = "";
							}
						feld2ErrorReport(feld);
					}
				}
			}
	}
	
	
	
}


private void text2ErrorReport(String text) {
	if (this.errorReport.isEmpty()) {
		this.errorReport =  text;
	}else {
		errorReport = errorReport + "\n" + text;	
	}
}


/**
 * @param ausgabe
 * @param rownumber
 * @param feld
 * @return
 */
private void feld2ErrorReport( Feld feld) {
	String feldError = feld.getError(); 
	Integer colnumber = feld.getColNumber();
	String ausgabe="";
	if (!feldError.isEmpty()) {
		ausgabe =  feld.getName() + " " + colnumber.toString() + " " + "Fehler : " + feldError;
		text2ErrorReport(ausgabe);
	}
	
}


public String getValueOfHeadField(String fieldnameart) throws ImportitException {
	
	for (Feld feld : kopfFelder) {
		if (feld.getName().equals(fieldnameart)) {
			return feld.getValue();
		}
		
	}
	throw new ImportitException("Es wurde das Feld mit dem Namen " + fieldnameart + "in den Kopffeldern nicht gefunden!");
}


public void setDbString(String dbString) {
	this.dbString = dbString;
	
}


public void setDbGroupString(String dbgroupString) {

	this.dbGroupString = dbgroupString;
	
}

public String getDbString() {
	return dbString;
}


public String getDbGroupString() {
	return dbGroupString;
}





public String getTippcommandString() {
	return tippcommandString;
}


public void setTippcommandString(String tippcommandString) {
	this.tippcommandString = tippcommandString;
}


public Boolean getIsimportiert() {
	return isimportiert;
}


public void setIsimportiert(Boolean isimportiert) {
	this.isimportiert = isimportiert;
}

}
