package de.abaspro.infosystem.importit;

import de.abaspro.infosystem.importit.util.AbstractTest;
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
        assertThat(getMessage(), containsString("Strukturprüfung abgeschlossen!"));
    }

}
