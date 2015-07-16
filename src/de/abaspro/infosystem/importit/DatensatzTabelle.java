package de.abaspro.infosystem.importit;

import java.util.ArrayList;

public class DatensatzTabelle {

	ArrayList<Feld> tabellenFelder;
	
	public void addTabellenFeld(Feld feld) {
		this.tabellenFelder.add(feld);
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
	
	private void loeschValues(){
		for (int i = 0; i < tabellenFelder.size(); i++) {
			Feld feld = tabellenFelder.get(i);
			feld.setValue("");
		}
	}

	public DatensatzTabelle(DatensatzTabelle datensatzTabelle){
		
		this.tabellenFelder = datensatzTabelle.getTabellenFelder();
	}
	
	public DatensatzTabelle(){		
		
		this.tabellenFelder = new ArrayList<Feld>();
		
	}
	
}
