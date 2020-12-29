package de.abas.infosystem.importit;

public class EnumerationItem {
	Integer number;
	String namebspr;
	String nameNeutral;

	public EnumerationItem(Integer number, String namebspr, String nameneutral) {
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
