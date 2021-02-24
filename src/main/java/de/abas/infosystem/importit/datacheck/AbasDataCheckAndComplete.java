package de.abas.infosystem.importit.datacheck;

import de.abas.ceks.jedp.*;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataprocessing.AbstractDataProcessing;
import de.abas.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class AbasDataCheckAndComplete {

    public static final String DEBUG_GETEDPSESSION = "debug.getedpsession";
    private final Logger logger = Logger.getLogger(AbasDataCheckAndComplete.class);
    private final List<Data> dataList;
    private final EDPSessionHandler edpSessionHandler;

    public AbasDataCheckAndComplete(EDPSessionHandler edpSessionHandler, List<Data> dataList) {

        this.edpSessionHandler = edpSessionHandler;
        this.dataList = dataList;

    }

    public boolean checkAndCompleteDataList() throws ImportitException {

        Data data = dataList.get(0);
        if (data == null) {
            return false;
        }

        if (checkAndCompleteData(data)) {

            for (Data dataset : dataList) {
                dataset.copyDatabase(data);

                if (!dataset.getSmlString().isEmpty()) {
                    fillSmlArray(dataset);
                    //TODO Warum wird deleteSmlFieldFromHeaderFields nochmal aufgerufen? Wenn nein dann weglöschen
                    deleteSmlFieldFromHeaderFields(dataset);
                }
                //TODO researche if abasType is already filled in timestamp
                dataset.copyAbasType(data);

            }

            return true;
        }
        return false;

    }

    private List<Field> createDefaultSMLFieldList(Data data) {
        if (data.getSmlString() != null) {

            moveSmlFieldsFromHeaderArrayToSmlArray(data);

            return data.getSmlFields();
        }
        return Collections.emptyList();

    }


    private void fillSmlArray(Data dataset) {
        List<Field> smlFields = createDefaultSMLFieldList(dataset);

        dataset.setSmlFields(smlFields);

    }

    private void deleteSmlFieldFromHeaderFields(Data dataset) {

        List<Field> headerFields = dataset.getHeaderFields();

        headerFields.removeIf((Field field) -> (field.getCompleteContent().startsWith("S.")
                || field.getCompleteContent().startsWith("s.")));
    }

    //TODO move to DATA.class
    private void moveSmlFieldsFromHeaderArrayToSmlArray(Data dataset) {
        List<Field> headerFields = dataset.getHeaderFields();
        List<Field> smlFields = dataset.getSmlFields();
        for (Field field : headerFields) {
            String completeContent = field.getCompleteContent();
            if (completeContent.startsWith("S.") || completeContent.startsWith("s.")) {
                smlFields.add(field);
            }
        }
        deleteSmlFieldFromHeaderFields(dataset);
    }

    private boolean checkAndCompleteData(Data data) throws ImportitException {
        FillingTypeCommandsData.fillTypeCommandData(data);

        return checkDatabaseNameAndWriteInData(data);

    }


    private Boolean checkDatabaseNameAndWriteInData(Data data) throws ImportitException {
        String database = null;
        String databaseGroup = null;

        if (data.getDatabase() != null && data.getGroup() != null) {
            database = data.getDatabase().toString();
            databaseGroup = data.getGroup().toString();
            String criteria = "0:grpDBDescr=(" + database + ");0:grpGrpNo="
                    + databaseGroup + ";swd<>VVAR;@englvar=true;@language=en";

            if (searchDatabaseAndWriteInData(data, criteria)) {
                return true;
            }
        }

        if (data.getDbString() != null && data.getDbGroupString() != null) {
            database = data.getDbString();
            databaseGroup = data.getDbGroupString();
            String criteria = "0:vdntxt==" + database + ";0:vgrtxtbspr==" + databaseGroup + ";"
                    + ";such<>VVAR;@englvar=false;@language=de";
            if (searchDatabaseAndWriteInData(data, criteria)) {
                return true;
            } else {
                criteria = "0:DBCmd==" + database + ";0:grpGrpCmd==" + databaseGroup + ";"
                        + ";swd<>VVAR;@englvar=true;@language=en";
                if (searchDatabaseAndWriteInData(data, criteria)) {
                    return true;
                }
            }
        }

        data.appendError(MessageUtil.getMessage("err.invalid.database.group", database, databaseGroup));
        return false;
    }

    /**
     * TODO write JAVADOC
     *
     * @param data
     * @param criteria
     * @return
     * @throws ImportitException
     */
    private boolean searchDatabaseAndWriteInData(Data data, String criteria) throws ImportitException {

        int mode = EDPConstants.ENUMPOS_CODE;
        logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "searchDatabase"));
        EDPSession edpSession = edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
        EDPQuery query = edpSession.createQuery();

        try {
            edpSession.setEnumMode(mode);
            if (AbstractDataProcessing.isDatabaseAvailableAndWriteInData(data, criteria, query))
                return true;

            if (query.getRecordCount() > 1) {
                data.appendError(MessageUtil.getMessage("err.invalid.selection.notUnique", criteria, query.getRecordCount()));
            }

        } catch (InvalidQueryException e) {
            data.appendError(MessageUtil.getMessage("err.invalid.selection.criteria", criteria, e));
            return false;
        } catch (InvalidSettingValueException e) {
            logger.error(e);
            throw new ImportitException(MessageUtil.getMessage(""));
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

}
