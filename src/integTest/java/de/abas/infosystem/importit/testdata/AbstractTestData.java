package de.abas.infosystem.importit.testdata;

import de.abas.ceks.jedp.*;
import de.abas.erp.db.*;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.esdk.test.util.TestSetup;
import de.abas.infosystem.importit.ImportitException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractTestData<V extends EditorObject & SelectableObject & Deletable, D extends MinimumDataset<V>> implements TestDataInterface {

    protected List<D> dataRecordList = new ArrayList<>();


    public void importDataList(DbContext ctx) throws ImportitException, CantBeginSessionException, CantBeginEditException, CantChangeFieldValException, InvalidRowOperationException, CantSaveException, ServerActionException, CantReadFieldPropertyException {
        if (ctx == null) {
            ctx = TestSetup.createClientContext();
        }
        try {

            for (D record : dataRecordList) {
                try (EditorObject editor = ctx.newObject(record.getEditorClass())) {


                    List<Field> fieldListHead = record.getFieldListHead();
                    writeFieldsInEditor(editor, fieldListHead);

                    editor.commit();

                } catch (Exception e) {
                    fail(e);
                }
            }
        } finally {
            ctx.close();

        }
    }

    protected void writeFieldsInEditor(EditorObject editor, List<Field> fieldListHead) {
        for (Field field : fieldListHead) {
            editor.setString(field.getFieldName(), field.getValue());
        }
    }

    protected void writeFieldsInEditorEDP(EDPEditor editor, List<Field> fieldListHead , Integer rowNumber) throws CantChangeFieldValException {
        for (Field field : fieldListHead) {
            editor.setFieldVal( rowNumber , field.getFieldName() , field.getValue());
        }
    }

    public void deleteData(DbContext ctx) throws ImportitException {

        try (DbContext ctxdelete = TestSetup.createClientContext()) {
            for (D record : dataRecordList) {

                SelectionBuilder<V> selectionBuilder = SelectionBuilder.create(record.getEditorClass());

                selectionBuilder.add(Conditions.eq("idno", record.getNumber()));

                Query<V> query = ctxdelete.createQuery(selectionBuilder.build());

                for (EditorObject editor : query.execute()) {


                    try {
                        editor.open(EditorAction.DELETE);
                        editor.commit();
                    } catch (CommandException e) {
                        fail(e);
                    } finally {
                        editor.close();
                    }

                }
            }
        }

    }


}
