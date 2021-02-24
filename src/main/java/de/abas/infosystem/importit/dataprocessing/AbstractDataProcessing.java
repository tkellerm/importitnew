package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.*;
import de.abas.eks.jfop.remote.EKSe;
import de.abas.infosystem.importit.*;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.DataTable;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static de.abas.ceks.jedp.EDPTools.*;


public abstract class AbstractDataProcessing implements AbasDataProcessable {

    public static final String DEBUG_PROGRESS_MANAGER_WAIT_END_THREAD = "debug.ProgressManager.waitEndThread";
    public static final String DEBUG_GETEDPSESSION = "debug.getedpsession";
    public static final String EDP_EDITOR_CREATE_EDITOR_ERROR = "edpEditor.createEditor.error";
    public static final String ERR_HEADFIELD_NOT_WRITABLE = "err.headfield.not.writable";
    public static final String ERR_TABLEFIELD_NOT_WRITABLE = "err.tablefield.not.writable";
    private final List<ProgressListener> progressListener = new ArrayList<>();

    protected EDPSessionHandler edpSessionHandler;
    protected Logger logger = Logger.getLogger(AbstractDataProcessing.class);

    private static final String MESSAGE_PROPERTY_IMPORT = "progress.message.import";
    private static final String MESSAGE_PROPERTY_CHECKSTRUCTUR = "progress.message.structurecheck";

    protected abstract void writeData(Data data) throws ImportitException;

    protected abstract boolean checkDataStructure(Data data) throws ImportitException;

    protected abstract void writeAbasIDinData(Data data) throws ImportitException;

    protected AbstractDataProcessing(EDPSessionHandler edpSessionHandler) {

        this.edpSessionHandler = edpSessionHandler;
    }

    @Override
    public void importDataListTransaction(List<Data> dataList) throws ImportitException {
        ProgressManager progress = new ProgressManager(MESSAGE_PROPERTY_IMPORT, dataList.size(), this.progressListener);
        Thread progressManagerThread = new Thread(progress);
        progressManagerThread.setPriority(Thread.MIN_PRIORITY);
        progressManagerThread.start();

        for (Data data : dataList) {
            writeData(data);
            logger.info(data.toString());
        }
        manageProgress(progress, progressManagerThread);
    }

    private void manageProgress(ProgressManager progress, Thread progressManagerThread) {
        progressManagerThread.setPriority(Thread.MAX_PRIORITY);
        progress.stop();
        while (progressManagerThread.getState() != Thread.State.TERMINATED) {
            try {
                //TODO search a other solution for sleep
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(e);
                Thread.currentThread().interrupt();
            }
            logger.debug(MessageUtil.getMessage(DEBUG_PROGRESS_MANAGER_WAIT_END_THREAD));
        }
    }

    @Override
    public void checkDataListStructure(List<Data> dataList) throws ImportitException {

        if (dataList == null) {
            throw new ImportitException(MessageUtil.getMessage("err.undefined.data.list"));
        }
        if (dataList.isEmpty()) {
            throw new ImportitException(MessageUtil.getMessage("err.empty.data.list"));
        }

        ProgressManager progress = new ProgressManager(MESSAGE_PROPERTY_CHECKSTRUCTUR, dataList.size(), this.progressListener);
        Thread progressManagerThread = new Thread(progress);
        progressManagerThread.setPriority(Thread.MIN_PRIORITY);
        progressManagerThread.start();

        Data firstDataRecord = dataList.get(0);
        if (firstDataRecord != null && checkDataStructure(firstDataRecord)) {
            for (Data dataset : dataList) {
                progress.sendProgress();
                dataset.copyDatabase(firstDataRecord);
                dataset.copyAbasType(firstDataRecord);
            }
        }


        manageProgress(progress, progressManagerThread);

    }


