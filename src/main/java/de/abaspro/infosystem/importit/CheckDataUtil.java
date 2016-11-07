package de.abaspro.infosystem.importit;

import de.abas.eks.jfop.remote.FOe;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;

/**
 * @author tkellermann
 *
 */
public class CheckDataUtil {

	static String[] VERWEIS = {"P" , "ID" , "VP" , "VID"};
	
	 /**
	 * @return Ergebnis
	 * 
	 * Die Funktion prüft, ob ein Wert zu einem AbasTyp passt
	 * 
	 * Da es die Funktion in Version 2012 nicht implementiert ist 
	 * wird es über den Jfop die FOP-Funktion F|isvalue aufgerufen. 
	 * 
	 * Mutliverweis-Felder werden nicht geprüft, ob der Wert einen eindeutigen Treffer gibt.
	 * 
	 * normale Verweisfelder werden überprüft.
	 * 
	 */
	
	public static  Boolean checkData(String abastyp , String value) {
		Boolean ergebnis=false;
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();

		String varnameErgebnis = "xtergebnis";

		if (!userTextBuffer.isVarDefined(varnameErgebnis)) {
			userTextBuffer.defineVar("Bool", varnameErgebnis);
		}
		userTextBuffer.setValue(varnameErgebnis, "0");
		
		int verweistyp = pruefObVerweis(abastyp);
		if (verweistyp == 0) {
//			kein Verweisfeld
			String formelStr = "U|" +varnameErgebnis + " = F|isvalue( \""  + value + "\" , \"" + abastyp + "\")";
			FOe.formula(formelStr);
			ergebnis = userTextBuffer.getBooleanValue(varnameErgebnis);	
		}else if (verweistyp == 1) {
//			prüfen ob der Wert für diese Datenbank eindeutig ist
			return checkVerweis(abastyp , value);
		} else if (verweistyp == 2) {
//			Multi-Verweisfeld keine Prüfung des Inhalts möglich
//			Rückgabe ist trotzdem true
			return true;
		} 		
		
		
		return ergebnis;
	}

	private static Boolean checkVerweis(String abastyp, String value) {

//		Typ zerlegen 
		
//		Datenbank bestimmen
		
//		Selektion aufbauen
		
		
		return null;
	}

	/**
	 * @param abastyp
	 * @return 
	 * 0 : kein Verweis
	 * 1 : normaler Verweis
	 * 2 : multi Verweisfeld
	 * 
	 */
	private static int pruefObVerweis(String abastyp) {
		
		for (String string : VERWEIS) {
			if (abastyp.startsWith(string)) {
				if (abastyp.startsWith("V")) {
//					Es ist ein Multiverweisfeld
					return 2;
				}else {
//					Es ist ein normales Verweisfeld
				    return 1;	
				}	
			}
			
		}
		
		return 0;
		
	}
	
	
	}
