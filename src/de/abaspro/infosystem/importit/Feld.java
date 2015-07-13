package de.abaspro.infosystem.importit;

public class Feld {

	private String  name;
	private String  value;
	private String  key;
	private Boolean option_notEmpty;
	private Boolean option_modifiable;
	private Boolean option_skip;
	private String  error;
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Boolean getOption_notEmpty() {
		return option_notEmpty;
	}
	
	public void setOption_notEmpty(Boolean option_notEmpty) {
		this.option_notEmpty = option_notEmpty;
	}
	
	public Boolean getOption_modifiable() {
		return option_modifiable;
	}
	
	public void setOption_modifiable(Boolean option_modifiable) {
		this.option_modifiable = option_modifiable;
	}
	
	public Boolean getOption_skip() {
		return option_skip;
	}
	
	public void setOption_skip(Boolean option_skip) {
		this.option_skip = option_skip;
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
	
	
}
