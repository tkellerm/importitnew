package de.abaspro.infosystem.importit.dataprocessing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.management.BadAttributeValueExpException;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantChangeFieldValException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadFieldPropertyException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.ConnectionLostException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEKSArtInfo;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPTools;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.ceks.jedp.InvalidSettingValueException;
import de.abas.ceks.jedp.ServerActionException;
import de.abas.ceks.jedp.StandardEDPSelection;
import de.abas.ceks.jedp.StandardEDPSelectionCriteria;
import de.abas.eks.jfop.remote.FOe;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abaspro.infosystem.importit.Data;
import de.abaspro.infosystem.importit.DataTable;
import de.abaspro.infosystem.importit.Field;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.infosystem.importit.OptionCode;
import de.abaspro.infosystem.importit.Vartab;
import de.abaspro.infosystem.importit.VartabField;
import de.abaspro.utils.Util;

public abstract class AbstractDataProcessing implements AbasDataProcessable {

	protected EDPSession edpSessionalt;
	protected EDPSessionHandler edpSessionHandler;
	protected Logger logger = Logger.getLogger(AbstractDataProcessing.class);

	protected abstract void writeData(Data data) throws ImportitException;

	protected abstract boolean checkDataStructure(Data data) throws ImportitException;

	public AbstractDataProcessing(EDPSessionHandler edpSessionHandler) {

		this.edpSessionHandler = edpSessionHandler;
	}

	@Override
	public void importDataListTransaction(ArrayList<Data> dataList) throws ImportitException {
		for (Data data : dataList) {
			writeData(data);
			logger.info(data.toString());
		}
	}

	@Override
	public void checkDataListStructure(ArrayList<Data> dataList) throws ImportitException {
		if (dataList != null) {
			if (!dataList.isEmpty()) {
				Data data = dataList.get(0);
				if (data != null) {
					if (checkDataStructure(data)) {
						for (Data dataset : dataList) {
							dataset.copyDatabase(data);
							dataset.copyAbasType(data);
						}
					}
				}

			} else {
				throw new ImportitException(Util.getMessage("err.empty.data.list"));
			}
		} else {
			throw new ImportitException(Util.getMessage("err.undefined.data.list"));
		}
	}

	@Override
	public void importDataList(ArrayList<Data> dataList) throws ImportitException {

		for (Data data : dataList) {
			writeData(data);
		}

	}

	@Override
	public void startTransaction() throws ImportitException {

		this.edpSessionHandler.startTransaction();

	}

	@Override
	public void abortTransaction() throws ImportitException {

		try {
			this.edpSessionHandler.abortTransaction();
		} catch (ConnectionLostException e) {
			throw new ImportitException(Util.getMessage("err.edp.connection.cancel"), e);
		}

	}

	@Override
	public void checkDataListValues(ArrayList<Data> dataList) throws ImportitException {

		for (Data data : dataList) {
			List<Field> headerFields = data.getHeaderFields();
			Boolean includeError = false;
			for (Field field : headerFields) {
				if (!checkDataField(field)) {
					includeError = true;
				}
			}
			List<DataTable> tableRows = data.getTableRows();
			for (DataTable dataTable : tableRows) {
				ArrayList<Field> tableFields = dataTable.getTableFields();
				for (Field field : tableFields) {
					if (!checkDataField(field)) {
						includeError = true;
					}
				}
			}
			if (includeError) {
				data.createErrorReport();
			}
		}

	}

	@Override
	public void commitTransaction() throws ImportitException {

		this.edpSessionHandler.commitTransaction();

	}

	@Deprecated
	protected void closeEdpSession(EDPSession edpSession) {
		if (edpSession.isConnected()) {
			edpSession.endSession();
			logger.info(Util.getMessage("info.edp.session.closed", edpSession.getSessionTag()));
		} else {
			logger.error(Util.getMessage("err.edp.session.lost", edpSession.getSessionTag()));
		}
	}

	// public void startEdpSession() throws ImportitException {
	// createSession(this.server, this.port, this.client, this.password);
	// }

	// public void startEdpSession(EDPVariableLanguage variableLanguage) throws
	// ImportitException {
	// createSession(this.server, this.port, this.client, this.password,
	// variableLanguage);
	// }

