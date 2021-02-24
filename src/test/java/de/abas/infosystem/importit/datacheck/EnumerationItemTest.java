package de.abas.infosystem.importit.datacheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EnumerationItemTest {

    private EnumerationItem testItem;
    private static final int NUMBER = 1;
    private static final String DESCRIPTION = "Description";
    private static final String NEUTRAL_NAME = "neutral name";


    @BeforeEach
    protected void prepareItem(){
        this.testItem = new EnumerationItem( NUMBER, DESCRIPTION, NEUTRAL_NAME);
    }


    @Test
    void setDatabase() {
        assertThat(testItem.getDatabase() , is(0) );

        testItem.setDatabase(2);

        assertThat(testItem.getDatabase() , is(2 ));
    }

    @Test
    void setDatabaseGroup() {
        assertThat(testItem.getDatabaseGroup() , is(0) );

        testItem.setDatabaseGroup(22);

        assertThat(testItem.getDatabaseGroup() , is(22 ));
    }

    @Test
    void getNumber() {
        assertThat(testItem.getNumber(), is(NUMBER));
    }

    @Test
    void getNamebspr() {
        assertThat(testItem.getNamebspr(), is(DESCRIPTION));
    }

    @Test
    void getNameNeutral() {
        assertThat(testItem.getNameNeutral(), is(NEUTRAL_NAME));
    }

    @Test
    void getDatabase() {
        assertThat(testItem.getDatabase() , is(0) );

    }

    @Test
    void getDatabaseGroup() {
        assertThat(testItem.getDatabaseGroup() , is(0) );
    }
}