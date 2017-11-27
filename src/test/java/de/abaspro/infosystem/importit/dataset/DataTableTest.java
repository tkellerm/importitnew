package de.abaspro.infosystem.importit.dataset;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.abaspro.infosystem.importit.ImportitException;

public class DataTableTest {

	@Test
	public void testDataTable() {
		DataTable dataTable = new DataTable();
		assertTrue(dataTable.isEmpty());
	}

	@Test
	public void testDataTableDataTable() {
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
