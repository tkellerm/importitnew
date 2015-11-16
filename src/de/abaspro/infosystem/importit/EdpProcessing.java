package de.abaspro.infosystem.importit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.CantChangeFieldValException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadFieldPropertyException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEKSArtInfo;
import de.abas.ceks.jedp.EDPEditAction;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPEditorOption;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPRowAddress;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPStoreRowMode;
import de.abas.ceks.jedp.EDPTools;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.erp.common.type.enums.EnumTypeCommands;

public class EdpProcessing {

	private String server;
	private Integer port;
	private String mandant;
	private String passwort;
	private EDPSession edpSession;
	
	public EdpProcessing(String server, Integer port, String mandant,
			String passwort) {
		super();
		this.server = server;
		this.port = port;
		this.mandant = mandant;
		this.passwort = passwort;
		this.edpSession = null;
	}
	
	public void startEdpSession() throws ImportitException {
		
		this.edpSession = sessionAufbauen(this.server, this.port, this.mandant, this.passwort);
	}

	public void closeEdpSession(){
		
		if (this.edpSession.isConnected()) {
			this.edpSession.endSession();
		}
		
	}
	
	private EDPSession sessionAufbauen(String server, int port, String mandant, String passwort) throws ImportitException 
    { 
      EDPSession  session = EDPFactory.createEDPSession ();
      if (server != null && 
    		  port != 0 && 
    		  mandant != null && 
    		  passwort != null) {
    	  
    	  		try {
    	  			session.beginSession(server , port, mandant, passwort, "ImportIt_21");
    	  		} catch (CantBeginSessionException ex) 
    	  			{
    	  			Logger.getLogger(Importit21.class.getName()).log(Level.SEVERE, null, ex);
    	  			throw new ImportitException("FEHLER\n EDP Session kann nicht gestartet werden\n" , ex);
    	  			}
                 
              session.setVariableLanguage(EDPVariableLanguage.ENGLISH);
        return session;
	}else {
		throw new ImportitException("Es wurde leider nicht alle Felder (Server , Port , Mandant , Passwort) gefüllt! Dadurch konnte keine EDP-Session aufgebaut werden" );
	}
	        
    }
	
	
	public void checkDatensatzList(ArrayList<Datensatz> datensatzList) throws ImportitException {
		
		if (datensatzList != null) {
			
			if (!datensatzList.isEmpty()) {
			startEdpSession();
//			Annahme: alles Datensätze in der Liste sind gleich von der Struktur, dann sollte auch nur der Erste geprüft werden	
//			Sonst ist die Laufzeit zu lange, wenn bei einer großen Excelliste für jeden Datensatz die Struktur geprüft werden soll
				
				    Datensatz datensatz = datensatzList.get(0);
				    
				    if (datensatz != null) {
				    	if (checkDatensatzStruktur(datensatz)) {
				    		
							for (Datensatz datensatzIter : datensatzList) {
								
//								Alle Abastypen aus dem ersten Datensatz in alle Felder füllen
								datensatzIter.copyAbasTypInDatensatz(datensatz);
								
							}
				    		
						}
				    	
					}

				closeEdpSession();
			}else {
				throw new ImportitException("Die übergebene Datensatzliste ist leer");
			}
			
		}else {
			throw new ImportitException("Die übergebene Datensatzliste war nicht definiert");
		}
		
		
		
	}

	private boolean checkDatensatzStruktur(Datensatz datensatz){

		//		prüfe Datenbank, ob vorhanden
		boolean dbok = checkDBorEditorCommdands(datensatz);
//		Wenn es ein Tipkommanndo gibt muss noch die Datenbank gesucht werden damit die Felder gecheckt werden können
		
				
		List<Feld> kopfFelder     = datensatz.getKopfFelder();
		List<Feld> tabellenFelder = datensatz.getTabellenFelder();
	
		boolean kopfok    = false;
		boolean tabelleok = false;
		try {
			kopfok = checkListofFeldAndGetabasType(kopfFelder, datensatz.getDatenbank() , datensatz.getGruppe() , false, datensatz.getOptionCode().getUseEnglishVariablen() );
			tabelleok = checkListofFeldAndGetabasType(tabellenFelder, datensatz.getDatenbank() , datensatz.getGruppe(), true, datensatz.getOptionCode().getUseEnglishVariablen());
		
		} catch (ImportitException e) {
			datensatz.appendError("Die Strukturprüfung war fehlerhaft" , e);
		}
				
		if (tabelleok & kopfok & dbok) {
			return true;	
		}else {
			return false;
		}
			
		
	}

