package de.abaspro.infosystem.importit;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidSettingValueException;
import de.abas.ceks.jedp.ServerActionException;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.common.type.enums.EnumTypeCommands;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abaspro.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abaspro.infosystem.importit.dataset.Data;
import de.abaspro.infosystem.importit.dataset.Field;
import de.abaspro.utils.Util;

public class AbasDataCheckAndComplete {

	private EDPSession edpSession;
	private Logger logger = Logger.getLogger(AbasDataCheckAndComplete.class);
	private ArrayList<Data> dataList;
	private EDPSessionHandler edpSessionHandler;

	public AbasDataCheckAndComplete(EDPSessionHandler edpSessionHandler, ArrayList<Data> dataList)
			throws ImportitException {

		this.edpSessionHandler = edpSessionHandler;
		this.dataList = dataList;

	}

	public boolean checkandcompleteDataList() throws ImportitException {

		Data data = dataList.get(0);
		if (data != null) {
			if (checkandcompleteData(data)) {
				List<Field> smlFieldList = createDefaultSMLFieldList(data);
				for (Data dataset : dataList) {
					dataset.copyDatabase(data);
					if (smlFieldList != null) {
						if (!dataset.getSmlString().isEmpty()) {
							fillSmlArray(dataset, smlFieldList);
							deleteSmlFieldfromHeaderFields(dataset);
						}
					}
					dataset.copyAbasType(data);

				}

				return true;
			} else
				return false;
		}

		return false;

	}

	private List<Field> createDefaultSMLFieldList(Data data) throws ImportitException {
		if (data.getSmlString() != null) {

			moveSmlFieldsfromHeaderArrayToSmlArray(data);

			List<Field> smlFieldlist = data.getSmlFields();

			try {
				if (checkSMLFieldList(smlFieldlist, data.getSmlString())) {
					return smlFieldlist;
				}
			} catch (Exception e) {
				throw new ImportitException(e.getMessage());
			}

		}
		return null;

	}

	private boolean checkSMLFieldList(List<Field> smlFieldlist, String smlString) throws ImportitException {
		String fieldNames = "idno,varName,typeOfAdditionalVar";
		String tableName = "12:24";
		String key = "Nummer";
		Boolean inTable = true;
		int aliveFlag = 1;
		logger.debug(Util.getMessage("debug.getedpsession", "createDefaultSMLFieldList"));

		Boolean notFound = false;
		for (Field field : smlFieldlist) {
			EDPSession edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);

			EDPQuery query = edpSession.createQuery();
			String criteria = "idno=" + smlString + ";varName==" + field.getName().replace("S.", "").replace("s.", "")
					+ ";@englvar=true;@language=en";
			try {
				query.startQuery(tableName, key, criteria, inTable, aliveFlag, true, true, fieldNames, 0, 10000);
				query.getLastRecord();
				if (query.getRecordCount() != 1) {
					notFound = true;
				}

				String[] fields = query.getFields();
				if (query.getRecordCount() == 1) {
					field.setAbasType(query.getField("typeOfAdditionalVar"));
				}

			} catch (InvalidQueryException e) {
				e.printStackTrace();
			} finally {
				releaseQuery(query);
				edpSessionHandler.freeEDPSession(edpSession);
			}
		}

