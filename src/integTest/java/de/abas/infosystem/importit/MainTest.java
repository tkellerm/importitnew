package de.abas.infosystem.importit;

import de.abas.ceks.jedp.*;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.infosystem.custom.owimportit.InfosystemImportit;
import de.abas.erp.db.schema.customer.Customer;
import de.abas.erp.db.schema.part.CustomerProductProperty;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.warehouse.WarehouseGroupProperty;
import de.abas.infosystem.importit.util.AbstractTest;
import de.abas.infosystem.importit.util.ValuePair;
import de.abas.utils.MessageUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import static de.abas.infosystem.importit.util.TestProperties.*;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;


public class MainTest extends AbstractTest {
    public static final String MAIN_STATUS_STRUCTURE_CHECK_SUCCESS = "main.status.structure.check.success";
    public static final String MAIN_CHECK_DATA_SUCCESS = "main.check.data.success";
    public static final String INFO_IMPORT_DATA_SUCCESS = "info.import.data.success";
    public static final String STRUCTURECHECK = "Structure check: ";
    public static final String DATACHECK = "Datenprüfung: ";
    public static final String PRODUCT = "product";


    @Test
    public void integTestImport() throws ImportitException {
        setInfosysloginInfo();

        infosystem.setYdatafile("owimportit/TestCustomer.xls");

        infosystem.invokeYpruefstrukt();

        assertThat(getMessage(), not(containsString("Java-Klasse nicht gefunden")));
        assertThat(infosystem.getYfehlerstruktur(), is(0));
        assertThat(infosystem.getYstatus(), is(MessageUtil.getMessage(MAIN_STATUS_STRUCTURE_CHECK_SUCCESS)));

        infosystem.invokeYpruefdat();
        assertThat(infosystem.getYstatus(), is(MessageUtil.getMessage(MAIN_CHECK_DATA_SUCCESS)));
        assertThat(infosystem.getYfehlerdatpruef(), is(0));

        infosystem.invokeYimport();
        assertThat(infosystem.getYstatus(), is(MessageUtil.getMessage(INFO_IMPORT_DATA_SUCCESS)));
        assertThat(infosystem.getYfehler(), is(0));

        assertNotNull(getObject(Customer.class, "1TEST0"));

        assertNotNull(getObject(Customer.class, "1TEST1"));
        assertNotNull(getObject(Customer.class, "1TEST2"));
        assertNotNull(getObject(Customer.class, "1TEST3"));
        assertNotNull(getObject(Customer.class, "1TEST4"));
        assertNotNull(getObject(Customer.class, "1TEST5"));
        assertNotNull(getObject(Customer.class, "1TEST6"));
        assertNotNull(getObject(Customer.class, "1TEST7"));

        for (int i = 0; i < 8; i++) {
            assertNotNull(getObject(Customer.class, "1TEST" + i));
        }
    }

    private void prepareTestManyData()  {
        setInfosysloginInfo();
        String importfile = "owimportit/Test2_Kunden.xlsx";
        long startKunden = System.currentTimeMillis();
        importDataFile(importfile);
        long endkunden = System.currentTimeMillis();
        System.out.println("Kunden : " + (endkunden - startKunden));
        importfile = "owimportit/Test2_Mitarb.xlsx";
        long startMitarbeiter = System.currentTimeMillis();
        importDataFile(importfile);
        long endMitarbeiter = System.currentTimeMillis();
        System.out.println(" Mitarbeiter : " + (endMitarbeiter - startMitarbeiter));

    }

    public void importDataFile(String importfile) {
        setInfosysloginInfo();

        infosystem.setYdatafile(importfile);
        System.out.println("StrukturPrüfung " + importfile);
        infosystem.invokeYpruefstrukt();
        System.out.println("Datenprüfung " + importfile);
        infosystem.invokeYpruefdat();
        System.out.println("Datemimport " + importfile);
        infosystem.invokeYimport();
        System.out.println("Ende Datemimport " + importfile);
    }

//    @Test
//    public void integManyData()  {
//        prepareTestManyData();
//
//        setInfosysloginInfo();
//
//        infosystem.setYdatafile("owimportit/Test2_Kundenakt_kurz.xlsx");
//
//        runImportitWithoutError(15);
//
//
//    }

    @Test
    public void integKeySelect() {
        setInfosysloginInfo();


        infosystem.setYdatafile("owimportit/TestCustomer_selkey.xlsx");

        runImportitWithoutError(8);

    }