	private boolean checkDBorEditorCommdands(Datensatz datensatz) {
		
		Boolean gefunden = false ;
		if (datensatz.getTippkommando()!=null && datensatz.getDatenbank() == null) {
//		Überprüfung der Tipkkommandos
		
			
			EnumTypeCommands[] typeCommands = EnumTypeCommands.values();
			for (EnumTypeCommands enumTypeCommands : typeCommands) {
				if (datensatz.getTippkommando() == enumTypeCommands.getCode()) {
					gefunden = true;

				}
			}
			if (gefunden) {
//				datenbank für Tipkkommando in Datensatz eintragen
				
				try {
					findDatenbankForTipkkommando(datensatz);
				} catch (ImportitException e) {
					datensatz.appendError(e);
				}
				
				
			}else {
				datensatz.appendError("Das Tippkommando mit der Nummer "
						+ datensatz.getTippkommando() + " ist nicht definiert");
			}
		}
		
		if (datensatz.getDatenbank()!= null && datensatz.getGruppe()!= null && datensatz.getTippkommando()!=null ) {
//			Überprüfung ob Datenbank vorhanden
			String key = "";
			int aliveFlag = EDPConstants.ALIVEFLAG_ALIVE;
			String fieldNames = "";
			
//			Vartab Tabellenname
			String tableName = "12:26";
			
			EDPQuery query = this.edpSession.createQuery();
			String krit = "0:grpDBDescr=(" + datensatz.getDatenbank().toString() + ");0:grpGrpNo=" + datensatz.getGruppe().toString() +    ";" +  ";@englvar=true;@language=en";

			
				try {
					
					query.startQuery(tableName, key, krit, true, aliveFlag, true, true, fieldNames, 0, 10000);
					query.getLastRecord();
					if (query.getRecordCount() == 1) {
						gefunden = true;
					}
					
				} catch (InvalidQueryException e) {
					datensatz.appendError("Die Suche nach der Datenbank mit dem Suchstring " + krit + "war fehlerhaft" , e);

				}
				
			if (!gefunden) {
				datensatz.appendError("Die Datenbank : Gruppe mit der Nummer "
						+ datensatz.getDatenbank() + ":"
						+ datensatz.getGruppe() + " ist nicht definiert");
			}
		}
		
		return gefunden;
	}

	private void findDatenbankForTipkkommando(Datensatz datensatz) throws ImportitException {
		
		if (datensatz.getTippkommando()!=null) {
//			EDPSession und EPDEditor für dieses Tippkommando öffnen und aus dem Dialog die Datenbank auslesen
			
				startEdpSession();
				if (this.edpSession != null ) {
					EDPEditor edpEditor = this.edpSession.createEditor();
					try {
						
						edpEditor.beginEditCmd(datensatz.getTippkommando().toString(), "");
						datensatz.setDatenbank(edpEditor.getEditDatabaseNr());
						datensatz.setGruppe(edpEditor.getEditGroupNr());
						edpEditor.endEditCancel();
						
					} catch (CantBeginEditException e) {
						throw new ImportitException("Das Holen der Datenbank zu dem Tippkommando war nicht möglich ", e );
						}
						
				}
				
		}		
		
	}

