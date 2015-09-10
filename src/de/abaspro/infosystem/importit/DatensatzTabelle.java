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

	public DatensatzTabelle(DatensatzTabelle datensatzTabelle) throws ImportitException{
		this.tabellenFelder = new ArrayList<Feld>();
		Feld feldneu;
		ArrayList<Feld> tabFelder = datensatzTabelle.getTabellenFelder();
		for (Feld feld : tabFelder) {
			feldneu = new Feld(feld.getCompleteContent(),feld);
			this.tabellenFelder.add(feldneu);
		}
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


	public boolean isEmpty() {
		Boolean isEmpty = true;
		for (Feld feld : this.tabellenFelder) {
			if (feld.getValue()!= null) {
				if (!feld.getValue().isEmpty()) {
					isEmpty = false;	
				}
			}
		}
		
		return isEmpty;
	}
	
}
