package de.abas.infosystem.importit.util;

import de.abas.ceks.jedp.*;
import de.abas.erp.db.Deletable;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.infosystem.custom.owimportit.InfosystemImportit;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.esdk.test.util.EsdkIntegTest;
import de.abas.esdk.test.util.TestSetup;
import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.testdata.TestDataCustomerPartNumber;
import de.abas.infosystem.importit.testdata.TestDataSML;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.List;
import java.util.Map;


public class AbstractTest extends EsdkIntegTest {

    String message = "";
    String hostname;

    String client;
    int port;
    String password;

    private final TestDataCustomerPartNumber testDataCustomerPartNumber= new TestDataCustomerPartNumber();
    private final TestDataSML testDataSML;

    protected InfosystemImportit infosystem ;

    public AbstractTest() {
        testDataSML = new TestDataSML();
    }

    @BeforeClass
    public static void setupImportit()  {

        ctx =TestSetup.createClientContext();

    }

    @Before
    public void prepareData() throws ImportitException, CantSaveException, CantChangeFieldValException, CantBeginSessionException, ServerActionException, CantReadFieldPropertyException, InvalidRowOperationException, CantBeginEditException {
        loadProperties();
        deleteObjectsWhichMatch(Product.class , "swd" , "TEST");
        deleteObjectsWhichMatch(Product.class , "idno" , "TEST");
        testDataCustomerPartNumber.deleteData(ctx);
        testDataSML.deleteData(ctx);
        testDataSML.importDataList(ctx);
        testDataCustomerPartNumber.importDataList(ctx);
        infosystem  = ctx.openInfosystem(InfosystemImportit.class);


    }

    @After
    public void cleanData(){
        if (infosystem != null){
            infosystem.close();
        }

    }


    @AfterClass
    public static void cleanup() {
        ctx.close();
    }


    public String getHostname() {
        return hostname;
    }

    public String getClient() {
        return client;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }

    public <C extends SelectableObject> C getObject(Class<C> type, String identNumber) throws ImportitException {

        final List<C> objects = ctx.createQuery(SelectionBuilder.create(type).add(Conditions.eq("idno", identNumber)).build())
                .execute();

        if (objects.size() > 1) {
            throw new ImportitException("identNumber not unique");
        }
        if (objects.isEmpty()) {
            throw new ImportitException("no object found with the identNumber");
        }
        return objects.get(0);
    }

    public <C extends SelectableObject> C getObjectSel(Class<C> type, Iterable<ValuePair> valuePairs) throws ImportitException {
        SelectionBuilder<C> selectBuilder = SelectionBuilder.create(type);
        for (ValuePair valuePair : valuePairs) {
            selectBuilder.add(Conditions.eq(valuePair.getField(), valuePair.getValue()));
        }
        final List<C> objects = ctx.createQuery(selectBuilder.build()).execute();

        if (objects.size() > 1) {
            throw new ImportitException("identNumber not unique");
        }
        if (objects.isEmpty()) {
            throw new ImportitException("no object found with the identNumber");
        }
        return objects.get(0);
    }

    private void loadProperties() throws ImportitException {
        System.out.println("loading properties ");
        Map<String, String> pr = TestProperties.loadProperties();
        hostname = pr.get("EDP_HOST");
        client = pr.get("EDP_CLIENT");
        port = Integer.valueOf(pr.get("EDP_PORT"));
        password = pr.get("EDP_PASSWORD");
        System.out.println("Hostname: " + hostname );
        System.out.println("Client: " + client );
        System.out.println("EDP-Port: " + port );
        System.out.println("Password: " + password );

    }

    public <C extends SelectableObject & Deletable> void deleteObjects(Class<C> type, String field, String value) {
        final List<C> objects = ctx.createQuery(SelectionBuilder.create(type).add(Conditions.eq(field, value)).build())
                .execute();
        for (final C object : objects) {
            object.delete();
        }
    }

    public <C extends SelectableObject & Deletable> void deleteObjectsWhichMatch(Class<C> type, String field, String value) {
        final List<C> objects = ctx
                .createQuery(SelectionBuilder.create(type).add(Conditions.matchIgCase(field, value)).build()).execute();
        for (final C object : objects) {
            object.delete();
        }
    }








}
