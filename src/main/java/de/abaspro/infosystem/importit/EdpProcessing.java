package de.abaspro.infosystem.importit;

import de.abas.ceks.jedp.*;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.common.type.enums.EnumTypeCommands;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abaspro.utils.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.management.BadAttributeValueExpException;

@Deprecated
public class EdpProcessing {

    private static String edpLogFile = "java/log/importit21edp.log";
    private String server;
    private Integer port;
    private String client;
    private String password;
    private EDPSession edpSession;
    private Logger logger = Logger.getLogger(Main.class);

    public EdpProcessing(String server, Integer port, String client, String password) throws ImportitException {
        super();
        validate(port, Util.getMessage("err.no.port"));
        validate(server, Util.getMessage("err.no.server"));
        validate(client, Util.getMessage("err.no.client"));
        validate(password, Util.getMessage("err.no.password"));
        this.server = server;
        this.port = port;
        this.client = client;
        this.password = password;
        this.edpSession = EDPFactory.createEDPSession();
    }

    private void validate(Integer integerVar, String errorText) throws ImportitException {
        if (integerVar != null) {
            if (integerVar == 0) {
                throw new ImportitException(errorText);
            }
        } else {
            throw new ImportitException(String.format("%s: %s", errorText, Util.getMessage("err.port.null.value")));
        }
    }

    private void validate(String stringVar, String errorText) throws ImportitException {
        if (stringVar != null) {
            if (stringVar.isEmpty()) {
                throw new ImportitException(errorText);
            }
        } else {
            throw new ImportitException(String.format("%s: %s", errorText, Util.getMessage("err.null.value")));
        }
    }

    private void startEdpSession() throws ImportitException {
        createSession(this.server, this.port, this.client, this.password);
    }

    private void startEdpSession(EDPVariableLanguage variableLanguage) throws ImportitException {
        createSession(this.server, this.port, this.client, this.password, variableLanguage);
    }

    private void closeEdpSession(EDPSession edpSession) {
        if (edpSession.isConnected()) {
            edpSession.endSession();
            logger.info(Util.getMessage("info.edp.session.closed", edpSession.getSessionTag()));
        } else {
            logger.error(Util.getMessage("err.edp.session.lost", edpSession.getSessionTag()));
        }
    }

    private void createSession(String server, int port, String client, String password) throws ImportitException {
        try {
            this.edpSession.beginSession(server, port, client, password, "ImportIt_21");
            this.edpSession.loggingOn(edpLogFile);
            logger.info(Util.getMessage("info.edp.session.begin", edpSession.getSessionTag()));
        } catch (CantBeginSessionException e) {
            logger.error(e);
            throw new ImportitException(Util.getMessage("err.edp.session.start", e));
        }
    }

    private void createSession(String server, int port, String client, String password, EDPVariableLanguage variableLanguage) throws ImportitException {
        try {
            this.edpSession.beginSession(server, port, client, password, "ImportIt_21");
            this.edpSession.loggingOn(edpLogFile);
            logger.info(Util.getMessage("info.edp.session.begin", edpSession.getSessionTag()));
            this.edpSession.setVariableLanguage(variableLanguage);
        } catch (CantBeginSessionException e) {
            logger.error(e);
            throw new ImportitException(Util.getMessage("err.edp.session.start", e));
        }
    }

    private EDPSession connectNewEdpSession(EDPVariableLanguage variableLanguage) throws ImportitException {
        EDPSession edpSession = EDPFactory.createEDPSession();
        while (!edpSession.isConnected()) {
            try {
                edpSession.beginSession(this.server, this.port, this.client, this.password, "ImportIt_21_m");
                edpSession.loggingOn(edpLogFile);
                logger.info(Util.getMessage("info.edp.session.begin", edpSession.getSessionTag()));
                edpSession.setVariableLanguage(variableLanguage);
            } catch (CantBeginSessionException e) {
                logger.error(e);
                throw new ImportitException(Util.getMessage("err.edp.session.start", e));
            } catch (Exception e) {
                logger.error(e);
                throw new ImportitException(Util.getMessage("err.edp.session.start", e));
            }
        }
        return edpSession;
    }


