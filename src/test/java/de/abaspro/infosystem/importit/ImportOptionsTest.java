package de.abaspro.infosystem.importit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImportOptionsTest {

	@Test
	public void testImportOptions() {

		ImportOptions[] values = ImportOptions.values();
		int length = values.length;
		assertEquals(length, 7);

	}

	@Test
	public void testToString() {

		assertEquals(ImportOptions.DONT_CHANGE_IF_EQUAL.toString(), "@dontChangeIfEqual");
		assertEquals(ImportOptions.SKIP.toString(), "@skip");
		assertEquals(ImportOptions.KEY.toString(), "@Schlüssel");
		assertEquals(ImportOptions.MODIFIABLE.toString(), "@modifiable");
		assertEquals(ImportOptions.NOTEMPTY.toString(), "@notempty");
		assertEquals(ImportOptions.SELECTION.toString(), "@selection");
		assertEquals(ImportOptions.KEYSELECTION.toString(), "@keyselection");
	}

}
