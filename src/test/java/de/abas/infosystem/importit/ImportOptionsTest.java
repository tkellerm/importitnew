package de.abas.infosystem.importit;

import static org.junit.jupiter.api.Assertions.*;

import de.abas.infosystem.importit.ImportOptions;
import org.junit.jupiter.api.Test;

class ImportOptionsTest {

    @Test
    void testImportOptions() {

        ImportOptions[] values = ImportOptions.values();
        int length = values.length;
        assertEquals(7, length);

    }

    @Test
    void testToString() {

        assertEquals("@dontChangeIfEqual", ImportOptions.DONT_CHANGE_IF_EQUAL.toString() );
        assertEquals("@skip", ImportOptions.SKIP.toString() );
        assertEquals("@Schlüssel" , ImportOptions.KEY.toString());
        assertEquals("@modifiable" , ImportOptions.MODIFIABLE.toString() );
        assertEquals("@notempty", ImportOptions.NOTEMPTY.toString());
        assertEquals("@selection" , ImportOptions.SELECTION.toString());
        assertEquals("@keyselection" ,ImportOptions.KEYSELECTION.toString() );
    }

}
