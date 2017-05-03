package de.abaspro.infosystem.importit;

import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abaspro.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private List<Field> headerFields = new ArrayList<Field>();
    private List<DataTable> tableFields = new ArrayList<DataTable>();
    private List<Field> smlFields = new ArrayList<Field>(); 
    private Integer database;
    private String dbString;
    private Integer group;
    private String dbGroupString;
    private Integer typeCommand;
    private String typeCommandString;
    private String smlString;
    private String importError = "";
    private Integer tableStartsAtField;
    private OptionCode optionCode;
    
    public String getSmlString() {
		return smlString;
	}

	public void setSmlString(String smlString) {
		this.smlString = smlString;
	}

	public List<Field> getSmlFields() {
		return smlFields;
	}

	private Integer keyField;
    private String abasId;
    private String errorReport = "";
    private String errorDebug = "";
    private Boolean isImported = false;

    public Integer getTableStartsAtField() {
        return tableStartsAtField;
    }

    public void setTableStartsAtField(Integer tableStartsAtField) {
        this.tableStartsAtField = tableStartsAtField;
    }

    public Integer getKeyField() {
        return keyField;
    }

    public OptionCode getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(OptionCode optionCode) {
        this.optionCode = optionCode;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public Integer getTypeCommand() {
        return typeCommand;
    }

    public void setTypeCommand(Integer typeCommand) {
        this.typeCommand = typeCommand;
    }

    public String getAbasId() {
        return abasId;
    }

    public void setAbasId(String abasId) {
        this.abasId = abasId;
    }

    public List<Field> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(List<Field> headerFields) throws ImportitException {
        this.headerFields = headerFields;
        this.keyField = checkKeyField(this.headerFields);
    }

    public void setSmlFields(List<Field> smlFields) {
		this.smlFields = smlFields;
	}

	public Boolean getOptionTransaction() {
        return getOptionCode().getInOneTransaction();
    }

    public void initOptions() {
        ArrayList<Field> fields = new ArrayList<>();
        fields.addAll(headerFields);
        for (DataTable row : tableFields) {
            fields.addAll(row.getTableFields());
        }
        for (Field field : fields) {
            field.setOptionGlobalModifiable(optionCode.getCheckFieldIsModifiable());
            field.setOptionDontChangeIfEqual(optionCode.getDontChangeIfEqual());
        }
    }

    private Integer checkKeyField(List<Field> headerFields) throws ImportitException {
        Integer keyField = 0;
        for (Field field : headerFields) {
            if (!field.getKey().isEmpty()) {
                if (keyField != 0) {
                    throw new ImportitException(Util.getMessage("data.err.too.many.keys"));
                }
                keyField = field.getColNumber();
            }
        }
        return keyField;
    }

    public List<DataTable> getTableRows() {
        return tableFields;
    }

    public Integer getDatabase() {
        return database;
    }

    public void setDatabase(Integer database) {
        this.database = database;
    }

    public String toString() {
        try {
            return this.getDatabase() + ":" + this.getGroup() + " " + getValueOfKeyField();
        } catch (ImportitException e) {
            return this.getDatabase() + ":" + this.getGroup();
        }
    }

    public String getValueOfKeyField() throws ImportitException {
        if (getHeaderFields() != null) {
            Field field = getHeaderFields().get(keyField);
            if (field != null) {
                return field.getValue();
            } else {
                throw new ImportitException(Util.getMessage("data.err.field.not.init", keyField));
            }
        } else {
            throw new ImportitException(Util.getMessage("data.err.head.fields.missing"));
        }
    }

    public String getKeyOfKeyfield() throws ImportitException {
        if (getHeaderFields() != null) {
            Field field = getHeaderFields().get(keyField);
            if (field != null) {
                return field.getKey();
            } else {
                throw new ImportitException(Util.getMessage("data.err.field.not.init", keyField));
            }
        } else {
            throw new ImportitException(Util.getMessage("data.err.head.fields.missing"));
        }

    }

    public String getNameOfKeyfield() throws ImportitException {
        if (getHeaderFields() != null) {
            Field field = getHeaderFields().get(keyField);
            if (field != null) {
                return field.getName();
            } else {
                throw new ImportitException(Util.getMessage("data.err.field.not.init", keyField));
            }

        } else {
            throw new ImportitException(Util.getMessage("data.err.head.fields.missing"));
        }
    }

    public List<Field> getTableFields() {
        List<DataTable> rows = this.getTableRows();
        if (rows.size() > 0) {
            return rows.get(0).getTableFields();
        } else {
            return null;
        }
    }

    public void copyAbasType(Data data) {
        for (int i = 0; i < headerFields.size(); i++) {
            if (headerFields.get(i).getAbasTyp().isEmpty()) {
                headerFields.get(i).setAbasType(data.headerFields.get(i).getAbasTyp());
            }
        }
        List<Field> tableFields = data.getTableFields();
        for (DataTable dataTable : this.tableFields) {
            dataTable.copyAbasType(tableFields);
        }
    }


    public void copyDatabase(Data data) {
        database = data.getDatabase();
        group = data.getGroup();
        typeCommand = data.getTypeCommand();
    }


    public void appendError(Exception e) {
        if (importError.isEmpty()) {
            importError = e.getMessage();
        } else {
            importError = importError + "\n" + e.getMessage();
        }
        if (errorDebug.isEmpty()) {
            errorDebug = e.getMessage() + "\n" + e.getStackTrace().toString();
        } else {
            errorDebug = errorDebug + "\n" + e.getMessage() + "\n" + e.getStackTrace().toString();
        }
    }

    public void appendError(String errorString) {
        importError = importError + "\n" + errorString;
    }

    public String getErrorReport() {
        return errorReport;
    }

    public void createErrorReport() {
        this.errorReport = "";
        if (this.importError != null) {
            textToErrorReport(this.importError);
        }
        String headText = Util.getMessage("data.err.head.text");
        for (Field field : headerFields) {
            if (field.getError() != null) {
                if (!field.getError().isEmpty()) {
                    if (!headText.isEmpty()) {
                        textToErrorReport(headText);
                        headText = "";
                    }
                    fieldToErrorReport(field);
                }
            }
        }
        for (DataTable row : tableFields) {
            int rowIndex = tableFields.indexOf(row);
            String rowText = Util.getMessage("data.err.row.text", rowIndex);
            ArrayList<Field> tableFields = row.getTableFields();
            for (Field field : tableFields) {
                if (field.getError() != null) {
                    if (!field.getError().isEmpty()) {
                        if (!rowText.isEmpty()) {
                            textToErrorReport(rowText);
                            rowText = "";
                        }
                        fieldToErrorReport(field);
                    }
                }
            }
        }
    }

    private void textToErrorReport(String text) {
        if (this.errorReport.isEmpty()) {
            this.errorReport = text;
        } else {
            errorReport = errorReport + "\n" + text;
        }
    }

    private void fieldToErrorReport(Field field) {
        if (!field.getError().isEmpty()) {
            textToErrorReport(Util.getMessage("data.err.field.error.report", field.getName(), field.getColNumber(), field.getError()));
        }
    }

    public String getValueOfHeadField(String fieldName) throws ImportitException {
        for (Field field : headerFields) {
            if (field.getName().equals(fieldName)) {
                return field.getValue();
            }

        }
        throw new ImportitException(Util.getMessage("data.head.err.field.not.found", fieldName));
    }

    public String getDbString() {
        return dbString;
    }

    public void setDbString(String dbString) {
        this.dbString = dbString;
    }

    public String getDbGroupString() {
        return dbGroupString;
    }

    public void setDbGroupString(String dbgroupString) {
        this.dbGroupString = dbgroupString;
    }

    public String getTypeCommandString() {
        return typeCommandString;
    }

    public void setTypeCommandString(String typeCommandString) {
        this.typeCommandString = typeCommandString;
    }

    public Boolean isImported() {
        return isImported;
    }

    public void setImported(Boolean isImported) {
        this.isImported = isImported;
    }

    public EDPVariableLanguage getEDPLanguage() {
        if (optionCode.useEnglishVariables()) {
            return EDPVariableLanguage.ENGLISH;
        } else {
            return EDPVariableLanguage.GERMAN;
        }
    }

}
