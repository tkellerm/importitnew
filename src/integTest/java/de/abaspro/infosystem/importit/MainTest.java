package de.abaspro.infosystem.importit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.abas.erp.db.EditorAction;
import de.abas.erp.db.schema.customer.Customer;
import de.abas.erp.db.schema.notes.Note;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.schema.warehouse.WarehouseGroupProperty;
import de.abaspro.infosystem.importit.util.AbstractTest;
import de.abaspro.infosystem.importit.util.ValuePair;
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
		prepareTestManyData();
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

		assertThat(infosys.getYstatus(), is(Util.getMessage("main.check.data.success")));
		assertThat(infosys.getYfehlerdatpruef(), is(0));

		long startimport = System.currentTimeMillis();
		infosys.invokeYimport();
		long endimport = System.currentTimeMillis();
		long diffimport = endimport - startimport;
		System.out.println("Import: " + diffimport + " " + infosys.getYdatafile());

		assertThat(infosys.getYstatus(), is(Util.getMessage("info.import.data.success")));
		// assertThat(infosys.getYfehler(), is(0));

		assertTrue(diffimport > 0);

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

	public void structureTestbykeyselection() {
		infosys.setYdatafile("owfw7/TestCustomer_selkey_FehlerStruktur.xlsx");
		infosys.invokeYpruefstrukt();
		assertThat(infosys.getYfehlerstruktur(), is(1));

	}

	public void dataTest() {
		infosys.setYdatafile("owfw7/TestCustomer_Fehlerdaten.xls");
		infosys.invokeYpruefstrukt();
		assertThat(infosys.getYfehlerstruktur(), is(0));
		infosys.invokeYpruefdat();
		assertThat(infosys.getYfehlerdatpruef(), is(1));

	}

	@Test
	public void CostumerPartNumberTest() {
		infosys.setYdatafile("owfw7/TestCustomerPartNumber.xlsx");
		infosys.invokeYpruefstrukt();
		assertThat(infosys.getYfehlerstruktur(), is(0));
		infosys.invokeYpruefdat();
		assertThat(infosys.getYfehlerdatpruef(), is(0));
		infosys.invokeYimport();
		assertThat(infosys.getYok(), is(1));
	}

	public void selfieldMitFehlerinStrukturTest() {
		infosys.setYdatafile("owfw7/TestCustomer_selkeyfield_FehlerStruktur.xlsx");
		infosys.invokeYpruefstrukt();
		assertThat(infosys.getYfehlerstruktur(), is(1));

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

	@Test
	public void integTestSMLImport() throws Exception {
		setInfosysloginInfo();
		infosys.setYdatafile("owfw7/TestPartWithSml.xlsx");
		infosys.invokeYpruefstrukt();
		assertThat(infosys.getYfehlerstruktur(), is(0));
		infosys.invokeYpruefdat();
		assertThat(infosys.getYfehlerdatpruef(), is(0));
		infosys.invokeYimport();
		assertThat(infosys.getYok(), is(8));
		infosys.close();

		Product part = getObject(Product.class, "1Test0");
		ProductEditor productEditor = null;
		if (part != null) {

			try {

				productEditor = part.createEditor();
				productEditor.open(EditorAction.VIEW);

				String qlang = productEditor.getString("s.qlang");
				String qdurch = productEditor.getString("s.qdurch");
				assertThat(qlang, is("100.00"));
				assertThat(qdurch, is("6.00"));
				productEditor.commit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (productEditor.active()) {
				productEditor.delete();
			}
		}

		ProductEditor productEditor2 = null;
		Product part2 = getObject(Product.class, "1TEST7");
		if (part != null) {
			try {
				productEditor2 = part2.createEditor();
				productEditor2.open(EditorAction.VIEW);

				String qlang = productEditor2.getString("s.qlang");
				String qdurch = productEditor2.getString("s.qdurch");

				assertThat(qlang, is("60.00"));
				assertThat(qdurch, is("5.00"));
				productEditor2.abort();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (productEditor2.active()) {
				productEditor2.delete();
			}
		}

	}

	@Test
	public void integTestWarehousePropertiesImport() throws Exception {

		setInfosysloginInfo();
		infosys.setYdatafile("owfw7/Test2_WarehouseGroupProperties.xlsx");
		infosys.invokeYpruefstrukt();
		assertThat(infosys.getYfehlerstruktur(), is(0));
		infosys.invokeYpruefdat();
		assertThat(infosys.getYfehlerdatpruef(), is(0));
		infosys.invokeYimport();
		assertThat(infosys.getYok(), is(1));
		infosys.close();

		ArrayList<ValuePair> valPairs = new ArrayList<ValuePair>();

		valPairs.add(new ValuePair("product", "10028"));
		valPairs.add(new ValuePair("warehGrp", "20"));
		WarehouseGroupProperty wGP = getObjectSel(WarehouseGroupProperty.class, valPairs);
		assertTrue(wGP != null);

		ArrayList<ValuePair> valPairs2 = new ArrayList<ValuePair>();
		valPairs2.add(new ValuePair("product", "10028"));
		valPairs2.add(new ValuePair("warehGrp", "26"));
		WarehouseGroupProperty wGP2 = getObjectSel(WarehouseGroupProperty.class, valPairs2);
		assertTrue(wGP2 != null);

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