	/**
	 * @param feldListe
	 * Liste der zu prüfenden Felder
	 * @param database
	 * Datenbank
	 * @param group
	 * Datenbankgruppe
	 * @param inTab
	 * Suche in Tabellenfeldern
	 * @param englVarNames
	 * 
	 * true : es wird nach englischen Variablenname gesucht.
	 * false : es wird nach deutschen Variablennamen gesucht.  
	 * 
	 * @return
	 * Wenn alles gut dann true
	 * @throws ImportitException
	 * Es gibt die folgenden Fälle für eine Exeption
	 * kein Treffer für eine Variable
	 * mehrere Treffer für eine Variable 
	 * falsche Selektion
	 * 
	 */
	private boolean checkListofFeldAndGetabasType(List<Feld> feldListe, Integer database , Integer group,  Boolean inTab , Boolean englVarNames ) throws ImportitException {
		
		String varNameVarName = "varName";
		String varNameVarNameNew = "varNameNew";
		String varNameVarNameEnglish = "varNameEnglish"; 
		String varNameVarTypeNew = "varTypeNew";
		String varNameVarType = "varType";
		String varNameVarLength = "vitle";

		if (feldListe !=null ) {
			EDPQuery query;
			for (Feld feld : feldListe) {
//				die Suche nach der Variablen wird über einen regulären Ausdruck gesucht . "~/==.{2}variablenname$" 
//				Der Ausdruck besagt das die ersten 2 Stellen ignoriert werden sollen und der String mit dem Veriablennamen beendet ist

					int recordCount=0;
					if (englVarNames) {
//						Bei englischen Variablen soll nur nach den englischen gesucht werden
						
						query = getEDPQueryVariable(feld.getName() , database , group , varNameVarNameEnglish , inTab);
						query.getLastRecord();
						recordCount = query.getRecordCount();
						
					}else {
						
//						bei der Suche deutschen Variablenamen wird zunächst nach den "neuen Variablen" (vnname) gesucht
						query = getEDPQueryVariable(feld.getName() , database , group , varNameVarNameNew , inTab);
						query.getLastRecord();
						recordCount = query.getRecordCount();
						
//						und wenn es keinen Treffer gab nach dem normalen Feld "vname"
						
						if (recordCount == 0) {
							query = getEDPQueryVariable(feld.getName() , database , group , varNameVarName , inTab);
							query.getLastRecord();
							recordCount = query.getRecordCount();
						}
						
					}
						
					if (recordCount > 1) {
						closeEdpSession();
						throw new ImportitException("Es wurde für die Variable " + feld.getName() + " mehrere Treffer gefunden! \n " );
					}else if (recordCount == 0) {
						closeEdpSession();
						throw new ImportitException("Die Variable " + feld.getName() + " ist nicht in der Vartab V-" + database +"-" + group + " vorhanden! \n " );
					}else {
//						Falls der neue Typ gesetzt ist, dann zuerst nach ihm suchen 
						String abasTyp = query.getField(varNameVarTypeNew);
						
						if (abasTyp.isEmpty()) {
							abasTyp = query.getField(varNameVarType);
						}
						feld.setAbasTyp(abasTyp);
						feld.setAbasFieldLength(getAbasFieldLength(abasTyp));
					}
				
			}
			
			return false;
			
		}else if (inTab) {
//			ein leerer Tabellensatz ist erlaubt
			return true;
		} else {
			throw new ImportitException("Die übergebene Feldliste für den Kopf war leer! Das funktioniert nicht");
		}
			 
	}

	private EDPQuery getEDPQueryVariable(String feldname, Integer database,
			Integer group, String suchfeld, Boolean inTab) throws ImportitException {
		
		if (!this.edpSession.isConnected()) {
			startEdpSession();
		}
		int aliveFlag = EDPConstants.ALIVEFLAG_BOTH;
		String[] fieldNames = {"varName" , "varNameNew" , "inTab" , "varType" , "varTypeNew"};
		String key = "";
		String tableName = "12:26";
		
		EDPQuery query = this.edpSession.createQuery();
		String krit = "0:grpDBDescr=(" + database.toString() + ");0:grpGrpNo=" + group +    ";" + suchfeld + "~/==\\b[a-z]{2}"  + feldname + "\\b;" + "1:inTab" + "=" + inTab.toString() + ";@englvar=true;@language=en";		
		
		try {
			query.startQuery(tableName, key, krit, true, aliveFlag, true, true, fieldNames, 0, 10000);
		
		} catch (InvalidQueryException e) {
			closeEdpSession();
			throw new ImportitException( "fehlerhafter Selektionsstring: " + krit , e);
		}
		
		return query;
	}

	public void importDatensatzList(ArrayList<Datensatz> datensatzList) throws ImportitException {
		startEdpSession();
		
		for (Datensatz datensatz : datensatzList) {
			writeDatensatzToAbas(datensatz);
			
			
		}
		closeEdpSession();
	}

