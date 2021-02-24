package de.abas.infosystem.importit.testdata;

import de.abas.ceks.jedp.EDPVariableLanguage;

public class SmlDataset extends MinimumDataset{



    public SmlDataset(String number, String searchWord, Class<de.abas.erp.db.schema.company.SelectionTemplateEditor> editorClass) {
        super(number, searchWord, editorClass , EDPVariableLanguage.GERMAN);
    }
}
