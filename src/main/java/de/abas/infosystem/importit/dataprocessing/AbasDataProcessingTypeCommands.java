package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.*;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.utils.MessageUtil;

import java.util.List;

public class AbasDataProcessingTypeCommands extends AbstractDataProcessing {

	public AbasDataProcessingTypeCommands(EDPSessionHandler edpSessionHandler) {
		super(edpSessionHandler);
	}

	@Override
	public void importDataListTransaction(List<Data> dataList) throws ImportitException {
		// Leere Funktion da die Transaction bei Tippkommando nicht funktioniert.
		throw new ImportitException(MessageUtil.getMessage("error.typecommand.importDataListTransaction"));
	}

	@Override
	protected void writeData(Data data) throws ImportitException {
		EDPSession edpSession = this.edpSessionHandler.getEDPSessionWriteData(data.getEDPLanguage());

		if (edpSession.isConnected()) {
			EDPEditor edpEditor = edpSession.createEditor();
			try {

				writeTypeCommands(data, edpEditor);

			} catch (Exception e) {
				logger.error(e);
				data.appendError(e);
			} finally {
				EDPUtils.releaseEDPEditor(edpEditor, logger);
				this.edpSessionHandler.freeEDPSession(edpSession);
			}
		} else {
			logger.error(MessageUtil.getMessage("err.no.edp.session"));
			throw new ImportitException(MessageUtil.getMessage("err.no.edp.session"));
		}

	}

	private void writeTypeCommands(Data data, EDPEditor edpEditor) throws ImportitException, CantChangeSettingException,
			CantSaveException, CantBeginEditException, CantReadSettingException {
		data.setImported(false);
		edpEditor.beginEditCmd(data.getTypeCommand().toString(), "");
		setEditorOption(data, edpEditor);
		writeFieldsInEditor(data, edpEditor);
		edpEditor.endEditSave();
		data.setImported(true);
	}

	@Override
	protected boolean checkDataStructure(Data data)  {


		if (!checkData(data)) return false;

		List<Field> headerFields = data.getHeaderFields();
		List<Field> tableFields = data.getTableFields();
		try {

			if (!checkFieldList(headerFields, data.getDatabase(), data.getGroup(), false,
						data.getOptionCode().useEnglishVariables())) return false;

			if (!checkFieldList(tableFields, data.getDatabase(), data.getGroup(), true,
						data.getOptionCode().useEnglishVariables())) return false;

		} catch (ImportitException e) {
			logger.error(e);
			data.appendError(MessageUtil.getMessage("err.structure.check", e));
		}


		return true;

	}

	private boolean checkData(Data data) {


		return checkDatabaseName(data);
	}

	@Override
	protected void writeAbasIDinData(Data data) {
		// Nichts machen da Tippkommando keine ID haben

	}
}
