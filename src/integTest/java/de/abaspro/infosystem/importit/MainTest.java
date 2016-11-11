package de.abaspro.infosystem.importit;

import de.abas.erp.db.schema.customer.Customer;
import de.abaspro.infosystem.importit.util.AbstractTest;
import de.abaspro.utils.Util;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class MainTest extends AbstractTest {

    @Test
    public void integTestImport() throws Exception {
        infosys.setYserver(getHostname());
        infosys.setYmandant(getClient());
        infosys.setYport(getPort());
        infosys.setYpasswort(getPassword());

        infosys.setYdatafile("ow1/TestCustomer.xls");

        infosys.invokeYpruefstrukt();

        assertThat(getMessage(), not(containsString("Java-Klasse nicht gefunden")));
        assertThat(infosys.getYfehlerstruktur(), is(0));
        assertThat(infosys.getYstatus(), is(Util.getMessage("main.status.structure.check.success")));

        infosys.invokeYpruefdat();
        assertThat(infosys.getYstatus(), is(Util.getMessage("main.check.data.success")));

        infosys.invokeYimport();
        assertThat(infosys.getYstatus(), is(Util.getMessage("info.import.data.success")));
        assertThat(infosys.getYfehler(), is(0));

        for (int i = 0; i < 8; i++) {
            assertNotNull(getObject(Customer.class, "1TEST" + i));
        }
    }

    @Test
    public void integTestDocumentation() {
        infosys.setYserver(getHostname());
        infosys.setYmandant(getClient());
        infosys.setYport(getPort());
        infosys.setYpasswort(getPassword());

        infosys.invokeYdoku();

        assertFalse(getMessage().contains(Util.getMessage("main.docu.error")));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for (int i = 0; i < 8; i++) {
            deleteObjects(Customer.class, "idno", "1TEST" + i);
        }
    }
}
