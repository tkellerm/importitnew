package de.abaspro.infosystem.importit;

import de.abaspro.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class DataTable {

	
	ArrayList<Field> tableFields = new ArrayList<>();
	
	public String getTableFieldValue(String fieldname) throws ImportitException{
		for (Field field : tableFields) {
			if (field.getName().equals(fieldname)) {
				return field.getValue();
			}
		}
		throw new ImportitException(Util.getMessage("data.table.err.field.not.found"));
	}

	public ArrayList<Field> getTableFields() {
		return tableFields;
	}

	public DataTable(DataTable dataTable) throws ImportitException{
		for (Field field : dataTable.getTableFields()) {
			tableFields.add(new Field(field.getCompleteContent(), field));
		}
	}
	
	public DataTable(){}

	public void copyAbasType(List<Field> tabellenfeldertoCopy) {
		if (tabellenfeldertoCopy!= null) {
			for (int i = 0; i < tableFields.size(); i++) {
				if (tableFields.get(i).getAbasTyp().isEmpty()) {
					tableFields.get(i).setAbasType(tabellenfeldertoCopy.get(i).getAbasTyp());
				}
			}
		}
	}

	public boolean isEmpty() {
		Boolean isEmpty = true;
		for (Field field : tableFields) {
			if (field.getValue()!= null) {
				if (!field.getValue().isEmpty()) {
					isEmpty = false;	
				}
			}
		}
		return isEmpty;
	}
	
}
