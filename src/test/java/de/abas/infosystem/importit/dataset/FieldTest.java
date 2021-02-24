package de.abas.infosystem.importit.dataset;


import de.abas.infosystem.importit.ImportOptions;
import de.abas.infosystem.importit.OptionCode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTest {

    @Test
    void testExtractSelectionString() {

        String testInput1 = "@selection='$,,nummer={0};@sort=Nummer'";
        String testOutputTest1 = "nummer={0};@sort=Nummer";

        String testInput2 = "@selection='%,,nummer={0};@sort=Nummer'";
        String testOutputTest2 = "nummer={0};@sort=Nummer";

        String testInput3 = "@selection='$,nummer={0};@sort=Nummer'";
        String testOutputTest3 = "nummer={0};@sort=Nummer";

        Field field1 = new Field(testInput1, true, 0, new OptionCode(0));
        String resultTest1 = field1.extractSelectionString(ImportOptions.SELECTION);

        Field field2 = new Field(testInput2, true, 0, new OptionCode(0));
        String resultTest2 = field2.extractSelectionString(ImportOptions.SELECTION);

        Field field3 = new Field(testInput3, true, 0, new OptionCode(0));
        String resultTest3 = field3.extractSelectionString(ImportOptions.SELECTION);
        assertEquals(testOutputTest1, resultTest1);
        assertEquals(testOutputTest2, resultTest2);
        assertEquals(testOutputTest3, resultTest3);

        String testInput4 = "@keyselection='$,,nummer={0};@sort=Nummer'";
        String outputTest4 = "nummer={0};@sort=Nummer";

        String testInput5 = "@keyselection='%,,nummer={0};@sort=Nummer'";
        String outputTest5 = "nummer={0};@sort=Nummer";

        String testInput6 = "@keyselection='$,nummer={0};@sort=Nummer'";
        String outputTest6 = "nummer={0};@sort=Nummer";

        String testInput7 = "@keyselection='nummer='{0};@sort=Nummer'";
        String outputTest7 = "nummer='{0};@sort=Nummer";

        Field field4 = new Field(testInput4, true, 0, new OptionCode(0));
        Field field5 = new Field(testInput5, true, 0, new OptionCode(0));
        Field field6 = new Field(testInput6, true, 0, new OptionCode(0));
        Field field7 = new Field(testInput7, true, 0, new OptionCode(0));
        String resultTest4 = field4.extractSelectionString(ImportOptions.KEYSELECTION);
        String resultTest5 = field5.extractSelectionString(ImportOptions.KEYSELECTION);
        String resultTest6 = field6.extractSelectionString(ImportOptions.KEYSELECTION);
        String resultTest7 = field7.extractSelectionString(ImportOptions.KEYSELECTION);
        assertEquals(outputTest4, resultTest4);
        assertEquals(outputTest5, resultTest5);
        assertEquals(outputTest6, resultTest6);
        assertEquals(outputTest7, resultTest7);

    }

    @Test
    void testExtractValue() {

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

    @Test
    void testFillOptionsSchlüssel() {
        String testInput1 = "nummer@Schlüssel=Nummer";
        Field field = new Field(testInput1, true, 0, new OptionCode(0));
        String key = field.getKeySelectionString();
        assertTrue(field.isOptionKey());
        assertEquals("nummer={0};@sort=Nummer", key);
    }

    @Test
    void testFillOptionsNotEmpty() {
        String testInput2 = "nummer@notempty";
        Field field2 = new Field(testInput2, true, 0, new OptionCode(0));
        assertTrue(field2.isOptionNotEmpty());
        assertThat(field2.getKeySelectionString(), is(""));
    }

    @Test
    void testFillOptionsModifiable() {
        String testInput = "nummer@modifiable";
        Field field = new Field(testInput, true, 0, new OptionCode(0));
        assertTrue(field.isOptionModifiable());
        assertThat(field.getKeySelectionString(), is(""));
    }

    @Test
    void testFillOptionsSkip() {
        String testInput = "nummer@skip";
        Field field = new Field(testInput, true, 0, new OptionCode(0));
        assertTrue(field.isOptionSkip());
        assertThat(field.getKeySelectionString(), is(""));
    }

    @Test
    void testFillOptionsKeySelection() {
        // At keyselection the field name is not necessary
        String testInput = "nummer@keyselection='$,,iso3={0}'";
        Field field = new Field(testInput, true, 0, new OptionCode(0));
        assertTrue(field.isOptionKeySelection());
        assertThat(field.getKeySelectionString() ,is("iso3={0}") );
    }

    @Test
     void testFillOptionsDontChangeIfEqual() {
        String testInput = "nummer@dontChangeIfEqual";
        Field field = new Field(testInput, true, 0, new OptionCode(0));
        assertTrue(field.isOptionDontChangeIfEqual());
        assertThat(field.getKeySelectionString(), is(""));
    }


    @Test
    void testFillOptionsModifiableAndSkip() {
        String testInput = "nummer@modifiable;@skip";
        Field field = new Field(testInput, true, 0, new OptionCode(0));
        assertThat(field.isOptionModifiable(), is(true));
        assertThat(field.isOptionSkip(), is(true));
        assertThat(field.getKeySelectionString(), is(""));
    }

    @Test
     void testFillOptionsModifiableAndSkipAndDontChangeIfEqual() {
        String testInput = "nummer@modifiable;@skip@dontChangeIfEqual";
        Field field = new Field(testInput, true, 0, new OptionCode(0));
        assertThat(field.isOptionModifiable(), is(true));
        assertThat(field.isOptionSkip(), is(true));
        assertThat(field.isOptionDontChangeIfEqual(), is(true));
        assertThat(field.getKeySelectionString(), is(""));
    }



    @Test
    void testCreateSelFromKey() {
        String testInput1 = "nummer@Schlüssel=Nummer";
        String testOutput1 = "nummer={0};@sort=Nummer";

        Field field = new Field(testInput1, true, 0, new OptionCode(0));
        String createSelfromSelection = field.createSelFromKey(ImportOptions.KEY);

        assertEquals(testOutput1, createSelfromSelection);

    }

    @Test
    void testExtractKeyFromSelection() {
        String testInput1 = "@keyselection='nummer={0};@sort=Nummer";
        String testOutput1 = "Nummer";
        Field field = new Field(testInput1, true, 0, new OptionCode(0));

        assertEquals(testOutput1, field.extractKeyFromSelection());

        String testInput2 = "@keyselection='nummer={0};@sort=Nummer;@test";
        String testOutput2 = "Nummer";
        Field field2 = new Field(testInput2, true, 0, new OptionCode(0));

        assertEquals(testOutput2, field2.extractKeyFromSelection());

        String testInput3 = "@keyselection='nummer='{0};@sort=Nummer'";

        String testOutput3 = "Nummer";
        Field field3 = new Field(testInput3, true, 0, new OptionCode(0));
        assertEquals(testOutput3, field3.extractKeyFromSelection());

    }
}