	private void writeDatensatzToAbas(Datensatz datensatz) {
		
		if (this.edpSession.isConnected()) {
			EDPEditor edpEditor = this.edpSession.createEditor();
			try {
				
			if (datensatz.getTippkommando()== null ) {
//				Verarbeitung der normalen Datenbanken
				writeDatabase(datensatz, edpEditor);
				
			}else {
//				Verarbeitung der TippKommandos
				writeTippKommandos(datensatz, edpEditor);
				
			}
				
			} catch (CantBeginEditException e) {
			
				datensatz.appendError(e);
			} catch (ImportitException e) {
			
				datensatz.appendError(e);
			} catch (InvalidQueryException e) {
			
				datensatz.appendError(e);
			} catch (CantSaveException e) {
			
				datensatz.appendError(e);
			} catch (CantChangeSettingException e) {
				
				datensatz.appendError(e);
			}finally{
				if (edpEditor.isActive()) {
					edpEditor.endEditCancel();
				}
			}
			
		}
		
	}

	private void writeTippKommandos(Datensatz datensatz, EDPEditor edpEditor) throws ImportitException, CantChangeSettingException, CantSaveException, CantBeginEditException {
		// TODO Auto-generated method stub
		edpEditor.beginEditCmd(datensatz.getTippkommando().toString(), "");
		setEditorOption(datensatz, edpEditor);
		writeFieldsInEditor(datensatz, edpEditor);
		edpEditor.endEditSave();
	}

	/**
	 * @param datensatz
	 * @param edpEditor
	 * @throws CantBeginEditException
	 * @throws CantChangeSettingException
	 * @throws ImportitException
	 * @throws CantSaveException
	 * @throws InvalidQueryException
	 */
	private void writeDatabase(Datensatz datensatz, EDPEditor edpEditor)
			throws CantBeginEditException, CantChangeSettingException,
			ImportitException, CantSaveException, InvalidQueryException {
		if (datensatz.getOptionCode().getAlwaysNew()) {
			setEditorOption(datensatz, edpEditor);
			edpEditor.beginEditNew(datensatz.getDatenbank().toString(),
					datensatz.getGruppe().toString());
			writeFieldsInEditor(datensatz, edpEditor);
			edpEditor.saveReload();
			String abasId = edpEditor.getEditRef();
			datensatz.setAbasId(abasId);
			edpEditor.endEditSave();
		} else {

			//				selektiere nach dem Schlüsselfeld
			EDPQuery edpQuery = this.edpSession.createQuery();

			String tableName = datensatz.getDatenbank().toString()
					+ ":" + datensatz.getGruppe().toString();

			String key = datensatz.getKeyOfKeyfield();

			if (key == null) {
				key = "";
			}
			String krit = "";
			//				datensatz.getOptionCode().getUseEnglishVariablen()
			if (datensatz.getOptionCode().getUseEnglishVariablen()) {
				krit = datensatz.getNameOfKeyfield() + "="
						+ datensatz.getValueOfKeyfield()
						+ ";@englvar=true;@language=en";
				edpQuery.startQuery(tableName, key, krit, "idno,swd,id");
			} else {
				krit = datensatz.getNameOfKeyfield() + "="
						+ datensatz.getValueOfKeyfield()
						+ ";@englvar=false;@language=de";
				edpQuery.startQuery(tableName, key, krit,
						"nummer,such,id");
			}

			edpQuery.getLastRecord();
			int recordCount = edpQuery.getRecordCount();
			if (recordCount == 1 || recordCount == 0) {
				setEditorOption(datensatz, edpEditor);
				if (recordCount == 1) {
					//				Eröffne eine Editor fals kein oder 1 Datensatz gefunden wurde 	
					edpEditor.beginEdit(edpQuery.getField("id"));
				}else {
					edpEditor.beginEditNew(datensatz.getDatenbank().toString(),
							datensatz.getGruppe().toString());
				}
				writeFieldsInEditor(datensatz, edpEditor);
				edpEditor.saveReload();
				String abasId = edpEditor.getEditRef();
				datensatz.setAbasId(abasId);
				edpEditor.endEditSave();
			} else {

				datensatz
						.appendError("Selektion auf Datensatz war nicht eindeutig! Folgende Selektion wurde verwendet :"
								+ krit);
			}

		}
	}

