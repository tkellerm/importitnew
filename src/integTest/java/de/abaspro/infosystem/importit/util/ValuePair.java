package de.abaspro.infosystem.importit.util;

public class ValuePair {
	private String field;
	private String value;

	public ValuePair(String field, String value) {
		super();
		this.field = field;
		this.value = value;
	}

	protected String getField() {
		return field;
	}

	protected String getValue() {
		return value;
	}

}
