package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.*;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.DataTable;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

public class AbasDataProcessingCustomerPartProperties extends AbstractDataProcessing {

    public static final String ARTIKEL = "artikel";
    public static final String PRODUCT = "product";

    public AbasDataProcessingCustomerPartProperties(EDPSessionHandler edpSessionHandler) {
        super(edpSessionHandler);
    }

    @Override
    protected void writeData(Data data) {

        try {
            writeCustomerPartProperties(data);
        } catch (Exception e) {
            logger.error(e);
            data.appendError(e);
        }
    }

    private void writeCustomerPartProperties(Data data)
            throws ImportitException, CantBeginEditException,
            InvalidRowOperationException, CantSaveException {

        EDPEditor edpEditor = null;

        data.setImported(false);

        logger.info(MessageUtil.getMessage("info.import.cust.part.props"));

        List<DataTable> tableRows = data.getTableRows();
        for (DataTable dataTable : tableRows) {

            try {
                String criteria = "";
                String objectId;
                // check for AbasID=0 because if you import the same line of the CustomerPartProperties
                // as a few steps before the it want to insert a new line instead of update the existing Line
                if (dataTable.getAbasID().isEmpty() || dataTable.getAbasID().equals(String.valueOf(0))) {
                    criteria = getObjectSearchCriteria(dataTable, data);
                    objectId = getSelObject(criteria, data);
                } else {

                    objectId = dataTable.getAbasID();
                }
                logger.debug("found ObjectID " + objectId);
                if (!objectId.equals("Z")) {

                    edpEditor = getEPDEditorforObjectId(data, objectId);

                    final String[] ignoredFieldNames = {"art", ARTIKEL, PRODUCT, "kl", "custVendor"};
                    writeFieldsInEditor(data, dataTable, edpEditor, ignoredFieldNames);

                    edpEditor.saveReload();
                    String abasId = edpEditor.getEditRef();
                    logger.info(MessageUtil.getMessage("info.save.editor.new", data.getDatabase().toString(),
                            data.getGroup().toString(), abasId));
                    dataTable.setAbasID(abasId);
                    edpEditor.endEditSave();
                    closeEdpEditor(data, edpEditor, abasId);
                } else {
                    data.appendError(MessageUtil.getMessage("err.selection.ambiguous", criteria));
                }

            } catch (Exception e) {
                logger.error(e);
                data.appendError(MessageUtil.getMessage("error.save.CustomerPartProperties", e.getMessage(), Arrays.toString(e.getStackTrace())));

            } finally {
                if (edpEditor != null) {
                    if (edpEditor.isActive()) {
                        edpEditor.endEditCancel();
                    }
                    EDPUtils.releaseEDPEditor(edpEditor, logger);
                }
            }
        }

    }


    private String[] checkDataCustomerPartProperties(Data data) throws ImportitException {
        Integer database = data.getDatabase();
        Integer group = data.getGroup();

        String varNameArt = "";
        String varNameKl = "";
        String[] varNames = new String[2];
        // TODO check if this check is necessary
        // Export in Util.class
        if (database == 2 && (group == 6 || group == 7)) {
            varNameArt = getVarNameArt(data, varNameArt);
            varNameKl = getVarNameKl(data, varNameKl);
            if (!varNameArt.isEmpty() && !varNameKl.isEmpty()) {
                varNames[0] = varNameArt;
                varNames[1] = varNameKl;
                return varNames;
            } else {
                throw new ImportitException(MessageUtil.getMessage("err.check.CostumerPartProperties.missingField"));
            }

        }
        throw new ImportitException(
                MessageUtil.getMessage("err.check.CostumerPartProperties.falseDatabase", data.getDatabase(), data.getGroup()));
    }

    private String getVarNameKl(Data data, String varNameKl) {
        List<Field> tableFields = data.getTableFields();
        for (Field field : tableFields) {
            //TODO create Constants for Strings create extra class for Constants example Fieldnames
            if (field.getName().equals("kl") || field.getName().equals("custVendor")) {
                varNameKl = field.getName();
            }
        }
        return varNameKl;
    }

    private String getVarNameArt(Data data, String varNameArt) {
        List<Field> headerFields = data.getHeaderFields();
        for (Field field : headerFields) {
            //TODO create Constants for Strings create extra class for Constants example Fieldnames and Databas and Groups
            if (field.getName().equals("art") || field.getName().equals(ARTIKEL)
                    || field.getName().equals(PRODUCT)) {
                varNameArt = field.getName();
            }
        }
        return varNameArt;
    }


    @Override
    protected boolean checkDataStructure(Data data) throws ImportitException {
        // Da die Datenbank fix ist bei den Kundenartikeleigenschaften
        data.setDatabase(2);
        data.setGroup(6);
        boolean validDb = checkDatabaseName(data);
        if (!validDb) {
            return false;
        }

        boolean validHead = false;
        boolean validTable = false;
        boolean existImportantFields = false;

        List<Field> headerFields = data.getHeaderFields();
        List<Field> tableFields = data.getTableFields();

        try {

            validHead = checkCustomerPartProperties(headerFields, data.getOptionCode().useEnglishVariables());
            validTable = checkFieldList(tableFields, 2, 6, false, data.getOptionCode().useEnglishVariables());
            String[] checkDataCustomerPartProperties = checkDataCustomerPartProperties(data);
            existImportantFields = checkDataCustomerPartProperties.length == 2;

        } catch (ImportitException e) {
            logger.error(e);
            data.appendError(MessageUtil.getMessage("err.structure.check", e.getMessage()));
            return false;
        }

        return validTable && validHead && existImportantFields;
    }

    private boolean checkCustomerPartProperties(List<Field> headerFields, Boolean englishVariables)
            throws ImportitException {
        if (headerFields.size() == 1) {
            for (Field field : headerFields) {
                String varName = field.getName();
                if (varName.equals(PRODUCT) || varName.equals("art") || varName.equals(ARTIKEL)) {
                    return checkFieldList(headerFields, 2, 6, false, englishVariables);
                }
            }

            throw new ImportitException(MessageUtil.getMessage("err.variables.missing"));
        } else {
            throw new ImportitException(MessageUtil.getMessage("err.too.many.head.fields"));
        }
    }

    @Override
    protected void writeAbasIDinData(Data data) {
        try {
            List<DataTable> tableRows = data.getTableRows();
            for (DataTable dataTable : tableRows) {
                String criteria = getObjectSearchCriteria(dataTable, data);
                String abasID = getSelObject(criteria, data);
                dataTable.setAbasID(abasID);
            }
        } catch (ImportitException e) {
            data.appendError(e);
        }
    }

    private String getObjectSearchCriteria(DataTable datatable, Data data) throws ImportitException {

        String[] varNames = checkDataCustomerPartProperties(data);

        String artField = varNames[0];
        String klField = varNames[1];

        return artField + "=" + data.getValueOfHeadField(artField) + ";" + klField + "="
                + datatable.getTableFieldValue(klField) + ";@sort=Kundenartikeleigenschaften";
    }
}
