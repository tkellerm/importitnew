package de.abas.infosystem.importit.dataprocessing;

import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.datacheck.AbasDataCheckAndComplete;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

import java.util.List;

public class AbasDataProcessFactory {

    Logger logger = Logger.getLogger(AbasDataProcessFactory.class);

    public AbasDataProcessable createAbasDataProcess(EDPSessionHandler edpSessionHandler, List<Data> dataList)
            throws ImportitException {

        AbasDataProcessable abasDataProcessable = null;

        AbasDataCheckAndComplete aCheckAndComplete = new AbasDataCheckAndComplete(edpSessionHandler, dataList);

        // prüfe welcher Typ haben die Datensatz in der datalist

        if (aCheckAndComplete.checkAndCompleteDataList()) {
            switch (getProcessingType(dataList)) {
                case STANDARD:
                    abasDataProcessable = new AbasDataProcessingStandardObject(edpSessionHandler);
                    logger.info(MessageUtil.getMessage("info.abasDataFactory.Standard"));
                    break;

                case TYPECOMMAND:
                    abasDataProcessable = new AbasDataProcessingTypeCommands(edpSessionHandler);
                    logger.info(MessageUtil.getMessage("info.abasDataFactory.TypCommand"));
                    break;

                case CUSTOMER_PART_PROPERTIES:
                    abasDataProcessable = new AbasDataProcessingCustomerPartProperties(edpSessionHandler);
                    logger.info(MessageUtil.getMessage("info.abasDataFactory.CustomerProperties"));
                    break;

                case WAREHOUSE_GROUP_PROPERTIES:
                    abasDataProcessable = new AbasDataProzessingWarehouseGroupProperties(edpSessionHandler);
                    logger.info(MessageUtil.getMessage("info.abasDataFactory.WarehouseGroupProperties"));
                    break;
                default:
                    break;
            }
        }

        return abasDataProcessable;

    }

    protected DataProcessingType getProcessingType(List<Data> dataList) throws ImportitException {
        Data data = dataList.get(0);

        if (data == null) {
            throw new ImportitException(MessageUtil.getMessage("error.abasDataFactory.noDataRecords"));
        }

        if ((data.getDatabase() == null || data.getGroup() == null) && data.getTypeCommandString() == null) {
            throw new ImportitException(MessageUtil.getMessage("error.abasDataFactory.noDatabaseOrTypeCommand", data.getDatabase(), data.getGroup(), data.getDbGroupString()));
        }

        if (data.getDatabase() != null && data.getGroup() != null) {
            //TODO create CONSTANSTS instead of numbers
            if (checkDataForSpecificDatabaseInformation(data, 2, 6, 7))
                return DataProcessingType.CUSTOMER_PART_PROPERTIES;

            if (checkDataForSpecificDatabaseInformation(data, 39, 3, 4))
                return DataProcessingType.WAREHOUSE_GROUP_PROPERTIES;
        }

        if (data.getTypeCommandString() != null && !data.getTypeCommandString().isEmpty()) {
            return DataProcessingType.TYPECOMMAND;
        }

        return DataProcessingType.STANDARD;

    }

    private boolean checkDataForSpecificDatabaseInformation(Data data, int database, int databaseGroup, int typeCommand) {
        return data.getDatabase() == database && (data.getGroup() == databaseGroup || data.getGroup() == typeCommand);
    }

}
