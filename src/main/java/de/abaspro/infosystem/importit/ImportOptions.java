package de.abaspro.infosystem.importit;

public enum ImportOptions {

	NOTEMPTY("@notempty"), MODIFIABLE("@modifiable"), SKIP("@skip"), KEY("@Schlüssel"), DONT_CHANGE_IF_EQUAL(
			"@dontChangeIfEqual"), SELECTION("@selection");

	private String value;

	ImportOptions(String text) {
		this.value = text;
	}

	public String toString() {
		return value;
	}

}