    @Override
    public void importDataList(List<Data> dataList) throws ImportitException {
        ProgressManager progress = new ProgressManager(MESSAGE_PROPERTY_IMPORT, dataList.size(), this.progressListener);

        Thread progressManagerThread = new Thread(progress);
        progressManagerThread.setPriority(Thread.MIN_PRIORITY);
        progressManagerThread.start();


        for (Data data : dataList) {
            progress.sendProgress();
            writeData(data);
        }
        manageProgress(progress, progressManagerThread);
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
            throw new ImportitException(MessageUtil.getMessage("err.edp.connection.cancel"), e);
        }

    }

    @Override
    public void commitTransaction() throws ImportitException {

        this.edpSessionHandler.commitTransaction();

    }

    @Override
    public boolean isTransactionActive() {
        return this.edpSessionHandler.isTransactionActive();
    }

    @Override
    public void checkDataListValues(List<Data> dataList) {


        ProgressManager progress = new ProgressManager("progress.message.datacheck", dataList.size(),
                this.progressListener);
        Thread progressManagerThread = new Thread(progress);
        progressManagerThread.setPriority(Thread.MIN_PRIORITY);
        progressManagerThread.start();

        dataList.forEach(s -> {
            try {
                checkDataValues(s, progress);
            } catch (ImportitException e) {
                logger.error(e);
            }
        });

        manageProgress(progress, progressManagerThread);
    }

    private void checkDataValues(Data data, ProgressManager progress) throws ImportitException {

        boolean includeError = false;

        List<Field> headerFields = data.getHeaderFields();

        if (!data.getOptionCode().getAlwaysNew()) {
            writeAbasIDinData(data);
        }

        if (checkFieldListData(headerFields)) {
            includeError = true;
        }

        logger.debug("start check sml");
        List<Field> smlfields = data.getSmlFields();

        if (checkFieldListData(smlfields)) {
            includeError = true;
        }
        logger.debug("start check tableRows");
        List<DataTable> tableRows = data.getTableRows();
        for (DataTable dataTable : tableRows) {
            List<Field> tableFields = dataTable.getTableFields();
            if (checkFieldListData(tableFields)) {
                includeError = true;
            }
        }

        if (includeError) {
            data.createErrorReport();
        }

        progress.sendProgress();

    }

    private boolean checkFieldListData(List<Field> fields) {
        boolean errorIncluded = false;
        for (Field field : fields) {
            try {
                checkDataField(field);
                if (!field.getError().isEmpty()){
                    errorIncluded = true;
                }
                logger.debug(MessageUtil.getMessage("info.AbstractDataProcessing.afterCheckField", field.getName(), field.getValue(), field.getError()));
            } catch (ImportitException e) {
                logger.error(e);
                field.setError(e.getMessage());
            }
        }
        return errorIncluded;
    }


    protected void writeFieldInclusivErrorHandling(Data data, Field field, EDPEditor edpEditor, Integer rowNumber) throws ImportitException {
        if (Boolean.FALSE.equals(field.isOptionSkip())) {
            if (field.getValue() == null) {
                throw new ImportitException(
                        MessageUtil.getMessage("err.null.value", field.getName(), data.getValueOfKeyField(), rowNumber));
            }

            if (Boolean.FALSE.equals(field.isKeySelectionField()) && !(field.isOptionNotEmpty() && field.getValue().isEmpty())) {
                try {

                    writeField(data, field, edpEditor, rowNumber);

                } catch (CantChangeFieldValException e) {
                    logger.error(e);
                    throwExceptionInCaseOfHeadOrTable(data, field, rowNumber, ERR_HEADFIELD_NOT_WRITABLE, ERR_TABLEFIELD_NOT_WRITABLE);

                } catch (CantReadFieldPropertyException e) {
                    logger.error(e);
                    throwExceptionInCaseOfHeadOrTable(data, field, rowNumber, "err.headfield.not.readable", "err.tablefield.not.readable");

                }

            }
        } else {
            logger.debug(MessageUtil.getMessage("info.skip.field", field.getName()));
        }
    }

    private void throwExceptionInCaseOfHeadOrTable(Data data, Field field, Integer rowNumber, String headFieldMessage, String tableFieldMessage) throws ImportitException {
        if (rowNumber == 0) {
            throw new ImportitException(MessageUtil.getMessage(headFieldMessage, field.getName(),
                    data.getValueOfKeyField(), data.getDatabase(), data.getGroup()));
        } else {
            throw new ImportitException(MessageUtil.getMessage(tableFieldMessage, field.getName(),
                    data.getValueOfKeyField(), data.getDatabase(), data.getGroup(),
                    rowNumber.toString()));
        }
    }

    private void writeField(Data data, Field field, EDPEditor edpEditor, Integer rowNumber) throws CantReadFieldPropertyException, CantChangeFieldValException, ImportitException {
        if (edpEditor.fieldIsModifiable(rowNumber, field.getName())) {

            String dataFieldValue = field.getValidateFieldValue();

            if (checkWriteifDontChangeIfEqual(field, edpEditor, rowNumber)) {

                edpEditor.setFieldVal(rowNumber, field.getName(), field.getValidateFieldValue());

                logger.debug(MessageUtil.getMessage("info.field.value.written", field.getName(),
                        dataFieldValue, rowNumber.toString()));

            }

        } else {
            if (Boolean.FALSE.equals(field.isOptionModifiable()) && !data.getNameOfKeyField().equals(field.getName())) {
                throwExceptionInCaseOfHeadOrTable(data, field, rowNumber, ERR_HEADFIELD_NOT_WRITABLE, ERR_TABLEFIELD_NOT_WRITABLE);
            }
        }
    }

    private boolean checkWriteifDontChangeIfEqual(Field field, EDPEditor edpEditor, Integer rowNumber)
            throws CantReadFieldPropertyException {

        if (Boolean.TRUE.equals(field.isOptionDontChangeIfEqual())) {
            if (Boolean.TRUE.equals(field.isReferenceField())) {
                String value = field.getValidateFieldValue();
                String editorFieldValueAbasId = edpEditor.getFieldVal(rowNumber, field.getName() + "^id");
                String editorFieldValue = edpEditor.getFieldVal(rowNumber, field.getName());
                if (value.equals(editorFieldValue)) {
                    return false;
                }
                if (value.equals(editorFieldValueAbasId)) {
                    return false;
                }
            }
            return !edpEditor.getFieldVal(rowNumber, field.getName()).equals(field.getValue());
        }

        return true;
    }

    private void checkDataField(Field field) throws ImportitException {
        String value = field.getValue();
        if (Boolean.FALSE.equals(field.isOptionSkip())) {

            logger.debug(MessageUtil.getMessage("info.check.data", field.getName(), field.getColNumber(), field.getAbasTyp(),
                    value));

            if (!field.getAbasTyp().isEmpty()) {
                EDPEKSArtInfo edpEksArtInfo = new EDPEKSArtInfo(field.getAbasTyp());
                int dataType = edpEksArtInfo.getDataType();
                if ((value != null) && (!(field.isOptionNotEmpty() && value.isEmpty()))) {
                    switch (dataType) {
                        case EDP_REFERENCE:
                        case EDP_ROWREFERENCE:
                            checkSeveralRefernceFields(field, value, edpEksArtInfo);
                            break;
                        case EDP_STRING:
                            checkStringField(field, value, edpEksArtInfo);
                            break;
                        case EDP_INTEGER:
                            checkIntegerField(field, value, edpEksArtInfo);
                            break;
                        case EDP_DOUBLE:
                            checkDoubleField(field, value, edpEksArtInfo);
                            break;
                        case EDP_DATE:
                            checkDateField(field, value, "err.check.data.conversion.date");
                            break;
                        case EDPTools.EDP_DATETIME:
                        case EDPTools.EDP_TIME:
                        case EDPTools.EDP_WEEK:
                            checkDateField(field, value, "err.check.data.conversion.time");
                            break;
                        default:
                            break;

                    }


                } else {
                    field.setError(MessageUtil.getMessage("err.check.data.null.value"));
                }
            }
        }
    }

    private void checkDateField(Field field, String value, String s) {
        if (Boolean.FALSE.equals(checkDataDate(field))) {
            field.setError(MessageUtil.getMessage(s, value));
        }
    }

    private void checkSeveralRefernceFields(Field field, String value, EDPEKSArtInfo edpEksArtInfo) {
        String edpErpArt = edpEksArtInfo.getERPArt();
        if (edpErpArt.startsWith("V")) {
            logger.debug(MessageUtil.getMessage("info.AbstractDataProcessing,start.check.MultiReference", field.getName(), value));
            checkMultiReferenceField(field, value, edpEksArtInfo);
        } else {
            logger.debug(MessageUtil.getMessage("info.AbstractDataProcessing,start.check.Reference", field.getName(), field.getValue()));
            checkReferenceField(field, edpEksArtInfo);
        }
    }


    private void checkDoubleField(Field field, String value, EDPEKSArtInfo edpEksArtInfo) throws ImportitException {
        int fractionDigits = edpEksArtInfo.getFractionDigits();
        int integerDigits = edpEksArtInfo.getIntegerDigits();
        if (value.length() > 0 && !value.equals("0")) {
            try {
                value = value.replace(" ", "");
                BigDecimal bigDecimalValue = new BigDecimal(value);
                BigDecimal roundBigDValue = bigDecimalValue.setScale(fractionDigits, RoundingMode.HALF_UP);
                String roundBigDValueStr = roundBigDValue.toString();
                String compValue = fillValueWithFractionDigits(value, fractionDigits);
                if (!roundBigDValueStr.equals(compValue)) {
                    field.setError(MessageUtil.getMessage("err.check.data.rounding", value, compValue, roundBigDValueStr));
                }
            } catch (NumberFormatException e) {
                field.setError(MessageUtil.getMessage("err.check.data.conversion.big.decimal", value));
            }
            if (value.split("[.,]")[0].length() > integerDigits) {
                field.setError(
                        MessageUtil.getMessage("err.check.data.too.many.digits", value, field.getAbasTyp(), field.getName()));
            }
        }
    }

    private void checkIntegerField(Field field, String value, EDPEKSArtInfo edpEksArtInfo) {
        try {
            int integerDigits = edpEksArtInfo.getIntegerDigits();
            if (value.length() > 0 && !value.equals("0")) {

                int intValue = Integer.parseInt(value);
                int valueLength = Integer.toString(intValue).length();
                if (integerDigits < valueLength) {
                    field.setError(MessageUtil.getMessage("err.check.data.too.big", value));
                }
            }
        } catch (NumberFormatException e) {
            field.setError(MessageUtil.getMessage("err.check.data.conversion.integer", value));
        }
    }

    private void checkStringField(Field field, String value, EDPEKSArtInfo edpEksArtInfo) {
        long fieldLength = edpEksArtInfo.getMaxLen();
        long valueLength = value.length();
        if (fieldLength < valueLength) {
            field.setError(MessageUtil.getMessage("err.check.data.field.length", value, valueLength, field.getName(),
                    Long.toString(fieldLength)));
        }
    }

    private void checkMultiReferenceField(Field field, String value, EDPEKSArtInfo edpEksArtInfo) {
        String edpErpArt = edpEksArtInfo.getERPArt();
        if (edpErpArt.equals("VPK1") || edpErpArt.equals("VPKS1") || edpErpArt.equals("VPKT1")) {
            if (value.startsWith("A ")) {
                checkReferenceField(field, new EDPEKSArtInfo("P7:0"));
            } else {
                checkReferenceField(field, new EDPEKSArtInfo("P2:1.2.5"));
            }
        } else if (edpErpArt.equals("VPK5") || edpErpArt.equals("VPKS5") || edpErpArt.equals("VPKT5")) {
            // TODO Implement more MultiReferenceFields
        }
    }

    private void checkReferenceField(Field field, EDPEKSArtInfo edpeksartinfo) {
        String value = field.getReferenceFieldValue();
        int databaseNumber = edpeksartinfo.getRefDatabaseNr();
        int groupNumber = edpeksartinfo.getRefGroupNr();
        if (!value.isEmpty()) {

            if (field.getFieldSelectionString().isEmpty()) {
                field.setAbasID(getEDPQueryReference(field, databaseNumber, groupNumber));
            } else {
                field.setAbasID(searchAbasIDforField(field, databaseNumber, groupNumber));
            }

        }
    }

