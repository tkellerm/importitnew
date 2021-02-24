package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.*;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.DataTable;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.utils.MessageUtil;

import java.util.List;

public class AbasDataProzessingWarehouseGroupProperties extends AbstractDataProcessing {

    public static final String ARTIKEL = "artikel";
    public static final String PRODUCT = "product";

    public AbasDataProzessingWarehouseGroupProperties(EDPSessionHandler edpSessionHandler) {
        super(edpSessionHandler);
    }

    @Override
    protected void writeData(Data data) {

        try {
            writeWarehouseGroupProperties(data);
        } catch (Exception e) {
            logger.error(e);
            data.appendError(e);
        }
    }

    private void writeWarehouseGroupProperties(Data data) {

        EDPEditor edpEditor = null;
        data.setImported(false);

        logger.info(MessageUtil.getMessage("info.import.part.warehouse.props"));

        List<DataTable> tableRows = data.getTableRows();
        for (DataTable dataTable : tableRows) {

            try {
                String criteria = "";
                String objectId;
                if (dataTable.getAbasID().isEmpty()) {
                    criteria = getObjectSearchCriteria(dataTable, data);
                    objectId = getSelObject(criteria, data);
                } else {

                    objectId = dataTable.getAbasID();
                }

                if (!objectId.equals("Z")) {

                    edpEditor = getEPDEditorforObjectId(data, objectId);

                    final String[] ignoreFieldNames = {"art", ARTIKEL, PRODUCT, "lgruppe", "warehGrp"};
                    writeFieldsInEditor(data, dataTable, edpEditor, ignoreFieldNames);

                    edpEditor.saveReload();
                    String abasId = edpEditor.getEditRef();

                    logger.info(MessageUtil.getMessage("info.save.editor.new", data.getDatabase().toString(),
                            data.getGroup().toString(), abasId));
                    dataTable.setAbasID(abasId);

                    closeEdpEditor(data, edpEditor, abasId);
                } else {
                    data.appendError(MessageUtil.getMessage("err.selection.ambiguous", criteria));
                }

            } catch (Exception e) {
                logger.error(e);
                data.appendError(e);
            } finally {
                releaseAndFreeEDPEditor(edpEditor);
            }
        }

    }

    private String[] checkDataWarehouseGroupProperties(Data data) throws ImportitException {
        Integer database = data.getDatabase();
        Integer group = data.getGroup();


        String[] varNames = new String[2];
        if (database.equals(39) && (group.equals(2) || group.equals(4))) {
            throw new ImportitException(MessageUtil.getMessage("err.check.warehouse.props.falseDatabase", data.getDatabase(), data.getGroup()));
        }
        String varNameArt = getVariableNameArt(data);
        String varNameLGruppe = getVariableNameLGruppe(data);

        if (varNameArt.isEmpty() || varNameLGruppe.isEmpty()) {
            throw new ImportitException(MessageUtil.getMessage("err.check.warehouse.props.missingField"));
        } else{
            varNames[0] = varNameArt;
            varNames[1] = varNameLGruppe;
            return varNames;
        }

    }

    private String getVariableNameLGruppe(Data data) throws ImportitException {
        List<Field> tableFields = data.getTableFields();
        for (Field field : tableFields) {
            if (field.getName().equals("lgruppe") || field.getName().equals("warehGrp")) {
                return field.getName();
            }
        }
        throw new ImportitException(MessageUtil.getMessage("err.check.warehouse.props.warehouseGroup.not.found", data.getDatabase(), data.getGroup()));
    }

    private String getVariableNameArt(Data data) throws ImportitException {
        List<Field> headerFields = data.getHeaderFields();
        for (Field field : headerFields) {
            if (field.getName().equals("art") || field.getName().equals(ARTIKEL)
                    || field.getName().equals(PRODUCT))
                return field.getName();
        }
        throw new ImportitException(MessageUtil.getMessage("err.check.warehouse.props.article.not.found", data.getDatabase(), data.getGroup()));
    }


    @Override
    protected boolean checkDataStructure(Data data) {
        // Da die Datenbank fix ist bei den Lagergruppeneigenschaften
        data.setDatabase(39);
        data.setGroup(3);
        boolean validDb = checkDatabaseName(data);
        boolean validHead = false;
        boolean validTable = false;
        boolean existImportantFields = false;
        if (validDb) {
            List<Field> headerFields = data.getHeaderFields();
            List<Field> tableFields = data.getTableFields();
            try {

                validHead = checkWarehouseGroupProperties(headerFields, data.getOptionCode().useEnglishVariables());
                validTable = checkFieldList(tableFields, 39, 3, false, data.getOptionCode().useEnglishVariables());
                String[] checkDataCustomerPartProperties = checkDataWarehouseGroupProperties(data);
                existImportantFields = checkDataCustomerPartProperties.length == 2;

            } catch (ImportitException e) {
                logger.error(e);
                data.appendError(MessageUtil.getMessage("err.structure.check", e.getMessage()));
            }
        }
        return validTable && validHead && validDb && existImportantFields;
    }

    private boolean checkWarehouseGroupProperties(List<Field> headerFields, Boolean englishVariables)
            throws ImportitException {
        if (headerFields.size() == 1) {
            for (Field field : headerFields) {
                String varName = field.getName();
                if (varName.equals(PRODUCT) || varName.equals("art") || varName.equals(ARTIKEL)) {
                    return checkFieldList(headerFields, 39, 3, false, englishVariables);
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

        String[] varNames = checkDataWarehouseGroupProperties(data);

        String artField = varNames[0];
        String lgruppeField = varNames[1];
        return artField + "=" + data.getValueOfHeadField(artField) + ";" + lgruppeField + "="
                + datatable.getTableFieldValue(lgruppeField);
    }
}