	// private void createSession(String server, int port, String client, String
	// password,
	// EDPVariableLanguage variableLanguage) throws ImportitException {
	// try {
	// this.edpSession.beginSession(server, port, client, password,
	// "ImportIt_21");
	// this.edpSession.loggingOn(edpLogFile);
	// logger.info(Util.getMessage("info.edp.session.begin",
	// edpSession.getSessionTag()));
	// this.edpSession.setVariableLanguage(variableLanguage);
	// } catch (CantBeginSessionException e) {
	// logger.error(e);
	// throw new ImportitException(Util.getMessage("err.edp.session.start", e));
	// }
	// }

	// protected EDPSession connectNewEdpSession(EDPVariableLanguage
	// variableLanguage) throws ImportitException {
	// EDPSession edpSession = EDPFactory.createEDPSession();
	// while (!edpSession.isConnected()) {
	// try {
	// edpSession.beginSession(this.server, this.port, this.client,
	// this.password, "ImportIt_21_m");
	// edpSession.loggingOn(edpLogFile);
	// logger.info(Util.getMessage("info.edp.session.begin",
	// edpSession.getSessionTag()));
	// edpSession.setVariableLanguage(variableLanguage);
	// } catch (CantBeginSessionException e) {
	// logger.error(e);
	// throw new ImportitException(Util.getMessage("err.edp.session.start", e));
	// } catch (Exception e) {
	// logger.error(e);
	// throw new ImportitException(Util.getMessage("err.edp.session.start", e));
	// }
	// }
	// return edpSession;
	// }

	// private void createSession(String server, int port, String client, String
	// password) throws ImportitException {
	// try {
	// this.edpSession.beginSession(server, port, client, password,
	// "ImportIt_21");
	// this.edpSession.loggingOn(edpLogFile);
	// logger.info(Util.getMessage("info.edp.session.begin",
	// edpSession.getSessionTag()));
	// } catch (CantBeginSessionException e) {
	// logger.error(e);
	// throw new ImportitException(Util.getMessage("err.edp.session.start", e));
	// }
	// }

