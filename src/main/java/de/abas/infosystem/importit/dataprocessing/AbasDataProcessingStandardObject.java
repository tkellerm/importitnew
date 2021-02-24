package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.*;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.SmlField;
import de.abas.infosystem.importit.SmlTab;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.utils.MessageUtil;

import java.text.MessageFormat;
import java.util.List;

public class AbasDataProcessingStandardObject extends AbstractDataProcessing {

    public AbasDataProcessingStandardObject(EDPSessionHandler edpSessionHandler) {
        super(edpSessionHandler);
    }

    @Override
    protected void writeData(Data data) {
        logger.debug(MessageUtil.getMessage("debug.getedpsession", "writeData"));

        try {
            writeDatabase(data);
        } catch (Exception e) {
            logger.error(e);
            data.appendError(e);
        }
    }

    private void writeDatabase(Data data) throws ImportitException, CantSaveException, InvalidRowOperationException {
        EDPEditor edpEditor = null;

        try {
            data.setImported(false);
            data.initOptions();

            if (data.getOptionCode().getAlwaysNew()) {

                logger.info(MessageUtil.getMessage("info.start.editor.new", data.getDatabase().toString(),
                        data.getGroup().toString()));

                edpEditor = createEDPEditorNew(data.getDatabase().toString(), data.getGroup().toString(),
                        data.getEDPLanguage(), data);

                writeFieldsInEditor(data, edpEditor);

                edpEditor.saveReload();
                String abasId = edpEditor.getEditRef();
                logger.info(MessageUtil.getMessage("info.save.editor.new", data.getDatabase().toString(),
                        data.getGroup().toString(), abasId));
                data.setAbasId(abasId);
                edpEditor.endEditSave();
                closeEdpEditor(data, edpEditor, abasId);
            } else {
                String objectId = getObjectId(data);

                if (!objectId.equals("Z")) {

                    edpEditor = getEPDEditorforObjectId(data, objectId);

                    writeFieldsInEditor(data, edpEditor);
                    edpEditor.saveReload();
                    String abasId = edpEditor.getEditRef();
                    logger.info(MessageUtil.getMessage("info.save.editor.new", data.getDatabase().toString(),
                            data.getGroup().toString()));
                    data.setAbasId(abasId);
                    edpEditor.endEditCancel();
                    closeEdpEditor(data, edpEditor, abasId);
                }
            }
        } catch (CantBeginEditException | CantReadStatusError e) {
            logger.error(e);
        } finally {
            if (edpEditor != null && (edpEditor.isActive())) {
                edpEditor.endEditCancel();
                releaseAndFreeEDPEditor(edpEditor);
            }

        }
    }

    private String getObjectId(Data data) throws ImportitException {
        String criteria = "";
        String objectId;
        if (data.getAbasId().isEmpty()) {
            if (!data.getNameOfKeyField().isEmpty()) {
                criteria = data.getNameOfKeyField() + "=" + data.getValueOfKeyField();
            } else {
                criteria = MessageFormat.format(data.getFieldKeySelectionString(), data.getKeyFieldValue());
            }
            objectId = getSelObject(criteria, data);
        } else {
            objectId = data.getAbasId();
        }
        if (objectId.equals("Z")) {
            data.appendError(MessageUtil.getMessage("err.selection.ambiguous", criteria));
        }
        return objectId;
    }

    @Override
    protected boolean checkDataStructure(Data data) throws ImportitException {
        boolean validDb = checkDatabaseName(data);
        boolean validHead = false;
        boolean validTable = false;
        boolean validSML = false;
        if (!validDb) {
            return false;
        }
        List<Field> headerFields = data.getHeaderFields();
        List<Field> tableFields = data.getTableFields();
        try {

            validHead = checkFieldList(headerFields, data.getDatabase(), data.getGroup(), false,
                    data.getOptionCode().useEnglishVariables());
            validTable = checkFieldList(tableFields, data.getDatabase(), data.getGroup(), true,
                    data.getOptionCode().useEnglishVariables());
            if (data.getSmlFields() != null) {
                validSML = checkSMLStructure(data);
            } else {
                validSML = true;
            }

        } catch (ImportitException e) {
            logger.error(e);
            data.appendError(MessageUtil.getMessage("err.structure.check", e));
            return false;
        }

        return validTable && validHead && validSML;
    }

    private boolean checkSMLStructure(Data data) throws ImportitException {
        boolean foundError = false;
        String numberSML = data.getSmlString();

        List<Field> smlFields = data.getSmlFields();
        if (!smlFields.isEmpty() && (numberSML == null || numberSML.isEmpty())) {
            data.appendError(MessageUtil.getMessage("error.smlnumber.not.found"));
            return false;
        }
        SmlTab smlTab = new SmlTab(this.edpSessionHandler, numberSML);

        for (Field field : smlFields) {
            //substring(2) because of the fieldname starts with "S." or "s."
            //TODO move substring to checkSmlTab
            SmlField smlField = smlTab.checkSmlTab(field.getName().substring(2));
            if (smlField == null) {
                data.appendError(MessageUtil.getMessage("err.field.not.found", field.getName().substring(2)));
                foundError = true;
            } else {
                field.setAbasType(smlField.getType());
            }
        }
        return !foundError;
    }

    @Override
    protected void writeAbasIDinData(Data data) throws ImportitException {
        String criteria = null;
        String keyOfKeyField = data.getKeyOfKeyField();

        if (!data.getSelectionStringOfKeyField().isEmpty()) {
            criteria = MessageFormat.format(data.getSelectionStringOfKeyField(), data.getValueOfKeyField());

        } else if (!keyOfKeyField.isEmpty()) {

            criteria = data.getNameOfKeyField() + "=" + data.getValueOfKeyField();

        }

        if (criteria != null) {
            makeSelection(data, criteria);
        }

    }

    private void makeSelection(Data data, String criteria) {
        try {
            String abasID = getSelObject(criteria, data);
            if (!abasID.equals("Z") && !abasID.equals("0")) {
                data.setAbasId(abasID);
            } else {
                data.setAbasId("");
                if (abasID.equals("Z")) {
                    data.appendError(MessageUtil.getMessage("error.checkdata.toManyResults"));
                }
            }
        } catch (ImportitException e) {
            data.appendError(e);
        }
    }

}
