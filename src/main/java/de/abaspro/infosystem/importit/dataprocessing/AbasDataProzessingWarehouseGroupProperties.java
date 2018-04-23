package de.abaspro.infosystem.importit.dataprocessing;

import java.util.ArrayList;
import java.util.List;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPEditAction;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.infosystem.importit.dataset.Data;
import de.abaspro.infosystem.importit.dataset.DataTable;
import de.abaspro.infosystem.importit.dataset.Field;
import de.abaspro.utils.Util;

public class AbasDataProzessingWarehouseGroupProperties extends AbstractDataProcessing {

	public AbasDataProzessingWarehouseGroupProperties(EDPSessionHandler edpSessionHandler) {
		super(edpSessionHandler);
	}

	@Override
	protected void writeData(Data data) throws ImportitException {

		try {
			writeWarehouseGroupProperties(data);
		} catch (Exception e) {
			logger.error(e);
			data.appendError(e);
		}
	}

	private void writeWarehouseGroupProperties(Data data)
			throws ImportitException, InvalidQueryException, CantChangeSettingException, CantBeginEditException,
			InvalidRowOperationException, CantSaveException, CantReadSettingException {

		EDPEditor edpEditor = null;
		EDPQuery edpQuery = null;
		data.setImported(false);

		logger.info(Util.getMessage("info.import.part.warehouse.props"));

		List<DataTable> tableRows = data.getTableRows();
		for (DataTable dataTable : tableRows) {

			try {
				String criteria = "";
				String objectId;
				if (dataTable.getAbasID().isEmpty()) {
					criteria = getObjectSearchCriteria(dataTable, data);
					objectId = getSelObject(criteria, data);
				} else {

					objectId = dataTable.getAbasID();
				}

				if (!objectId.equals("Z")) {

					edpEditor = getEPDEditorforObjectId(data, objectId);

					final String[] IgnoreFieldNames = { "art", "artikel", "product", "lgruppe", "warehGrp" };
					writeFieldsInEditor(data, dataTable, edpEditor, IgnoreFieldNames);

					edpEditor.saveReload();

					String abasId = edpEditor.getEditRef();

					logger.info(Util.getMessage("info.save.editor.new", data.getDatabase().toString(),
							data.getGroup().toString(), abasId));
					dataTable.setAbasID(abasId);
					// edpEditor.endEditSave();
					data.setImported(true);
					if (edpEditor.isActive()) {
						edpEditor.endEditCancel();
						logger.info(Util.getMessage("info.cancel.editor.save", data.getDatabase().toString(),
								data.getGroup().toString(), abasId));
					} else {
						logger.info(Util.getMessage("info.editor.not.active"));
					}
					releaseAndFreeEDPEditor(edpEditor);
				} else {
					data.appendError(Util.getMessage("err.selection.ambiguous", criteria));
				}

			} catch (Exception e) {
				logger.error(e);
				data.appendError(e);
			} finally {
				EDPUtils.releaseEDPEditor(edpEditor, logger);
				EDPUtils.releaseQuery(edpQuery, logger);
			}
		}

	}

	private String[] checkDataWarehouseGroupProperties(Data data) throws ImportitException {
		Integer database = data.getDatabase();
		Integer group = data.getGroup();
		Boolean artVarFound = false;
		Boolean lgruppeVarFound = false;
		String varNameArt = "";
		String varNameLGruppe = "";
		String[] varNames = new String[2];
		if (database == 39 & (group == 3 || group == 4)) {
			List<Field> headerFields = data.getHeaderFields();
			for (Field field : headerFields) {
				if (field.getName().equals("art") || field.getName().equals("artikel")
						|| field.getName().equals("product")) {
					artVarFound = true;
					varNameArt = field.getName();
				}
			}
			List<Field> tableFields = data.getTableFields();
			for (Field field : tableFields) {
				if (field.getName().equals("lgruppe") || field.getName().equals("warehGrp")) {
					lgruppeVarFound = true;
					varNameLGruppe = field.getName();
				}
			}
			if (artVarFound & lgruppeVarFound) {
				varNames[0] = varNameArt;
				varNames[1] = varNameLGruppe;
				return varNames;
			} else {
				throw new ImportitException(Util.getMessage("err.check.warehouse.props.missingField"));
			}

		}
		throw new ImportitException(
				Util.getMessage("err.check.warehouse.props.falseDatabase", data.getDatabase(), data.getGroup()));
	}

