package de.abaspro.infosystem.importit.dataprocessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EDPUtilsTest {

	@Test
	public void testReplaceAblank() {
		String input = "A Stanzen";
		String output = "Stanzen";
		String result = EDPUtils.ReplaceCharPlusBlank("A", input);

		assertEquals(output, result);

		input = "A STANZEN";
		output = "STANZEN";
		result = EDPUtils.ReplaceCharPlusBlank("A", input);
		assertEquals(output, result);

		input = "A  STANZEN";
		output = " STANZEN";
		result = EDPUtils.ReplaceCharPlusBlank("A", input);
		assertEquals(output, result);
	}

}
