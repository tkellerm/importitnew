package de.abaspro.infosystem.importit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.abas.eks.jfop.annotation.Stateful;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.ButtonEvent;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.event.EventHandler;
import de.abas.erp.axi.event.ObjectEventHandler;
import de.abas.erp.axi.event.listener.ButtonListenerAdapter;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.infosystem.custom.owjava.InfosystemImportit;
import de.abas.erp.db.infosystem.custom.owjava.InfosystemImportit.Row;
import de.abas.erp.db.selection.Selection;
@Stateful
public class Importit21 extends EventHandler<InfosystemImportit> {
	
	ArrayList<Datensatz> datensatzList;
	EdpProcessing edpProcessing;
	private Logger logger = Logger.getLogger(Importit21.class);
	

	
	public Importit21() throws IOException {
		super(InfosystemImportit.class);
	}

	@Override
	  protected void configureEventHandler(ObjectEventHandler<InfosystemImportit> objectHandler) {
	    super.configureEventHandler(objectHandler);
	    // add user defined listener
	    objectHandler.addListener(InfosystemImportit.META.ypruefstrukt, new PruefStrukturButtonListener());
	    objectHandler.addListener(InfosystemImportit.META.yintabladen, new IntabladenButtonListener());
	    objectHandler.addListener(InfosystemImportit.META.ypruefdat, new PruefDatenButtonListener());
	    objectHandler.addListener(InfosystemImportit.META.yimport, new ImportButtonListener());
	    
	  }
	
	/**
	 * @author tkellermann
	 * 
	 * Unterklasse für den Start des Datenimports
	 *
	 */
	class ImportButtonListener extends ButtonListenerAdapter<InfosystemImportit>{

	    @Override
	    public void after(ButtonEvent<InfosystemImportit> event) throws EventException {
	      super.after(event);
	      yimportButtonInvoked(event);
	    }
	
		/**
		 * @param event
		 * 
		 * Daten importieren
		 * 
		 */
		private void yimportButtonInvoked(
				ButtonEvent<InfosystemImportit> event) {
			
			InfosystemImportit infosysImportit = event.getSourceRecord();
			
			try {
				logger.debug("Start import der Daten");
				edpProcessing.importDatensatzList(datensatzList);
				logger.debug("Ende import der Daten");
			} catch (ImportitException e) {
				AbasExceptionOutput(e);
			}
			
			infosysImportit.setYok(getimportitDatasets(datensatzList)); 
			infosysImportit.setYfehler(geterrorDatasets(datensatzList));
			
		}


	}
	
	/**
	 * @author tkellermann
	 *
	 * Unterklasse um die Prüfung der Daten aus der Liste datensatzList auf Inhaltliche Fehler prüfen.
	 * 
	 * -Feldlänge
	 * -Datum
	 * -Zahlformate
	 * 
	 * -nicht prüfen der Verweisfelder
	 *  
	 *
	 */
	class PruefDatenButtonListener extends ButtonListenerAdapter<InfosystemImportit>{

	    @Override
	    public void after(ButtonEvent<InfosystemImportit> event) throws EventException {
	      super.after(event);
	      logger.debug("Start prüfen der Daten");
	      String name = logger.getName();
	      Level loglevel = logger.getLevel();
	      ypruefdatButtonInvoked(event);
	      logger.debug("Ende prüfen der Daten");
	    }
	
	
	
	
		private void ypruefdatButtonInvoked(
				ButtonEvent<InfosystemImportit> event) {
			InfosystemImportit infosysImportit = event.getSourceRecord();
			try {
				 
				edpProcessing.checkDatensatzListValues(datensatzList);
				
			} catch (ImportitException e) {
				
				AbasExceptionOutput(e);
			}
			
			
			
			
		}


	}

		class IntabladenButtonListener extends ButtonListenerAdapter<InfosystemImportit>{

	    @Override
	    public void after(ButtonEvent<InfosystemImportit> event) throws EventException {
	      super.after(event);
	      yintabladenButtonInvoked(event);
	    }
	    
