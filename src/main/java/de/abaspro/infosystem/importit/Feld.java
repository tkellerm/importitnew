package de.abaspro.infosystem.importit;




/**
 * @author tkellermann
 *
 */
public class Feld {

	private String  name;
	private String  value;
	private String  key;
	private Boolean optionNotEmpty;
	

	private Boolean optionGlobalDontChangeIfEqual;
	private Boolean optionModifiable;
	private Boolean optionGlobalModifiable;
	private Boolean optionSkip;
	private Boolean optionDontChangeIfEqual;
	private String  error;
	private String  completeContent;
	private Boolean fieldtoCopy;
	private Integer colNumber;
	private String 	abasTyp;
	private long abasFieldLength;
	
	
	


	public void setOptionGlobalDontChangeIfEqual(Boolean optionGlobalDontChangeIfEqual) {
		this.optionGlobalDontChangeIfEqual = optionGlobalDontChangeIfEqual;
	}


	public void setOptionGlobalModifiable(Boolean optionGlobalModifiable) {
		this.optionGlobalModifiable = optionGlobalModifiable;
	}


	@Override
	public String toString() {
		return "Feld [name=" + name + ", value=" + value + ", abasTyp="
				+ abasTyp + "]";
	}

	
	/**
	 * @param completeContent
	 * @param fieldInHead
	 * 
	 * Konstruktor um Feld anzulegen, 
	 * über den Parameter fieldInHead wird festgelegt ob das Object ein Kopf oder Datenfeld ist.   
	 * 
	 */
		
	public Feld(String completeContent, Boolean fieldToCopy , Integer col) {
		super();
		this.completeContent = completeContent;
		this.fieldtoCopy = fieldToCopy;
		this.colNumber = col;
		this.key = "";
		this.error = "";
		this.name = "";
		this.abasTyp = "";
		this.optionGlobalModifiable = false;
		this.optionGlobalDontChangeIfEqual = false;
		this.optionModifiable = false;
		this.optionNotEmpty = false;
		this.optionSkip = false;
		this.optionDontChangeIfEqual = false;
		if (fieldToCopy) {
	//		Es ist ein Kopfffeld und daher wird nicht value, sondern name gefüllt
			this.name = extractValue(completeContent);
			fillOptions(completeContent);
		}else {
	//		Es ist ein normales Feld  und es wird der String in value geschrieben
			this.value = extractValue(completeContent);
		}
		
	}

	/**
	 * @param completeContent
	 * @param headfield
	 * @throws ImportitException
	 * 
	 * Dieser Konstruktor wird verwendet um Daten zu einem Kopffeld zu speichern
	 * 
	 * Falls das übertragene Feld kein Kopffeld ist, wird eine {@link ImportitException} geworfen
	 * 
	 */
	
	public Feld(String completeContent, Feld headfield)throws ImportitException {
		super();
		this.optionGlobalModifiable = false;
		this.optionGlobalDontChangeIfEqual = false;
		
		if (headfield.getFieldInHead()) {
			
			this.completeContent = completeContent;
			this.fieldtoCopy = false;
			this.name = "";
			this.name = headfield.getName();
			this.value = extractValue(completeContent);
			this.optionModifiable = headfield.getOptionModifiable();
			this.optionNotEmpty = headfield.getOptionNotEmpty();
			this.optionSkip = headfield.getOptionSkip();
			this.optionDontChangeIfEqual = headfield.getOptionDontChangeIfEqual();	
			this.colNumber = headfield.colNumber;
			this.key = "";
			this.error = "";
			this.abasTyp = "";
			
		}else {
			throw new ImportitException("Es wurde kein Feld mit der Eigenschaft fieldInHead übergeben!");
		}
			
	}

	/**
	 * @return the optiondontChangeIfEqual
	 */
	public Boolean getOptionDontChangeIfEqual() {
		return optionDontChangeIfEqual;
	}


	/**
	 * @param optionDontChangeIfEqual the option_dontChangeIfEqual to set
	 */
	public void setOptionDontChangeIfEqual(Boolean optionDontChangeIfEqual) {
		this.optionDontChangeIfEqual = optionDontChangeIfEqual;
	}


	/**
	 * 
	 * @return ergebnis
	 * 
	 * Die Funktion prüft, ob der Inhalt des Felds den Vorgaben des Abastyps entspricht.  
	 * 
	 * 
	 */


