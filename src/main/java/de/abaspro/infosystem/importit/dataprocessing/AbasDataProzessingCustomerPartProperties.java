package de.abaspro.infosystem.importit.dataprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPEditAction;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abaspro.infosystem.importit.Data;
import de.abaspro.infosystem.importit.DataTable;
import de.abaspro.infosystem.importit.Field;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.utils.Util;

public class AbasDataProzessingCustomerPartProperties
		extends AbstractDataProcessing {

	
	
	public AbasDataProzessingCustomerPartProperties(String server, Integer port,
			String client, String password) {
		super(server, port, client, password);
		// TODO Auto-generated constructor stub
	}

	

	@Override
	protected void writeData(Data data) throws ImportitException {
		EDPSession edpSession = connectNewEdpSession(data.getEDPLanguage());
        if (edpSession.isConnected()) {
            EDPEditor edpEditor = edpSession.createEditor();
            try {
                writeCustomerPartProperties(data, edpEditor);
            } catch (Exception e) {
                logger.error(e);
                data.appendError(e);
            } finally {
                if (edpSession.isConnected()) {
                    if (edpEditor.isActive()) {
                        logger.info(Util.getMessage("info.end.editor"));
                        edpEditor.endEditCancel();
                    }
                    closeEdpSession(edpSession);
                } else {
                    logger.error(Util.getMessage("err.end.editor"));
                }
            }
        } else {
            logger.error(Util.getMessage("err.no.edp.session"));
            throw new ImportitException(Util.getMessage("err.no.edp.session"));
        }

	}
	
	private void writeCustomerPartProperties(Data data, EDPEditor edpEditor) throws ImportitException, InvalidQueryException, CantChangeSettingException, CantBeginEditException, InvalidRowOperationException, CantSaveException, CantReadSettingException {
        data.setImported(false);
        String[] varNames = checkDataCustomerPartProperties(data);
        if (varNames.length == 2) {
            logger.info(Util.getMessage("info.import.cust.part.props"));
            data.setDatabase(2);
            data.setGroup(6);
            String artField = varNames[0];
            String klField = varNames[1];
            List<DataTable> tableRows = data.getTableRows();
            for (DataTable dataTable : tableRows) {
                dataTable.getTableFieldValue(klField);
                EDPQuery edpQuery = this.edpSession.createQuery();
                String criteria = artField + "="
                        + data.getValueOfHeadField(artField)
                        + ";" + klField + "=" + dataTable.getTableFieldValue(klField);
                String key = "Kundenartikeleigenschaften";
                int recordCount = getQueryTotalHits(criteria, key, data, edpQuery);
                if (recordCount == 1 || recordCount == 0) {
                    setEditorOption(data, edpEditor);
                    if (recordCount == 1) {
                        String abasVersion = edpSession.getABASVersionNumber().substring(0, 4);
                        if (2013 <= new Integer(abasVersion)) {
                            String idField = edpQuery.getField("id");
                            edpEditor.beginEdit("2:6", idField);
                            if (edpEditor.getRowCount() > 0 && data.getOptionCode().getDeleteTable()) {
                                edpEditor.deleteAllRows();
                            }
                            logger.info(Util.getMessage("info.update.cust.part.props", data.getDatabase().toString(), data.getGroup().toString(), edpEditor.getEditRef()));
                        } else {
                            logger.error(String.format("%s %s", Util.getMessage("err.edit.cust.part.props", criteria, edpQuery.getField("id")), Util.getMessage("err.edit.cust.part.props.2013")));
                            throw new ImportitException(Util.getMessage("err.edit.cust.part.props.2013"));
                        }
                    } else {
                        logger.info(Util.getMessage("info.new.cust.part.props", data.getDatabase().toString(), data.getGroup().toString()));
                        edpEditor.beginEditNew(data.getDatabase().toString(), data.getGroup().toString());
                    }
                    final String[] IgnoreFieldNames = {"art", "artikel", "product", "kl", "custVendor"};
                    writeFieldsInEditor(data, dataTable, edpEditor, IgnoreFieldNames);
                    edpEditor.endEditSave();
                    data.setImported(true);
                    edpSession.loggingOff();
                    recordCount = getQueryTotalHits(criteria, key, data, edpQuery);
                    if (recordCount == 0) {
                        String errorText = Util.getMessage("err.data.not.found.small", data.getValueOfHeadField(artField), dataTable.getTableFieldValue(klField));
                        logger.error(errorText);
                        data.appendError(errorText);

                    } else if (recordCount > 1) {
                        String errorText = Util.getMessage("err.data.not.found.major", data.getValueOfHeadField(artField), dataTable.getTableFieldValue(klField));
                        logger.error(errorText);
                        data.appendError(errorText);
                    }
                } else {
                    data.appendError(Util.getMessage("err.selection.ambiguous", criteria));
                }
            }
        }
    }

	 private String[] checkDataCustomerPartProperties(Data data) {
	        Integer database = data.getDatabase();
	        Integer group = data.getGroup();
	        Boolean artVarFound = false;
	        Boolean klVarFound = false;
	        String varNameArt = "";
	        String varNameKl = "";
	        String[] varNames = new String[2];
	        if (database == 2 & (group == 6 || group == 7)) {
	            List<Field> headerFields = data.getHeaderFields();
	            for (Field field : headerFields) {
	                if (field.getName().equals("art") || field.getName().equals("artikel") || field.getName().equals("product")) {
	                    artVarFound = true;
	                    varNameArt = field.getName();
	                }
	            }
	            List<Field> tableFields = data.getTableFields();
	            for (Field field : tableFields) {
	                if (field.getName().equals("kl") || field.getName().equals("custVendor")) {
	                    klVarFound = true;
	                    varNameKl = field.getName();
	                }
	            }
	            if (artVarFound & klVarFound) {
	                varNames[0] = varNameArt;
	                varNames[1] = varNameKl;
	                return varNames;
	            }
	        }
	        return ArrayUtils.EMPTY_STRING_ARRAY;

	    }
	
	 private int getQueryTotalHits(String criteria, String key, Data data, EDPQuery edpQuery) throws InvalidQueryException {
	        Integer database = data.getDatabase();
	        Integer group = data.getGroup();
	        String databaseDescr;
	        if (group != -1) {
	            databaseDescr = database + ":" + group;
	        } else {
	            databaseDescr = database.toString();
	        }
	        if (data.getOptionCode().useEnglishVariables()) {
	            criteria = criteria + ";@englvar=true;@language=en";
	            edpQuery.startQuery(databaseDescr, key, criteria, "id");
	        } else {
	            criteria = criteria + ";@englvar=false;@language=de";
	            edpQuery.startQuery(databaseDescr, key, criteria, "id");
	        }
	        edpQuery.getLastRecord();
	        return edpQuery.getRecordCount();
	    }
	 
	 private void writeFieldsInEditor(Data data, DataTable dataTable, EDPEditor edpEditor, String[] ignoreFields) throws ImportitException {
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
		protected boolean checkDataStructure(Data data) {
			 boolean validDb = checkDatabaseName(data);
		        boolean validHead = false;
		        boolean validTable = false;
		        if (validDb) {
		            List<Field> headerFields = data.getHeaderFields();
		            List<Field> tableFields = data.getTableFields();
		            validHead = false;
		            validTable = false;
		            try {
		                
		                    validHead = checkCustomerPartProperties(headerFields, data.getOptionCode().useEnglishVariables());
		                    validTable = getAbasType(tableFields, 2, 6, false, data.getOptionCode().useEnglishVariables());
		                 
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
	 
	 
	 
	 private boolean checkCustomerPartProperties(List<Field> headerFields, Boolean englishVariables) throws ImportitException {
	        if (headerFields.size() == 1) {
	            for (Field field : headerFields) {
	                String varName = field.getName();
	                if (varName.equals("product") || varName.equals("art") || varName.equals("artikel")) {
	                    return getAbasType(headerFields, 2, 6, false, englishVariables);
	                }
	            }
	            throw new ImportitException(Util.getMessage("err.variables.missing"));
	        } else {
	            throw new ImportitException(Util.getMessage("err.too.many.head.fields"));
	        }
	    }
	 
}
