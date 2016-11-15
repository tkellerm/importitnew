package de.abaspro.infosystem.importit;

import static de.abaspro.infosystem.importit.ImportOptions.*;

public class Field {

    private String name;
    private String value;
    private String key;
    private Boolean optionNotEmpty;
    private Boolean optionModifiable;
    private Boolean optionGlobalModifiable;
    private Boolean optionSkip;
    private Boolean optionDontChangeIfEqual;
    private String error;
    private String completeContent;
    private Boolean fieldToCopy;
    private Integer colNumber;
    private String abasTyp;

    public Field(String completeContent, Boolean fieldToCopy, Integer col) {
        this.completeContent = completeContent;
        this.fieldToCopy = fieldToCopy;
        this.colNumber = col;
        this.key = "";
        this.error = "";
        this.name = "";
        this.abasTyp = "";
        this.optionGlobalModifiable = false;
        this.optionModifiable = false;
        this.optionNotEmpty = false;
        this.optionSkip = false;
        this.optionDontChangeIfEqual = false;
        if (fieldToCopy) {
            this.name = extractValue(completeContent);
            fillOptions(completeContent);
        } else {
            this.value = extractValue(completeContent);
        }
    }

    public Field(String completeContent, Field headfield) throws ImportitException {
        this.optionGlobalModifiable = false;
        if (headfield.getFieldInHead()) {
            this.completeContent = completeContent;
            this.fieldToCopy = false;
            this.name = "";
            this.name = headfield.getName();
            this.value = extractValue(completeContent);
            this.optionModifiable = headfield.getOptionModifiable();
            this.optionNotEmpty = headfield.getOptionNotEmpty();
            this.optionSkip = headfield.getOptionSkip();
            this.optionDontChangeIfEqual = headfield.getOptionDontChangeIfEqual();
            this.colNumber = headfield.colNumber;
            this.key = "";
            this.error = "";
            this.abasTyp = "";
        } else {
            throw new ImportitException("Es wurde kein Field mit der Eigenschaft fieldInHead �bergeben!");
        }
    }

    public void setOptionGlobalModifiable(Boolean optionGlobalModifiable) {
        this.optionGlobalModifiable = optionGlobalModifiable;
    }

    @Override
    public String toString() {
        return "Field [name=" + name + ", value=" + value + ", abasTyp=" + abasTyp + "]";
    }

    public Boolean getOptionDontChangeIfEqual() {
        return optionDontChangeIfEqual;
    }

    public void setOptionDontChangeIfEqual(Boolean optionDontChangeIfEqual) {
        this.optionDontChangeIfEqual = optionDontChangeIfEqual;
    }

    private void fillOptions(String completeContent) {
        if (!completeContent.isEmpty()) {
            optionNotEmpty = completeContent.contains(NOTEMPTY.toString());
            optionModifiable = completeContent.contains(MODIFIABLE.toString());
            optionSkip = completeContent.contains(SKIP.toString());
            optionDontChangeIfEqual = completeContent.contains(DONT_CHANGE_IF_EQUAL.toString());
            if (completeContent.contains(KEY.toString())) {
                String modifiable = completeContent.substring(completeContent.indexOf(MODIFIABLE.toString() + "="));
                key = modifiable.substring(0, modifiable.indexOf("@"));
            }
        } else {
            optionSkip = true;
        }
    }

    private String extractValue(String completeContent) {
        int index = completeContent.indexOf("@");
        if (index != -1) {
            return completeContent.substring(0, index);
        }
        return completeContent;
    }

    public String getCompleteContent() {
        return completeContent;
    }

    public Integer getColNumber() {
        return colNumber;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (abasTyp.equals("B")) {
            if (value.equals("ja") || value.equals("yes") || value.equals("true")) {
                return "1";
            } else if (value.equals("nein") || value.equals("no") || value.equals("false")) {
                return "0";
            }
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getOptionModifiable() {
        return this.optionModifiable || this.optionGlobalModifiable;
    }

    public Boolean getOptionSkip() {
        return optionSkip;
    }

    public String getKey() {
        return key;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Boolean getFieldInHead() {
        return fieldToCopy;
    }

    public String getAbasTyp() {
        return abasTyp;
    }

    public void setAbasType(String abasTyp) {
        this.abasTyp = abasTyp;
    }

    public Boolean getOptionNotEmpty() {
        return optionNotEmpty;
    }

}
