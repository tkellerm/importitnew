package de.abas.infosystem.importit.dataprocessing;

import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataset.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AbasDataProcessFactoryTest {


	@Test
	final void testcheckTypDataList() {
		// Kundenartikeleigenschaften
		DataProcessingType result = DataProcessingType.CUSTOMER_PART_PROPERTIES;
		int database = 2;
		int group = 6;
		String typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		result = DataProcessingType.CUSTOMER_PART_PROPERTIES;
		database = 2;
		group = 7;
		typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Lagergruppeneigenschaften

		result = DataProcessingType.WAREHOUSE_GROUP_PROPERTIES;
		database = 39;
		group = 3;
		typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		result = DataProcessingType.WAREHOUSE_GROUP_PROPERTIES;
		database = 39;
		group = 4;
		typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Tippkommando
		result = DataProcessingType.TYPECOMMAND;
		database = 0;
		group = 0;
		typeCommandString = "Lbuchung";
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Tippkommando aber gleichzeitig einträge für
		// Kundenartikeleigenschaften
		result = DataProcessingType.CUSTOMER_PART_PROPERTIES;
		database = 2;
		group = 6;
		typeCommandString = "Lbuchung";
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Tippkommando aber gleichzeitig einträge für Lagergruppeneigenschaften, Datenbank hat Vorang vor TypeCommand
		result = DataProcessingType.WAREHOUSE_GROUP_PROPERTIES;
		database = 39;
		group = 3;
		typeCommandString = "Lbuchung";
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Sonstige Datenbanken
		result = DataProcessingType.STANDARD;
		database = 0;
		group = 1;
		typeCommandString = "";
		checkTypDataListFrame(result, database, group, typeCommandString);

	}

	private void checkTypDataListFrame(DataProcessingType result, int database, int group, String typeCommandString) {
		ArrayList<Data> dataList = new ArrayList<>();
		Data data = new Data();
		try {
			data.setDatabase(database);
			data.setGroup(group);
			data.setTypeCommandString(typeCommandString);
			dataList.add(data);
			AbasDataProcessFactory fact = new AbasDataProcessFactory();
			DataProcessingType value = fact.getProcessingType(dataList);
			assertThat(value, is(result));
		} catch (ImportitException e) {
			e.printStackTrace();
		}
	}

}
