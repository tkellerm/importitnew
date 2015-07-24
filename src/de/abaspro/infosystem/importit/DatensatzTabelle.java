package de.abaspro.infosystem.importit;

import java.util.ArrayList;
import java.util.List;

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


	public void copyAbasTypInTable(List<Feld> tabellenfeldertoCopy) {
//		Wenn die übergebene Feldliste leer oder null ist dann soll nichts passieren
		if (tabellenfeldertoCopy!= null) {
			for (int i = 0; i < this.tabellenFelder.size(); i++) {
				if (this.tabellenFelder.get(i).getAbastyp().isEmpty()) {
					this.tabellenFelder.get(i).setAbastyp(tabellenfeldertoCopy.get(i).getAbastyp());	
				}

			}
		}
	
	}
	
}