	private void fillOptions(String completeContent2) {
	// falls der String leer ist dann das Feld ignorieren
		if (!completeContent2.isEmpty()) {
	
			this.optionNotEmpty   = completeContent2.contains(ImportOptionen.NOTEMPTY.getSearchstring());
			this.optionModifiable = completeContent2.contains(ImportOptionen.MODIFIABLE.getSearchstring());
			this.optionSkip       = completeContent2.contains(ImportOptionen.SKIP.getSearchstring());
			this.optionDontChangeIfEqual = completeContent2.contains(ImportOptionen.DONT_CHANGE_IF_EQUAL.getSearchstring());
			
			if (completeContent2.contains(ImportOptionen.KEY.getSearchstring())) {
		//		Es muss in dem FELD @KEY=NAME_OF_KEY
				Integer startPosKey = completeContent2.indexOf(ImportOptionen.MODIFIABLE.getSearchstring() + "=");
				String tempstring = completeContent2.substring(startPosKey);
				Integer tempindex  = tempstring.indexOf("@");
				this.key = tempstring.substring(0, tempindex);
			}
		}else {
			this.optionSkip = true;
		}
	}



	private String extractValue(String completeContent2) {
		
		int index = completeContent2.indexOf("@");
		if (index != -1 ) {
			
			return completeContent2.substring(0, index );
		
		}else {
			return completeContent2;
		}
	}



	public String getCompleteContent() {
		return completeContent;
	}

	public Boolean getFieldtoCopy() {
		return fieldtoCopy;
	}

	public Integer getColNumber() {
		return colNumber;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
//		Änderung wenn der Typ B oder Boolean ist muss "ja" in "1"  oder "nein" in "0" umgeändert werden.
		if (this.abasTyp.equals("B")) {
			if (this.value.equals("ja")|| this.value.equals("yes") || this.value.equals("true")) {
				return "1";
			}else if (this.value.equals("nein")|| this.value.equals("no") || this.value.equals("false")) {
				return "0";
			}else return this.value;
			
		}else return this.value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 
	 * 
	 * @return true, wenn eines der beiden optionen option_notempty oder option_global_notEmpty gesetzt ist. 
	 */
	public Boolean getOptionGlobalDontChangeIfEqual() {
		Boolean optdontChange = this.optionDontChangeIfEqual || this.optionGlobalDontChangeIfEqual;
		return optdontChange;
	}
	
	public void setOptionNotEmpty(Boolean option_notEmpty) {
		this.optionNotEmpty = option_notEmpty;
	}
	
	public Boolean getOptionModifiable() {
		Boolean opModifiable = this.optionModifiable || this.optionGlobalModifiable; 
		return opModifiable;
	}
	
	public void setOptionModifiable(Boolean option_modifiable) {
		this.optionModifiable = option_modifiable;
	}
	
	public Boolean getOptionSkip() {
		return optionSkip;
	}
	
	public void setOptionSkip(Boolean option_skip) {
		this.optionSkip = option_skip;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}	
	
	public Boolean getFieldInHead() {
		return fieldtoCopy;
	}
	
	/**
	 * @return the abasTyp
	 */
	public String getAbasTyp() {
		return abasTyp;
	}

	/**
	 * @param abasTyp the abasTyp to set
	 */
	public void setAbasTyp(String abasTyp) {
		this.abasTyp = abasTyp;
	}

	/**
	 * @return the abasFieldLength
	 */
	public long getAbasFieldLength() {
		return abasFieldLength;
	}

	public Boolean getOptionNotEmpty() {
		return optionNotEmpty;
	}


	/**
	 * @param l the abasFieldLength to set
	 */
	public void setAbasFieldLength(long l) {
		this.abasFieldLength = l;
	}

	/**
	 * @param abasFieldLength the abasFieldLength to set
	 * @throws ImportitException 
	 */
	public void setAbasFieldLength(String abasFieldLengthString) throws ImportitException {
		
		Integer abasFieldLength = 0;
		try {
			abasFieldLength = new Integer(abasFieldLengthString);	
		} catch (NumberFormatException e) {
			throw new ImportitException("Falsches Format der Feldlänge" , e);
		}
			
		this.abasFieldLength = abasFieldLength;
		
	}
	
}
