package de.abaspro.infosystem.importit;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.common.type.enums.EnumDialogBox;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.owjava.InfosystemImportit;
import de.abas.erp.db.infosystem.custom.owjava.InfosystemImportit.Row;
import de.abas.jfop.base.Color;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abaspro.utils.file.DirEdit;

/**
 * @author tkellermann
 * 
 * Infosystem Importit21 dient zum Importieren von Excel-Dateien 
 * nach abas
 * 
 * 
 * 
 */

@Stateful
public class Importit21 extends EventHandler<InfosystemImportit> {
	
	private ArrayList<Datensatz> datensatzList;
	private EdpProcessing edpProcessing;
	private Logger logger = Logger.getLogger(Importit21.class);
	
	static private String docuVerzStr = "java/projects/importitnew/docu";
	static private String docuVerzAufrufStr = "win/tmp/docu/";
	
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
	    objectHandler.addListener(InfosystemImportit.META.ydoku, new DokuButtonListener());
	    objectHandler.addListener(InfosystemImportit.META.ymandant, new MandantListener());
	  }
	
	
	public class MandantListener extends FieldListenerAdapter<InfosystemImportit> {
		
		@Override
		public void exit(FieldEvent<InfosystemImportit> event)
				throws EventException {
			super.exit(event);
			InfosystemImportit infosysImportit = event.getSourceRecord();
			String eigenerMandant = infosysImportit.getYeigmandant();
			ScreenControl screencontrol = getScreenCtrl();
			Color red = Color.RED;
			Color black = Color.BLACK;
			Color white = Color.WHITE;
			
			if (!infosysImportit.getYmandant().equals(eigenerMandant)) {
				screencontrol.setColor(infosysImportit, infosysImportit.META.ymandant, black  , red );
			}else {
				screencontrol.setColor(infosysImportit, infosysImportit.META.ymandant, black  , white );
			}
			
		}
		
		
	}
	
	
	 
	public class OptionsListener extends FieldListenerAdapter<InfosystemImportit> {

		@Override
		public void exit(FieldEvent<InfosystemImportit> event)
				throws EventException {
			super.exit(event);
			InfosystemImportit infosysImportit = event.getSourceRecord();
			
			if (datensatzList.size() > 0) {
				OptionCode optioncode = datensatzList.get(0).getOptionCode();
				
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
			}else {
				TextBox textbox = new TextBox(getContext(), "Fehler", "Es wurde noch kein Datei eingelesen!");
				textbox.show();
			}
				

		}

		

	}

		class DokuButtonListener  extends ButtonListenerAdapter<InfosystemImportit> {

		@Override
		public void after(ButtonEvent<InfosystemImportit> event)
				throws EventException {
			super.after(event);
			ydokuButtonInvoked(event);
		}

		private void ydokuButtonInvoked(ButtonEvent<InfosystemImportit> event) {
//		doku-Verzeichnis in win/tmp/ kopieren und danach den Browser öffnen
			InfosystemImportit infosysImportit = event.getSourceRecord();
			DbContext dbcontext = getContext();
			
//			String mandantdir = dbcontext.getEnvironmentVariable("DIR_MANDANTDIR");
//			String sharename = dbcontext.getEnvironmentVariable("SHARENAME");
			
			UserTextBuffer userTextBuffer = BufferFactory.newInstance(true).getUserTextBuffer();
			userTextBuffer.defineVar("GL50", "xtmandantdir");
			FOe.formula("U|xtmandantdir = E|MANDANTDIR");
			String mandantdir = userTextBuffer.getStringValue("xtmandantdir");

			File  docuVerz = new File(mandantdir + "/" + docuVerzStr);
			File docuVerzAufruf = new File(mandantdir + "/" + docuVerzAufrufStr);
			try {
				
			if (!docuVerzAufruf.exists()) {
				docuVerzAufruf.mkdirs();
			}
			
				if (docuVerz.exists()) {
					DirEdit.copyDir(docuVerz, docuVerzAufruf);

					if (docuVerzAufruf.exists()) {
						String path = docuVerzAufruf.getPath().toString();
						
//						file://///cebitmaster40/entw-fepco/owdoku/docu/Dokumentation.html
						String url = "-FILE " +  docuVerzAufrufStr + "Dokumentation.html";
						FOe.browser(url);
					}
				}
			} catch (IOException e) {
				abasExceptionOutput(e);
			}
		}
		
					
					
				
					
					
	}


	
	/**
		 * 
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
					if (checkDatensatzListNotEmpty()) {
						if (checkDatensatzListOfTransaction()) {
							logger.info("Start Transaction");
							edpProcessing.startTransaction();
							edpProcessing.importDatensatzListTransaction(datensatzList);
							
	//						Abfragen ob die Transaction abgebrochen werden soll
							
							TextBox textBox = new TextBox(getContext(), "Entscheidung", "Es sind " + geterrorDatasets(datensatzList) + " Fehler aufgetreten \nSoll alles zurückgerollt werden!");
							
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
						TextBox textbox = new TextBox(getContext(), "Fehler", "Strukturprüfung wurde nicht durchgeführt!");
						textbox.show();
					}
					
					logger.info("Ende import der Daten");
				} catch (ImportitException e) {
					logger.error(e);
					abasExceptionOutput(e);
					}
				
				infosysImportit.setYok(getimportitDatasets(datensatzList)); 
				infosysImportit.setYfehler(geterrorDatasets(datensatzList));
				
				
			}
			
			private Boolean checkDatensatzListOfTransaction() throws ImportitException{
				if (datensatzList.size() >= 1 ) {
					Datensatz datensatz = datensatzList.get(0);
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
			if (checkDatensatzListNotEmpty()) {
				try {
					 
					if (infosysImportit.getYfehlerstruktur() == 0 ) {
						edpProcessing.checkDatensatzListValues(datensatzList);
						infosysImportit.setYok(getimportitDatasets(datensatzList));
						infosysImportit
								.setYfehlerdatpruef(geterrorDatasets(datensatzList));
					}else {
						throw new ImportitException("Es sind noch Fehler aus der Strukturprüfung vorhanden! Bitte zuerst beheben!");
					}
					
				} catch (ImportitException e) {
					
					abasExceptionOutput(e);
				}
				TextBox textbox = new TextBox(getContext(), "Fertig", "Datenprüfung abgeschlossen!");
				textbox.show();	
			}else {
				TextBox textbox = new TextBox(getContext(), "Fehler", "Strukturprüfung wurde nicht durchgeführt!");
				textbox.show();
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
					if (checkDatensatzListNotEmpty()) {
							for (Datensatz datensatz : datensatzList) {
								if (datensatz.getTippkommando() == null) {
									datensatz.createErrorReport();
									String errorReport = datensatz
											.getErrorReport();

									//							Alles ausgeben oder wenn yshowonlyerrorline gesetzt nur die fehlerhaften Datensätze

									if (!(errorReport.isEmpty() & infosysImportit
											.getYshowonlyerrorline())
											|| (!infosysImportit
													.getYshowonlyerrorline())) {
										Row row = infosysImportit.table()
												.appendRow();
										row.setYsel(datensatz
												.getValueOfKeyfield());
										if (datensatz.getAbasId() != null) {
											row.setString(Row.META.ydatensatz,
													datensatz.getAbasId());
										}
										if (errorReport.isEmpty()) {
											row.setYicon("icon:ok");
										} else {
											row.setYicon("icon:stop");
											int errorReportlength = errorReport
													.length();
											int fieldLength = Row.META.ytfehler
													.getLength();
											if (errorReportlength > fieldLength) {
												row.setYtfehler(errorReport
														.substring(0,
																fieldLength));
											} else {
												row.setYtfehler(errorReport);
											}

											StringReader reader = new StringReader(
													errorReport);
											row.setYkomtext(reader);
										}
									}

								} else {
									//							Tippkommando
									datensatz.createErrorReport();
									String errorReport = datensatz
											.getErrorReport();
									if (!(errorReport.isEmpty() & infosysImportit
											.getYshowonlyerrorline())
											|| (!infosysImportit
													.getYshowonlyerrorline())) {
										Row row = infosysImportit.table()
												.appendRow();

										row.setYsel("Tippkommando "
												+ datensatz.getTippkommando()
												+ " "
												+ "Datensatznummer "
												+ datensatzList
														.indexOf(datensatz));

										if (errorReport.isEmpty()) {
											row.setYicon("icon:ok");
										} else {
											row.setYicon("icon:stop");
											int errorReportlength = errorReport
													.length();
											int fieldLength = Row.META.ytfehler
													.getLength();
											if (errorReportlength > fieldLength) {
												row.setYtfehler(errorReport
														.substring(0,
																fieldLength));
											} else {
												row.setYtfehler(errorReport);
											}

											StringReader reader = new StringReader(
													errorReport);
											row.setYkomtext(reader);
										}
									}

									//							if (datensatz.getImportError() == null) {
									//								row.setYicon("icon:ok");
									//							}else {
									//								row.setYicon("icon:stop");
									//								row.setYtfehler(datensatz.getImportError().substring(0, 70));
									//								StringReader reader = new StringReader(datensatz.getImportError());
									//								row.setYkomtext(reader);
									//							}
								}

							}
						
					}else {
						if (infosysImportit.getYdatafile().isEmpty()) {
							TextBox textBox = new TextBox(getContext(),
									"Fehler",
									"Es wurde  keine Excel-Datei eingetragen.");
							textBox.show();
						}else {
							TextBox textBox = new TextBox(getContext(),
									"Fehler",
									"Es wurde für die Excel-Datei noch keine Strukturprüfung durchgeführt.");
							textBox.show();
						}
					}
				} catch (ImportitException e) {

					abasExceptionOutput(e);
				} catch (IOException e) {
					abasExceptionOutput(e);
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
			infosysImportit.setYfehlerdatpruef(0);
			infosysImportit.setYfehlerstruktur(0);
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
				
				
				infosysImportit.setYfehlerstruktur(geterrorDatasets(datensatzList));
				
				showDatenbankInfos(infosysImportit , datensatzList);
				showOptions(infosysImportit , datensatzList);
				
				TextBox textbox = new TextBox(getContext(), "Fertig", "Strukturprüfung abgeschlossen!");
				textbox.show();
				
			} catch (ImportitException e) {
				abasExceptionOutput(e);
			}	      
	    }

		
	  }
	
	private int geterrorDatasets(ArrayList<Datensatz> datensatzList2) {
		int numberOfError = 0;
		if (checkDatensatzListNotEmpty()) {
			for (Datensatz datensatz : datensatzList2) {
				datensatz.createErrorReport();
				String error = datensatz.getErrorReport();
				if (error != null) {
					if (!error.isEmpty()) {
						numberOfError++;
					}
				}
			}
		}
		return numberOfError;
	}

	/**
	 * Überprüft ob die Datensatzliste gefüllt ist.
	 * 
	 * @return true wenn Datesatz List gefüllt ist.
	 */
	private boolean checkDatensatzListNotEmpty() {
		
		if (datensatzList != null) {
			if (datensatzList.size() > 0) {
				return true;
			}
		}
		return false;	
			
	}

	private int getimportitDatasets(ArrayList<Datensatz> datensatzList2) {
		int numberOfOk = 0;
		if (checkDatensatzListNotEmpty()) {
			for (Datensatz datensatz : datensatzList2) {
				datensatz.createErrorReport();
				String error = datensatz.getErrorReport();
				if (error != null) {
					if (error.isEmpty()) {
						numberOfOk++;
					}
				} else {
					numberOfOk++;
				}
			}
		}
		return numberOfOk;
	}

	private void showOptions(InfosystemImportit infosysImportit, ArrayList<Datensatz> datensatzList) {
		if (datensatzList.size() >= 1 ) {
			Datensatz datensatz = datensatzList.get(0);
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
			Datensatz datensatz = datensatzList.get(0);
			
			if (datensatz.getDatenbank() != null ) {
				infosysImportit.setYdb(datensatz.getDatenbank().toString());
			}
			
			if (datensatz.getGruppe() !=null ) {
				infosysImportit.setYgruppe(datensatz.getGruppe().toString());
			}
			
			if (datensatz.getTippkommando() != null) {
				infosysImportit.setYtippkommando(datensatz.getTippkommando().toString());	
			}
			infosysImportit.setYtababspalte(datensatz.getTableStartsAtField() + 1);			
		}
	}

	private void abasExceptionOutput(Exception e){
		TextBox textBox = new TextBox(getContext(), "Fehler", e.toString());
		textBox.show();
		
	}
	
}
