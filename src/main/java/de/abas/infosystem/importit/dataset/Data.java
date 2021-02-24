package de.abas.infosystem.importit.dataset;

import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.OptionCode;
import de.abas.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Data {

	public static final String DATA_ERR_FIELD_NOT_INIT = "data.err.field.not.init";
	public static final String DATA_ERR_HEAD_FIELDS_MISSING = "data.err.head.fields.missing";
	private List<Field> headerFields = new ArrayList<>();
	private final List<DataTable> tableFields = new ArrayList<>();
	private List<Field> smlFields = new ArrayList<>();
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
	private Integer keyFieldPosition;
	private String abasId;
	private String errorReport = "";
	private String errorDebug = "";
	private Boolean isImported = false;

	public String getSmlString() {
		return smlString;
	}

	public void setSmlString(String smlString) {
		this.smlString = smlString;
	}

	public List<Field> getSmlFields() {
		return smlFields;
	}

	public Integer getTableStartsAtField() {
		return tableStartsAtField;
	}

	public void setTableStartsAtField(Integer tableStartsAtField) {
		this.tableStartsAtField = tableStartsAtField;
	}

	public Integer getKeyFieldPosition() {
		return keyFieldPosition;
	}

	public String getFieldKeySelectionString() {
		Field keyField = this.getKeyField();
		return keyField.getKeySelectionString();
	}

	public String getKeyFieldValue() {
		Field keyField = this.getKeyField();
		return keyField.getValue();
	}

	public Field getKeyField() {

		for (Field field : headerFields) {
			String key = field.getKey();
			String keySelectionString = field.getKeySelectionString();
			if (!(key.isEmpty() && keySelectionString.isEmpty())) {
				return field;
			}
		}

		return null;
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
		this.keyFieldPosition = checkKeyField(this.headerFields);
	}

	public void setSmlFields(List<Field> smlFields) {
		this.smlFields = smlFields;
	}

	public Boolean getOptionTransaction() {
		return getOptionCode().getInOneTransaction();
	}

	public void initOptions() {
		List<Field> fields = new ArrayList<>(headerFields);
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
			if (!field.getKey().isEmpty() || !field.getKeySelection().isEmpty()) {
				if (keyField != 0) {
					throw new ImportitException(MessageUtil.getMessage("data.err.too.many.keys"));
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
			Field field = getHeaderFields().get(keyFieldPosition);
			if (field != null) {
				return field.getValue();
			} else {
				throw new ImportitException(MessageUtil.getMessage(DATA_ERR_FIELD_NOT_INIT, keyFieldPosition));
			}
		} else {
			throw new ImportitException(MessageUtil.getMessage(DATA_ERR_HEAD_FIELDS_MISSING));
		}
	}

	public String getKeyOfKeyField() throws ImportitException {

		if (getHeaderFields() != null) {
			Field field = getHeaderFields().get(this.keyFieldPosition);
			if (field != null) {
				return field.getKey();
			} else {
				throw new ImportitException(MessageUtil.getMessage(DATA_ERR_FIELD_NOT_INIT, keyFieldPosition));
			}
		} else {
			throw new ImportitException(MessageUtil.getMessage(DATA_ERR_HEAD_FIELDS_MISSING));
		}

	}

	public String getSelectionStringOfKeyField() throws ImportitException {
		if (getHeaderFields() != null) {
			Field field = getHeaderFields().get(this.keyFieldPosition);
			if (field != null) {
				return field.getKeySelection();
			} else {
				throw new ImportitException(MessageUtil.getMessage(DATA_ERR_FIELD_NOT_INIT, keyFieldPosition));
			}
		} else {
			throw new ImportitException(MessageUtil.getMessage(DATA_ERR_HEAD_FIELDS_MISSING));
		}

	}

	public String getNameOfKeyField() throws ImportitException {
		if (getHeaderFields() != null) {
			Field field = getHeaderFields().get(keyFieldPosition);
			if (field != null) {
				return field.getName();
			} else {
				throw new ImportitException(MessageUtil.getMessage(DATA_ERR_FIELD_NOT_INIT, keyFieldPosition));
			}

		} else {
			throw new ImportitException(MessageUtil.getMessage(DATA_ERR_HEAD_FIELDS_MISSING));
		}
	}

	public List<Field> getTableFields() {
		List<DataTable> rows = this.getTableRows();
		if (!rows.isEmpty()) {
			return rows.get(0).getTableFields();
		} else {
			return new ArrayList<>();
		}
	}

	public void copyAbasType(Data data) {
		List<Field> dataHeaderFields = data.getHeaderFields();

		for (int i = 0; i < dataHeaderFields.size(); i++) {
			//TODO fail-fast !!! minum else with exception
			// or check it when creatze headerFieldList and TableFieldList
			// this.headerFields.get(i).getAbasTyp().isEmpty() first check then the null checks
			if (this.headerFields.size() >= i && this.headerFields.get(i) != null && data.headerFields.get(i) != null && this.headerFields.get(i).getAbasTyp().isEmpty()) {

					this.headerFields.get(i).setAbasType(data.headerFields.get(i).getAbasTyp());

			}

		}
		List<Field> tableFieldsLocal = data.getTableFields();
		for (DataTable dataTable : this.tableFields) {
			dataTable.copyAbasType(tableFieldsLocal);
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
			errorDebug = e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
		} else {
			errorDebug = errorDebug + "\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
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
		errorReportFromHeaderFields();

		errorReportFromTableFields();
	}

	private void errorReportFromHeaderFields() {
		String headText = MessageUtil.getMessage("data.err.head.text");
		for (Field field : headerFields) {
			if (field.getError() != null && !field.getError().isEmpty()) {

					if (!headText.isEmpty()) {
						textToErrorReport(MessageUtil.getMessage("data.err.head.text"));
						headText = "";
					}
					fieldToErrorReport(field);

			}
		}
	}

	private void errorReportFromTableFields() {
		for (DataTable row : tableFields) {
			int rowIndex = tableFields.indexOf(row);
			String rowText = MessageUtil.getMessage("data.err.row.text", rowIndex);
			List<Field> tableFieldsLocal = row.getTableFields();
			for (Field field : tableFieldsLocal) {
				if (field.getError() != null && !field.getError().isEmpty()) {

					if (!rowText.isEmpty()) {
						textToErrorReport(rowText);
						rowText = "";
					}
					fieldToErrorReport(field);

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
			textToErrorReport(MessageUtil.getMessage("data.err.field.error.report", field.getName(), field.getColNumber(),
					field.getError()));
		}
	}

	public String getValueOfHeadField(String fieldName) throws ImportitException {
		for (Field field : headerFields) {
			if (field.getName().equals(fieldName)) {
				return field.getValue();
			}

		}
		throw new ImportitException(MessageUtil.getMessage("data.head.err.field.not.found", fieldName));
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

	public void setDbGroupString(String dbGroupString) {
		this.dbGroupString = dbGroupString;
	}

	public String getTypeCommandString() {
		return typeCommandString;
	}

	public void setTypeCommandString(String typeCommandString) {
		this.typeCommandString = typeCommandString;
	}

	public boolean isImported() {
		return isImported;
	}

	public void setImported(Boolean isImported) {
		this.isImported = isImported;
	}

	public EDPVariableLanguage getEDPLanguage() {
		if (Boolean.TRUE.equals(optionCode.useEnglishVariables())) {
			return EDPVariableLanguage.ENGLISH;
		} else {
			return EDPVariableLanguage.GERMAN;
		}
	}

	public void fillKeyField() {
		Integer pos = getKeyFieldPosition();
		Field keyField = this.headerFields.get(pos);
		keyField.convertToKeyField();

	}

}
