package de.abas.infosystem.importit.datacheck;

public class EnumerationItem {
    private final Integer number;
    private final String namebspr;
    private final String nameNeutral;



    private int database;
    private int databaseGroup;



    public EnumerationItem(int  number, String namebspr, String neutralName) {
        this.number = number;
        this.namebspr = namebspr;
        this.nameNeutral = neutralName;
        this.database = 0;
        this.databaseGroup = 0;

    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public void setDatabaseGroup(int databaseGroup) {
        this.databaseGroup = databaseGroup;
    }

    public Integer getNumber() {
        return number;
    }

    public String getNamebspr() {
        return namebspr;
    }

    public String getNameNeutral() {
        return nameNeutral;
    }

    public int getDatabase() {
        return database;
    }

    public int getDatabaseGroup() {
        return databaseGroup;
    }

}
