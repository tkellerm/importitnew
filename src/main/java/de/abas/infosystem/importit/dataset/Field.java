package de.abas.infosystem.importit.dataset;

import de.abas.ceks.jedp.EDPEKSArtInfo;
import de.abas.ceks.jedp.EDPTools;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.infosystem.importit.ImportOptions;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.OptionCode;
import de.abas.utils.MessageUtil;

import java.text.MessageFormat;

public class Field {

    private String name;
    private String value;
    private String keySelectionString;
    private String key;
    private boolean optionNotEmpty;
    private boolean optionModifiable;
    private boolean optionGlobalModifiable;
    private boolean optionSkip;
    private boolean optionDontChangeIfEqual;
    private boolean optionKeySelection;
    private String error;
    private final String completeContent;
    private boolean fieldToCopy;
    private final Integer colNumber;
    private String abasTyp;
    private String abasID;
    private String fieldSelectionString;
    private final OptionCode optionCode;
    private boolean optionKey;


    public Field(String completeContent, boolean fieldToCopy, Integer col, OptionCode optionCode) {
        initField();
        this.completeContent = completeContent;
        this.fieldToCopy = fieldToCopy;
        this.colNumber = col;
        this.optionCode = optionCode;
        this.name = extractValue(completeContent);
        if (fieldToCopy) {
            fillOptions();
            fillkeyfield();
        }
    }

    public Field(String completeContent, Field headfield) throws ImportitException {
        this.optionGlobalModifiable = false;
        if (headfield.getFieldInHead()) {
            initField();
            this.completeContent = completeContent;
            this.name = headfield.getName();
            this.key = headfield.getKey();
            this.optionCode = headfield.optionCode;
            this.value = extractValue(completeContent);
            this.optionModifiable = headfield.isOptionModifiable();
            this.optionNotEmpty = headfield.isOptionNotEmpty();
            this.optionSkip = headfield.isOptionSkip();
            this.optionDontChangeIfEqual = headfield.isOptionDontChangeIfEqual();
            this.optionKeySelection = headfield.isOptionKeySelection();
            this.keySelectionString = headfield.getKeySelectionString();
            this.fieldSelectionString = headfield.getFieldSelectionString();
            this.colNumber = headfield.colNumber;
        } else {
            throw new ImportitException(MessageUtil.getMessage("error.Field.noFieldinHead"));
        }
    }

    private void initField() {
        this.error = "";
        this.name = "";
        this.abasTyp = "";
        this.abasID = "";
        this.value = "";
        this.key = "";
        this.fieldToCopy = false;
        this.keySelectionString = "";
        this.fieldSelectionString = "";
        this.optionGlobalModifiable = false;
        this.optionModifiable = false;
        this.optionNotEmpty = false;
        this.optionSkip = false;
        this.optionKeySelection = false;
        this.optionDontChangeIfEqual = false;
    }


    protected void   fillkeyfield() {
        if (this.optionKey) {
            this.key = extractkey(ImportOptions.KEY);
        } else {
            if (this.optionKeySelection) {

                this.key = extractKeyFromSelection();
            }
        }

    }

    protected String extractKeyFromSelection() {
        final String SORT = "@sort=";
        String keyString = "";
        if (this.keySelectionString.contains(SORT)) {
            int indexOf = this.keySelectionString.indexOf(SORT);
            keyString = this.keySelectionString.substring(indexOf + SORT.length());
            if (keyString.contains(";")) {
                keyString = keyString.substring(0, keyString.indexOf(";"));
            }
            if (keyString.contains("'")) {
                keyString = keyString.substring(0, keyString.indexOf("'"));
            }


        }
        return keyString;
    }



    protected String getKeySelectionString() {
        return keySelectionString;
    }

    protected void setKeySelectionString(String keySelectionString) {
        this.keySelectionString = keySelectionString;
    }

    protected Boolean isOptionKeySelection() {
        return optionKeySelection;
    }

    protected void setOptionKeySelection(Boolean optionKeySelection) {
        this.optionKeySelection = optionKeySelection;
    }

    public void setAbasID(String abasID) {
        this.abasID = abasID;
    }



    public void setOptionGlobalModifiable(Boolean optionGlobalModifiable) {
        this.optionGlobalModifiable = optionGlobalModifiable;
    }

    @Override
    public String toString() {
        return "Field [name=" + name + ", value=" + value + ", abasTyp=" + abasTyp + "]";
    }

    public Boolean isOptionDontChangeIfEqual() {
        return optionDontChangeIfEqual;
    }

    public void setOptionDontChangeIfEqual(Boolean optionDontChangeIfEqual) {
        this.optionDontChangeIfEqual = optionDontChangeIfEqual;
    }

