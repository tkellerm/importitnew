package de.abaspro.infosystem.importit;

import java.util.ArrayList;


public class Enumeration {
	
	ArrayList<EnumerationItem> listOfEnumerationItem = new java.util.ArrayList<EnumerationItem>();
	
	
	public ArrayList<EnumerationItem> getListOfEnumItems() {
		return listOfEnumerationItem;
	}

	public EnumerationItem searchItem(Integer number){
		
		for (EnumerationItem enumerationItem : listOfEnumerationItem) {
			if (enumerationItem.getNumber() == number) {
				return enumerationItem;
			}
		}
		
		return null;
		
	}
	
	public EnumerationItem searchItem(String searchString){
		
		try {
			Integer searchNumber = new Integer(searchString);
			return searchItem(searchNumber);
		} catch (NumberFormatException e) {
//			Es ist kein Integerwert, dann suche nach dem String 
		}
		
		for (EnumerationItem enumerationItem : listOfEnumerationItem) {
			
			
			
//			Suche nach Bediensprache 
			if (enumerationItem.getNamebspr().toUpperCase().equals(searchString.toUpperCase())) {
				return enumerationItem;
			}
			
			if (enumerationItem.getNameNeutral().toUpperCase().equals(searchString.toUpperCase())) {
				return enumerationItem;
			}
			
		}
		
		return null;
		
	}

}