//TODO Check duplicate with EDPUtils
    private void endQuery(EDPQuery query) {
        if (query != null) {
            if (query.getSession().isConnected()) {
                query.breakQuery();
                logger.info(MessageUtil.getMessage("info.end.edp.query"));
            }
            this.edpSessionHandler.freeEDPSession(query.getSession());
        }

    }

    private String fillValueWithFractionDigits(String value, int fractionDigits) throws ImportitException {
        Double doubleValue = Double.valueOf(value);
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
        boolean result;
        BufferFactory bufferFactory = BufferFactory.newInstance(true);
        UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();
        String varnameResult = "xtergebnis";
        if (!userTextBuffer.isVarDefined(varnameResult)) {
            userTextBuffer.defineVar("Bool", varnameResult);
        }
        userTextBuffer.setValue(varnameResult, "0");
        String formulaString = "U|" + varnameResult + " = F|isvalue( \"" + value + "\" , \"" + abastyp + "\")";
        EKSe.formula(formulaString);
        result = userTextBuffer.getBooleanValue(varnameResult);
        return result;
    }

    private String fillString(String value, int stringLength) throws ImportitException {
        if (value.length() == 1) {

            StringBuilder multipleStringBuilder = new StringBuilder();
            for (int i = 0; i < stringLength; i++) {
                multipleStringBuilder.append(value);
            }
            return multipleStringBuilder.toString();
        } else {
            throw new ImportitException(MessageUtil.getMessage("err.fill.string.bad.attribute"));
        }

    }

    private String getEDPQueryReference(Field field, Integer database, Integer group) {

        logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "getEDPQueryReference"));
        EDPSession edpSession;
        EDPQuery query = null;
        try {

            edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
            String selectionString = "@noswd=" + field.getReferenceFieldValue()
                    + ";@englvar=true;@language=en;@database=" + database.toString();
            query = getQueryWithSelectionString(database, group, edpSession, selectionString);
            return analyzeSelectionQuery(field, query);
        } catch (ImportitException e) {
            field.setError(MessageUtil.getMessage("err.check.reference", field.getAbasTyp(), field.getValue()));
        } finally {
            logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "getEDPQueryReference"));
            endQuery(query);
        }
        return "";

    }

    protected String searchAbasIDforField(Field field, Integer database, Integer group) {

        EDPSession edpSession;
        EDPQuery query = null;
        try {
            edpSession = this.edpSessionHandler.getEDPSession(field.getEDPVariableLanguage());
            logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "searchAbasIDforField"));
            String value = field.getValue();
            if (database == 7 && group == 0) {
                value = EDPUtils.replaceCharPlusBlank("A", value);
            }

            String selectionString = MessageFormat.format(field.getFieldSelectionString(), value);

            selectionString = selectionString + ";@database=" + constructTableName(database, group);
            query = getQueryWithSelectionString(database, group, edpSession, selectionString);
            return analyzeSelectionQuery(field, query);
        } catch (ImportitException e) {
            field.setError(MessageUtil.getMessage("err.check.reference", field.getAbasTyp(), field.getValue()));
        } finally {
            endQuery(query);
        }
        return "";
    }

    private String analyzeSelectionQuery(Field field, EDPQuery query) {
        query.getLastRecord();
        String abasID = query.getField("id");
        int recordCount = query.getRecordCount();
        if (recordCount == 0) {
            field.setError(MessageUtil.getMessage("err.check.reference.not.found", field.getAbasTyp(), field.getValue()));
            return "0";
        } else if (recordCount > 1) {
            field.setError(MessageUtil.getMessage("err.check.reference.not.unique", field.getAbasTyp(), field.getValue()));
            return "Z";
        } else {
            return abasID;
        }
    }

    private EDPQuery getQueryWithSelectionString(Integer database, Integer group, EDPSession edpSession,
                                                 String selectionString) throws ImportitException {
        String fieldNames;
        EDPQuery query = null;
        try {
            if (edpSession.getVariableLanguage() == EDPVariableLanguage.GERMAN) {
                fieldNames = "id,nummer,sn";
            } else {
                fieldNames = "id,idno,recordNo";
            }

            String tableName = constructTableName(database, group);
            query = edpSession.createQuery();
            StandardEDPSelectionCriteria criteria = new StandardEDPSelectionCriteria(selectionString);
            StandardEDPSelection edpCriteria = new StandardEDPSelection(tableName, criteria);
            edpCriteria.setDatabase(database.toString());
            if (group != null && group != -1) {
                edpCriteria.setGroup(group.toString());
            }

            logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION,
                    "getQueryWithSelectionString startQuery " + edpCriteria.getEDPString()));
            query.startQuery(edpCriteria, fieldNames);
            logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "getQueryWithSelectionString endQuery"));

        } catch (InvalidQueryException e) {
            throw new ImportitException(MessageUtil.getMessage("err.edp.query.bad.selection.string", selectionString), e);
        } catch (CantReadSettingException e1) {
            logger.error(e1);

        }
        return query;
    }

    private String constructTableName(Integer database, Integer group) {
        String tableName;
        if (group == -1) {
            tableName = database.toString() + ":";
        } else {
            tableName = database.toString() + ":" + group.toString();
        }
        return tableName;
    }

    protected void setEditorOption(Data data, EDPEditor edpEditor)
            throws CantChangeSettingException, CantReadSettingException {
        OptionCode optionCode = data.getOptionCode();
        if (optionCode != null) {
            setEditorOptionNoFOP(edpEditor, optionCode);
            setEditorOptionUseEnglishVariableLanguage(edpEditor, optionCode);
        }
    }

    private void setEditorOptionUseEnglishVariableLanguage(EDPEditor edpEditor, OptionCode optionCode) throws CantChangeSettingException {
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

    private void setEditorOptionNoFOP(EDPEditor edpEditor, OptionCode optionCode) throws CantReadSettingException {
        if (optionCode.noFop()) {
            if (edpEditor.getSession().getFOPMode()) {
                edpEditor.getSession().setFOPMode(false);
            }
        } else {
            if (!edpEditor.getSession().getFOPMode()) {
                edpEditor.getSession().setFOPMode(true);
            }
        }
    }

    protected void writeFieldsInEditor(Data data, EDPEditor edpEditor) throws ImportitException {
        if (edpEditor.isActive()) {
            List<Field> headerFields = data.getHeaderFields();
            writeFielsForFieldLists(data, edpEditor, headerFields, 0);
            if (data.getSmlFields() != null && !data.getSmlString().isEmpty()) {
                writeSMLFields(data, edpEditor);
            }
            List<DataTable> tableRows = data.getTableRows();
            if (tableRows != null && edpEditor.hasTablePart()) {
                for (DataTable dataTable : tableRows) {
                    Integer rowCount = edpEditor.getRowCount();
                    Integer rowNumber = insertRow(data, edpEditor, rowCount);
                    List<Field> tableFields = dataTable.getTableFields();
                    writeFielsForFieldLists(data, edpEditor, tableFields, rowNumber);
                }
            }
        }
    }

    private void writeFielsForFieldLists(Data data, EDPEditor edpEditor, List<Field> headerFields, int i) throws ImportitException {
        for (Field field : headerFields) {
            writeFieldInclusivErrorHandling(data, field, edpEditor, i);
        }
    }

    private void writeSMLFields(Data data, EDPEditor edpEditor) throws ImportitException {

        String smlNumber = data.getSmlString();

        try {
            if (edpEditor.getVariableLanguage().equals(EDPVariableLanguage.GERMAN)) {
                edpEditor.setFieldVal("sach", smlNumber);
                List<Field> smlfields = data.getSmlFields();
                writeFielsForFieldLists(data, edpEditor, smlfields, 0);

            }
        } catch (CantChangeFieldValException | CantReadSettingException e) {
            throw new ImportitException(MessageUtil.getMessage("error.writeSML"), e);
        }

    }

    protected Integer insertRow(Data data, EDPEditor edpEditor, Integer rowCount) throws ImportitException {
        try {
            if (rowCount == 0) {
                edpEditor.insertRow(1);
                return 1;
            } else {
                if (data.getTypeCommand() == null || rowCount > 1) {
                    int newRowNumber = rowCount + 1;
                    edpEditor.insertRow(newRowNumber);
                    return newRowNumber;
                } else {
                    return rowCount;
                }
            }
        } catch (InvalidRowOperationException e) {
            logger.error(e);
            throw new ImportitException(MessageUtil.getMessage("err.row.insert"));
        }
    }

    /**
     * TODO write JAVADOC
     * @param criteria
     * @param data
     * @return
     * @throws ImportitException
     */
    protected String getSelObject(String criteria, Data data) throws ImportitException {
        EDPSession edpSession;
        EDPQuery edpQuery = null;
        try {

            logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "getSelObject"));
            if (data.getOptionCode().useEnglishVariables()) {
                edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);

            } else {
                edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.GERMAN);

            }

            edpQuery = edpSession.createQuery();
            String tableName = data.getDatabase().toString() + ":" + data.getGroup().toString();
            String key;
            key = data.getKeyOfKeyField();
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
             //TODO create Constant or addError and Exception
                return "Z";
            } else {
                //TODO create constant
                return "0";
            }
        } catch (InvalidQueryException e) {
            throw new ImportitException(
                    MessageUtil.getMessage("err.abstractDataProcessing.selObject.invalidQuery", e, criteria));
        } finally {
            endQuery(edpQuery);
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

    protected void writeFieldsInEditor(Data data, DataTable dataTable, EDPEditor edpEditor, String[] ignoreFields)
            throws ImportitException {
        if (Boolean.TRUE.equals(edpEditor.isActive())) {
            if (edpEditor.getEditAction() == EDPEditAction.NEW) {
                writeFieldsModusNew(data, dataTable, edpEditor);
            } else if (edpEditor.getEditAction() == EDPEditAction.UPDATE)
                writeFieldsModusUpdate(data, dataTable, edpEditor, ignoreFields);
        }
    }

    private void writeFieldsModusUpdate(Data data, DataTable dataTable, EDPEditor edpEditor, String[] ignoreFields) throws ImportitException {
        List<Field> headerFields = data.getHeaderFields();
        for (Field field : headerFields) {
            if (dontIgnoreField(field, ignoreFields)) {
                writeFieldInclusivErrorHandling(data, field, edpEditor, 0);
            }
        }
        if (dataTable != null && edpEditor.hasTablePart()) {
            Integer rowNumber = edpEditor.getCurrentRow();
            List<Field> tableFields = dataTable.getTableFields();
            for (Field field : tableFields) {
                if (dontIgnoreField(field, ignoreFields)) {
                    writeFieldInclusivErrorHandling(data, field, edpEditor, rowNumber);
                }
            }
        }
    }

    private void writeFieldsModusNew(Data data, DataTable dataTable, EDPEditor edpEditor) throws ImportitException {
        writeListOfFieldsInEditor(data, edpEditor, data.getHeaderFields(), 0);
        if (dataTable != null && edpEditor.hasTablePart()) {
            Integer rowCount = edpEditor.getRowCount();
            Integer rowNumber = insertRow(data, edpEditor, rowCount);
            writeListOfFieldsInEditor(data, edpEditor, dataTable.getTableFields(), rowNumber);
        }
    }

    private void writeListOfFieldsInEditor(Data data, EDPEditor edpEditor, List<Field> headerFields, int i) throws ImportitException {
        for (Field field : headerFields) {
            writeFieldInclusivErrorHandling(data, field, edpEditor, i);
        }
    }

    protected Boolean checkDatabaseName(Data data)  {
        if (data.getDatabase() != null && data.getGroup() != null) {
            return writeDatabaseInData(data);
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
                        data.appendError(
                                MessageUtil.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
                        return false;
                    }
                }
            }
        }
        return false;
    }

    protected void closeEdpEditor(Data data, EDPEditor edpEditor, String abasId){
        data.setImported(true);
        if (edpEditor.isActive()) {
            edpEditor.endEditCancel();
            logger.info(MessageUtil.getMessage("info.cancel.editor.save", data.getDatabase().toString(),
                    data.getGroup().toString(), abasId));
        } else {
            logger.info(MessageUtil.getMessage("info.editor.not.active"));
        }
        releaseAndFreeEDPEditor(edpEditor);
    }


    private Boolean writeDatabaseInData(Data data)  {
        String criteria = "0:grpDBDescr=(" + data.getDatabase().toString() + ");0:grpGrpNo="
                + data.getGroup().toString() + ";" + ";swd<>VVAR;@englvar=true;@language=en";
        if (searchDatabase(data, criteria)) {
            return true;
        } else {
            data.appendError(MessageUtil.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
            return false;
        }
    }

    private boolean searchDatabase(Data data, String criteria)  {

        int mode = EDPConstants.ENUMPOS_CODE;
        logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "searchdatabase"));
        EDPSession edpSession = this.edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
        try {
            edpSession.setEnumMode(mode);
        } catch (InvalidSettingValueException e) {
            logger.error(e);
        }
        EDPQuery query = edpSession.createQuery();
        try {
            if (isDatabaseAvailableAndWriteInData(data, criteria, query))
                return true;
        } catch (InvalidQueryException e) {
            data.appendError(MessageUtil.getMessage("err.invalid.selection.criteria", criteria, e));
            return false;
        } finally {
            EDPUtils.releaseQuery(query, logger);
            this.edpSessionHandler.freeEDPSession(edpSession);
        }
        return false;
    }

    public static boolean isDatabaseAvailableAndWriteInData(Data data, String criteria, EDPQuery query) throws InvalidQueryException {
        String key = "";
        int aliveFlag = EDPConstants.ALIVEFLAG_ALIVE;
        String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
        String tableName = "12:26";

        query.startQuery(tableName, key, criteria, false, aliveFlag, true, true, fieldNames, 0, 10000);
        query.getLastRecord();
        if (query.getRecordCount() == 1) {
            String dbString = query.getField("grpDBDescr");
            dbString = dbString.replaceAll("\\(*", "");
            dbString = dbString.replaceAll("\\)*", "");
            data.setDatabase(Integer.valueOf(dbString));
            String group = query.getField("grpGrpNo");
            group = group.replace(" ", "");
            data.setGroup(Integer.valueOf(group));
            return true;
        }
        return false;
    }

    protected boolean checkFieldList(List<Field> fieldList, Integer database, Integer group, Boolean inTab,
                                     Boolean englishVariables) throws ImportitException {
        if (fieldList == null) {
            if (Boolean.TRUE.equals(inTab)) {
                return true;
            } else {
                throw new ImportitException(MessageUtil.getMessage("err.invalid.head.fields"));
            }
        }

        logger.info(MessageUtil.getMessage("info.start.getting.vartab", database, group));

        Vartab vartab = new Vartab(this.edpSessionHandler, database, group);

        logger.info(MessageUtil.getMessage("info.end.getting.vartab", database, group));

        boolean error = false;

        for (Field field : fieldList) {

            error = fillVartabFieldAndType(englishVariables, vartab, error, field);
        }
        if (error) {
            logger.info(MessageUtil.getMessage("info.not.all.fields.found"));
            return false;
        } else {
            logger.info(MessageUtil.getMessage("info.all.fields.found"));
            return true;
        }

    }

    private boolean fillVartabFieldAndType(Boolean englishVariables, Vartab vartab, boolean error, Field field) {
        if (Boolean.FALSE.equals(field.isOptionSkip())) {
            VartabField vartabField;

            vartabField = getVartabField(englishVariables, vartab, field);

            if (Boolean.FALSE.equals(field.iswithKeySelection())) {
                if (vartabField != null) {
                    field.setAbasType(vartabField.getActiveType());
                    logger.trace(MessageUtil.getMessage("info.found.field.with.type", field.getName(),
                            vartabField.getActiveType()));
                } else {
                    String errorText = MessageUtil.getMessage("err.field.not.found", field.getName());
                    error = true;
                    field.setError(errorText);
                    logger.error(errorText);
                }
            } else {
                logger.info(MessageUtil.getMessage("info.found.field.with.keyselection", field.getName(),
                        field.getKeySelection()));
            }
        }
        return error;
    }

    private VartabField getVartabField(Boolean englishVariables, Vartab vartab, Field field) {
        VartabField vartabField;
        if (Boolean.TRUE.equals(englishVariables)) {
            vartabField = vartab.checkVartabEnglish(field.getName());

        } else {
            vartabField = vartab.checkVartabGerman(field.getName());
        }
        return vartabField;
    }

    protected EDPEditor createEDPEditorNew(String database, String group, EDPVariableLanguage edpVariableLanguage,
                                           Data data)  {

        EDPEditor edpEditor;
        do {
            EDPSession edpSession = this.edpSessionHandler.getEDPSessionWriteData(edpVariableLanguage);
            try {
                edpEditor = edpSession.createEditor();
                setEditorOption(data, edpEditor);
                edpEditor.beginEditNew(database, group);

            } catch (CantBeginEditException | CantChangeSettingException | CantReadSettingException e) {
                edpEditor = null;
                logger.error(MessageUtil.getMessage(EDP_EDITOR_CREATE_EDITOR_ERROR), e);

            }
        } while (edpEditor == null);
        return edpEditor;

    }

    protected EDPEditor createEDPEditorEdit(String objectId, EDPVariableLanguage edpVariableLanguage, Data data)
            {

        EDPEditor edpEditor = null;
        do {
            EDPSession edpSession = this.edpSessionHandler.getEDPSessionWriteData(edpVariableLanguage);
            try {
                edpEditor = edpSession.createEditor();
                setEditorOption(data, edpEditor);
                edpEditor.beginEdit(objectId);

            } catch (CantBeginEditException e) {
                logger.error(MessageUtil.getMessage(EDP_EDITOR_CREATE_EDITOR_ERROR), e);
            } catch (CantChangeSettingException | CantReadSettingException e) {
                edpEditor = null;
                logger.error(MessageUtil.getMessage(EDP_EDITOR_CREATE_EDITOR_ERROR), e);
            }
        } while (edpEditor == null);
        return edpEditor;

    }

    protected void releaseAndFreeEDPEditor(EDPEditor edpEditor) {
        EDPUtils.releaseEDPEditor(edpEditor, logger);
        this.edpSessionHandler.freeEDPSession(edpEditor.getSession());
    }


    protected EDPEditor getEPDEditorforObjectId(Data data, String objectId)
            throws CantReadStatusError, InvalidRowOperationException {
        EDPEditor edpEditor;
        if (!objectId.equals("Z")) {
            if (!objectId.equals("0")) {
                edpEditor = createEDPEditorEdit(objectId, data.getEDPLanguage(), data);

                if (edpEditor.getRowCount() > 0 && data.getOptionCode().getDeleteTable()) {
                    edpEditor.deleteAllRows();
                }
                logger.info(MessageUtil.getMessage("info.editor.start.update", data.getDatabase().toString(),
                        data.getGroup().toString(), edpEditor.getEditRef()));

            } else {
                logger.info(MessageUtil.getMessage("info.editor.start.new", data.getDatabase().toString(),
                        data.getGroup().toString()));

                edpEditor = createEDPEditorNew(data.getDatabase().toString(), data.getGroup().toString(),
                        data.getEDPLanguage(), data);

            }
        } else {
            throw new IllegalArgumentException(MessageUtil.getMessage("error.getEDPEditorforObjectID.ObjectIDZ"));
        }
        return edpEditor;
    }

    public void addListener(ProgressListener toAdd) {
        this.progressListener.add(toAdd);
    }

}
