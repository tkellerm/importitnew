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
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPEditorOption;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPRowAddress;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPStoreRowMode;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;

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
//			Annahme: alles Datensätze in der Liste sind gleich von der Struktur, dann sollte auch nur der Eerste geprüft werden	
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

	private boolean checkDatensatzStruktur(Datensatz datensatz) throws ImportitException {

		//		prüfe Datenbank, ob vorhanden
		
				
		List<Feld> kopfFelder = datensatz.getKopfFelder();
		List<Feld> tabellenFelder = datensatz.getTabellenFelder();
	
		boolean kopfok = checkListofFeldAndGetabasType(kopfFelder, datensatz.getDatenbank() , datensatz.getGruppe() , false, datensatz.getOptionCode().getUseEnglishVariablen() );
		boolean tabelleok = checkListofFeldAndGetabasType(tabellenFelder, datensatz.getDatenbank() , datensatz.getGruppe(), true, datensatz.getOptionCode().getUseEnglishVariablen());		
		if (tabelleok & kopfok) {
			return true;	
		}else {
			return false;
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
						feld.setAbastyp(abasTyp);						
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
		String krit = "grpDBDescr=(" + database.toString() + ");grpGrpNo=" + group +    ";" + suchfeld + "~/==\\b[a-z]{2}"  + feldname + "\\b;" + "InTab" + "=" + inTab.toString() + ";@englvar=true;@language=en";
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
			if (datensatz.getOptionCode().getAlwaysNew()) {
				
					edpEditor.beginEditNew(datensatz.getDatenbank().toString(), datensatz.getGruppe().toString());
					setEditorOption(datensatz , edpEditor);
					writeFieldsInEditor(datensatz , edpEditor);
					edpEditor.endEditSave();
			}else {
				
//				selektiere nach dem Schlüsselfeld
				EDPQuery edpQuery = this.edpSession.createQuery();
				
				String tableName = datensatz.getDatenbank().toString() + ":" + datensatz.getGruppe().toString();
				
				String key = datensatz.getKeyOfKeyfield();
				
				if (key == null) {
					key="";
				}
				
				String krit = datensatz.getNameOfKeyfield() + "=" + datensatz.getValueOfKeyfield() + ";@englvar=true;@language=en";
				edpQuery.startQuery(tableName, key, krit, "nummer,such,id" );		
				edpQuery.getLastRecord();
				int recordCount = edpQuery.getRecordCount();
				if (recordCount == 1 || recordCount == 0) {
//				Eröffne eine Editor fals kein oder 1 Datensatz gefunden wurde 	
					edpEditor.beginEdit(edpQuery.getField("id"));
					
					writeFieldsInEditor(datensatz , edpEditor);
					edpEditor.endEditSave();
				}else {
					
					datensatz.appendError("Selektion auf Datensatz war nicht eindeutig! Folgende Selektion wurde verwendet :" + krit);
				}
				
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
				
				writefield(datensatz, feld, edpEditor, 0);
				
				}
//			TabelleFelder schreiben
			
			List<DatensatzTabelle> tabellenZeilen = datensatz.getTabellenzeilen();
			
			if (tabellenZeilen !=null && edpEditor.hasTablePart()) {
				for (DatensatzTabelle datensatzTabelle : tabellenZeilen) {
					Integer aktzeile_nurInfo = edpEditor.getCurrentRow();
					try {
						edpEditor.insertRow(EDPRowAddress.LAST_ROW);
					} catch (InvalidRowOperationException e) {
						throw new ImportitException("Die Zeilen konnten nicht eingefügt werden!" ,e );
					}
					Integer rowNumber = edpEditor.getCurrentRow();
					ArrayList<Feld> tabellenFelder = datensatzTabelle.getTabellenFelder();
					for (Feld feld : tabellenFelder) {
						writefield(datensatz, feld, edpEditor, rowNumber);
					
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
	private void writefield(Datensatz datensatz, Feld feld, EDPEditor edpEditor , Integer rowNumber )
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
					
						if (edpEditor.fieldIsModifiable(rowNumber, feld.getName())) {
//									beschreibe das Feld 
							edpEditor.setFieldVal(rowNumber, feld.getName(), feld.getValue());
							
						}else {
							
							if (!feld.getOption_modifiable()) {
								
								throw new ImportitException("Das Kopffeld " + feld.getName() + "ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
										+ "für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + "nicht änderbar");

							}
						}	
					} catch (CantChangeFieldValException e) {
						throw new ImportitException("Das Kopffeld " + feld.getName() + "ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
								+ "für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + "nicht änderbar"  , e);
						
					}catch (CantReadFieldPropertyException e) {
						throw new ImportitException("Für das Kopffeld " + feld.getName() + "ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
								+ "für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + "die Abasfeldeigenschaften nicht auslesbar"  , e);
					}
					
				}
				
			}else {
	
				throw new ImportitException("Der Editor war nicht aktiv");
				}
		
	}
	
	
	
}
