package de.abaspro.infosystem.importit.dataprocessing;

import java.util.List;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantChangeFieldValException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadFieldPropertyException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantReadStatusException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.ceks.jedp.ServerActionException;
import de.abaspro.infosystem.importit.Data;
import de.abaspro.infosystem.importit.Field;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.utils.Util;

public class AbasDataProcessingStandardObject extends AbstractDataProcessing {

	public AbasDataProcessingStandardObject(EDPSessionHandler edpSessionHandler) {
		super(edpSessionHandler);
	}

	@Override
	protected void writeData(Data data) throws ImportitException {
		logger.debug(Util.getMessage("debug.getedpsession", "writeData"));
		EDPSession edpSession = this.edpSessionHandler.getEDPSessionWriteData(data.getEDPLanguage());
		if (edpSession.isConnected()) {
			EDPEditor edpEditor = edpSession.createEditor();
			try {
				writeDatabase(data, edpEditor);
			} catch (Exception e) {
				logger.error(e);
				data.appendError(e);
			} finally {
				if (!edpEditor.isReleased()) {
					try {
						edpEditor.release();
					} catch (ServerActionException e) {
						logger.error(e);
					}
				}

				this.edpSessionHandler.freeEDPSession(edpSession);
			}
		} else {
			super.logger.error(Util.getMessage("err.no.edp.session"));
			throw new ImportitException(Util.getMessage("err.no.edp.session"));
		}
	}

	private void writeDatabase(Data data, EDPEditor edpEditor)
			throws CantBeginEditException, CantChangeSettingException, ImportitException, CantSaveException,
			InvalidQueryException, CantReadFieldPropertyException, CantChangeFieldValException,
			InvalidRowOperationException, ServerActionException, CantReadStatusException, CantReadSettingException {

		data.setImported(false);
		data.initOptions();
		setEditorOption(data, edpEditor);

		if (data.getOptionCode().getAlwaysNew()) {

			logger.info(Util.getMessage("info.start.editor.new", data.getDatabase().toString(),
					data.getGroup().toString()));
			edpEditor.beginEditNew(data.getDatabase().toString(), data.getGroup().toString());
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
		} else {
			String criteria = data.getNameOfKeyfield() + "=" + data.getValueOfKeyField();
			String objectId = getSelObject(criteria, data);
			if (!objectId.equals("Z")) {

				if (!objectId.equals("0")) {
					edpEditor.beginEdit(objectId);
					if (edpEditor.getRowCount() > 0 && data.getOptionCode().getDeleteTable()) {
						edpEditor.deleteAllRows();
					}
					logger.info(Util.getMessage("info.editor.start.update", data.getDatabase().toString(),
							data.getGroup().toString(), edpEditor.getEditRef()));

				} else {
					logger.info(Util.getMessage("info.editor.start.new", data.getDatabase().toString(),
							data.getGroup().toString()));
					edpEditor.beginEditNew(data.getDatabase().toString(), data.getGroup().toString());
				}
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
			} else {
				data.appendError(Util.getMessage("err.selection.ambiguous", criteria));
			}
		}
	}

	@Override
	protected boolean checkDataStructure(Data data) throws ImportitException {
		boolean validDb = checkData(data);
		boolean validHead = false;
		boolean validTable = false;
		if (validDb) {
			List<Field> headerFields = data.getHeaderFields();
			List<Field> tableFields = data.getTableFields();
			// validHead = false;
			// validTable = false;
			try {

				validHead = getAbasType(headerFields, data.getDatabase(), data.getGroup(), false,
						data.getOptionCode().useEnglishVariables());
				validTable = getAbasType(tableFields, data.getDatabase(), data.getGroup(), true,
						data.getOptionCode().useEnglishVariables());

			} catch (ImportitException e) {
				logger.error(e);
				data.appendError(Util.getMessage("err.structure.check", e));
			}
		}
		if (validTable && validHead && validDb) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkData(Data data) throws ImportitException {
		Boolean exists = false;
		exists = checkDatabaseName(data);
		return exists;
	}

}