package de.abas.infosystem.importit.dataprocessing;


import de.abas.infosystem.importit.dataprocessing.EDPUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
