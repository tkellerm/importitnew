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
	 private OptionCode optionCode;

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


	public void setKopfFelder(List<Feld> kopfFelder) {
		this.kopfFelder = kopfFelder;
	}


	public List<DatensatzTabelle> getTabellenzeilen() {
		return tabellenZeilen;
	}


	public void addKopfFeld(Feld feld) {
		kopfFelder.add(feld);
	}
	
	
	public void setzKopfFeld(Integer index, String value){
		
		Feld feld = kopfFelder.get(index);
		feld.setValue(value);
		
	}
	
	
	/**
	 * loescht alle Tabellenzeilen bis auf die Kopiervorlage(index = 0)
	 */
	
	public void initTabelle() {
		
		for (int i = 0; i < tabellenZeilen.size(); i++) {
			DatensatzTabelle tabzeile = tabellenZeilen.get(i);
			if (!tabzeile.getKopiervorlage()) {
				tabellenZeilen.remove(i);
			}			
		}
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
	 
}