    private void runImportitWithoutError(int numberOfSuccessfulRecords) {
        System.out.println("Test run with file " + infosystem.getYdatafile());
        long startpruef = System.currentTimeMillis();
        infosystem.invokeYpruefstrukt();
        long endpruef = System.currentTimeMillis();
        System.out.println(STRUCTURECHECK + (endpruef - startpruef) + "ms ");

        assertThat(infosystem.getYfehlerstruktur(), is(0));
        assertThat(infosystem.getYstatus(), is(MessageUtil.getMessage(MAIN_STATUS_STRUCTURE_CHECK_SUCCESS)));

        long startpruefdat = System.currentTimeMillis();
        infosystem.invokeYpruefdat();
        long endpruefdat = System.currentTimeMillis();
        System.out.println(DATACHECK + (endpruefdat - startpruefdat) + "ms ");

        assertThat(infosystem.getYstatus(), is(MessageUtil.getMessage(MAIN_CHECK_DATA_SUCCESS)));
        assertThat(infosystem.getYfehlerdatpruef(), is(0));

        long startimport = System.currentTimeMillis();
        infosystem.invokeYimport();
        long endimport = System.currentTimeMillis();
        long diffimport = endimport - startimport;
        System.out.println("Import: " + diffimport + "ms ");

        assertThat(infosystem.getYstatus(), is(MessageUtil.getMessage(INFO_IMPORT_DATA_SUCCESS)));
        assertThat(infosystem.getYfehler(), is(0));
        assertThat(infosystem.getYok(), is(numberOfSuccessfulRecords));
        assertNotEquals(diffimport, 0);
    }

    @Test
    public void integFieldSelect() {
        setInfosysloginInfo();


        infosystem.setYdatafile("owimportit/TestCustomer_selkeyfield.xlsx");
        runImportitWithoutError(8);

    }

