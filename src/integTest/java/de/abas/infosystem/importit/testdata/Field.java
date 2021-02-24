package de.abas.infosystem.importit.testdata;

public class Field {
    private final String fieldName;
    private String value;

    public Field(String fieldName, String value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