	/**
	 * @param datensatz
	 * 
	 * @param edpEditor
	 * 
	 * @throws CantChangeSettingException
	 *  
	 */
	private void setEditorOption(Datensatz datensatz, EDPEditor edpEditor) throws CantChangeSettingException {
		
		OptionCode optionCode = datensatz.getOptionCode();
		
		if (optionCode!= null) {
			
			if (optionCode.getNofop()) {
				edpEditor.getSession().setFOPMode(false);
			}else {
				edpEditor.getSession().setFOPMode(true);
			}
//			Tabelle löschen 
			if (optionCode.getDeleteTable()) {
				
				edpEditor.setEditorOption(EDPEditorOption.STOREROWMODE, EDPStoreRowMode.DELETE_TAIL.getModeStr());
			}else {
				edpEditor.setEditorOption(EDPEditorOption.STOREROWMODE, EDPStoreRowMode.DELETE_NONE.getModeStr());
			}
//			Englische Variablen nutzen
			if (optionCode.getUseEnglishVariablen()) {
				edpEditor.getSession().setVariableLanguage(EDPVariableLanguage.ENGLISH);
			}else {
				edpEditor.getSession().setVariableLanguage(EDPVariableLanguage.GERMAN);
			}
		}
		
	}

	/**
	 * @param datensatz
	 * @param edpEditor
	 * @throws ImportitException
	 */
	
	private void writeFieldsInEditor(Datensatz datensatz, EDPEditor edpEditor) throws ImportitException {
		
		if (edpEditor.isActive()) {
			
//			Kopffelder schreiben
			
			List<Feld> kopfFelder = datensatz.getKopfFelder();
			for (Feld feld : kopfFelder) {
				
				writeField(datensatz, feld, edpEditor, 0);
				
				}
			
//			TabelleFelder schreiben
			
			List<DatensatzTabelle> tabellenZeilen = datensatz.getTabellenzeilen();
			
			if (tabellenZeilen !=null && edpEditor.hasTablePart()) {
				for (DatensatzTabelle datensatzTabelle : tabellenZeilen) {
					Integer rowNumbervorher = edpEditor.getRowCount();
					try {
						if (rowNumbervorher == 0) {
							edpEditor.insertRow(1);
						}else {
							edpEditor.insertRow(rowNumbervorher + 1);	
						}
						
					} catch (InvalidRowOperationException e) {
						throw new ImportitException("Die Zeilen konnten nicht eingefügt werden!" ,e );
					}
					Integer rowNumber = edpEditor.getCurrentRow();
					ArrayList<Feld> tabellenFelder = datensatzTabelle.getTabellenFelder();
					for (Feld feld : tabellenFelder) {
						writeField(datensatz, feld, edpEditor, rowNumber);
					
					}
				}
			}				
			}		
		}

	/**
	 * @param datensatz
	 * @param edpEditor
	 * @param feld
	 * @param rowNumber
	 * @throws ImportitException
	 */
	private void writeField(Datensatz datensatz, Feld feld, EDPEditor edpEditor , Integer rowNumber )
			throws ImportitException {
		/**
		 *Falls das Feld mit Skip gekennzeichnet ist wird es ignoriert
		 */
		
		if (!feld.getOption_skip()) {
			/** 
			 * Falls die Option notEmpty gesetzt ist, wird geprüft,
			 * ob der Feldwert leer ist, 
			 *  wenn ja dann wird da wird das Feld nicht beschrieben
			 *  wenn die Option nicht gesetzt ist wird der Wert in dem Feld gelöscht  
			 */  
			if (feld.getValue() == null) {
				
				throw new ImportitException("Das Feld " + feld.getName() + "war im Datensatz " + datensatz.getValueOfKeyfield() + "in der Zeile " + rowNumber + " mit dem Wert NULL belegt");
				
			}
			if (!(feld.getOption_notEmpty() && feld.getValue().isEmpty())) {
				/** 
				 *Wenn die option modifiable gesetzt ist, wird vor dem Schreiben geprüft, ob das Feld Beschreibbar ist.
				 *Wenn nicht dann wird der Wert nicht geschrieben.
				 *
				 * Beim Schreiben in ein geschütztes Feld wird eine Exception geworfen.
				 *  
				 */
				try {
						/**
						 * Das Feld soll nur beschrieben werden, wenn es beschreibbar ist.
						 * Fall die option modifiable nicht gesetzt ist, wird eine ImportitExecption abgesetzt
						 * 
						 */
					EDPVariableLanguage variableLanguage = edpEditor.getSession().getVariableLanguage();
					EDPEditAction action = edpEditor.getEditAction();
					int database = edpEditor.getEditDatabaseNr();
					boolean aktive = edpEditor.isActive();
					String cfield = edpEditor.getCurrentFieldName();
					edpEditor.setFieldVal("name", "test");
					String test = edpEditor.getFieldVal("name");
					String fieldtyp = edpEditor.getFieldEDPType("name");
					
					String test2 = "2";
						if (edpEditor.fieldIsModifiable(rowNumber, feld.getName())) {
//									beschreibe das Feld 
							
							if (!(feld.getOption_dontChangeIfEqual() & 
									edpEditor.getFieldVal(feld.getName()).equals(feld.getValue()))) {
								edpEditor.setFieldVal(rowNumber, feld.getName(), feld.getValue());	
							}
							
							
						}else {
							
							if (!feld.getOption_modifiable() && !datensatz.getNameOfKeyfield().equals(feld.getName())) {
								if (rowNumber == 0) {
									throw new ImportitException("Das Kopffeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
											+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + "nicht änderbar");	
								}else {
									throw new ImportitException("Das Tabellenfeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
											+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " in Zeile " + rowNumber.toString() + " nicht änderbar");
								}
								

							}
						}	
					} catch (CantChangeFieldValException e) {
						if (rowNumber == 0) {
						throw new ImportitException("Das Kopffeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
								+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " nicht änderbar"  , e);
						}else {
							throw new ImportitException("Das Tabellenfeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
									+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " in Zeile " + rowNumber.toString() + " nicht änderbar"  , e);
						}
						
					}catch (CantReadFieldPropertyException e) {
						if (rowNumber == 0) {
						throw new ImportitException("Für das Kopffeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
								+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " die Abasfeldeigenschaften nicht auslesbar"  , e);
						}else {
							throw new ImportitException("Für das Tabellenfeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
									+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " in Zeile " + rowNumber.toString() + " die Abasfeldeigenschaften nicht auslesbar"  , e);
							
					}	
					} catch (CantReadSettingException e) {

						throw new ImportitException(""  , e);
					}
					
				}
				
			}else {
	
				throw new ImportitException("Der Editor war nicht aktiv");
				}
		
	}
	
