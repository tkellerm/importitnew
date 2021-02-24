package de.abas.infosystem.importit.testdata;

import de.abas.ceks.jedp.*;
import de.abas.erp.db.DbContext;
import de.abas.infosystem.importit.ImportitException;

public interface TestDataInterface {

    void importDataList(DbContext ctx) throws ImportitException, CantBeginSessionException, CantBeginEditException, CantChangeFieldValException, InvalidRowOperationException, CantSaveException, ServerActionException, CantReadFieldPropertyException;

    void deleteData(DbContext ctx) throws ImportitException;

}
