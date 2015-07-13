package de.abaspro.infosystem.importit;

import java.util.ArrayList;

public class DatensatzTabelle {

	ArrayList<Feld> tabellenFelder = new ArrayList<Feld>();
	Boolean istKopierVorlage;
	
	public void addTabellenFeld(Feld feld) {
		tabellenFelder.add(feld);
	}
	
	
	public void setzTabellenFeldValue(Integer index, String value){
		
		Feld feld = tabellenFelder.get(index);
		feld.setValue(value);
		
	}


	public ArrayList<Feld> getTabellenFelder() {
		return tabellenFelder;
	}


	public void setTabellenFelder(ArrayList<Feld> tabellenFelder) {
		this.tabellenFelder = tabellenFelder;
	}


	public Boolean getKopiervorlage() {
		return istKopierVorlage;
	}


	public void setKopiervorlage(Boolean kopiervorlage) {
		this.istKopierVorlage = kopiervorlage;
	}
	
	private void loeschValues(){
		for (int i = 0; i < tabellenFelder.size(); i++) {
			Feld feld = tabellenFelder.get(i);
			feld.setValue("");
		}
	}

	public DatensatzTabelle DatensatzTabelle(DatensatzTabelle datensatzTabelle) throws CloneNotSupportedException {
		DatensatzTabelle kopie;
		kopie = (DatensatzTabelle) datensatzTabelle.clone();

		kopie.loeschValues();
		return kopie;
	}
	
}