		if (notFound) {
			return false;
		} else
			return true;
	}

	private void fillSmlArray(Data dataset, List<Field> smlFieldList) throws ImportitException {
		List<Field> smlFieldsnew = createDefaultSMLFieldList(dataset);

		dataset.setSmlFields(smlFieldsnew);

	}

	private String getSMLFieldValue(Field field, Data dataset) throws ImportitException {

		List<Field> headFields = dataset.getHeaderFields();

		for (Field headField : headFields) {
			if (headField.getName().equals(field.getName()) || headField.getName().equals(field.getName())) {
				return headField.getValue();
			}
		}

		List<Field> smlFields = dataset.getSmlFields();
		for (Field field2 : smlFields) {
			if (field2.getName().equals(field.getName()) || field2.getName().equals(field.getName())) {
				return field2.getValue();
			}
		}

		throw new ImportitException(Util.getMessage("SML.error.fieldnotfound", field.getName()));

	}

	private void deleteSmlFieldfromHeaderFields(Data dataset) {

		List<Field> headerFields = dataset.getHeaderFields();

		headerFields.removeIf((Field field) -> (field.getCompleteContent().startsWith("S.")
				|| field.getCompleteContent().startsWith("s.")));
	}

	private void moveSmlFieldsfromHeaderArrayToSmlArray(Data dataset) {
		List<Field> headerFields = dataset.getHeaderFields();
		List<Field> smlFields = dataset.getSmlFields();
		for (Field field : headerFields) {
			String completeContent = field.getCompleteContent();
			if (completeContent.startsWith("S.") || completeContent.startsWith("s.")) {
				smlFields.add(field);
			}
		}
		deleteSmlFieldfromHeaderFields(dataset);
	}

	private boolean checkandcompleteData(Data data) throws ImportitException {
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
			logger.debug(Util.getMessage("debug.getedpsession", "findDatabaseForTypeCommand"));
			EDPSession edpSession = edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);

			EDPEditor edpEditor = edpSession.createEditor();
			try {
				edpEditor.beginEditCmd(data.getTypeCommand().toString(), "");
				data.setDatabase(edpEditor.getEditDatabaseNr());
				data.setGroup(edpEditor.getEditGroupNr());
				edpEditor.endEditCancel();
			} catch (CantBeginEditException e) {
				throw new ImportitException(Util.getMessage("err.getting.database", e));
			} finally {
				if (edpEditor.isActive()) {
					edpEditor.endEditCancel();
				}
				edpSessionHandler.freeEDPSession(edpSession);
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

	private Boolean checkDatabaseName(Data data) throws ImportitException {
		if (data.getDatabase() != null && data.getGroup() != null) {
			String criteria = "0:grpDBDescr=(" + data.getDatabase().toString() + ");0:grpGrpNo="
					+ data.getGroup().toString() + ";swd<>VVAR;@englvar=true;@language=en";
			if (searchDatabase(data, criteria)) {
				return true;
			} else {
				data.appendError(Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
				return false;
			}
		} else {
			if (data.getDbString() != null && data.getDbGroupString() != null) {
				String criteria = "0:vdntxt==" + data.getDbString() + ";0:vgrtxtbspr==" + data.getDbGroupString() + ";"
						+ ";such<>VVAR;@englvar=false;@language=de";
				if (searchDatabase(data, criteria)) {
					return true;
				} else {
					criteria = "0:DBCmd==" + data.getDbString() + ";0:grpGrpCmd==" + data.getDbGroupString() + ";"
							+ ";swd<>VVAR;@englvar=true;@language=en";
					if (searchDatabase(data, criteria)) {
						return true;
					} else {
						data.appendError(Util.getMessage("err.invalid.database.group", data.getDbString(),
								data.getDbGroupString()));
						return false;
					}
				}
			}
		}
		return false;
	}

	private boolean searchDatabase(Data data, String criteria) throws ImportitException {
		String key = "";
		int aliveFlag = EDPConstants.ALIVEFLAG_ALIVE;
		String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
		String tableName = "12:26";
		Boolean inTable = false;
		int mode = EDPConstants.ENUMPOS_CODE;
		logger.debug(Util.getMessage("debug.getedpsession", "searchDatabase"));
		edpSession = edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
		EDPQuery query = edpSession.createQuery();

		try {
			edpSession.setEnumMode(mode);
			query.startQuery(tableName, key, criteria, inTable, aliveFlag, true, true, fieldNames, 0, 10000);
			query.getLastRecord();
			if (query.getRecordCount() == 1) {
				String dbString = query.getField("grpDBDescr");
				dbString = dbString.replaceAll("\\(*", "");
				dbString = dbString.replaceAll("\\)*", "");
				data.setDatabase(new Integer(dbString));
				String group = query.getField("grpGrpNo");
				group = group.replaceAll(" ", "");
				data.setGroup(new Integer(group));
				return true;
			}

			if (query.getRecordCount() > 1) {
				data.appendError(Util.getMessage("err.invalid.selection.notUnique", criteria, query.getRecordCount()));
			}

		} catch (InvalidQueryException e) {
			data.appendError(Util.getMessage("err.invalid.selection.criteria", criteria, e));
			return false;
		} catch (InvalidSettingValueException e) {
			logger.error(e);
			throw new ImportitException(Util.getMessage(""));
		} finally {
			releaseQuery(query);
			edpSessionHandler.freeEDPSession(edpSession);
		}
		return false;
	}

	private void releaseQuery(EDPQuery query) {

		try {
			query.release();
		} catch (ServerActionException e) {
			logger.error(e);
		}

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

}
