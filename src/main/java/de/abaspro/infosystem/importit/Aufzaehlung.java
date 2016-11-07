package de.abaspro.infosystem.importit;

import java.util.ArrayList;


public class Aufzaehlung {
	
	ArrayList<AufzaehlungItem> listOfAufzaehlungItem = new java.util.ArrayList<AufzaehlungItem>();
	
	
	public ArrayList<AufzaehlungItem> getListOfAufzaehlungItem() {
		return listOfAufzaehlungItem;
	}

	public AufzaehlungItem searchItem(Integer number){
		
		for (AufzaehlungItem aufzaehlungItem : listOfAufzaehlungItem) {
			if (aufzaehlungItem.getNumber() == number) {
				return aufzaehlungItem;
			}
		}
		
		return null;
		
	}
	
	public AufzaehlungItem searchItem(String searchString){
		
		try {
			Integer searchNumber = new Integer(searchString);
			return searchItem(searchNumber);
		} catch (NumberFormatException e) {
//			Es ist kein Integerwert, dann suche nach dem String 
		}
		
		for (AufzaehlungItem aufzaehlungItem : listOfAufzaehlungItem) {
			
			
			
//			Suche nach Bediensprache 
			if (aufzaehlungItem.getNamebspr().toUpperCase().equals(searchString.toUpperCase())) {
				return aufzaehlungItem;
			}
			
			if (aufzaehlungItem.getNameNeutral().toUpperCase().equals(searchString.toUpperCase())) {
				return aufzaehlungItem;
			}
			
		}
		
		return null;
		
	}

}