    @Test
    public void structureTestbykeyselection() {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestCustomer_selkey_FehlerStruktur.xlsx");
        infosystem.invokeYpruefstrukt();
        System.out.println("test if the wrong field suchxxx is found by the structure test");
        assertThat(infosystem.getYfehlerstruktur(), is(1));
        infosystem.setYshowonlyerrorline(TRUE);
        infosystem.invokeYintabladen();
        assertThat(infosystem.getRowCount(), is(1));
        InfosystemImportit.Row row = infosystem.table().getRow(1);
        assertThat(row.getYfehlerda(), is(TRUE));
        assertThat(row.getYtfehler(), is("Fehler aus den Kopffeldern:\nsuchxxx 1 Fehler: Das Feld mit dem Namen s"));
        Writer writer = new StringWriter();
        try {
            writer = row.getYkomtext(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThat(writer.toString(), is("Fehler aus den Kopffeldern:\nsuchxxx 1 Fehler: Das Feld mit dem Namen suchxxx wurde in der Vartab nicht gefunden"));
    }

    @Test
    public void dataTest() {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestCustomer_Fehlerdaten.xls");
        infosystem.invokeYpruefstrukt();
        assertThat(infosystem.getYfehlerstruktur(), is(0));
        infosystem.invokeYpruefdat();
        System.out.println("Get Error in data check");
        assertThat(infosystem.getYfehlerdatpruef(), is(1));

    }

    @Test
    public void customerPartNumberWith3RecordsTest() {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestCustomerPartNumberKlein.xlsx");
        runImportitWithoutError(3);
    }

    @Test
    public void customerPartNumberWithRepeatedRecordsTest() {
        ArrayList<ValuePair> valPairs = new ArrayList<>();
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestCustomerPartNumber.xlsx");
        infosystem.invokeYpruefstrukt();
        System.out.println("Status after structure-check: " + infosystem.getYstatus());
        assertThat(infosystem.getYfehlerstruktur(), is(0));
        infosystem.invokeYpruefdat();
        System.out.println("Status after data-check: " + infosystem.getYstatus());
        assertThat(infosystem.getYfehlerdatpruef(), is(0));
        infosystem.invokeYimport();
        System.out.println("Status after import " + infosystem.getYstatus() + "Error quantity :" + infosystem.getYfehler());
        assertThat(infosystem.getYok(), is(9));
        valPairs.add(new ValuePair(PRODUCT, "1TPROD003"));
        valPairs.add(new ValuePair("custVendor", "1TEST002"));
        System.out.println("Check the last entry in the Test-File");
        try {
            SelectableObject customerProductPropertySelectable = getObjectSel(CustomerProductProperty.class, valPairs);
            if (customerProductPropertySelectable instanceof CustomerProductProperty) {
                CustomerProductProperty customerProductProperty = (CustomerProductProperty) customerProductPropertySelectable;
                assertThat(customerProductProperty.getCustProductNo(), is("3.Versuch TEIL3 KU2"));
            }
        } catch (Exception e) {
            Assert.fail();
        }


    }

    @Test
    public void selfieldMitFehlerinStrukturTest() {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestCustomer_selkeyfield_FehlerStruktur.xlsx");
        infosystem.invokeYpruefstrukt();
        assertThat(infosystem.getYfehlerstruktur(), is(1));

    }

    private void setInfosysloginInfo() {
//		infosystem.setYserver(getHostname());
//		infosystem.setYmandant(getClient());
//		infosystem.setYport(getPort());
        infosystem.setYpasswort(getPassword());
        System.out.println("get Passwort");

    }


    @Test
    public void integTestSMLImport() throws ServerActionException, CantBeginEditException, ImportitException, CantReadFieldPropertyException, CantBeginSessionException {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestPartWithSml.xlsx");
        runImportitWithoutError(8);


        EDPSession edpSession = null;
        try {
            edpSession = EDPFactory.createEDPSession();

            edpSession.beginSession(loadProperties().get(EDP_HOST),
                    Integer.parseInt(loadProperties().get(EDP_PORT)),
                    loadProperties().get(EDP_CLIENT),
                    loadProperties().get(EDP_PASSWORD),
                    "testSMLDAtA");
            Product part = getObject(Product.class, "1TEST0");
            checkProductWithSML(part, edpSession, "s.qlang",
                    "100.00", "s.qdurch", "6.00");
            Product part2 = getObject(Product.class, "1TEST7");
            checkProductWithSML(part2, edpSession, "s.qlang",
                    "60.00", "s.qdurch", "5.00");

        } finally {
            if (edpSession != null) {
                edpSession.release();
            }
        }

    }

    private void checkProductWithSML(Product part, EDPSession edpSession, String smlFieldName1,
                                     String smlFieldValue1, String smlFieldName2, String smlFieldValue2) throws CantBeginEditException, CantReadFieldPropertyException, ImportitException {
        if (part == null) {
            throw new ImportitException("Part is null!");
        }
        EDPEditor edpEditor = null;
        try {
            edpEditor = edpSession.createEditor();
            edpEditor.beginView(part.getId().toString());
            String qlang = edpEditor.getFieldVal(0, smlFieldName1);
            String qdurch = edpEditor.getFieldVal(0, smlFieldName2);
            assertThat(qlang.trim(), is(smlFieldValue1));
            assertThat(qdurch.trim(), is(smlFieldValue2));

        } finally {
            if (edpEditor != null && edpEditor.isActive()) {
                try {
                    edpEditor.endEditCancel();
                    edpEditor.release();
                } catch (ServerActionException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Test
    public void integTestSMLImportWithErrorInStructure() {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/TestPartWithSml_with_wrong_field.xlsx");
        infosystem.invokeYpruefstrukt();
        assertThat(infosystem.getYfehlerstruktur(), is(1));

    }

    @Test
    public void integTestWarehousePropertiesImport() throws ImportitException {
        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/Test2_WarehouseGroupProperties.xlsx");
        runImportitWithoutError(1);

        ArrayList<ValuePair> valPairs = new ArrayList<>();

        valPairs.add(new ValuePair(PRODUCT, "1TPROD001"));
        valPairs.add(new ValuePair("warehGrp", "20"));
        WarehouseGroupProperty wGP = getObjectSel(WarehouseGroupProperty.class, valPairs);
        assertNotNull(wGP);

        ArrayList<ValuePair> valPairs2 = new ArrayList<>();
        valPairs2.add(new ValuePair(PRODUCT, "1TPROD001"));
        valPairs2.add(new ValuePair("warehGrp", "26"));
        WarehouseGroupProperty wGP2 = getObjectSel(WarehouseGroupProperty.class, valPairs2);
        assertNotNull(wGP2);

    }

    @Test
    public void integTestProductionList() {

        setInfosysloginInfo();
        infosystem.setYdatafile("owimportit/Test_Fertlist.xlsx");
        runImportitWithoutError(1);


    }

}
