package de.abaspro.infosystem.importit;

import de.abas.erp.db.schema.customer.Customer;
import de.abaspro.infosystem.importit.util.AbstractTest;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

public class Importit21Test extends AbstractTest {

    @Test
    public void integTest() throws Exception {
        infosys.setYserver(getHostname());
        infosys.setYmandant(getClient());
        infosys.setYport(getPort());
        infosys.setYpasswort(getPassword());

        infosys.setYdatafile("ow1/TestCustomer.xls");

        infosys.invokeYpruefstrukt();

        assertThat(getMessage(), not(containsString("Java-Klasse nicht gefunden")));
        assertThat(infosys.getYfehlerstruktur(), is(0));
        assertThat(infosys.getYstatus(), is("Strukturprüfung durchgelaufen"));

        infosys.invokeYpruefdat();
        assertThat(infosys.getYstatus(), is("Datenprüfung erfolgreich"));

        infosys.invokeYimport();
        assertThat(infosys.getYstatus(), is("Import erfolgreich"));
        assertThat(infosys.getYfehler(), is(0));

        for (int i = 0; i < 8; i++) {
            assertNotNull(getObject(Customer.class, "1TEST" + i));
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for (int i = 0; i < 8; i++) {
            deleteObjects(Customer.class, "idno", "1TEST" + i);
        }
    }
}