    protected void fillOptions() {
        if (!this.completeContent.isEmpty()) {
            optionNotEmpty = this.completeContent.contains(ImportOptions.NOTEMPTY.toString());
            optionModifiable = this.completeContent.contains(ImportOptions.MODIFIABLE.toString());
            optionSkip = this.completeContent.contains(ImportOptions.SKIP.toString());
            optionDontChangeIfEqual = this.completeContent.contains(ImportOptions.DONT_CHANGE_IF_EQUAL.toString());

            if (this.completeContent.contains(ImportOptions.KEY.toString())) {
                this.optionKey = true;
                this.keySelectionString = extractSelectionString(ImportOptions.KEY);
            }
            if (this.completeContent.contains(ImportOptions.KEYSELECTION.toString())) {
                this.optionKeySelection = true;
                this.keySelectionString = extractSelectionString(ImportOptions.KEYSELECTION);
            }
            if (completeContent.contains(ImportOptions.SELECTION.toString())) {
                this.setFieldSelectionString(extractSelectionString(ImportOptions.SELECTION));
            }

        } else {
            optionSkip = true;
        }
//        checkIfOptionPossible();

    }

//    private void checkIfOptionPossible() {
//        if (this.optionKeySelection && (this.optionKey || this.optionSkip)){
//
//        }
//    }

    protected String extractSelectionString(ImportOptions importOptions) {
        switch (importOptions) {
            case SELECTION:
            case KEYSELECTION:
                return createSelfromSelection(importOptions);
            case KEY:
                return createSelFromKey(importOptions);
            default:
                return createSelFromFieldname();
        }
    }

    protected String createSelFromFieldname() {

        return this.name + "={0}";
    }

    protected String createSelFromKey(ImportOptions importOptions) {

        return this.name + "={0};@sort=" + extractkey(importOptions);
    }

    private String extractkey(ImportOptions importOptions) {
        int indexoftrenner = this.completeContent.indexOf("@");
        int optionpluslength = (importOptions.toString() + "=").length();

        return this.completeContent.substring(optionpluslength + indexoftrenner);
    }

    protected String createSelfromSelection(ImportOptions importOptions) {
        // @selection=selectionString
        String testString = importOptions.toString() + "='";
        int lengthTestString = testString.length();
        int index = this.completeContent.indexOf(testString);

        String substring = this.completeContent.substring(index + lengthTestString);
        String result;
        if (substring.contains("'")) {

            result = substring.substring(0, substring.lastIndexOf("'"));
        } else {
            result = substring;
        }

        if (result.startsWith("$,,") || result.startsWith("%,,")) {
            return result.substring(3);
        }
        if (result.startsWith("$,")) {
            return result.substring(2);
        }

        return result;
    }

    protected static String extractValue(String completeContent) {
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
            if (value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
                return "1";
            } else if (value.equalsIgnoreCase("nein") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
                return "0";
            }
        }
        return value;
    }

    public String getReferenceFieldValue() {
        if (abasTyp.startsWith("V")) {
            return this.value.replaceFirst("^[A-Z][ ]", "");
        } else {
            return this.value;
        }

    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean isOptionModifiable() {
        return this.optionModifiable || this.optionGlobalModifiable;
    }

    public Boolean isOptionSkip() {
        return optionSkip;
    }

    public String getKey() {
        return this.key;

    }

    public String getKeySelection() {
        return keySelectionString;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean getFieldInHead() {
        return fieldToCopy;
    }

    public String getAbasTyp() {
        return abasTyp;
    }

    public void setAbasType(String abasTyp) {
        this.abasTyp = abasTyp;
    }

    public boolean isOptionNotEmpty() {
        return optionNotEmpty;
    }

    public String getFieldSelectionString() {
        return fieldSelectionString;
    }

    public void setFieldSelectionString(String fieldSelectionString) {
        this.fieldSelectionString = fieldSelectionString;
    }

    public EDPVariableLanguage getEDPVariableLanguage() {
        if (optionCode.useEnglishVariables()) {
            return EDPVariableLanguage.ENGLISH;
        } else {
            return EDPVariableLanguage.GERMAN;
        }
    }

    /**
     * This function check the different Value-Fields in this Class.
     *
     * @return evaluated Value
     */
    public String getValidateFieldValue() {

        if (this.isReferenceField()) {
            if (!this.abasID.isEmpty()) {
                return this.abasID;
            } else if (!this.fieldSelectionString.isEmpty()) {
                // Es muss noch die Selektionskriterien hinzugefügt werden,
                // damit es so in das Feld eingetragen werden kann
                return "$,," + MessageFormat.format(this.fieldSelectionString, this.value);
            } else {
                return this.value;
            }
        } else {
            return this.value;
        }

    }

    public boolean isReferenceField() {
        if (!this.abasTyp.isEmpty()) {
            int datatyp = new EDPEKSArtInfo(this.abasTyp).getDataType();
            if (datatyp == EDPTools.EDP_REFERENCE) {
                return true;
            }
            return datatyp == EDPTools.EDP_ROWREFERENCE;
        }
        return false;

    }

    public boolean iswithKeySelection() {
        return this.optionKeySelection;
    }

    public Boolean isKeySelectionField() {
        if (this.optionKeySelection) {
            return true;
        }
        return !this.keySelectionString.isEmpty() && this.name.isEmpty();
    }

    public void convertToKeyField() {
        if (this.keySelectionString.isEmpty()) {
            this.keySelectionString = createSelFromFieldname();
        }

    }

    public boolean isOptionKey() {
        return optionKey;
    }

}
