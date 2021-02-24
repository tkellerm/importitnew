package de.abas.infosystem.importit.datacheck;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

public class FillingTypeCommandsData {

    private static final Logger logger = Logger.getLogger(FillingTypeCommandsData.class);
    public static final String DEBUG_GETEDPSESSION = "debug.getedpsession";

    private FillingTypeCommandsData() {
        throw new IllegalStateException("Utility class");
    }

    public static void fillTypeCommandData(Data data) throws ImportitException {

        EnumerationItem typeCommandMeta = null;
        if (data.getTypeCommand() == null && data.getTypeCommandString() != null) {
            typeCommandMeta = checkTypeCommandString(data.getTypeCommandString());
        }
        if (data.getTypeCommand() != null) {
            typeCommandMeta = EnumTypeCommand.getInstance().searchItem(data.getTypeCommand());
        }
        if (typeCommandMeta != null) {
            if (typeCommandMeta.getDatabase() == 0 && typeCommandMeta.getDatabaseGroup() == 0) {
                // TODO move to SearchItem in Class EnumTypeCommand
                DatabaseDataset databaseDataset = findDatabaseForTypeCommand(typeCommandMeta.getNumber());
                typeCommandMeta.setDatabase(databaseDataset.database);
                typeCommandMeta.setDatabaseGroup(databaseDataset.databaseGroup);
            }
            data.setDatabase(typeCommandMeta.getDatabase());
            data.setGroup(typeCommandMeta.getDatabaseGroup());
        }
    }


    private static EnumerationItem checkTypeCommandString(String typeCommandString) {
        Enumeration enumeration = EnumTypeCommand.getInstance();
        if (!enumeration.getListOfEnumItems().isEmpty()) {

            return enumeration.searchItem(typeCommandString);
        }
        return null;
    }

    private static DatabaseDataset findDatabaseForTypeCommand(int typeCommandNumber) throws ImportitException {

        logger.debug(MessageUtil.getMessage(DEBUG_GETEDPSESSION, "findDatabaseForTypeCommand"));
        EDPSessionHandler edpSessionHandler = EDPSessionHandler.getInstance();
        EDPSession localedpSession = edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
        DatabaseDataset databaseDataset = new DatabaseDataset();

        EDPEditor edpEditor = localedpSession.createEditor();
        try {
            edpEditor.beginEditCmd(String.valueOf(typeCommandNumber), "");
            databaseDataset.database = edpEditor.getEditDatabaseNr();
            databaseDataset.databaseGroup = edpEditor.getEditGroupNr();
            edpEditor.endEditCancel();
        } catch (CantBeginEditException e) {
            throw new ImportitException(MessageUtil.getMessage("err.getting.database", typeCommandNumber, e));
        } finally {
            if (edpEditor.isActive()) {
                edpEditor.endEditCancel();
            }
            edpSessionHandler.freeEDPSession(localedpSession);
        }

        return databaseDataset;
    }

//TODO  Make a real Class from this
    private static class DatabaseDataset {
        protected int database;
        protected int databaseGroup;
    }
}
