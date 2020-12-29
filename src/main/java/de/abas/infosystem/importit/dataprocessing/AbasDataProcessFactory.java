package de.abas.infosystem.importit.dataprocessing;

import java.util.ArrayList;

import de.abas.utils.Util;
import org.apache.log4j.Logger;

import de.abas.infosystem.importit.AbasDataCheckAndComplete;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataset.Data;

public class AbasDataProcessFactory {

	Logger logger = Logger.getLogger(AbasDataProcessFactory.class);

	public AbasDataProcessable createAbasDataProcess(EDPSessionHandler edpSessionHandler, ArrayList<Data> dataList)
			throws ImportitException {

		AbasDataProcessable abasDataProcessable = null;

		AbasDataCheckAndComplete aCheckAndComplete = new AbasDataCheckAndComplete(edpSessionHandler, dataList);

		// pruefe welcher datensatz ist datalist

		if (aCheckAndComplete.checkandcompleteDataList()) {
			switch (checkTypDataList(dataList)) {
			case 1:
				// Standard Object
				abasDataProcessable = new AbasDataProcessingStandardObject(edpSessionHandler);
				logger.info(Util.getMessage("info.abasDataFactory.Standard"));
				break;
			case 2:
				// Typcommando
				abasDataProcessable = new AbasDataProcessingTypeCommands(edpSessionHandler);
				logger.info(Util.getMessage("info.abasDataFactory.TypCommand"));
				break;
			case 3:
				// CustomerPartProperties
				abasDataProcessable = new AbasDataProzessingCustomerPartProperties(edpSessionHandler);
				logger.info(Util.getMessage("info.abasDataFactory.CustomerProperties"));
				break;
			case 4:
				// WarehouseGroupProperties
				abasDataProcessable = new AbasDataProzessingWarehouseGroupProperties(edpSessionHandler);
				logger.info(Util.getMessage("info.abasDataFactory.WarehouseGroupProperties"));
				break;
			default:
				break;
			}
		}

		return abasDataProcessable;

	}

	protected int checkTypDataList(ArrayList<Data> dataList) throws ImportitException {

		// 1 : standard
		// 2 : Typcommand
		// 3 : CustomerPartProperties
		// 4 : WarehouseGroupProperties

		Data data = dataList.get(0);

		if (data != null) {
			if ((data.getDatabase() != null && data.getGroup() != null) || data.getTypeCommandString() != null) {
				if (data.getDatabase() != null && data.getGroup() != null) {

					if (data.getDatabase() == 2 && (data.getGroup() == 6 || data.getGroup() == 7)) {
						return 3;
					}

					if (data.getDatabase() == 39 && (data.getGroup() == 3 || data.getGroup() == 4)) {
						return 4;
					}
				}
				if (data.getTypeCommandString() != null) {
					if (!data.getTypeCommandString().isEmpty()) {
						return 2;
					}

				}
				return 1;
			} else {
				throw new ImportitException(" Database ist " + data.getDatabase() + "Gruppe ist " + data.getGroup()
						+ " sind null :" + data.getDbGroupString());
			}

		} else {
			throw new ImportitException(" Daten sind null");
		}

	}

}
