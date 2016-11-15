package de.abaspro.infosystem.importit;

public class OptionCode {
	
	private Boolean alwaysNew;
	private Boolean nofop;
	private Boolean inOneTransaction;
	private Boolean deleteTable;
	private Boolean checkFieldIsModifiable;
	private Boolean useEnglishVariablen;
	private Boolean dontChangeIfEqual;
	private int optionsCode;

	
	
	public OptionCode(int optionsCode) {
		super();
		setOptionCode(optionsCode);
	}

	public OptionCode() {
		super();
		setOptionCode(0);
	}
	
	private void setOptionCodeToOptionen(Integer optCode) {
    	
		/*		Anweisung f�r eine Erweiterung :
		 * soviele 00000 Nullen wie Optionen  
		 */		
    	String binoption="0000000"+Integer.toBinaryString(optCode);
        binoption=binoption.substring ( (binoption.length()-7) );
        
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
        // use EnglVariablen -> 32
        // dontChangeIfEqual -> 64       
         
        this.alwaysNew = binoption.substring(6).equals("1");
        this.nofop = binoption.substring(5,6).equals("1");
        this.inOneTransaction = binoption.substring(4,5).equals("1");
        this.deleteTable = binoption.substring(3,4).equals("1");
        this.checkFieldIsModifiable = binoption.substring(2,3).equals("1");
        this.useEnglishVariablen = binoption.substring(1, 2).equals("1");
        this.dontChangeIfEqual = binoption.substring(0,1).equals("1");
        
    }

	public void setOptionCode(Boolean alwaysNew , Boolean nofop , Boolean inOnetransaction , Boolean deletetable , Boolean checkFieldIsModifiable, Boolean useEnglishVariablen, Boolean dontChangeIfEqual){
    	
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
        // use EnglVariablen -> 32
		// dontChangeIfEqual -> 64  
		
    	Integer optcode=0;   	
    	
    	if (alwaysNew) { optcode = 1; }
    	if (nofop) { optcode = optcode + 2; }
    	if (inOnetransaction) { optcode = optcode + 4; }
    	if (deletetable) { optcode = optcode + 8; }
    	if (checkFieldIsModifiable) { optcode = optcode + 16; }
    	if (useEnglishVariablen) {optcode = optcode + 32;}
    	if (dontChangeIfEqual) {optcode = optcode + 64;}
    	setOptionCode(optcode);
    }

	private void setOptionCode(Integer optcode){
		
	   	this.optionsCode = optcode;
	//  jetzt noch die Optionen setzen;  
		setOptionCodeToOptionen(optcode);
	}

	public int getOptionsCode() {
		return optionsCode;
	}

	public void setOptionsCode(int optionsCode) {
		setOptionCode(optionsCode);		
	}

	public Boolean getAlwaysNew() {
		return alwaysNew;
	}

	public Boolean noFop() {
		return nofop;
	}

	public Boolean getInOneTransaction() {
		return inOneTransaction;
	}

	public Boolean getDeleteTable() {
		return deleteTable;
	}

	public Boolean getCheckFieldIsModifiable() {
		return checkFieldIsModifiable;
	}

	public Boolean useEnglishVariables() {
		return useEnglishVariablen;
	}

	/**
	 * @return the dontChangeIfEqual
	 */
	public Boolean getDontChangeIfEqual() {
		return dontChangeIfEqual;
	}

	
	

}
