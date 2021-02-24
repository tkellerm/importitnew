package de.abas.infosystem.importit;

public class OptionCode {
	
	private boolean alwaysNew;
	private boolean nofop;
	private boolean inOneTransaction;
	private boolean deleteTable;
	private boolean checkFieldIsModifiable;
	private boolean useEnglishVariablen;
	private boolean dontChangeIfEqual;
	private int optionsCode;

	
	
	public OptionCode(int optionsCode) {
		setOptionCode(optionsCode);
	}


	private void setOptionCodeToOptionen(Integer optCode) {
    	
		/*		Anweisung für eine Erweiterung :
		 * so viele 00000 Nullen wie Optionen
		 */		
    	String binaryOption="0000000"+Integer.toBinaryString(optCode);
        binaryOption=binaryOption.substring ( (binaryOption.length()-7) );
        
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Delete Table  -> 8
        // Modifiable -> 16
        // use EnglVariablen -> 32
        // dontChangeIfEqual -> 64       
         
        this.alwaysNew = binaryOption.substring(6).equals("1");
        this.nofop = binaryOption.charAt(5) == '1';
        this.inOneTransaction = binaryOption.charAt(4) == '1';
        this.deleteTable = binaryOption.charAt(3) == '1';
        this.checkFieldIsModifiable = binaryOption.charAt(2) == '1';
        this.useEnglishVariablen = binaryOption.charAt(1) == '1';
        this.dontChangeIfEqual = binaryOption.charAt(0) == '1';
        
    }

	public void setOptionCode(Boolean alwaysNew , Boolean nofop , Boolean inOnetransaction , Boolean deletetable , Boolean checkFieldIsModifiable, Boolean useEnglishVariablen, Boolean dontChangeIfEqual){
    	
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
        // use EnglVariablen -> 32
		// dontChangeIfEqual -> 64  
		
    	int optionCode =0;
    	
    	if (Boolean.TRUE.equals(alwaysNew)) { optionCode = 1; }
    	if (Boolean.TRUE.equals(nofop)) { optionCode = optionCode + 2; }
    	if (Boolean.TRUE.equals(inOnetransaction)) { optionCode = optionCode + 4; }
    	if (Boolean.TRUE.equals(deletetable)) { optionCode = optionCode + 8; }
    	if (Boolean.TRUE.equals(checkFieldIsModifiable)) { optionCode = optionCode + 16; }
    	if (Boolean.TRUE.equals(useEnglishVariablen)) {
			optionCode = optionCode + 32;}
    	if (Boolean.TRUE.equals(dontChangeIfEqual)) {
			optionCode = optionCode + 64;}
    	setOptionCode(optionCode);
    }

	private void setOptionCode(Integer optionCode){
		
	   	this.optionsCode = optionCode;

		setOptionCodeToOptionen(optionCode);
	}

	public int getOptionsCode() {
		return optionsCode;
	}

	public boolean getAlwaysNew() {
		return alwaysNew;
	}

	public boolean noFop() {
		return nofop;
	}

	public boolean getInOneTransaction() {
		return inOneTransaction;
	}

	public boolean getDeleteTable() {
		return deleteTable;
	}

	public boolean getCheckFieldIsModifiable() {
		return checkFieldIsModifiable;
	}

	public boolean useEnglishVariables() {
		return useEnglishVariablen;
	}

	public boolean getDontChangeIfEqual() {
		return dontChangeIfEqual;
	}

	
	

}
