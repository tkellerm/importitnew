package de.abaspro.infosystem.importit;

public enum ImportOptionen {

	NOTEMPTY ("@notempty" ) , MODIFIABLE( "@modifiable") , SKIP("@skip") , KEY("@Schlüssel");

	private String eintrag;
	
	private ImportOptionen(String text) {
		
		this.eintrag = text ;
	}
	
	public String getSearchstring(){
		return eintrag;
	}
	
	
}
