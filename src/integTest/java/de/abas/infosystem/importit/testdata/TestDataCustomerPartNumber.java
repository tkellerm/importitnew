package de.abas.infosystem.importit.testdata;

import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.db.schema.part.ProductEditor;

public class TestDataCustomerPartNumber extends AbstractTestData{

    public TestDataCustomerPartNumber() {
        dataRecordList.add(new MinimumDataset("1TEST001" , "TEST001", CustomerEditor.class, EDPVariableLanguage.ENGLISH));
        dataRecordList.add(new MinimumDataset("1TEST002" , "TEST002", CustomerEditor.class, EDPVariableLanguage.ENGLISH));
        dataRecordList.add(new MinimumDataset("1TPROD001" , "TPROD001", ProductEditor.class, EDPVariableLanguage.ENGLISH));
        dataRecordList.add(new MinimumDataset("1TPROD002" , "TPROD002", ProductEditor.class, EDPVariableLanguage.ENGLISH));
        dataRecordList.add(new MinimumDataset("1TPROD003" , "TPROD003", ProductEditor.class, EDPVariableLanguage.ENGLISH));
    }






}
