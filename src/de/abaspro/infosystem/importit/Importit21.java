package de.abaspro.infosystem.importit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.abas.ceks.jedp.TransactionException;
import de.abas.eks.jfop.annotation.Stateful;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.api.gui.ButtonSet;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.ButtonEvent;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.event.EventHandler;
import de.abas.erp.axi.event.FieldEvent;
import de.abas.erp.axi.event.ObjectEventHandler;
import de.abas.erp.axi.event.listener.ButtonListenerAdapter;
import de.abas.erp.axi.event.listener.FieldListenerAdapter;
import de.abas.erp.common.type.enums.EnumDialogBox;
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
	    objectHandler.addListener(InfosystemImportit.META.yoptalwaysnew, new OptionsListener());
	    objectHandler.addListener(InfosystemImportit.META.yoptdeltab, new OptionsListener());
	    objectHandler.addListener(InfosystemImportit.META.yoptdontchangeifeq, new OptionsListener());
	    objectHandler.addListener(InfosystemImportit.META.yoptmodifiable, new OptionsListener());
	    objectHandler.addListener(InfosystemImportit.META.yoptnofop, new OptionsListener());
	    objectHandler.addListener(InfosystemImportit.META.yopttransaction, new OptionsListener());
	    objectHandler.addListener(InfosystemImportit.META.yoptuseenglvars, new OptionsListener());
	  }
	
	public class OptionsListener extends FieldListenerAdapter<InfosystemImportit> {

		@Override
		public void exit(FieldEvent<InfosystemImportit> event)
				throws EventException {
			super.exit(event);
			InfosystemImportit infosysImportit = event.getSourceRecord();
			
			if (datensatzList.size() > 0) {
				OptionCode optioncode = datensatzList.get(1).getOptionCode();
				
				Boolean alwaysNew 				= infosysImportit.getYoptalwaysnew();
				Boolean nofop  					= infosysImportit.getYoptnofop();
				Boolean inOnetransaction 		= infosysImportit.getYopttransaction();
				Boolean deletetable				= infosysImportit.getYoptdeltab();
				Boolean checkFieldIsModifiable	= infosysImportit.getYoptmodifiable();
				Boolean useEnglishVariablen		= infosysImportit.getYoptuseenglvars();
				Boolean dontChangeIfEqual		= infosysImportit.getYoptdontchangeifeq();
				
				optioncode.setOptionCode(alwaysNew, nofop, inOnetransaction, deletetable, checkFieldIsModifiable, useEnglishVariablen, dontChangeIfEqual);
				showOptions(infosysImportit, datensatzList);
				
				dontChangeIfEqual		= infosysImportit.getYoptdontchangeifeq();
				String t3 = "";
			}else {
				TextBox textbox = new TextBox(getContext(), "Fehler", "Es wurde noch kein Datei eingelesen!");
				textbox.show();
			}
				

		}

		

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
			 * Die DatensatzList wird importiert
			 * 
			 */
			private void yimportButtonInvoked(
					ButtonEvent<InfosystemImportit> event) {
				
				InfosystemImportit infosysImportit = event.getSourceRecord();
				
				try {
					logger.info("Start import der Daten");
					if (datensatzList!=null) {
						if (checkDatensatzListOfTransaction()) {
							logger.info("Start Transaction");
							edpProcessing.startTransaction();
							edpProcessing.importDatensatzListTransaction(datensatzList);
							
	//						Abfragen ob die Transaction abgebrochen werden soll
							
							TextBox textBox = new TextBox(getContext(), "Entscheidung", "Soll alles zurückgerollt werden!");
							
							textBox.setButtons(ButtonSet.NO_YES);
							EnumDialogBox button = textBox.show();
							
							if (button.equals(EnumDialogBox.Yes)) {
								logger.info("Abbruch der Transaction");
								edpProcessing.abortTransaction();
							}else {
								logger.info("Commit Transaction");
								edpProcessing.commitTransaction();
							}									
							
						}else {
							logger.info("Import ohne Transaction");
							edpProcessing.importDatensatzList(datensatzList);	
						}
						
					}else {
						TextBox textbox = new TextBox(getContext(), "Fehler", "Bitte die Datei neu einlesen");
						textbox.show();
					}
					
					logger.info("Ende import der Daten");
				} catch (ImportitException e) {
					logger.error(e);
					AbasExceptionOutput(e);
					}
				
				infosysImportit.setYok(getimportitDatasets(datensatzList)); 
				infosysImportit.setYfehler(geterrorDatasets(datensatzList));
				
				
			}
			
			private Boolean checkDatensatzListOfTransaction() throws ImportitException{
				if (datensatzList.size() >= 1 ) {
					Datensatz datensatz = datensatzList.get(1);
					return datensatz.getOptionTransaction();
				}else {
					throw new  ImportitException("Es wurde noch keine Datei eingelesen!");
				}
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
				
				infosysImportit.setYok(getimportitDatasets(datensatzList)); 
				infosysImportit.setYfehler(geterrorDatasets(datensatzList));
				
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
							String errorReport = datensatz.getErrorReport();
							
//							Alles ausgeben oder wenn yshowonlyerrorline gesetzt nur die fehlerhaften Datensätze
							
							if (!(errorReport.isEmpty() & infosysImportit.getYshowonlyerrorline()) || 
									(!infosysImportit.getYshowonlyerrorline()) ) {
								Row row = infosysImportit.table().appendRow();
								row.setYsel(datensatz.getValueOfKeyfield());
								if (datensatz.getAbasId() != null) {
									row.setString(row.META.ydatensatz, datensatz.getAbasId());
								}
								if (errorReport.isEmpty()) {
									row.setYicon("icon:ok");
								}else {
									row.setYicon("icon:stop");
									int errorReportlength = errorReport.length();
									int fieldLength = row.META.ytfehler.getLength();
									if (errorReportlength > fieldLength) {
										row.setYtfehler(errorReport.substring(0, fieldLength));	
									}else {
										row.setYtfehler(errorReport);
									}
									
									StringReader reader = new StringReader(errorReport);
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
				ExcelImportProcessing excelProcessing = new ExcelImportProcessing(infosysImportit.getYdatafile());
				datensatzList = excelProcessing.getDatensatzList();
				logger.info("Ende Excelproccessing");
				logger.info("Start checkDatensatzList");
				edpProcessing = new EdpProcessing(infosysImportit.getYserver(), infosysImportit.getYport(), infosysImportit.getYmandant(), infosysImportit.getYpasswort());
				edpProcessing.checkDatensatzList(datensatzList);
				logger.info("Ende checkDatensatzList");
				
				infosysImportit.setYok(getimportitDatasets(datensatzList)); 
				infosysImportit.setYfehler(geterrorDatasets(datensatzList));
				
				showDatenbankInfos(infosysImportit , datensatzList);
				showOptions(infosysImportit , datensatzList);
				
				
			} catch (ImportitException e) {
				AbasExceptionOutput(e);
			}	      
	    }

		
	  }
	
	private int geterrorDatasets(ArrayList<Datensatz> datensatzList2) {
		int numberOfError = 0;
		for (Datensatz datensatz : datensatzList2) {
			String error = datensatz.getErrorReport();
			if (error != null) {
				if (!datensatz.getErrorReport().isEmpty()) {
					numberOfError++;
				}
			}
		}
		return numberOfError;
	}

	private int getimportitDatasets(ArrayList<Datensatz> datensatzList2) {
		int numberOfOk = 0;
		for (Datensatz datensatz : datensatzList2) {
			String error = datensatz.getErrorReport();
			if (error != null) {
				if (datensatz.getErrorReport().isEmpty()) {
					numberOfOk++;
				}
			}else {
				numberOfOk++;
			}
		}
		
		return numberOfOk;
	}

	private void showOptions(InfosystemImportit infosysImportit, ArrayList<Datensatz> datensatzList) {
		if (datensatzList.size() >= 1 ) {
			Datensatz datensatz = datensatzList.get(1);
			OptionCode optionCode = datensatz.getOptionCode();
			infosysImportit.setYoptalwaysnew(optionCode.getAlwaysNew());
			infosysImportit.setYoptnofop(optionCode.getNofop());
			infosysImportit.setYoptmodifiable(optionCode.getCheckFieldIsModifiable());
			infosysImportit.setYoptdeltab(optionCode.getDeleteTable());
			infosysImportit.setYopttransaction(optionCode.getInOneTransaction());
			infosysImportit.setYoptuseenglvars(optionCode.getUseEnglishVariablen());
			infosysImportit.setYoptdontchangeifeq(optionCode.getDontChangeIfEqual());
			infosysImportit.setYoption(optionCode.getOptionsCode());
			}
		
	}
	
	private void showDatenbankInfos(InfosystemImportit infosysImportit,
			ArrayList<Datensatz> datensatzList) {

		if (datensatzList.size() >= 1 ) {
			Datensatz datensatz = datensatzList.get(1);
			
			infosysImportit.setYdb(datensatz.getDatenbank().toString());
			infosysImportit.setYgruppe(datensatz.getGruppe().toString());
			infosysImportit.setYtippkommando(datensatz.getTippkommando().toString());
			infosysImportit.setYtababspalte(datensatz.getTableStartsAtField());			
		}
	}

	private void AbasExceptionOutput(Exception e){
		TextBox textBox = new TextBox(getContext(), "Fehler", e.toString());
		textBox.show();
		
	}
	
}
