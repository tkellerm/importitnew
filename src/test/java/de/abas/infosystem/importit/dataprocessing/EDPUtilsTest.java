package de.abas.infosystem.importit.dataprocessing;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

 class EDPUtilsTest {

	@Test
	 void testReplaceAblank() {
		String input = "A Stanzen";
		String output = "Stanzen";
		String result = EDPUtils.replaceCharPlusBlank("A", input);

		assertEquals(output, result);

		input = "A STANZEN";
		output = "STANZEN";
		result = EDPUtils.replaceCharPlusBlank("A", input);
		assertEquals(output, result);

		input = "A  STANZEN";
		output = " STANZEN";
		result = EDPUtils.replaceCharPlusBlank("A", input);
		assertEquals(output, result);
	}

}
