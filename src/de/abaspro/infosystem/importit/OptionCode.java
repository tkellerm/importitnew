package de.abaspro.infosystem.importit;

public class OptionCode {
	
	private Boolean alwaysNew;
	private Boolean nofop;
	private Boolean inOneTransaction;
	private Boolean deleteTable;
	private Boolean checkFieldIsModifiable;
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
    	
    	String binoption="0000"+Integer.toBinaryString(optCode);
        binoption=binoption.substring ( (binoption.length()-5) );
        
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
       
        this.alwaysNew = binoption.substring(4).equals("1");
        this.nofop = binoption.substring(3,4).equals("1");
        this.inOneTransaction = binoption.substring(2,3).equals("1");
        this.deleteTable = binoption.substring(1,2).equals("1");
        this.checkFieldIsModifiable = binoption.substring(0,1).equals("1");

    }

	public void setOptionCode(Boolean alwaysNew , Boolean nofop , Boolean inOnetransaction , Boolean deletetable , Boolean checkFieldIsModifiable){
    	
        // Immerneu -> 1
        // NoFop    -> 2
        // Rollback -> 4
        // Loe Tab  -> 8
        // Modifiable -> 16
  	
    	Integer optcode=0;   	
    	
    	if (alwaysNew) { optcode = 1; }
    	if (nofop) { optcode = optcode + 2; }
    	if (inOnetransaction) { optcode = optcode + 4; }
    	if (deletetable) { optcode = optcode + 8; }
    	if (checkFieldIsModifiable) { optcode = optcode + 16; }
    	
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

	public Boolean getNofop() {
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

	
	

}
