package de.abas.infosystem.importit.testdata;


import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.erp.db.Deletable;
import de.abas.erp.db.EditorObject;
import de.abas.erp.db.SelectableObject;
import de.abas.infosystem.importit.ImportitException;

import java.util.ArrayList;
import java.util.List;

public class MinimumDataset<T extends EditorObject & SelectableObject & Deletable> {

    private static final String FIELDNAME_NUMBER_ENGLISH = "idno";
    private static final String FIELDNAME_NUMBER_GERMAN = "nummer";
    private static final String FIELDNAME_SEARCHWORD_ENGLISH = "swd";
    private static final String FIELDNAME_SEARCHWORD_GERMAN = "such";
    private final List<Field> fieldListHead = new ArrayList<>();
    private final List<Row> rowList= new ArrayList<>();
    private final Class<T> editorClass;


    public MinimumDataset(String number, String searchWord, Class<T> editorClass, EDPVariableLanguage edpVariableLanguage) {
        this.editorClass = editorClass;
        if (edpVariableLanguage.equals(EDPVariableLanguage.ENGLISH)) {
            fieldListHead.add(new Field(FIELDNAME_NUMBER_ENGLISH, number));
            fieldListHead.add(new Field(FIELDNAME_SEARCHWORD_ENGLISH, searchWord));
        }else {
            fieldListHead.add(new Field(FIELDNAME_NUMBER_GERMAN, number));
            fieldListHead.add(new Field(FIELDNAME_SEARCHWORD_GERMAN, searchWord));
        }

    }



    public List<Field> getFieldListHead() {
        return fieldListHead;
    }



    public List<Row> getRowList() {
        return rowList;
    }

    public Class<T> getEditorClass() {
        return editorClass;
    }

    public CharSequence getNumber() throws ImportitException {
        for (Field field: fieldListHead) {
            if (field.getFieldName().equals(FIELDNAME_NUMBER_ENGLISH) ||
                    field.getFieldName().equals(FIELDNAME_NUMBER_GERMAN)){
                return field.getValue();
            }
        }
        throw new ImportitException("Error no field with name idno in List");
    }

}
