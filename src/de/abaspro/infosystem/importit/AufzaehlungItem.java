package de.abaspro.infosystem.importit;

public class AufzaehlungItem {
	Integer number;
	String namebspr;
	String nameNeutral;

	public AufzaehlungItem(Integer number, String namebspr, String nameneutral) {
		super();
		this.number = number;
		this.namebspr = namebspr;
		this.nameNeutral = nameneutral;
	}
	
	
	public Integer getNumber() {
		return number;
	}

	public String getNamebspr() {
		return namebspr;
	}

	public String getNameNeutral() {
		return nameNeutral;
	}
	
}
