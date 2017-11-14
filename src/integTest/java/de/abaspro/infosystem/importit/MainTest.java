package de.abaspro.infosystem.importit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.abas.erp.db.schema.customer.Customer;
import de.abas.erp.db.schema.notes.Note;
import de.abaspro.infosystem.importit.util.AbstractTest;
import de.abaspro.utils.Util;

public class MainTest extends AbstractTest {
	Logger log = Logger.getLogger(MainTest.class);

	@Test
	public void integTestImport() throws Exception {
		setInfosysloginInfo();

		infosys.setYdatafile("owfw7/TestCustomer.xls");

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

	@Before
	public void prepareTestManyData() throws Exception {
		setInfosysloginInfo();
		String importfile = "owfw7/Test2_Kunden.xlsx";
		long startKunden = System.currentTimeMillis();
		importDataFile(importfile);
		long endkunden = System.currentTimeMillis();
		System.out.println("Kunden : " + (endkunden - startKunden));
		importfile = "owfw7/Test2_Mitarb.xlsx";
		long startMitarbeiter = System.currentTimeMillis();
		importDataFile(importfile);
		long endMitarbeiter = System.currentTimeMillis();
		System.out.println(" Mitarbeiter : " + (endMitarbeiter - startMitarbeiter));

	}

	private void importDataFile(String importfile) {
		setInfosysloginInfo();
		infosys.setYdatafile(importfile);
		System.out.println("StrukturPrüfung " + importfile);
		infosys.invokeYpruefstrukt();
		System.out.println("Datenprüfung " + importfile);
		infosys.invokeYpruefdat();
		System.out.println("Datemimport " + importfile);
		infosys.invokeYimport();
		System.out.println("Ende Datemimport " + importfile);
	}

	@Test
	public void integManyData() throws Exception {
		setInfosysloginInfo();

		infosys.setYdatafile("owfw7/Test2_Kundenakt_kurz.xlsx");

		long startpruef = System.currentTimeMillis();
		infosys.invokeYpruefstrukt();
		long endpruef = System.currentTimeMillis();
		System.out.println("Strukturprüfung: " + (endpruef - startpruef) + " " + infosys.getYdatafile());

		assertThat(infosys.getYfehlerstruktur(), is(0));
		assertThat(infosys.getYstatus(), is(Util.getMessage("main.status.structure.check.success")));

		long startpruefdat = System.currentTimeMillis();
		infosys.invokeYpruefdat();
		long endpruefdat = System.currentTimeMillis();
		System.out.println("Datenprüfung: " + (endpruefdat - startpruefdat));

		assertThat(infosys.getYstatus(), is(Util.getMessage("main.err.check.data")));
		assertThat(infosys.getYfehlerdatpruef(), is(Util.getMessage("main.err.check.data")));

		long startimport = System.currentTimeMillis();
		infosys.invokeYimport();
		long endimport = System.currentTimeMillis();
		long diffimport = endimport - startimport;
		System.out.println("Import: " + diffimport + " " + infosys.getYdatafile());

		assertThat(infosys.getYstatus(), is(Util.getMessage("info.import.data.success")));
		// assertThat(infosys.getYfehler(), is(0));

		assertFalse(diffimport > 0);

	}

	@Test
	public void integKeySelect() throws Exception {
		setInfosysloginInfo();

		infosys.setYdatafile("owfw7/TestCustomer_selkey.xlsx");

		long startpruef = System.currentTimeMillis();
		infosys.invokeYpruefstrukt();
		long endpruef = System.currentTimeMillis();
		System.out.println("Strukturprüfung: " + (endpruef - startpruef) + " " + infosys.getYdatafile());

		assertThat(infosys.getYfehlerstruktur(), is(0));
		assertThat(infosys.getYstatus(), is(Util.getMessage("main.status.structure.check.success")));

		long startpruefdat = System.currentTimeMillis();
		infosys.invokeYpruefdat();
		long endpruefdat = System.currentTimeMillis();
		System.out.println("Datenprüfung: " + (endpruefdat - startpruefdat));

		assertThat(infosys.getYstatus(), is(Util.getMessage("main.err.check.data")));
		assertThat(infosys.getYfehlerdatpruef(), is(Util.getMessage("main.err.check.data")));

		long startimport = System.currentTimeMillis();
		infosys.invokeYimport();
		long endimport = System.currentTimeMillis();
		long diffimport = endimport - startimport;
		System.out.println("Import: " + diffimport + " " + infosys.getYdatafile());

		assertThat(infosys.getYstatus(), is(Util.getMessage("info.import.data.success")));
		// assertThat(infosys.getYfehler(), is(0));

		assertFalse(diffimport > 0);

	}

	@Test
	public void integFieldSelect() throws Exception {
		setInfosysloginInfo();

		infosys.setYdatafile("owfw7/TestCustomer_selkeyfield.xlsx");

		long startpruef = System.currentTimeMillis();
		infosys.invokeYpruefstrukt();
		long endpruef = System.currentTimeMillis();
		System.out.println("Strukturprüfung: " + (endpruef - startpruef) + " " + infosys.getYdatafile());

		assertThat(infosys.getYfehlerstruktur(), is(0));
		assertThat(infosys.getYstatus(), is(Util.getMessage("main.status.structure.check.success")));

		long startpruefdat = System.currentTimeMillis();
		infosys.invokeYpruefdat();
		long endpruefdat = System.currentTimeMillis();
		System.out.println("Datenprüfung: " + (endpruefdat - startpruefdat));

		assertThat(infosys.getYstatus(), is(Util.getMessage("main.check.data.success")));
		assertThat(infosys.getYfehlerdatpruef(), is(0));

		long startimport = System.currentTimeMillis();
		infosys.invokeYimport();
		long endimport = System.currentTimeMillis();
		long diffimport = endimport - startimport;
		System.out.println("Import: " + diffimport + " " + infosys.getYdatafile());

		assertThat(infosys.getYstatus(), is(Util.getMessage("info.import.data.success")));
		// assertThat(infosys.getYfehler(), is(0));

		assertFalse(diffimport == 0);
	}

	private void setInfosysloginInfo() {
		infosys.setYserver(getHostname());
		infosys.setYmandant(getClient());
		infosys.setYport(getPort());
		infosys.setYpasswort(getPassword());
	}

	@Test
	public void integTestDocumentation() {
		setInfosysloginInfo();

		infosys.invokeYdoku();

		assertFalse(getMessage().contains(Util.getMessage("main.docu.error")));
	}

	@Override
	public void cleanup() {
		super.cleanup();
		for (int i = 0; i < 8; i++) {
			deleteObjects(Customer.class, "idno", "1TEST" + i);
		}
		deleteObjectsmatch(Note.class, "swd", "T");
	}
}