    public void checkDataListStructure(ArrayList<Data> dataList) throws ImportitException {
        if (dataList != null) {
            if (!dataList.isEmpty()) {
                try {
                    startEdpSession(EDPVariableLanguage.ENGLISH);
                    Data data = dataList.get(0);
                    if (data != null) {
                        if (checkDataStructure(data)) {
                            for (Data dataset : dataList) {
                                dataset.copyDatabase(data);
                                dataset.copyAbasType(data);
                            }
                        }
                    }
                } finally {
                    closeEdpSession(this.edpSession);
                }
            } else {
                throw new ImportitException(Util.getMessage("err.empty.data.list"));
            }
        } else {
            throw new ImportitException(Util.getMessage("err.undefined.data.list"));
        }
    }

    private boolean checkDataStructure(Data data) {
        boolean validDb = checkData(data);
        boolean validHead = false;
        boolean validTable = false;
        if (validDb) {
            List<Field> headerFields = data.getHeaderFields();
            List<Field> tableFields = data.getTableFields();
            validHead = false;
            validTable = false;
            try {
                if (isSpecialDb(data)) {
                    validHead = checkCustomerPartProperties(headerFields, data.getOptionCode().useEnglishVariables());
                    validTable = getAbasType(tableFields, 2, 6, false, data.getOptionCode().useEnglishVariables());
                } else {
                    validHead = getAbasType(headerFields, data.getDatabase(), data.getGroup(), false, data.getOptionCode().useEnglishVariables());
                    validTable = getAbasType(tableFields,
                            data.getDatabase(), data.getGroup(),
                            true, data.getOptionCode()
                                    .useEnglishVariables());
                }
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

    private boolean isSpecialDb(Data data) {
        if (data.getDatabase() == 2 && (data.getGroup() == 6 || data.getGroup() == 7)) {
            return true;
        }
        return false;
    }

    private boolean checkData(Data data) {
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

    private Boolean checkDatabaseName(Data data) {
        if (data.getDatabase() != null && data.getGroup() != null) {
            String criteria = "0:grpDBDescr=("
                    + data.getDatabase().toString() + ");0:grpGrpNo="
                    + data.getGroup().toString() + ";"
                    + ";@englvar=true;@language=en";
            if (searchDatabase(data, criteria)) {
                return true;
            } else {
                data.appendError(Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
                return false;
            }
        } else {
            if (data.getDbString() != null && data.getDbGroupString() != null) {
                String criteria = "0:vdntxt==" + data.getDbString()
                        + ";0:vgrtxtbspr=="
                        + data.getDbGroupString() + ";"
                        + ";@englvar=false;@language=de";
                if (searchDatabase(data, criteria)) {
                    return true;
                } else {
                    criteria = "0:DBCmd==" + data.getDbString()
                            + ";0:grpGrpCmd==" + data.getDbGroupString()
                            + ";" + ";@englvar=true;@language=en";
                    if (searchDatabase(data, criteria)) {
                        return true;
                    } else {
                        data.appendError(Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private boolean searchDatabase(Data data, String criteria) {
        String key = "";
        int aliveFlag = EDPConstants.ALIVEFLAG_ALIVE;
        String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
        String tableName = "12:26";
        Boolean inTable = false;
        int mode = EDPConstants.ENUMPOS_CODE;
        try {
            this.edpSession.setEnumMode(mode);
        } catch (InvalidSettingValueException e) {
            logger.error(e);
        }
        EDPQuery query = this.edpSession.createQuery();
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
        }
        return false;
    }

    private void findDatabaseForTypeCommand(Data data) throws ImportitException {
        if (data.getTypeCommand() != null) {
            startEdpSession(EDPVariableLanguage.ENGLISH);
            if (this.edpSession != null) {
                EDPEditor edpEditor = this.edpSession.createEditor();
                try {
                    edpEditor.beginEditCmd(data.getTypeCommand().toString(), "");
                    data.setDatabase(edpEditor.getEditDatabaseNr());
                    data.setGroup(edpEditor.getEditGroupNr());
                    edpEditor.endEditCancel();
                } catch (CantBeginEditException e) {
                    throw new ImportitException(Util.getMessage("err.getting.database", e));
                }
            }
        }
    }

    private boolean getAbasType(List<Field> fieldList, Integer database, Integer group, Boolean inTab, Boolean englishVariables) throws ImportitException {
        if (fieldList != null) {
            if (!edpSession.isConnected()) {
                startEdpSession(EDPVariableLanguage.ENGLISH);
            }
            logger.info(Util.getMessage("info.start.getting.vartab", database, group));
            Vartab vartab = new Vartab(edpSession, database, group);
            logger.info(Util.getMessage("info.end.getting.vartab", database, group));
            closeEdpSession(this.edpSession);
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
                        logger.trace(Util.getMessage("info.found.field.with.type", field.getName(), vartabField.getActiveType()));
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

    public void importDataList(ArrayList<Data> dataList) throws ImportitException {
        edpSession.loggingOn("java/log/importit21edp.log");
        try {
            startEdpSession();
            for (Data data : dataList) {
                writeData(data);
            }
        } finally {
            closeEdpSession(this.edpSession);
        }
    }

    public void importDataListTransaction(ArrayList<Data> dataList) throws ImportitException {
        edpSession.loggingOn("java/log/importit21edp.log");
        for (Data data : dataList) {
            writeData(data);
            logger.info(data.toString());
        }
    }

    private void writeData(Data data) throws ImportitException {
        EDPSession edpSession = connectNewEdpSession(data.getEDPLanguage());
        if (edpSession.isConnected()) {
            EDPEditor edpEditor = edpSession.createEditor();
            try {
                if (data.getTypeCommand() == null) {
                    if(isSpecialDb(data)) {
                        writeCustomerPartProperties(data, edpEditor);
                    } else {
                        writeDatabase(data, edpEditor);
                    }
                } else {
                    writeTypeCommands(data, edpEditor);
                }
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

    private void writeTypeCommands(Data data, EDPEditor edpEditor) throws ImportitException, CantChangeSettingException, CantSaveException, CantBeginEditException, CantReadSettingException {
        data.setImported(false);
        edpEditor.beginEditCmd(data.getTypeCommand().toString(), "");
        setEditorOption(data, edpEditor);
        writeFieldsInEditor(data, edpEditor);
        edpEditor.endEditSave();
        data.setImported(true);
    }

    private void writeDatabase(Data data, EDPEditor edpEditor)
            throws CantBeginEditException, CantChangeSettingException,
            ImportitException, CantSaveException, InvalidQueryException, CantReadFieldPropertyException, CantChangeFieldValException, InvalidRowOperationException, ServerActionException, CantReadStatusException, CantReadSettingException {
        data.setImported(false);
        data.initOptions();
        if (data.getOptionCode().getAlwaysNew()) {
            setEditorOption(data, edpEditor);
            logger.info(Util.getMessage("info.start.editor.new", data.getDatabase().toString(), data.getGroup().toString()));
            edpEditor.beginEditNew(data.getDatabase().toString(),
                    data.getGroup().toString());
            writeFieldsInEditor(data, edpEditor);
            edpEditor.saveReload();
            String abasId = edpEditor.getEditRef();
            logger.info(Util.getMessage("info.save.editor.new", data.getDatabase().toString(), data.getGroup().toString(), abasId));
            data.setAbasId(abasId);
            edpEditor.endEditSave();
            data.setImported(true);
            if (edpEditor.isActive()) {
                edpEditor.endEditCancel();
                logger.info(Util.getMessage("info.cancel.editor.save", data.getDatabase().toString(), data.getGroup().toString(), abasId));
            } else {
                logger.info(Util.getMessage("info.editor.not.active"));
            }
        } else {
            String criteria = data.getNameOfKeyfield() + "=" + data.getValueOfKeyField();
            String objectId = getSelObject(criteria, data);
            if (!objectId.equals("Z")) {
                setEditorOption(data, edpEditor);
                if (!objectId.equals("0")) {
                    edpEditor.beginEdit(objectId);
                    if (edpEditor.getRowCount() > 0 && data.getOptionCode().getDeleteTable()) {
                        edpEditor.deleteAllRows();
                    }
                    logger.info(Util.getMessage("info.editor.start.update", data.getDatabase().toString(), data.getGroup().toString(), edpEditor.getEditRef()));

                } else {
                    logger.info(Util.getMessage("info.editor.start.new", data.getDatabase().toString(), data.getGroup().toString()));
                    edpEditor.beginEditNew(data.getDatabase().toString(),
                            data.getGroup().toString());
                }
                writeFieldsInEditor(data, edpEditor);
                edpEditor.saveReload();
                String abasId = edpEditor.getEditRef();
                logger.info(Util.getMessage("info.save.editor.new", data.getDatabase().toString(), data.getGroup().toString()));
                data.setAbasId(abasId);
                edpEditor.endEditSave();
                data.setImported(true);
                if (edpEditor.isActive()) {
                    edpEditor.endEditCancel();
                    logger.info(Util.getMessage("info.cancel.editor.save", data.getDatabase().toString(), data.getGroup().toString(), abasId));
                } else {
                    logger.info(Util.getMessage("info.editor.not.active"));
                }
            } else {
                data.appendError(Util.getMessage("err.selection.ambiguous", criteria));
            }
        }
    }

    private String getSelObject(String criteria, Data data) throws ImportitException, InvalidQueryException {
        EDPSession edpSession = null;
        try {
            if (data.getOptionCode().useEnglishVariables()) {
                edpSession = connectNewEdpSession(EDPVariableLanguage.ENGLISH);
            } else {
                edpSession = connectNewEdpSession(EDPVariableLanguage.GERMAN);
            }
            EDPQuery edpQuery = edpSession.createQuery();
            String tableName = data.getDatabase().toString()
                    + ":" + data.getGroup().toString();
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
        } finally {
            closeEdpSession(edpSession);
        }
    }

    private void setEditorOption(Data data, EDPEditor edpEditor) throws CantChangeSettingException, CantReadSettingException {
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

    private void writeFieldsInEditor(Data data, EDPEditor edpEditor) throws ImportitException {
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


    private Integer insertRow(Data data, EDPEditor edpEditor, Integer rowCount) throws ImportitException {
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

    private boolean dontIgnoreField(Field field, String[] ignoreFields) {
        String fieldName = field.getName();
        for (String ignoreField : ignoreFields) {
            if (ignoreField.equals(fieldName)) {
                return false;
            }
        }
        return true;
    }

    private void writeField(Data data, Field field, EDPEditor edpEditor, Integer rowNumber) throws ImportitException {
        if (!field.getOptionSkip()) {
            if (field.getValue() == null) {
                throw new ImportitException(Util.getMessage("err.null.value", field.getName(), data.getValueOfKeyField(), rowNumber));
            }
            if (!(field.getOptionNotEmpty() && field.getValue().isEmpty())) {
                try {
                    if (edpEditor.fieldIsModifiable(rowNumber, field.getName())) {
                        String dataFieldValue = field.getValue();
                        if (!(field.getOptionDontChangeIfEqual() &
                                edpEditor.getFieldVal(rowNumber, field.getName()).equals(dataFieldValue))) {
                            edpEditor.setFieldVal(rowNumber, field.getName(), dataFieldValue);
                            logger.debug(Util.getMessage("info.field.value.written", field.getName(), dataFieldValue, rowNumber.toString()));
                        }
                    } else {
                        if (!field.getOptionModifiable() && !data.getNameOfKeyfield().equals(field.getName())) {
                            if (rowNumber == 0) {
                                throw new ImportitException(Util.getMessage("err.headfield.not.writable", field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup()));
                            } else {
                                throw new ImportitException(Util.getMessage("err.tablefield.not.writable", field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), rowNumber.toString()));
                            }
                        }
                    }
                } catch (CantChangeFieldValException e) {
                    logger.error(e);
                    if (rowNumber == 0) {
                        throw new ImportitException(Util.getMessage("err.headfield.not.writable", field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup()));
                    } else {
                        throw new ImportitException(Util.getMessage("err.tablefield.not.writable", field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), rowNumber.toString()));
                    }

                } catch (CantReadFieldPropertyException e) {
                    logger.error(e);
                    if (rowNumber == 0) {
                        throw new ImportitException(Util.getMessage("err.headfield.not.readable", field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), e));
                    } else {
                        throw new ImportitException(Util.getMessage("err.tablefield.not.readable", field.getName(), data.getValueOfKeyField(), data.getDatabase(), data.getGroup(), rowNumber.toString(), e));
                    }
                }
            }
        } else {
            logger.debug(Util.getMessage("info.skip.field", field.getName()));
        }
    }

    public void checkDataListValues(ArrayList<Data> dataList) throws ImportitException {
        try {
            startEdpSession();
            for (Data data : dataList) {
                List<Field> headerFields = data.getHeaderFields();
                Boolean includeError = false;
                for (Field field : headerFields) {
                    if (!checkDataField(field)) {
                        includeError = true;
                    }
                }
                List<DataTable> tableRows = data
                        .getTableRows();
                for (DataTable dataTable : tableRows) {
                    ArrayList<Field> tableFields = dataTable
                            .getTableFields();
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
        } finally {
            closeEdpSession(this.edpSession);
        }
    }

    private Boolean checkDataField(Field field) throws ImportitException {
        String value = field.getValue();
        if (!field.getOptionSkip()) {
            logger.debug(Util.getMessage("info.check.data", field.getName(), field.getColNumber(), field.getAbasTyp(), value));
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
                            field.setError(Util.getMessage("err.check.data.field.length", value, valueLength, field.getName(), fieldLength.toString()));
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
                                BigDecimal roundBigDValue = bigDecimalValue.setScale(fractionDigits, RoundingMode.HALF_UP);
                                String roundBigDValueStr = roundBigDValue.toString();
                                String compValue = fillValueWithFractionDigits(value, fractionDigits);
                                if (!roundBigDValueStr.equals(compValue)) {
                                    field.setError(Util.getMessage("err.check.data.rounding", value, compValue, roundBigDValueStr));
                                }
                            } catch (NumberFormatException e) {
                                field.setError(Util.getMessage("err.check.data.conversion.big.decimal", value));
                            } catch (BadAttributeValueExpException e) {
                                throw new ImportitException(Util.getMessage("err.check.data.bad.attribute"), e);
                            }
                            if (value.split("[\\.,]")[0].length() > integerDigits) {
                                field.setError(Util.getMessage("err.check.data.too.many.digits", value, field.getAbasTyp(), field.getName()));
                            }
                        }
                    } else if (dataType == EDPTools.EDP_DATE) {
                        if (!checkDataDate(field)) {
                            field.setError(Util.getMessage("err.check.data.conversion.date", value));
                        }
                    } else if (dataType == EDPTools.EDP_DATETIME || dataType == EDPTools.EDP_TIME || dataType == EDPTools.EDP_WEEK) {
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
            EDPQuery query = null;
            try {
                query = getEDPQueryReference(value,
                        databaseNumber, groupNumber,
                        field.getColNumber());
                query.getLastRecord();
                int recordCount = query.getRecordCount();
                if (recordCount == 0) {
                    field.setError(Util.getMessage("err.check.reference.not.found", field.getAbasTyp(), value));
                } else if (recordCount > 1) {
                    field.setError(Util.getMessage("err.check.reference.not.unique", field.getAbasTyp(), value));
                }
            } catch (ImportitException e) {
                field.setError(Util.getMessage("err.check.reference", field.getAbasTyp(), value));
            } finally {
                if (query != null) {
                    if (query.getSession().isConnected()) {
                        query.getSession().endSession();
                        logger.info(Util.getMessage("info.end.edp.query"));
                    }
                }
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


    private EDPQuery getEDPQueryReference(String value, Integer database, Integer group, Integer rowNumber) throws ImportitException {
        if (!this.edpSession.isConnected()) {
            startEdpSession();
        }
        String[] fieldNames = {"id", "nummer"};
        String tableName = "";
        if (group == -1) {
            tableName = database.toString() + ":";
        } else {
            tableName = database.toString() + ":" + group.toString();
        }
        EDPQuery query = this.edpSession.createQuery();
        String selectionString = "@noswd=" + value + ";@englvar=true;@language=en;@database=" + database.toString();
        StandardEDPSelectionCriteria criteria = new StandardEDPSelectionCriteria(selectionString);
        StandardEDPSelection edpCriteria = new StandardEDPSelection(tableName, criteria);
        edpCriteria.setDatabase(database.toString());
        try {
            query.startQuery(edpCriteria, fieldNames.toString());

        } catch (InvalidQueryException e) {
            closeEdpSession(this.edpSession);
            throw new ImportitException(Util.getMessage("err.edp.query.bad.selection.string", selectionString), e);
        }
        return query;
    }

    public void startTransaction() throws ImportitException {
        startEdpSession();
        try {
            this.edpSession.startTransaction();
        } catch (TransactionException e) {
            throw new ImportitException(Util.getMessage("err.transaction.start"), e);
        }
    }

    public void commitTransaction() throws ImportitException {
        try {
            this.edpSession.commitTransaction();
        } catch (TransactionException e) {
            throw new ImportitException(Util.getMessage("err.transaction.commit"), e);
        } finally {
            closeEdpSession(this.edpSession);
        }
    }

    public void abortTransaction() throws ImportitException {
        try {
            this.edpSession.abortTransaction();
        } catch (TransactionException e) {
            throw new ImportitException(Util.getMessage("err.transaction.cancel"), e);

        } catch (ConnectionLostException e) {
            throw new ImportitException(Util.getMessage("err.edp.connection.cancel"), e);
        } finally {
            closeEdpSession(this.edpSession);
        }

    }

}
