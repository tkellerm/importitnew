package de.abaspro.infosystem.importit.dataset;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.abaspro.infosystem.importit.ImportOptions;

public class FieldTest {

	@Test
	public void testExtractSelectionString() {

		String inputTest1 = "@selection='$,,nummer={0};@sort=Nummer'";
		String outputTest1 = "nummer={0};@sort=Nummer";

		String inputTest2 = "@selection='%,,nummer={0};@sort=Nummer'";
		String outputTest2 = "nummer={0};@sort=Nummer";

		String inputTest3 = "@selection='$,nummer={0};@sort=Nummer'";
		String outputTest3 = "nummer={0};@sort=Nummer";

		String resultTest1 = Field.extractSelectionString(inputTest1, ImportOptions.SELECTION);
		String resultTest2 = Field.extractSelectionString(inputTest2, ImportOptions.SELECTION);
		String resultTest3 = Field.extractSelectionString(inputTest3, ImportOptions.SELECTION);
		assertEquals(outputTest1, resultTest1);
		assertEquals(outputTest2, resultTest2);
		assertEquals(outputTest3, resultTest3);

		String inputTest4 = "@keyselection='$,,nummer={0};@sort=Nummer'";
		String outputTest4 = "nummer={0};@sort=Nummer";

		String inputTest5 = "@keyselection='%,,nummer={0};@sort=Nummer'";
		String outputTest5 = "nummer={0};@sort=Nummer";

		String inputTest6 = "@keyselection='$,nummer={0};@sort=Nummer'";
		String outputTest6 = "nummer={0};@sort=Nummer";

		String resultTest4 = Field.extractSelectionString(inputTest4, ImportOptions.KEYSELECTION);
		String resultTest5 = Field.extractSelectionString(inputTest5, ImportOptions.KEYSELECTION);
		String resultTest6 = Field.extractSelectionString(inputTest6, ImportOptions.KEYSELECTION);
		assertEquals(outputTest4, resultTest4);
		assertEquals(outputTest5, resultTest5);
		assertEquals(outputTest6, resultTest6);

	}

	@Test
	public void testExtractValue() {

		String inputtest1 = "nummer@key";
		String outputtest1 = "nummer";

		String inputtest2 = "nummer@key@test";
		String outputtest2 = "nummer";

		String inputtest3 = "@keyselection='$,,nummmer={0};";
		String outputtest3 = "";

		String resultTest1 = Field.extractValue(inputtest1);
		String resultTest2 = Field.extractValue(inputtest2);
		String resultTest3 = Field.extractValue(inputtest3);

		assertEquals(outputtest1, resultTest1);
		assertEquals(outputtest2, resultTest2);
		assertEquals(outputtest3, resultTest3);

	}

}
