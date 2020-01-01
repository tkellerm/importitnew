package de.abaspro.infosystem.importit.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.utils.Util;

public class DataTable {

	List<Field> tableFields;
	// AbasID wegen den Kundenartikeleigenschaften
	String abasID;

	public DataTable() {
		super();
		this.tableFields = new ArrayList<>();
		this.abasID = "";
	}

	public DataTable(DataTable dataTable) throws ImportitException {
		this.tableFields = new ArrayList<>();
		for (Field field : dataTable.getTableFields()) {
			Field field2 = new Field(field.getCompleteContent(), field);
			tableFields.add(field2);
		}
	}

	public String getTableFieldValue(String fieldname) throws ImportitException {
		for (Field field : tableFields) {
			if (field.getName().equals(fieldname)) {
				return field.getValue();
			}
		}
		throw new ImportitException(Util.getMessage("data.table.err.field.not.found"));
	}

	public List<Field> getTableFields() {
		return tableFields;
	}

	public List<Field> getActiveTableFields() {
		return tableFields.stream().filter(field -> !field.getOptionSkip()).collect(Collectors.toList());
	}

	public void copyAbasType(List<Field> tabellenfeldertoCopy) {
		if (tabellenfeldertoCopy != null) {
			for (int i = 0; i < tableFields.size(); i++) {
				if (tableFields.get(i).getAbasTyp().isEmpty()) {
					tableFields.get(i).setAbasType(tabellenfeldertoCopy.get(i).getAbasTyp());
				}
			}
		}
	}

	public void copySelectionFields(List<Field> tabellenfeldertoCopy) {
		if (tabellenfeldertoCopy != null) {
			for (int i = 0; i < tableFields.size(); i++) {
				if (tableFields.get(i).getFieldSelectionString().isEmpty()) {
					tableFields.get(i).setFieldSelectionString(tabellenfeldertoCopy.get(i).getFieldSelectionString());
				}
				if (tableFields.get(i).getKeySelectionString().isEmpty()) {
					tableFields.get(i).setKeySelectionString(tabellenfeldertoCopy.get(i).getKeySelectionString());
				}
				tableFields.get(i).setOptionKeySelection(tabellenfeldertoCopy.get(i).getOptionKeySelection());
			}
		}
	}

	public boolean isEmpty() {
		Boolean isEmpty = true;
		for (Field field : tableFields) {
			if (field.getValue() != null) {
				if (!field.getValue().isEmpty()) {
					isEmpty = false;
				}
			}
		}
		return isEmpty;
	}

	public void setAbasID(String abasID) {
		this.abasID = abasID;
	}

	public String getAbasID() {
		return abasID;
	}

}