	/**
	 * @param datensatzList
	 * 
	 * Prüft ob alle Werte in der Datensatzlist zu den Feld-Werten passen
	 * Die Prüfung ist in die Klasse CheckDataUtil ausgegliedert.
	 * Aber von hier wird es wegen den EDP-Einstellungen aufgerufen.  
	 * @throws ImportitException 
	 * 
	 * 
	 */
	
	public void checkDatensatzListValues(ArrayList<Datensatz> datensatzList) throws ImportitException {
		startEdpSession();
		
		for (Datensatz datensatz : datensatzList) {
			List<Feld> kopfFelder = datensatz.getKopfFelder();
			for (Feld feld : kopfFelder) {
				checkData(feld);
				
			}
			
		}
		
		closeEdpSession();
	}

	private long getAbasFieldLength(String abasTyp){
		EDPEKSArtInfo edpeksartinfo = new EDPEKSArtInfo(abasTyp);
		return edpeksartinfo.getMaxLineLen();
	}
	
	private Boolean checkData(Feld feld) {
		
		String[] VERWEIS = {"P" , "ID" , "VP" , "VID"};
		
		EDPEKSArtInfo edpeksartinfo = new EDPEKSArtInfo(feld.getAbasTyp());
		
		int datatyp = edpeksartinfo.getDataType();
		if (datatyp == EDPTools.EDP_REFERENCE || datatyp == EDPTools.EDP_ROWREFERENCE ) {
			if (!edpeksartinfo.getERPArt().startsWith("V")) {
//				normales Verweisfeld
				
				
				
			}else {
//				Multiverweisfeld
				
			}
			
		}
		
		return null;
	}
		
		
		
		
		
private EDPQuery getEDPQueryVerweis(String value, Integer database,
		Integer group, Boolean inTab) throws ImportitException {
	
	if (!this.edpSession.isConnected()) {
		startEdpSession();
	}
	int aliveFlag = EDPConstants.ALIVEFLAG_BOTH;
	String[] fieldNames = {"id" , "nummer"};
	String key = "";
	String tableName = database.toString() + ":" + group.toString();
	
	EDPQuery query = this.edpSession.createQuery();
	String krit = "0:grpDBDescr=(" + database.toString() + ");0:grpGrpNo=" + group + ";@englvar=true;@language=en";		
	
	try {
		query.startQuery(tableName, key, krit, true, aliveFlag, true, true, fieldNames, 0, 10000);
	
	} catch (InvalidQueryException e) {
		closeEdpSession();
		throw new ImportitException( "fehlerhafter Selektionsstring: " + krit , e);
	}
	
	return query;
}
	
}
