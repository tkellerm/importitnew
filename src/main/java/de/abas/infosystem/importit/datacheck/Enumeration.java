package de.abas.infosystem.importit.datacheck;

import java.util.List;


/**
 * TODO write JAVADOC
 */
public class Enumeration {
	//TODO Change to a MAP
	private List<EnumerationItem> listOfEnumerationItem = new java.util.ArrayList<>();
	
	
	public List<EnumerationItem> getListOfEnumItems() {
		return listOfEnumerationItem;
	}

	public EnumerationItem searchItem(int number) {
		
		for (EnumerationItem enumerationItem : listOfEnumerationItem) {
			if (enumerationItem.getNumber().equals(number)) {
				return enumerationItem;
			}
		}
		return null;
	}
	
	public EnumerationItem searchItem(String searchString) {
		
		try {
			Integer searchNumber = Integer.valueOf(searchString);
			return searchItem(searchNumber);
		} catch (NumberFormatException e) {
//			Es ist kein Integerwert, dann suche nach dem String 
		}
		
		for (EnumerationItem enumerationItem : listOfEnumerationItem) {
			
			
			
//			Suche nach Bediensprache 
			if (enumerationItem.getNamebspr().equalsIgnoreCase(searchString)) {
				return enumerationItem;
			}
//			Suche nach Sprachneutrale Ausgabe
			if (enumerationItem.getNameNeutral().equalsIgnoreCase(searchString)) {
				return enumerationItem;
			}
			
		}

		return null;
		
	}

}
