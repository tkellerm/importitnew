package de.abaspro.infosystem.importit;

import java.util.ArrayList;

import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.ButtonEvent;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.event.EventHandler;
import de.abas.erp.axi.event.ObjectEventHandler;
import de.abas.erp.axi.event.listener.ButtonListenerAdapter;
import de.abas.erp.db.infosystem.custom.owjava.InfosystemImportit;

public class Importit21 extends EventHandler<InfosystemImportit> {

	public Importit21() {
		super(InfosystemImportit.class);

	}

	@Override
	  protected void configureEventHandler(ObjectEventHandler<InfosystemImportit> objectHandler) {
	    super.configureEventHandler(objectHandler);
	    // add user defined listener
	    objectHandler.addListener(InfosystemImportit.META.ypruefstrukt, new PruefStrukturButtonListener());
	  }

	  class PruefStrukturButtonListener extends ButtonListenerAdapter<InfosystemImportit>{

	    @Override
	    public void after(ButtonEvent<InfosystemImportit> event) throws EventException {
	      super.after(event);
	      ypruefstruktButtonInvoked(event);
	    }

	    private void ypruefstruktButtonInvoked(ButtonEvent<InfosystemImportit> event) {
	    	
	    	InfosystemImportit infosysImportit = event.getSourceRecord();
	    	try {
	    		
				ExcelProcessing excelProcessing = new ExcelProcessing(infosysImportit.getYdatafile());
				ArrayList<Datensatz> datensatzList = excelProcessing.getDatensatzList();
				
				
			} catch (ImportitException e) {
				TextBox textBox = new TextBox(getContext(), "Fehler", e.toString());
				textBox.show();
			}
	      
	    }
	  }

	
	
}
