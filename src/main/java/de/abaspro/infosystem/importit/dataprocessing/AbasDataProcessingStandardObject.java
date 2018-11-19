package de.abaspro.infosystem.importit.dataprocessing;

import java.text.MessageFormat;
import java.util.List;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantChangeFieldValException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadFieldPropertyException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantReadStatusError;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.ceks.jedp.ServerActionException;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.infosystem.importit.SmlField;
import de.abaspro.infosystem.importit.SmlTab;
import de.abaspro.infosystem.importit.dataset.Data;
import de.abaspro.infosystem.importit.dataset.Field;
import de.abaspro.utils.Util;

public class AbasDataProcessingStandardObject extends AbstractDataProcessing {

	public AbasDataProcessingStandardObject(EDPSessionHandler edpSessionHandler) {
		super(edpSessionHandler);
	}

	@Override
	protected void writeData(Data data) throws ImportitException {
		logger.debug(Util.getMessage("debug.getedpsession", "writeData"));

		try {
			writeDatabase(data);
		} catch (Exception e) {
			logger.error(e);
			data.appendError(e);
		}
	}

	private void writeDatabase(Data data) throws CantChangeSettingException, ImportitException, CantSaveException,
			InvalidQueryException, CantReadFieldPropertyException, CantChangeFieldValException,
			InvalidRowOperationException, ServerActionException, CantReadSettingException {
		EDPEditor edpEditor = null;

		try {
			data.setImported(false);
			data.initOptions();

			if (data.getOptionCode().getAlwaysNew()) {

				logger.info(Util.getMessage("info.start.editor.new", data.getDatabase().toString(),
						data.getGroup().toString()));

				edpEditor = createEDPEditorNew(data.getDatabase().toString(), data.getGroup().toString(),
						data.getEDPLanguage(), data);

				writeFieldsInEditor(data, edpEditor);

				edpEditor.saveReload();
				String abasId = edpEditor.getEditRef();
				logger.info(Util.getMessage("info.save.editor.new", data.getDatabase().toString(),
						data.getGroup().toString(), abasId));
				data.setAbasId(abasId);
				edpEditor.endEditSave();
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
				String criteria = "";
				String objectId;
				if (data.getAbasId().isEmpty()) {
					if (!data.getNameOfKeyfield().isEmpty()) {
						criteria = data.getNameOfKeyfield() + "=" + data.getValueOfKeyField();
					} else {
						criteria = MessageFormat.format(data.getFieldKeySelectionString(), data.getKeyFieldValue());
					}
					objectId = getSelObject(criteria, data);
				} else {
					objectId = data.getAbasId();
				}

				if (!objectId.equals("Z")) {

					edpEditor = getEPDEditorforObjectId(data, objectId);

					writeFieldsInEditor(data, edpEditor);
					edpEditor.saveReload();
					String abasId = edpEditor.getEditRef();
					logger.info(Util.getMessage("info.save.editor.new", data.getDatabase().toString(),
							data.getGroup().toString()));
					data.setAbasId(abasId);
					edpEditor.endEditSave();
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
			}
		} catch (CantBeginEditException e) {
			logger.error(e);
		} catch (CantReadStatusError e) {
			logger.error(e);
		} finally {
			if (edpEditor != null) {
				if (edpEditor.isActive()) {
					edpEditor.endEditCancel();
					releaseAndFreeEDPEditor(edpEditor);
				}
			}
		}
	}

	@Override
	protected boolean checkDataStructure(Data data) throws ImportitException {
		boolean validDb = checkData(data);
		boolean validHead = false;
		boolean validTable = false;
		boolean validSML = false;
		if (validDb) {
			List<Field> headerFields = data.getHeaderFields();
			List<Field> tableFields = data.getTableFields();
			// validHead = false;
			// validTable = false;
			try {

				validHead = checkFieldList(headerFields, data.getDatabase(), data.getGroup(), false,
						data.getOptionCode().useEnglishVariables());
				validTable = checkFieldList(tableFields, data.getDatabase(), data.getGroup(), true,
						data.getOptionCode().useEnglishVariables());
				if (data.getSmlFields() != null) {
					validSML = checkSMLStructure(data);
				} else {
					validSML = true;
				}

			} catch (ImportitException e) {
				logger.error(e);
				data.appendError(Util.getMessage("err.structure.check", e));
			}
		}
		if (validTable && validHead && validDb && validSML) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkSMLStructure(Data data) throws ImportitException {
		String numberSML = data.getSmlString();
		Boolean founderror = false;
		SmlTab smlTab = new SmlTab(this.edpSessionHandler, numberSML);

		List<Field> smlfields = data.getSmlFields();
		for (Field field : smlfields) {
			SmlField smlField = smlTab.checkSmlTab(field.getName().substring(2));
			if (smlField == null) {
				data.appendError(Util.getMessage("err.field.not.found", field.getName().substring(2)));
				founderror = true;
			} else {
				field.setAbasType(smlField.getType());
			}
		}
		if (founderror) {
			return false;
		} else
			return true;

	}

	private boolean checkData(Data data) throws ImportitException {
		Boolean exists = false;
		exists = checkDatabaseName(data);
		return exists;
	}

	@Override
	protected void writeAbasIDinData(Data data) throws ImportitException {
		String criteria = null;
		String keyOfKeyfield = data.getKeyOfKeyfield();

		if (!data.getSelectionStringOfKeyfield().isEmpty()) {
			criteria = MessageFormat.format(data.getSelectionStringOfKeyfield(), data.getValueOfKeyField());

		} else if (!keyOfKeyfield.isEmpty()) {

			criteria = data.getNameOfKeyfield() + "=" + data.getValueOfKeyField();

		}

		if (criteria != null) {
			makeSelection(data, criteria);
		}

	}

	private void makeSelection(Data data, String criteria) throws ImportitException {
		try {
			String abasID = getSelObject(criteria, data);
			if (!abasID.equals("Z") && !abasID.equals("0")) {
				data.setAbasId(abasID);
			} else {
				data.setAbasId("");
				if (abasID.equals("Z")) {
					data.appendError(Util.getMessage("error.checkdata.toManyResults"));
				}
			}
		} catch (ImportitException e) {
			data.appendError(e);
		}
	}

}