	    public void yintabladenButtonInvoked(ButtonEvent<InfosystemImportit> event) {
//			Ausgabe der Datensätze in der Tabelle
			
			InfosystemImportit infosysImportit = event.getSourceRecord();
			
			infosysImportit.table().clear();
				try {
					for (Datensatz datensatz : datensatzList) {
						if (datensatz.getTippkommando()== null) {
							datensatz.createErrorReport();
//					Es ist ein Datenbank-Kommando
							if ((datensatz.getError() != null & infosysImportit.getYshowonlyerrorline()) || 
									(!infosysImportit.getYshowonlyerrorline()) ) {
//								Alles ausgeben oder wenn yshowonlyerrorline gesetzt nur die fehlerhaften Datensätze 
								Row row = infosysImportit.table().appendRow();
								row.setYsel(datensatz.getValueOfKeyfield());
								if (datensatz.getAbasId() != null) {
									row.setString(row.META.ydatensatz, datensatz.getAbasId());
								}
								if (datensatz.getErrorReport() == null) {
									row.setYicon("icon:ok");
								}else {
									row.setYicon("icon:stop");
									int errorReportlength = datensatz.getErrorReport().length();
									int fieldLength = row.META.ytfehler.getLength();
									if (errorReportlength > fieldLength) {
										row.setYtfehler(datensatz.getErrorReport().substring(0, fieldLength));	
									}else {
										row.setYtfehler(datensatz.getErrorReport());
									}
									
									StringReader reader = new StringReader(datensatz.getErrorReport());
									row.setYkomtext(reader);
								}
							}
							
							
						}else {
//							Tippkommando
							Row row = infosysImportit.table().appendRow();
							row.setYsel("Tippkommando " + datensatz.getTippkommando() + " "  + "Datensatznummer " + datensatzList.indexOf(datensatz));
							if (datensatz.getError() == null) {
								row.setYicon("icon:ok");
							}else {
								row.setYicon("icon:stop");
								row.setYtfehler(datensatz.getError().substring(0, 70));
								StringReader reader = new StringReader(datensatz.getError());
								row.setYkomtext(reader);
							}
						}
						
					}
				} catch (ImportitException e) {

					AbasExceptionOutput(e);
				} catch (IOException e) {
					AbasExceptionOutput(e);
				}
			}
			
		}
		
		
		
		
	    class PruefStrukturButtonListener extends ButtonListenerAdapter<InfosystemImportit>{

	    @Override
	    public void after(ButtonEvent<InfosystemImportit> event) throws EventException {
	      super.after(event);
	      ypruefstruktButtonInvoked(event);
	    }

	    private void ypruefstruktButtonInvoked(ButtonEvent<InfosystemImportit> event) {
	    	
	    	InfosystemImportit infosysImportit = event.getSourceRecord();
	    	infosysImportit.table().clear();
	    	infosysImportit.setYok(0); 
			infosysImportit.setYfehler(0);
	    	try {
//	    		prüfe noch ob passwort eingeben wurde 
	    		logger.info("Start Excelproccessing");
				ExcelProcessing excelProcessing = new ExcelProcessing(infosysImportit.getYdatafile());
				datensatzList = excelProcessing.getDatensatzList();
				logger.info("Ende Excelproccessing");
				logger.info("Start checkDatensatzList");
				edpProcessing = new EdpProcessing(infosysImportit.getYserver(), infosysImportit.getYport(), infosysImportit.getYmandant(), infosysImportit.getYpasswort());
				edpProcessing.checkDatensatzList(datensatzList);
				logger.info("Ende checkDatensatzList");
				String daten = "test2"; 
				
				infosysImportit.setYok(getimportitDatasets(datensatzList)); 
				infosysImportit.setYfehler(geterrorDatasets(datensatzList));
				
			} catch (ImportitException e) {
				AbasExceptionOutput(e);
			}
	      
	    }
	  }
	

	private int geterrorDatasets(ArrayList<Datensatz> datensatzList2) {
		int numberOfError = 0;
		for (Datensatz datensatz : datensatzList2) {
			String error = datensatz.getError();
			if (error != null) {
				if (!datensatz.getError().isEmpty()) {
					numberOfError++;
				}
			}
		}
		return numberOfError;
	}

	private int getimportitDatasets(ArrayList<Datensatz> datensatzList2) {
		int numberOfOk = 0;
		for (Datensatz datensatz : datensatzList2) {
			String error = datensatz.getError();
			if (error != null) {
				if (datensatz.getError().isEmpty()) {
					numberOfOk++;
				}
			}else {
				numberOfOk++;
			}
		}
		
		return numberOfOk;
	}

	private void AbasExceptionOutput(Exception e){
		TextBox textBox = new TextBox(getContext(), "Fehler", e.toString());
		textBox.show();
		
	}
	
}
