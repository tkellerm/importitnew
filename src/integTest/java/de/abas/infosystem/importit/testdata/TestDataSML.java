package de.abas.infosystem.importit.testdata;

import de.abas.ceks.jedp.*;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.company.SelectionTemplateEditor;
import de.abas.esdk.test.util.TestSetup;
import de.abas.infosystem.importit.ImportitException;

import java.util.List;

import static de.abas.infosystem.importit.util.TestProperties.*;

public class TestDataSML extends AbstractTestData{


    public TestDataSML() {
        SmlDataset smlData = new SmlDataset("3008", "SMLTEST", SelectionTemplateEditor.class);

        List<Row> smlRowList = smlData.getRowList();
        Row row = new Row();
        List<Field> rowFieldList = row.getFieldList();
        rowFieldList.add(new Field("vname", "qlang"));
        rowFieldList.add(new Field("zusatzart", "R6.2"));
        rowFieldList.add(new Field("benenn", "Length"));
        smlRowList.add(row);
        Row row2 = new Row();
        List<Field> rowFieldList2 = row2.getFieldList();
        rowFieldList2.add(new Field("vname", "qdurch"));
        rowFieldList2.add(new Field("zusatzart", "R6.2"));
        rowFieldList2.add(new Field("benenn", "Diameter"));
        smlRowList.add(row2);
        dataRecordList.add(smlData);
    }

    @Override
    public void importDataList(DbContext ctx) throws ImportitException, CantBeginSessionException, CantBeginEditException, CantChangeFieldValException, InvalidRowOperationException, CantSaveException, ServerActionException, CantReadFieldPropertyException {

        if (ctx == null) {
            ctx = TestSetup.createClientContext();
        }
        try {
            for (Object record : dataRecordList) {
                if (record instanceof MinimumDataset) {
                    MinimumDataset<?> miniRecord = (MinimumDataset) record;

                    if (miniRecord.getEditorClass().equals(SelectionTemplateEditor.class)) {
                        EDPSession edpSession = null;
                        try {
                            edpSession = EDPFactory.createEDPSession();

                            writeSML(miniRecord, edpSession);
                        }finally {
                            if (edpSession != null) {
                                edpSession.setVariableLanguage(EDPVariableLanguage.ENGLISH);
                                edpSession.release();
                            }

                        }
                    } else {
                        super.importDataList(ctx);
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw e;

        }
        finally {
            ctx.close();


        }

    }

    private void writeSML(MinimumDataset<?> miniRecord, EDPSession edpSession) throws CantBeginSessionException, ImportitException, CantBeginEditException, CantChangeFieldValException, InvalidRowOperationException, CantSaveException, CantReadFieldPropertyException, ServerActionException {
        edpSession.beginSession(loadProperties().get(EDP_HOST),
                Integer.valueOf(loadProperties().get(EDP_PORT)),
                loadProperties().get(EDP_CLIENT),
                loadProperties().get(EDP_PASSWORD),
                "testSMLDAtA");
        edpSession.setVariableLanguage(EDPVariableLanguage.GERMAN);
        EDPEditor edpEditor = edpSession.createEditor();
        edpEditor.beginEditCommandString("<(Company)> <(New)>, (CharacteristicsBar)");
        writeFieldsInEditorEDP(edpEditor, miniRecord.getFieldListHead(), 0);
        int rowNumber = 0;
        for (Row row : miniRecord.getRowList()) {
            List<Field> rowFieldList = row.getFieldList();
            rowNumber++;
            edpEditor.insertRow(rowNumber);
            writeFieldsInEditorEDP(edpEditor, rowFieldList, rowNumber);
        }
        edpEditor.saveReload();
        System.out.println("Sachmerkmalsleiste " + edpEditor.getFieldVal(0, "nummer") + " angelegt");
        edpEditor.endEditSave();
        edpSession.setVariableLanguage(EDPVariableLanguage.ENGLISH);
        if (edpEditor.isActive()) {
            edpEditor.release();
        }
    }


}