	protected void writeField(Data data, Field field, EDPEditor edpEditor, Integer rowNumber) throws ImportitException {
		if (!field.getOptionSkip()) {
			if (field.getValue() == null) {
				throw new ImportitException(
						Util.getMessage("err.null.value", field.getName(), data.getValueOfKeyField(), rowNumber));
			}
			if (!(field.getOptionNotEmpty() && field.getValue().isEmpty())) {
				try {
					if (edpEditor.fieldIsModifiable(rowNumber, field.getName())) {
						String dataFieldValue = field.getValue();
						if (!(field.getOptionDontChangeIfEqual()
								& edpEditor.getFieldVal(rowNumber, field.getName()).equals(dataFieldValue))) {
							edpEditor.setFieldVal(rowNumber, field.getName(), dataFieldValue);
							logger.debug(Util.getMessage("info.field.value.written", field.getName(), dataFieldValue,
									rowNumber.toString()));
						}
					} else {
						if (!field.getOptionModifiable() && !data.getNameOfKeyfield().equals(field.getName())) {
							if (rowNumber == 0) {
								throw new ImportitException(
										Util.getMessage("err.headfield.not.writable", field.getName(),
												data.getValueOfKeyField(), data.getDatabase(), data.getGroup()));
							} else {
								throw new ImportitException(Util.getMessage("err.tablefield.not.writable",
										field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup(),
										rowNumber.toString()));
							}
						}
					}
				} catch (CantChangeFieldValException e) {
					logger.error(e);
					if (rowNumber == 0) {
						throw new ImportitException(Util.getMessage("err.headfield.not.writable", field.getName(),
								data.getValueOfKeyField(), data.getDatabase(), data.getGroup()));
					} else {
						throw new ImportitException(Util.getMessage("err.tablefield.not.writable", field.getName(),
								data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), rowNumber.toString()));
					}

				} catch (CantReadFieldPropertyException e) {
					logger.error(e);
					if (rowNumber == 0) {
						throw new ImportitException(Util.getMessage("err.headfield.not.readable", field.getName(),
								data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), e));
					} else {
						throw new ImportitException(Util.getMessage("err.tablefield.not.readable", field.getName(),
								data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), rowNumber.toString(),
								e));
					}
				}
			}
		} else {
			logger.debug(Util.getMessage("info.skip.field", field.getName()));
		}
	}

	private Boolean checkDataField(Field field) throws ImportitException {
		String value = field.getValue();
		if (!field.getOptionSkip()) {
			logger.debug(Util.getMessage("info.check.data", field.getName(), field.getColNumber(), field.getAbasTyp(),
					value));
			EDPEKSArtInfo edpEksArtInfo = new EDPEKSArtInfo(field.getAbasTyp());
			int dataType = edpEksArtInfo.getDataType();
			if (value != null) {
				if (!(field.getOptionNotEmpty() && value.isEmpty())) {
					if (dataType == EDPTools.EDP_REFERENCE || dataType == EDPTools.EDP_ROWREFERENCE) {
						String edpErpArt = edpEksArtInfo.getERPArt();
						if (edpErpArt.startsWith("V")) {
							if (edpErpArt.equals("VPK1") || edpErpArt.equals("VPKS1") || edpErpArt.equals("VPKT1")) {
								if (value.startsWith("A ")) {
									checkReferenceField(field, new EDPEKSArtInfo("P7:0"));
								} else {
									checkReferenceField(field, new EDPEKSArtInfo("P2:1.2.5"));
								}
							}
						} else {
							checkReferenceField(field, edpEksArtInfo);
						}
					} else if (dataType == EDPTools.EDP_STRING) {
						Long fieldLength = edpEksArtInfo.getMaxLen();
						Long valueLength = (long) value.length();
						if (fieldLength < valueLength) {
							field.setError(Util.getMessage("err.check.data.field.length", value, valueLength,
									field.getName(), fieldLength.toString()));
						}
					} else if (dataType == EDPTools.EDP_INTEGER) {
						int integerDigits = edpEksArtInfo.getIntegerDigits();
						if (value.length() > 0 && !value.equals("0")) {
							try {
								Integer intValue = new Integer(value);
								Integer valueLength = intValue.toString().length();
								if (integerDigits < valueLength) {
									field.setError(Util.getMessage("err.check.data.too.big", value));
								}
							} catch (NumberFormatException e) {
								field.setError(Util.getMessage("err.check.data.conversion.integer", value));
							}
						}
					} else if (dataType == EDPTools.EDP_DOUBLE) {
						int fractionDigits = edpEksArtInfo.getFractionDigits();
						int integerDigits = edpEksArtInfo.getIntegerDigits();
						if (value.length() > 0 && !value.equals("0")) {
							try {
								value = value.replaceAll(" ", "");
								BigDecimal bigDecimalValue = new BigDecimal(value);
								BigDecimal roundBigDValue = bigDecimalValue.setScale(fractionDigits,
										RoundingMode.HALF_UP);
								String roundBigDValueStr = roundBigDValue.toString();
								String compValue = fillValueWithFractionDigits(value, fractionDigits);
								if (!roundBigDValueStr.equals(compValue)) {
									field.setError(Util.getMessage("err.check.data.rounding", value, compValue,
											roundBigDValueStr));
								}
							} catch (NumberFormatException e) {
								field.setError(Util.getMessage("err.check.data.conversion.big.decimal", value));
							} catch (BadAttributeValueExpException e) {
								throw new ImportitException(Util.getMessage("err.check.data.bad.attribute"), e);
							}
							if (value.split("[\\.,]")[0].length() > integerDigits) {
								field.setError(Util.getMessage("err.check.data.too.many.digits", value,
										field.getAbasTyp(), field.getName()));
							}
						}
					} else if (dataType == EDPTools.EDP_DATE) {
						if (!checkDataDate(field)) {
							field.setError(Util.getMessage("err.check.data.conversion.date", value));
						}
					} else if (dataType == EDPTools.EDP_DATETIME || dataType == EDPTools.EDP_TIME
							|| dataType == EDPTools.EDP_WEEK) {
						if (!checkDataDate(field)) {
							field.setError(Util.getMessage("err.check.data.conversion.time", value));
						}
					}
				}
			} else {
				field.setError(Util.getMessage("err.check.data.null.value"));
			}
		}
		if (field.getError().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	private void checkReferenceField(Field field, EDPEKSArtInfo edpeksartinfo) {
		String value = field.getValue();
		int databaseNumber = edpeksartinfo.getRefDatabaseNr();
		int groupNumber = edpeksartinfo.getRefGroupNr();
		if (databaseNumber == 7 && groupNumber == 0) {
			value = value.replaceAll("[A ]", "");
		}
		if (!value.isEmpty()) {
			// TODO Überprüfen wegen edpsession in 2 Verschiedenen Stufen
			EDPSession edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
			EDPQuery query = null;
			try {
				query = getEDPQueryReference(value, databaseNumber, groupNumber, field.getColNumber(), edpSession);
				query.getLastRecord();
				String abasID = query.getField("id");
				int recordCount = query.getRecordCount();
				if (recordCount == 0) {
					field.setError(Util.getMessage("err.check.reference.not.found", field.getAbasTyp(), value));
				} else if (recordCount > 1) {
					field.setError(Util.getMessage("err.check.reference.not.unique", field.getAbasTyp(), value));
				} else {
					field.setAbasID(abasID);
				}
			} catch (ImportitException e) {
				field.setError(Util.getMessage("err.check.reference", field.getAbasTyp(), value));
			} finally {
				if (query != null) {

					if (query.getSession().isConnected()) {
						query.breakQuery();
						logger.info(Util.getMessage("info.end.edp.query"));
					}
				}
				this.edpSessionHandler.freeEDPSession(edpSession);
			}
		}
	}

	private String fillValueWithFractionDigits(String value, int fractionDigits) throws BadAttributeValueExpException {
		Double doubleValue = new Double(value);
		NumberFormat numberFormat = new DecimalFormat("#.#########");
		String stringDoubleValue = numberFormat.format(doubleValue);
		String[] valueList = stringDoubleValue.split("\\.");
		String zeros = fillString("0", fractionDigits);
		if (valueList.length > 1) {
			valueList[1] = (valueList[1] + zeros).substring(0, fractionDigits);
			return valueList[0] + "." + valueList[1];
		} else {
			return valueList[0] + "." + zeros;
		}
	}

	private Boolean checkDataDate(Field field) {
		String abastyp = field.getAbasTyp();
		String value = field.getValue();
		Boolean result = false;
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();
		String varnameResult = "xtergebnis";
		if (!userTextBuffer.isVarDefined(varnameResult)) {
			userTextBuffer.defineVar("Bool", varnameResult);
		}
		userTextBuffer.setValue(varnameResult, "0");
		String formulaString = "U|" + varnameResult + " = F|isvalue( \"" + value + "\" , \"" + abastyp + "\")";
		FOe.formula(formulaString);
		result = userTextBuffer.getBooleanValue(varnameResult);
		return result;
	}

	private String fillString(String value, int stringLength) throws BadAttributeValueExpException {
		if (value.length() == 1) {
			String multipleString = "";
			for (int i = 0; i < stringLength; i++) {
				multipleString = multipleString + value;
			}
			return multipleString;
		} else {
			throw new BadAttributeValueExpException(Util.getMessage("err.fill.string.bad.attribute"));
		}

	}

	private EDPQuery getEDPQueryReference(String value, Integer database, Integer group, Integer rowNumber,
			EDPSession edpSession) throws ImportitException {
		logger.debug(Util.getMessage("debug.getedpsession", "getEDPQueryReference"));

		String[] fieldNames = { "id", "nummer" };
		String tableName = "";
		if (group == -1) {
			tableName = database.toString() + ":";
		} else {
			tableName = database.toString() + ":" + group.toString();
		}
		EDPQuery query = edpSession.createQuery();
		String selectionString = "@noswd=" + value + ";@englvar=true;@language=en;@database=" + database.toString();
		StandardEDPSelectionCriteria criteria = new StandardEDPSelectionCriteria(selectionString);
		StandardEDPSelection edpCriteria = new StandardEDPSelection(tableName, criteria);
		edpCriteria.setDatabase(database.toString());
		try {
			query.startQuery(edpCriteria, fieldNames.toString());
		} catch (InvalidQueryException e) {
			throw new ImportitException(Util.getMessage("err.edp.query.bad.selection.string", selectionString), e);
		}
		return query;
	}

	protected void setEditorOption(Data data, EDPEditor edpEditor)
			throws CantChangeSettingException, CantReadSettingException {
		OptionCode optionCode = data.getOptionCode();
		if (optionCode != null) {
			if (optionCode.noFop()) {
				if (edpEditor.getSession().getFOPMode()) {
					edpEditor.getSession().setFOPMode(false);
				}
			} else {
				if (!edpEditor.getSession().getFOPMode()) {
					edpEditor.getSession().setFOPMode(true);
				}
			}
			if (optionCode.useEnglishVariables()) {
				try {
					if (!edpEditor.getSession().getVariableLanguage().equals(EDPVariableLanguage.ENGLISH)) {
						edpEditor.getSession().setVariableLanguage(EDPVariableLanguage.ENGLISH);
					}
					if (!edpEditor.getVariableLanguage().equals(EDPVariableLanguage.ENGLISH)) {
						edpEditor.setVariableLanguage(EDPVariableLanguage.ENGLISH);
					}
				} catch (CantReadSettingException e) {
					logger.error(e);
				}

			} else {
				try {
					if (!edpEditor.getSession().getVariableLanguage().equals(EDPVariableLanguage.GERMAN)) {
						edpEditor.getSession().setVariableLanguage(EDPVariableLanguage.GERMAN);
					}
					if (!edpEditor.getVariableLanguage().equals(EDPVariableLanguage.GERMAN)) {
						edpEditor.setVariableLanguage(EDPVariableLanguage.GERMAN);
					}
				} catch (CantReadSettingException e) {
					logger.error(e);
				}
			}
		}
	}

	protected void writeFieldsInEditor(Data data, EDPEditor edpEditor) throws ImportitException {
		if (edpEditor.isActive()) {
			List<Field> headerFields = data.getHeaderFields();
			for (Field field : headerFields) {
				writeField(data, field, edpEditor, 0);
			}
			List<DataTable> tableRows = data.getTableRows();
			if (tableRows != null && edpEditor.hasTablePart()) {
				for (DataTable dataTable : tableRows) {
					Integer rowCount = edpEditor.getRowCount();
					Integer rowNumber = insertRow(data, edpEditor, rowCount);
					ArrayList<Field> tableFields = dataTable.getTableFields();
					for (Field field : tableFields) {
						writeField(data, field, edpEditor, rowNumber);
					}
				}
			}
		}
	}

	protected Integer insertRow(Data data, EDPEditor edpEditor, Integer rowCount) throws ImportitException {
		try {
			if (rowCount == 0) {
				edpEditor.insertRow(1);
				return 1;
			} else {
				if ((data.getTypeCommand() != null && rowCount > 1) || (data.getTypeCommand() == null)) {
					Integer newRowNumber = rowCount + 1;
					edpEditor.insertRow(newRowNumber);
					return newRowNumber;
				} else {
					return rowCount;
				}
			}
		} catch (InvalidRowOperationException e) {
			logger.error(e);
			throw new ImportitException(Util.getMessage("err.row.insert"));
		}
	}

	protected String getSelObject(String criteria, Data data) throws ImportitException {
		EDPSession edpSession = null;
		EDPQuery edpQuery = null;
		try {
			logger.debug(Util.getMessage("debug.getedpsession", "getSelObject"));
			if (data.getOptionCode().useEnglishVariables()) {
				edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);

			} else {
				edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.GERMAN);

			}
			edpQuery = edpSession.createQuery();
			String tableName = data.getDatabase().toString() + ":" + data.getGroup().toString();
			String key;
			key = data.getKeyOfKeyfield();
			if (key == null) {
				key = "";
			}
			if (data.getOptionCode().useEnglishVariables()) {
				criteria = criteria + ";@englvar=true;@language=en";
				edpQuery.startQuery(tableName, key, criteria, "idno,swd,id");
			} else {
				criteria = criteria + ";@englvar=false;@language=de";
				edpQuery.startQuery(tableName, key, criteria, "nummer,such,id");
			}
			edpQuery.getLastRecord();
			int recordCount = edpQuery.getRecordCount();
			String objectId = edpQuery.getField("id");
			edpQuery.breakQuery();
			if (recordCount == 1) {
				return objectId;
			} else if (recordCount > 1) {
				return "Z";
			} else {
				return "0";
			}
		} catch (InvalidQueryException e) {
			throw new ImportitException(Util.getMessage("err.abstractDataProcessing.selObject.invalidQuery", e));
		} finally {
			if (!edpQuery.isReleased()) {
				try {
					edpQuery.release();
				} catch (ServerActionException e) {
					logger.error(e);
				}
			}

			this.edpSessionHandler.freeEDPSession(edpSession);
		}
	}

	protected Boolean checkDatabaseName(Data data) throws ImportitException {
		if (data.getDatabase() != null && data.getGroup() != null) {
			String criteria = "0:grpDBDescr=(" + data.getDatabase().toString() + ");0:grpGrpNo="
					+ data.getGroup().toString() + ";" + ";@englvar=true;@language=en";
			if (searchDatabase(data, criteria)) {
				return true;
			} else {
				data.appendError(Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
				return false;
			}
		} else {
			if (data.getDbString() != null && data.getDbGroupString() != null) {
				String criteria = "0:vdntxt==" + data.getDbString() + ";0:vgrtxtbspr==" + data.getDbGroupString() + ";"
						+ ";@englvar=false;@language=de";
				if (searchDatabase(data, criteria)) {
					return true;
				} else {
					criteria = "0:DBCmd==" + data.getDbString() + ";0:grpGrpCmd==" + data.getDbGroupString() + ";"
							+ ";@englvar=true;@language=en";
					if (searchDatabase(data, criteria)) {
						return true;
					} else {
						data.appendError(
								Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
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
		logger.debug(Util.getMessage("debug.getedpsession", "searchdatabase"));
		EDPSession edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
		try {
			edpSession.setEnumMode(mode);
		} catch (InvalidSettingValueException e) {
			logger.error(e);
		}
		EDPQuery query = edpSession.createQuery();
		try {
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
		} catch (InvalidQueryException e) {
			data.appendError(Util.getMessage("err.invalid.selection.criteria", criteria, e));
			return false;
		} finally {
			if (!query.isReleased()) {
				try {
					query.release();
				} catch (ServerActionException e) {
					logger.error(e);
				}
			}
			this.edpSessionHandler.freeEDPSession(edpSession);
		}
		return false;
	}

	protected boolean getAbasType(List<Field> fieldList, Integer database, Integer group, Boolean inTab,
			Boolean englishVariables) throws ImportitException {
		if (fieldList != null) {

			logger.info(Util.getMessage("info.start.getting.vartab", database, group));
			Vartab vartab = new Vartab(this.edpSessionHandler, database, group);
			logger.info(Util.getMessage("info.end.getting.vartab", database, group));
			Boolean error = false;
			for (Field field : fieldList) {
				if (!field.getOptionSkip()) {
					VartabField vartabField;
					if (englishVariables) {
						vartabField = vartab.checkVartabEnglish(field.getName());

					} else {
						vartabField = vartab.checkVartabGerman(field.getName());
					}
					if (vartabField != null) {
						field.setAbasType(vartabField.getActiveType());
						logger.trace(Util.getMessage("info.found.field.with.type", field.getName(),
								vartabField.getActiveType()));
					} else {
						String errorText = Util.getMessage("err.field.not.found", field.getName());
						error = true;
						field.setError(errorText);
						logger.error(errorText);
					}
				}
			}
			if (error) {
				logger.info(Util.getMessage("info.not.all.fields.found"));
				return false;
			} else {
				logger.info(Util.getMessage("info.all.fields.found"));
				return true;
			}
		} else if (inTab) {
			return true;
		} else {
			throw new ImportitException(Util.getMessage("err.invalid.head.fields"));
		}
	}

}
