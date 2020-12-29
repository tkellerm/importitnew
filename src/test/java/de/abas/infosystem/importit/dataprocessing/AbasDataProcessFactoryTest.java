package de.abas.infosystem.importit.dataprocessing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


import java.util.ArrayList;



import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataprocessing.AbasDataProcessFactory;
import de.abas.infosystem.importit.dataset.Data;
import org.junit.jupiter.api.Test;

public class AbasDataProcessFactoryTest {

	// @Test
	// public final void testCreateAbasDataProcess() {
	// fail("Not yet implemented"); // TODO
	// }

	@Test
	public final void testcheckTypDataList() {
		// Kundenartikeleigenschaften
		Integer result = 3;
		Integer database = 2;
		Integer group = 6;
		String typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		result = 3;
		database = 2;
		group = 7;
		typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Lagergruppeneigenschaften

		result = 4;
		database = 39;
		group = 3;
		typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		result = 4;
		database = 39;
		group = 4;
		typeCommandString = null;
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Tippkommando
		result = 2;
		database = 0;
		group = 0;
		typeCommandString = "Lbuchung";
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Tippkommando aber gleichzeitig einträge für
		// Kundenartikeleigenschaften
		result = 3;
		database = 2;
		group = 6;
		typeCommandString = "Lbuchung";
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Tippkommando aber gleichzeitig einträge für Lagergruppeneigenschaften
		result = 4;
		database = 39;
		group = 3;
		typeCommandString = "Lbuchung";
		checkTypDataListFrame(result, database, group, typeCommandString);

		// Sonstige Datenbanken
		result = 1;
		database = 0;
		group = 1;
		typeCommandString = "";
		checkTypDataListFrame(result, database, group, typeCommandString);

	}

	private void checkTypDataListFrame(int result, int database, int group, String typeCommandString) {
		ArrayList<Data> dataList = new ArrayList<Data>();
		Data data = new Data();
		try {
			data.setDatabase(database);
			data.setGroup(group);
			data.setTypeCommandString(typeCommandString);
			dataList.add(data);
			AbasDataProcessFactory fact = new AbasDataProcessFactory();
			int value = fact.checkTypDataList(dataList);
			assertThat(value, is(result));
		} catch (ImportitException e) {
			e.printStackTrace();
		}
	}

}
