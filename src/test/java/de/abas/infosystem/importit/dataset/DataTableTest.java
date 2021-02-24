package de.abas.infosystem.importit.dataset;


import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.dataset.DataTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DataTableTest {

	@Test
	void testDataTable() {
		DataTable dataTable = new DataTable();
		assertTrue(dataTable.isEmpty());
	}

	@Test
	void testDataTableDataTable() {
		DataTable dataTable = new DataTable();
		try {
			DataTable dataTable2 = new DataTable(dataTable);
			assertTrue(dataTable2.isEmpty());
		} catch (ImportitException e) {
			// TODO: handle exception
		}
		// fail("Not yet implemented"); // TODO
	}



}
