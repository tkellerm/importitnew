package de.abas.infosystem.importit.datacheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EnumerationTest {

    private static final int ITEM1_NUMBER=1;
    private static final String ITEM1_DESCRIPTION="Beschreibung 1";
    private static final String ITEM1_NEUTRAL="NEUTRAL1";

    private static final int ITEM2_NUMBER=22;
    private static final String ITEM2_DESCRIPTION="Beschreibung 2";
    private static final String ITEM2_NEUTRAL="NEUTRAL2";

    private static final EnumerationItem ITEM_1 = new EnumerationItem(ITEM1_NUMBER, ITEM1_DESCRIPTION, ITEM1_NEUTRAL);
    private static final EnumerationItem ITEM_2 = new EnumerationItem(ITEM2_NUMBER, ITEM2_DESCRIPTION, ITEM2_NEUTRAL);

    private Enumeration testEnum;

    @BeforeEach
    void prepareData(){

        testEnum = new Enumeration();
                testEnum.getListOfEnumItems().add(ITEM_1);
        testEnum.getListOfEnumItems().add(ITEM_2);
    }


    @Test
    void getListOfEnumItems() {
        assertThat(testEnum.getListOfEnumItems().size(), is(2));
        assertTrue(testEnum.getListOfEnumItems() instanceof List);
        assertNotNull(testEnum.getListOfEnumItems());
    }

    @Test
    void testsearchItemWithInteger() {
        assertThat(testEnum.searchItem(ITEM1_NUMBER) , is(ITEM_1));
        assertThat(testEnum.searchItem(ITEM2_NUMBER) , is(ITEM_2));
        assertNull(testEnum.searchItem(10));

    }

    @Test
    void testSearchItemWithString() {
        assertThat(testEnum.searchItem(ITEM1_DESCRIPTION) , is(ITEM_1));
        assertThat(testEnum.searchItem(ITEM2_NEUTRAL) , is(ITEM_2));
        assertThat(testEnum.searchItem(ITEM2_NEUTRAL.toUpperCase()) , is(ITEM_2));
        assertThat(testEnum.searchItem(ITEM2_NEUTRAL.toLowerCase()) , is(ITEM_2));
        assertThat(testEnum.searchItem(String.valueOf(ITEM2_NUMBER)), is(ITEM_2));
        assertNull(testEnum.searchItem("NULL"));
    }
}