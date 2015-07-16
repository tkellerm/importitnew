package de.abaspro.infosystem.importit;

import java.util.ArrayList;
import java.util.List;

public class Datensatz {

	 private List<Feld> kopfFelder = new ArrayList<Feld>();
	 private List<DatensatzTabelle> tabellenZeilen = new ArrayList<DatensatzTabelle>();
	 
	 private Integer database;
	 private Integer group;
	 private Integer tippcommand;
	 private String  error;
	 private Integer tableStartsAtField;
	 private OptionCode optionCode;
	 private Integer keyfield;

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


	public List<Feld> getKopfFelder() {
		return kopfFelder;
	}


	public void setKopfFelder(List<Feld> kopfFelder) throws ImportitException {
		this.kopfFelder = kopfFelder;
		this.keyfield = checkKeyField(this.kopfFelder);
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


	public String getError() {
		return error;
	}


	public void setError(String error) {
		this.error = error;
	}


	public Integer getDatenbank() {
		return database;
	}


	public void setDatenbank(Integer datenbank) {
		this.database = datenbank;
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
	 
}