	private void writeFieldsInEditor(Data data, DataTable dataTable, EDPEditor edpEditor, String[] ignoreFields)
			throws ImportitException {
		if (edpEditor.isActive()) {
			if (edpEditor.getEditAction() == EDPEditAction.NEW) {
				List<Field> headerFields = data.getHeaderFields();
				for (Field field : headerFields) {
					writeField(data, field, edpEditor, 0);
				}
				if (dataTable != null && edpEditor.hasTablePart()) {
					Integer rowCount = edpEditor.getRowCount();
					Integer rowNumber = insertRow(data, edpEditor, rowCount);
					ArrayList<Field> tableFields = dataTable.getTableFields();
					for (Field field : tableFields) {
						writeField(data, field, edpEditor, rowNumber);
					}
				}
			} else if (edpEditor.getEditAction() == EDPEditAction.UPDATE) {
				List<Field> headerFields = data.getHeaderFields();
				for (Field field : headerFields) {
					if (dontIgnoreField(field, ignoreFields)) {
						writeField(data, field, edpEditor, 0);
					}
				}
				if (dataTable != null && edpEditor.hasTablePart()) {
					Integer rowNumber = edpEditor.getCurrentRow();
					ArrayList<Field> tableFields = dataTable.getTableFields();
					for (Field field : tableFields) {
						if (dontIgnoreField(field, ignoreFields)) {
							writeField(data, field, edpEditor, rowNumber);
						}
					}
				}
			}
		}
	}

	private boolean dontIgnoreField(Field field, String[] ignoreFields) {
		String fieldName = field.getName();
		for (String ignoreField : ignoreFields) {
			if (ignoreField.equals(fieldName)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean checkDataStructure(Data data) throws ImportitException {
		// Da die Datenbank fix ist bei den Lagergruppeneigenschaften
		data.setDatabase(39);
		data.setGroup(3);
		boolean validDb = checkDatabaseName(data);
		boolean validHead = false;
		boolean validTable = false;
		boolean existImportantFields = false;
		if (validDb) {
			List<Field> headerFields = data.getHeaderFields();
			List<Field> tableFields = data.getTableFields();
			validHead = false;
			validTable = false;
			try {

				validHead = checkWarehouseGroupProperties(headerFields, data.getOptionCode().useEnglishVariables());
				validTable = checkFieldList(tableFields, 39, 3, false, data.getOptionCode().useEnglishVariables());
				String[] checkDataCustomerPartProperties = checkDataWarehouseGroupProperties(data);
				existImportantFields = checkDataCustomerPartProperties.length == 2;

			} catch (ImportitException e) {
				logger.error(e);
				data.appendError(Util.getMessage("err.structure.check", e.getMessage()));
			}
		}
		if (validTable && validHead && validDb & existImportantFields) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkWarehouseGroupProperties(List<Field> headerFields, Boolean englishVariables)
			throws ImportitException {
		if (headerFields.size() == 1) {
			for (Field field : headerFields) {
				String varName = field.getName();
				if (varName.equals("product") || varName.equals("art") || varName.equals("artikel")) {
					return checkFieldList(headerFields, 39, 3, false, englishVariables);
				}
			}

			throw new ImportitException(Util.getMessage("err.variables.missing"));
		} else {
			throw new ImportitException(Util.getMessage("err.too.many.head.fields"));
		}
	}

	@Override
	protected void writeAbasIDinData(Data data) {
		try {
			List<DataTable> tableRows = data.getTableRows();
			for (DataTable dataTable : tableRows) {
				String criteria = getObjectSearchCriteria(dataTable, data);
				String test;
				String abasID = getSelObject(criteria, data);
				dataTable.setAbasID(abasID);
			}

		} catch (ImportitException e) {
			data.appendError(e);
		}
	}

	private String getObjectSearchCriteria(DataTable datatable, Data data) throws ImportitException {

		String[] varNames = checkDataWarehouseGroupProperties(data);

		String artField = varNames[0];
		String lgruppeField = varNames[1];
		String criteria = artField + "=" + data.getValueOfHeadField(artField) + ";" + lgruppeField + "="
				+ datatable.getTableFieldValue(lgruppeField);
		return criteria;
	}
}
