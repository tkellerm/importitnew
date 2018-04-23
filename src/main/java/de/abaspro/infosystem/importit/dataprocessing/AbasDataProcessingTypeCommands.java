package de.abaspro.infosystem.importit.dataprocessing;

import java.util.ArrayList;
import java.util.List;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.common.type.enums.EnumTypeCommands;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abaspro.infosystem.importit.Enumeration;
import de.abaspro.infosystem.importit.EnumerationItem;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.infosystem.importit.dataset.Data;
import de.abaspro.infosystem.importit.dataset.Field;
import de.abaspro.utils.Util;

public class AbasDataProcessingTypeCommands extends AbstractDataProcessing {

	public AbasDataProcessingTypeCommands(EDPSessionHandler edpSessionHandler) {
		super(edpSessionHandler);
	}

	@Override
	public void importDataListTransaction(ArrayList<Data> dataList) throws ImportitException {
		// Leere Funktion da die Transaction bei Tippkomanndo´s nicht funktiert.
		throw new ImportitException(Util.getMessage("error.typecommand.importDataListTransaction"));
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
			logger.error(Util.getMessage("err.no.edp.session"));
			throw new ImportitException(Util.getMessage("err.no.edp.session"));
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
	protected boolean checkDataStructure(Data data) throws ImportitException {
		boolean validDb = checkData(data);
		boolean validHead = false;
		boolean validTable = false;
		if (validDb) {
			List<Field> headerFields = data.getHeaderFields();
			List<Field> tableFields = data.getTableFields();
			validHead = false;
			validTable = false;
			try {

				validHead = checkFieldList(headerFields, data.getDatabase(), data.getGroup(), false,
						data.getOptionCode().useEnglishVariables());
				validTable = checkFieldList(tableFields, data.getDatabase(), data.getGroup(), true,
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
		if (data.getTypeCommand() == null && data.getTypeCommandString() != null) {
			data.setTypeCommand(checkTypeCommandString(data.getTypeCommandString()));
		}
		if (data.getTypeCommand() != null && data.getDatabase() == null) {
			EnumTypeCommands[] typeCommands = EnumTypeCommands.values();
			for (EnumTypeCommands enumTypeCommands : typeCommands) {
				if (data.getTypeCommand() == enumTypeCommands.getCode()) {
					exists = true;
				}

			}
			if (exists) {
				try {
					findDatabaseForTypeCommand(data);
				} catch (ImportitException e) {
					data.appendError(e);
				}
			} else {
				data.appendError(Util.getMessage("err.invalid.type.command", data.getTypeCommand()));
			}
		}
		exists = checkDatabaseName(data);
		return exists;
	}

	private void findDatabaseForTypeCommand(Data data) throws ImportitException {
		if (data.getTypeCommand() != null) {

			EDPSession edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);

			EDPEditor edpEditor = edpSession.createEditor();
			try {
				edpEditor.beginEditCmd(data.getTypeCommand().toString(), "");
				data.setDatabase(edpEditor.getEditDatabaseNr());
				data.setGroup(edpEditor.getEditGroupNr());
				edpEditor.endEditCancel();
			} catch (CantBeginEditException e) {
				throw new ImportitException(Util.getMessage("err.getting.database", e));
			} finally {
				EDPUtils.releaseEDPEditor(edpEditor, logger);
				this.edpSessionHandler.freeEDPSession(edpSession);
			}
		}
	}

	private Integer checkTypeCommandString(String typeCommandString) {
		Integer typeCommandCode = null;
		Enumeration enumeration = fillEnumeration();
		if (enumeration.getListOfEnumItems().size() > 0) {
			EnumerationItem enumerationItem = enumeration.searchItem(typeCommandString);
			typeCommandCode = enumerationItem.getNumber();
		}
		return typeCommandCode;
	}

	private Enumeration fillEnumeration() {
		Enumeration enumeration = new Enumeration();
		ArrayList<EnumerationItem> listOfEnumItems = enumeration.getListOfEnumItems();
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		GlobalTextBuffer globalTextbuffer = bufferFactory.getGlobalTextBuffer();
		UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();
		int cmdNameMax = globalTextbuffer.getIntegerValue("cmdNameMax");
		for (Integer i = 0; i < cmdNameMax; i++) {
			String descrVar = "xtnamebspr";
			String neutralNameVar = "xtnameneutral";
			String enumerationVar = "xtaufzaehlung";
			String descr;
			String neutralName;
			if (!userTextBuffer.isVarDefined(descrVar)) {
				userTextBuffer.defineVar("Text", descrVar);
			}
			if (!userTextBuffer.isVarDefined(neutralNameVar)) {
				userTextBuffer.defineVar("Text", neutralNameVar);
			}
			if (!userTextBuffer.isVarDefined(enumerationVar)) {
				userTextBuffer.defineVar("A198", enumerationVar);
			}
			FOe.assign("U|" + enumerationVar + " = \"(" + i + ")\"");
			Boolean success = globalTextbuffer.getBooleanValue("success");
			if (success) {
				FOe.formula("U|" + neutralNameVar + " = 'U|" + enumerationVar + "(L=\":\")'");
				neutralName = userTextBuffer.getStringValue(neutralNameVar);
				descr = globalTextbuffer.getStringValue("cmdName" + i);
				EnumerationItem enumerationItem = new EnumerationItem(i, descr, neutralName);
				listOfEnumItems.add(enumerationItem);
			}
		}
		return enumeration;
	}

	@Override
	protected void writeAbasIDinData(Data data) {
		// Nichts machen da Tipkommandos keine ID haben

	}
}
