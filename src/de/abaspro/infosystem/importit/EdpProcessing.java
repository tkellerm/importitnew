package de.abaspro.infosystem.importit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.management.BadAttributeValueExpException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.CantChangeFieldValException;
import de.abas.ceks.jedp.CantChangeSettingException;
import de.abas.ceks.jedp.CantReadFieldPropertyException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.ConnectionLostException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEKSArtInfo;
import de.abas.ceks.jedp.EDPEditAction;
import de.abas.ceks.jedp.EDPEditFieldList;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPTools;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.ceks.jedp.InvalidSettingValueException;
import de.abas.ceks.jedp.StandardEDPSelection;
import de.abas.ceks.jedp.StandardEDPSelectionCriteria;
import de.abas.ceks.jedp.TransactionException;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.common.type.enums.EnumTypeCommands;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;


public class EdpProcessing {

	private String server;
	private Integer port;
	private String mandant;
	private String passwort;
	private EDPSession edpSession;
	private Logger logger = Logger.getLogger(Importit21.class);
	
	/**
	 * @param server
	 * @param port
	 * @param mandant
	 * @param passwort
	 * @throws ImportitException
	 */
	public EdpProcessing(String server, Integer port, String mandant,
			String passwort) throws ImportitException {
		super();
//		check Parameter
		
		
		checkNotNullandNotEmpty(port, "Bitte Port eintragen!");
		checkNotNullandNotEmpty(server, "Bitte Server eintragen!");
		checkNotNullandNotEmpty(mandant,"Bitte Mandant eintragen!");
		checkNotNullandNotEmpty(passwort, "Bitte Passwort eintragen!");
		
		this.server = server;
		this.port = port;
		this.mandant = mandant;
		this.passwort = passwort;
		this.edpSession = EDPFactory.createEDPSession ();
	}

	/**
	 * @param integerVar
	 * @param errortext
	 * @throws ImportitException 
	 * 
	 * 
	 * Empty = 0
	 * 
	 */
	private void checkNotNullandNotEmpty(Integer integerVar, String errortext) throws ImportitException {
		final String fehlerNull = "Darf nicht null sein!";
		
		if (integerVar!=null) {
			if (integerVar == 0) {
				throw new ImportitException(errortext);
			}			
		}else {
			throw new ImportitException(errortext + fehlerNull);
		}
		
	}

	/**
	 * @param stringVar
	 * @param errortext
	 * @throws ImportitException
	 */
	private void checkNotNullandNotEmpty(String stringVar, String errortext)
			throws ImportitException {
		final String fehlerNull = "Darf nicht null sein!";
		if (stringVar != null) {
			if (stringVar.isEmpty()) {
				throw new ImportitException(errortext);
			}
			
		}else {
			throw new ImportitException(errortext + fehlerNull);
		}
	}
	
	public void startEdpSession() throws ImportitException {
		
		sessionAufbauen(this.server, this.port, this.mandant, this.passwort);
	}

public void startEdpSession(EDPVariableLanguage varlanguage) throws ImportitException {
		
		sessionAufbauen(this.server, this.port, this.mandant, this.passwort, varlanguage);
	}
	
	public void closeEdpSession(){
		
		if (this.edpSession.isConnected()) {
			this.edpSession.endSession();
		}
		
	}
	
	private void sessionAufbauen(String server, int port, String mandant, String passwort ) throws ImportitException 
    { 
      
    	  		try {
    	  			this.edpSession.beginSession(server , port, mandant, passwort, "ImportIt_21");
    	  		} catch (CantBeginSessionException ex) 
    	  			{
    	  			logger.error(ex);
    	  			throw new ImportitException("FEHLER\n EDP Session kann nicht gestartet werden\n" , ex);
    	  			}
    }
	
	private void sessionAufbauen(String server, int port, String mandant, String passwort , EDPVariableLanguage varlanguage ) throws ImportitException 
    { 
      
    	  		try {
    	  			this.edpSession.beginSession(server , port, mandant, passwort, "ImportIt_21");
    	  		} catch (CantBeginSessionException ex) 
    	  			{
    	  			logger.error(ex);
    	  			throw new ImportitException("FEHLER\n EDP Session kann nicht gestartet werden\n" , ex);
    	  			}
                 
              this.edpSession.setVariableLanguage(varlanguage);
        
	        
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
								datensatzIter.copyDatabaseinDatensatz(datensatz);
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
		
				
		boolean kopfok = false;
		boolean tabelleok = false;
		if (dbok) {
			List<Feld> kopfFelder = datensatz.getKopfFelder();
			List<Feld> tabellenFelder = datensatz.getTabellenFelder();
			kopfok = false;
			tabelleok = false;
			try {
				switch (checkSpecialDB(datensatz)) {
				case 1:
					//		Kundenartikeleigenschaften
					kopfok = checkKundenartikeleigenschaftenKopf(kopfFelder,
							datensatz.getOptionCode().getUseEnglishVariablen());
					//				Bei der Kundenartikeleigenschaften sind alle Variablen in der Gruppe 6 gespeichert und dort gibt es keine Tabelle
					tabelleok = checkListofFeldAndGetabasType(tabellenFelder,
							2, 6, false, datensatz.getOptionCode()
									.getUseEnglishVariablen());
					break;

				default:
					//				Alle normalen Datenbanken
					kopfok = checkListofFeldAndGetabasType(kopfFelder,
							datensatz.getDatenbank(), datensatz.getGruppe(),
							false, datensatz.getOptionCode()
									.getUseEnglishVariablen());
					tabelleok = checkListofFeldAndGetabasType(tabellenFelder,
							datensatz.getDatenbank(), datensatz.getGruppe(),
							true, datensatz.getOptionCode()
									.getUseEnglishVariablen());
					break;
				}

			} catch (ImportitException e) {
				logger.error(e);
				datensatz.appendError("Die Strukturprüfung war fehlerhaft", e);
			}
		}
		if (tabelleok & kopfok & dbok) {
			return true;	
		}else {
			return false;
		}
			
		
	}

	private boolean checkKundenartikeleigenschaftenKopf(List<Feld> kopfFelder, Boolean englVar) throws ImportitException {
		
		if (kopfFelder.size() == 1) {
				for (Feld feld : kopfFelder) {
					String varName = feld.getName();
					if (varName.equals("product")|| varName.equals("art") || varName.equals("artikel") ) {
							return checkListofFeldAndGetabasType(kopfFelder, 2 , 6 , false , englVar);
					}
				}
				throw new ImportitException("Es wurde die Variable art, artikel oder product nicht gefunden!");	
			}else {
				throw new ImportitException("In den Kopffeldern sind mehre Felder, bei den Kundenartikelschaften ist nur eine Variable art, artikel oder product erlaubt!");
			}
				
	}

	private int checkSpecialDB(Datensatz datensatz) {
		if (datensatz.getDatenbank() == 2 && (datensatz.getGruppe() == 6 ||datensatz.getGruppe() == 7)) {
//			Kundenartikeleigenschaften
			return 1;
		}else {
			return 0;
		}
	}

	private boolean checkDBorEditorCommdands(Datensatz datensatz) {
		
		Boolean gefunden = false ;
		
		
		if (datensatz.getTippkommando()==null && datensatz.getTippcommandString()!=null) {
			
			datensatz.setTippkommando(checkTippCommandString(datensatz.getTippcommandString()));
			
		}
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
		
		gefunden = checkDatabaseName(datensatz);
		
		
		
		return gefunden;
	}

		
	

	private Integer checkTippCommandString(String tippcommandString) {
		
		Integer tippCommandCode = null;
		
		Aufzaehlung aufzaehlung = fuellAufzaehlung();
		
		if (aufzaehlung.getListOfAufzaehlungItem().size() > 0) {
			AufzaehlungItem aufzaehlungItem = aufzaehlung.searchItem(tippcommandString);
			tippCommandCode =  aufzaehlungItem.getNumber();
		}
		
		
		return tippCommandCode;
	}

	private Aufzaehlung fuellAufzaehlung() {
		

		Aufzaehlung aufzaehlung = new Aufzaehlung();
		ArrayList<AufzaehlungItem> listAufzaehlung = aufzaehlung.getListOfAufzaehlungItem();
		
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		GlobalTextBuffer globalTextbuffer = bufferFactory.getGlobalTextBuffer();
		UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();
		int maxkkomnam = globalTextbuffer.getIntegerValue("cmdNameMax");	
		for (Integer i = 0; i < maxkkomnam; i++) {
			
			String varnameNamebspr = "xtnamebspr";
			String varnameNameNeutral = "xtnameneutral";
			String varnameAufzaehlung = "xtaufzaehlung";
			String namebspr;
			String nameNeutral;
			Integer nummer;
			
			
			if (!userTextBuffer.isVarDefined(varnameNamebspr)) {
				userTextBuffer.defineVar("Text", varnameNamebspr);
			}
			if (!userTextBuffer.isVarDefined(varnameNameNeutral)) {
				userTextBuffer.defineVar("Text", varnameNameNeutral);
			}
			
			if (!userTextBuffer.isVarDefined(varnameAufzaehlung)) {
				userTextBuffer.defineVar("A198", varnameAufzaehlung);
			}
			
			
//			userTextBuffer.setValue(varnameAufzaehlung, i);
			FOe.assign("U|" + varnameAufzaehlung + " = \"(" + i + ")\"" );
			Boolean mehr = globalTextbuffer.getBooleanValue("success");		
			if (mehr) {
				nummer  = i;
				FOe.formula("U|" + varnameNameNeutral + " = 'U|" + varnameAufzaehlung + "(L=\":\")'" );
				nameNeutral = userTextBuffer.getStringValue(varnameNameNeutral);
				namebspr = globalTextbuffer.getStringValue("cmdName" + i);				
				AufzaehlungItem aufzaehlungsitem = new AufzaehlungItem(nummer, namebspr, nameNeutral);
				listAufzaehlung.add(aufzaehlungsitem);
				
			}

		}
		
		return aufzaehlung;
	}

	private Boolean checkDatabaseName(Datensatz datensatz) {
		// prüfen ob datenbank im datensatz gesetzt ist

		if (datensatz.getDatenbank() != null && datensatz.getGruppe() != null) {

			String krit = "0:grpDBDescr=("
					+ datensatz.getDatenbank().toString() + ");0:grpGrpNo="
					+ datensatz.getGruppe().toString() + ";"
					+ ";@englvar=true;@language=en";

			if (searchDatabase(datensatz, krit)) {
				return true;
				
			} else {
				datensatz.appendError("Die Datenbank : Gruppe mit der Nummer "
						+ datensatz.getDatenbank() + ":"
						+ datensatz.getGruppe() + " ist nicht definiert");
				return false;
			}

		} else {
			// Datenbank oder gruppe == null
			// dies kann bedeuten das es ein Tipkommando ist, oder das die
			// Datenbanken als String übergeben wurden
			// daher die Prüfung,ob die beiden String Felder gestetzt sind.

			if (datensatz.getDbString() != null
					&& datensatz.getDbGroupString() != null) {
				// Suche über die bediensprachliche Bezeichnung
				String krit = "0:vdntxt==" + datensatz.getDbString()
						+ ";0:vgrtxtbspr=="
						+ datensatz.getDbGroupString() + ";"
						+ ";@englvar=false;@language=de";

				if (searchDatabase(datensatz, krit)) {
					return true;
				} else {
					// Suche über die Sprachneutrale Bezeichnung
					krit = "0:DBCmd==" + datensatz.getDbString()
							+ ";0:grpGrpCmd==" + datensatz.getDbGroupString()
							+ ";" + ";@englvar=true;@language=en";
					if (searchDatabase(datensatz, krit)) {
						return true;
					} else {
						datensatz
								.appendError("Die Datenbank : Gruppe mit den Namen "
										+ datensatz.getDatenbank()
										+ ":"
										+ datensatz.getGruppe()
										+ " ist nicht definiert");
						return false;
					}
				}
			}

		}

		return false;
	}

	/**
	 * @param datensatz
	 * @param krit
	 * @param option 
	 * @return 
	 */
	private boolean searchDatabase(Datensatz datensatz, String krit) {
		//			Überprüfung ob Datenbank vorhanden
					String key = "";
					int aliveFlag = EDPConstants.ALIVEFLAG_ALIVE;
					String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
					
		//			Vartab Tabellenname
					String tableName = "12:26";
					Boolean inTable = false;
					int mode = EDPConstants.ENUMPOS_CODE;
					try {
						this.edpSession.setEnumMode(mode);
					} catch (InvalidSettingValueException e1) {
						logger.error(e1);
					}
					EDPQuery query = this.edpSession.createQuery();

						try {
							
							query.startQuery(tableName, key, krit, inTable, aliveFlag, true, true, fieldNames, 0, 10000);
							query.getLastRecord();
							if (query.getRecordCount() == 1) {
							 
								String dbstring = query.getField("grpDBDescr");
								dbstring = dbstring.replaceAll("\\(*", "");
								dbstring = dbstring.replaceAll("\\)*", "");
								datensatz.setDatenbank(new Integer(dbstring));
								String gruppe = query.getField("grpGrpNo");
								gruppe = gruppe.replaceAll(" ", "");
								datensatz.setGruppe(new Integer(gruppe));
								return true;
							}
							
						} catch (InvalidQueryException e) {
							datensatz.appendError("Die Suche nach der Datenbank mit dem Suchstring " + krit + "war fehlerhaft" , e);
							return false;
						}
						return false;
	}

	private void findDatenbankForTipkkommando(Datensatz datensatz) throws ImportitException {
		
		if (datensatz.getTippkommando()!=null) {
//			EDPSession und EPDEditor für dieses Tippkommando öffnen und aus dem Dialog die Datenbank auslesen
			
				startEdpSession(EDPVariableLanguage.ENGLISH);
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
		
		if (feldListe !=null ) {
			if (!edpSession.isConnected()) {
				startEdpSession(EDPVariableLanguage.ENGLISH);
			}
			logger.info("START Hole Vartab für Datenbank " + database + ":" + group);
			Vartab vartab = new Vartab(edpSession, database , group);
			logger.info("ENDE Hole Vartab für Datenbank");
			closeEdpSession();
			Boolean fehlergefunden = false;
			for (Feld feld : feldListe) {
				if (!feld.getOption_skip()) {
					
				
					VartabFeld vartabfeld = null; 
					if (englVarNames) {
		//					Checke auf englische Variablennamen					
						vartabfeld = vartab.checkVartabforEnglVarName(feld.getName());
						
					}else {
		//					Checke auf deutsche Variablennamen	
						vartabfeld = vartab.checkVartabforGermanVarName(feld.getName());					
					}
					
					if (vartabfeld != null) {
						feld.setAbasTyp(vartabfeld.getAktivType());
						logger.trace("Feld " + feld.getName() + " mit abastyp " + vartabfeld.getAktivType() + " gefunden" ); 
						feld.setAbasFieldLength(getAbasFieldLength(vartabfeld.getVarLength()));
						
						
					}else {
						String fehlertext = "Das Feld mit dem Namen " + feld.getName() + " wurde in der Vartab nicht gefunden";
						fehlergefunden = true;
						feld.setError(fehlertext);
						logger.error(fehlertext);
					}
				}	
			}
			
			if (fehlergefunden) {
				logger.info("Es wurden nicht alle Felder in der Vartab gefunden!");
				return false;
			}else {
				logger.info("Es wurden alle Felder in der Vartab gefunden!");
				return true;
			}
			
			}else if (inTab) {
//				ein leerer Tabellensatz ist erlaubt
				return true;
			} else {
				throw new ImportitException("Die übergebene Feldliste für den Kopf war leer! Das funktioniert nicht");
			}
		
	
	}
	
	
	
	/**
	 * @param datensatzList
	 * @throws ImportitException
	 * 
	 * Es wird eine EDPSession aufgebaut und die Datensatzliste eingelesen.
	 * Danach wird die EDpSession wieder beendet.
	 * 
	 */
	public void importDatensatzList(ArrayList<Datensatz> datensatzList) throws ImportitException {
		edpSession.loggingOn("java/log/importit21edp.log");
		startEdpSession();

		for (Datensatz datensatz : datensatzList) {
			writeDatensatzToAbas(datensatz);
		}
		closeEdpSession();
	}
	/**
	 * @param datensatzList
	 * @throws ImportitException
	 * 
	 * Es wird das Object datensatzlist eingelesen
	 * 
	 * Es wird keine EDPSession aufgebaut und geschlossen dies muss in dem aufrufenden Programmteil erledigt werden 
	 */
	public void importDatensatzListTransaction(ArrayList<Datensatz> datensatzList) throws ImportitException {
		

		for (Datensatz datensatz : datensatzList) {
			writeDatensatzToAbas(datensatz);
		}
		
	}
	private void writeDatensatzToAbas(Datensatz datensatz) throws ImportitException {
		
		if (this.edpSession.isConnected()) {
			EDPEditor edpEditor = this.edpSession.createEditor();
			try {
				
			if (datensatz.getTippkommando()== null ) {
				
				switch (checkSpecialDB(datensatz)) {
				case 1:
//					Verarbeitung Kundenartikeleigenschaften
					writedatabaseKundenArtikeleigenschaften(datensatz , edpEditor);
					break;

				default:
//					Verarbeitung der normalen Datenbanken
					writeDatabase(datensatz, edpEditor);					
					break;
				}

				
			}else {
//				Verarbeitung der TippKommandos
				writeTippKommandos(datensatz, edpEditor);
				
			}
				
			} catch (CantBeginEditException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (ImportitException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (InvalidQueryException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (CantSaveException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (CantChangeSettingException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (CantReadFieldPropertyException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (CantChangeFieldValException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (InvalidRowOperationException e) {
				logger.error(e);
				datensatz.appendError(e);
			} catch (CantReadSettingException e) {
				logger.error(e);
				datensatz.appendError(e);
			}finally{
				if (edpEditor.isActive()) {
					logger.info("Editor beenden");
					edpEditor.endEditCancel();
				}
			}
			
		}else {
			logger.error("Die EDPSession ist nicht gestartet");
			throw new ImportitException("Die EDPSession ist nicht gestartet");
		}
		
	}

	private void writedatabaseKundenArtikeleigenschaften(Datensatz datensatz,
			EDPEditor edpEditor) throws ImportitException, InvalidQueryException, CantChangeSettingException, CantBeginEditException, InvalidRowOperationException, CantSaveException, CantReadSettingException {
			
		datensatz.setIsimportiert(false);
		String[] varnames = checkDatensatzKundenartikeleigenschaften(datensatz);
		if (varnames.length == 2) {
			
//			Datenbank auf 2 und Gruppe auf 6 setzen
			logger.info("Import Kundenartikeleigenschaften");
			
			datensatz.setDatenbank(2);
			datensatz.setGruppe(6);
//			prüfen ob es für artikel und kunde schon einen Datensatz gibt
//			Es mus jede Zeile des Datensatzes geprüft werden
//			da im Kopf der Artikel und in der Zeile der Kunde steht
			String fieldnameart = varnames[0];
			String fieldnamekl  = varnames[1];

			List<DatensatzTabelle> tabZeilen = datensatz.getTabellenzeilen();
			
			for (DatensatzTabelle datensatzTabelle : tabZeilen) {
				
					datensatzTabelle.getTabellenFieldValue(fieldnamekl);
					EDPQuery edpQuery = this.edpSession.createQuery();
					int recordCount;
					String krit = fieldnameart +  "=" 
							+ datensatz.getValueOfHeadField(fieldnameart)
							+ ";" + fieldnamekl + "=" + datensatzTabelle.getTabellenFieldValue(fieldnamekl) ;
					String key = "Kundenartikeleigenschaften";
					
					recordCount = getQueryTotalHits(krit , key , datensatz, edpQuery );
					
					if (recordCount == 1 || recordCount == 0) {
						setEditorOption(datensatz, edpEditor);
						if (recordCount == 1) {
							String abasVersion = edpSession.getABASVersionNumber().substring(0, 4);
							
							if (2013 <= new Integer(abasVersion)   ) {
								//				Eröffne eine Editor fals kein oder 1 Datensatz gefunden wurde
								String xtid =  edpQuery.getField("id");
								edpEditor.beginEdit("2:6", xtid);
								
								if (edpEditor.getRowCount() > 0 && datensatz.getOptionCode().getDeleteTable()) {
									edpEditor.deleteAllRows();
								}
								logger.info("Editor Kundenartikeleigenschaften starten UPDATE" + " " + datensatz.getDatenbank().toString() +":" +datensatz.getGruppe().toString() + " ID:" +edpEditor.getEditRef());

								
							}else {
								logger.error("Editor Kundenartikeleigenschaften:  die Selektion " + krit + " hat einen Treffer erhalten. ID: " + edpQuery.getField("id") + "; Das Ändern der Kundenartikeleigenschaften ist erst ab der Version 2013R1 möglich!"); 
								throw new ImportitException("Das Ändern der Kundenartikeleigenschaften ist erst ab der Version 2013R1 möglich!");
							}
						}else {
							
							logger.info("Editor Kundenartikeleigenschaften starten NEW " + datensatz.getDatenbank().toString() +":" +datensatz.getGruppe().toString());
							edpEditor.beginEditNew(datensatz.getDatenbank().toString(),
									datensatz.getGruppe().toString());
						}
						
						final String[] IgnoreFieldNames = {"art" , "artikel" , "product", "kl" , "custVendor" };
						writeFieldsInEditor(datensatz,datensatzTabelle, edpEditor, IgnoreFieldNames );
						edpEditor.endEditSave();
						
						datensatz.setIsimportiert(true);
						
						edpSession.loggingOff();
						recordCount = getQueryTotalHits(krit,key , datensatz, edpQuery);
						if (recordCount == 0) {
							String errortext = "Der Datensatz mit Artikel " + datensatz.getValueOfHeadField(fieldnameart) + " und dem Kunden " + datensatzTabelle.getTabellenFieldValue(fieldnamekl) + " konnte nicht gefunden werden! es gibt ein PRoblem beim Speichern";
									logger.error(errortext);
							datensatz.appendError(errortext);
							
						}else if (recordCount > 1 ) {
							
							String errortext = "Der Datensatz mit Artikel " + datensatz.getValueOfHeadField(fieldnameart) + " und dem Kunden " + datensatzTabelle.getTabellenFieldValue(fieldnamekl) + " wurde mehrmals gefunden werden! Es gibt ein größeres Problem! ";
									logger.error(errortext);
							datensatz.appendError(errortext);
						}
						
						
					}else {
						datensatz
						.appendError("Selektion auf Datensatz war nicht eindeutig! Folgende Selektion wurde verwendet :"
								+ krit);
					}
				
				}
			
		}


	}

	private int getQueryTotalHits(String krit, String key , Datensatz datensatz, EDPQuery edpQuery) throws InvalidQueryException {
		
		Integer database = datensatz.getDatenbank();
		Integer group = datensatz.getGruppe();
		String databaseDescr;
		if (group != -1) {
			databaseDescr = database + ":" + group ;	
		}else {
			databaseDescr = database.toString() ;
		}
		

		if (datensatz.getOptionCode().getUseEnglishVariablen()) {
			krit = krit + ";@englvar=true;@language=en";
			edpQuery.startQuery(databaseDescr,key , krit, "id");
		} else {
			krit = krit + ";@englvar=false;@language=de";
			edpQuery.startQuery(databaseDescr, key , krit,	"id");
		}

		edpQuery.getLastRecord();
		return edpQuery.getRecordCount();
	}

	/**
	 * @param datensatz
	 * 
	 * checkDatensatz ob Es wirklich Kundenartikeleigenschaften sind 
	 * prüfen ob die Felder kl und art als Felder vorhanden sind
	 * nur so kann man Kundenartikeleigenschaften beschreiben
	 * 
	 * @return array mit 2 Werten wert[0] VariablenName Artikel
	 * 							  wert[1] VariablenName Kunde
	 *  
	 */
	private String[] checkDatensatzKundenartikeleigenschaften(Datensatz datensatz) {
		
		Integer database = datensatz.getDatenbank();
		Integer group = datensatz.getGruppe();
		Boolean varnameArtGefunden = false;
		Boolean varnameKlGefunden = false;
		String varNameArt = "";
		String varNameKl = "";
		String[] varNames = new String[2];
		if (database==2 & (group == 6 || group == 7)) {
			
			List<Feld> kopffelder = datensatz.getKopfFelder();
			for (Feld feld : kopffelder) {
				if (feld.getName().equals("art") || feld.getName().equals("artikel") || feld.getName().equals("product") ) {
					varnameArtGefunden = true;
					varNameArt = feld.getName();
				}
			}
			
			List<Feld> tabellenfelder = datensatz.getTabellenFelder();
			for (Feld feld : tabellenfelder) {
				if (feld.getName().equals("kl") || feld.getName().equals("custVendor")) {
					varnameKlGefunden = true;
					varNameKl = feld.getName();
				}
			}
			
			if (varnameArtGefunden & varnameKlGefunden){
				varNames[0] = varNameArt ;
				varNames[1] = varNameKl ; 
				
				
				return varNames;
			}
		}
		
		return ArrayUtils.EMPTY_STRING_ARRAY;
		
		
	}

	private void writeTippKommandos(Datensatz datensatz, EDPEditor edpEditor) throws ImportitException, CantChangeSettingException, CantSaveException, CantBeginEditException {
		datensatz.setIsimportiert(false);
		edpEditor.beginEditCmd(datensatz.getTippkommando().toString(), "");
		setEditorOption(datensatz, edpEditor);
		writeFieldsInEditor(datensatz, edpEditor);
		edpEditor.endEditSave();
		datensatz.setIsimportiert(true);
	}

	/**
	 * @param datensatz
	 * @param edpEditor
	 * @throws CantBeginEditException
	 * @throws CantChangeSettingException
	 * @throws ImportitException
	 * @throws CantSaveException
	 * @throws InvalidQueryException
	 * @throws CantReadFieldPropertyException 
	 * @throws CantChangeFieldValException 
	 * @throws InvalidRowOperationException 
	 */
	private void writeDatabase(Datensatz datensatz, EDPEditor edpEditor)
			throws CantBeginEditException, CantChangeSettingException,
			ImportitException, CantSaveException, InvalidQueryException, CantReadFieldPropertyException, CantChangeFieldValException, InvalidRowOperationException {
		
		datensatz.setIsimportiert(false);
		if (datensatz.getOptionCode().getAlwaysNew()) {
			setEditorOption(datensatz, edpEditor);
			logger.info("Editor starten Always NEW " + datensatz.getDatenbank().toString() +":" +datensatz.getGruppe().toString());
			edpEditor.beginEditNew(datensatz.getDatenbank().toString(),
					datensatz.getGruppe().toString());
			writeFieldsInEditor(datensatz, edpEditor);
			edpEditor.saveReload();
			String abasId = edpEditor.getEditRef();
			logger.info("Editor save Always NEW " + datensatz.getDatenbank().toString() + ":" + datensatz.getGruppe().toString() + " ID :" + abasId);
			datensatz.setAbasId(abasId);
			edpEditor.endEditSave();
			datensatz.setIsimportiert(true);
			
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
					if (edpEditor.getRowCount() > 0 && datensatz.getOptionCode().getDeleteTable()) {
						edpEditor.deleteAllRows();
					}
					logger.info("Editor starten UPDATE" + " " + datensatz.getDatenbank().toString() +":" +datensatz.getGruppe().toString() + " ID:" +edpEditor.getEditRef());

				}else {
					
					logger.info("Editor starten NEW " + datensatz.getDatenbank().toString() +":" +datensatz.getGruppe().toString());
					edpEditor.beginEditNew(datensatz.getDatenbank().toString(),
							datensatz.getGruppe().toString());
				}
				
				writeFieldsInEditor(datensatz, edpEditor);
				
				edpEditor.saveReload();
//				edpEditor.setEditorOption(EDPEditorOption.STOREROWMODE, EDPStoreRowMode.DELETE_NONE.getModeStr());
				String abasId = edpEditor.getEditRef();
				
				logger.info("Editor save Always NEW " + datensatz.getDatenbank().toString() + ":" + datensatz.getGruppe().toString() + " ID:" + abasId);
				
				datensatz.setAbasId(abasId);
				edpEditor.endEditSave();
				datensatz.setIsimportiert(true);
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
			
//	Auskommentiert weil beim Schreiben der UPDATE oder der NEW Modus verwendet wird und nicht der Store - Mode			
////			Tabelle löschen 
//			if (optionCode.getDeleteTable()) {
//				
//				edpEditor.setEditorOption(EDPEditorOption.STOREROWMODE, EDPStoreRowMode.DELETE_TAIL.getModeStr());
//			}else {
//				edpEditor.setEditorOption(EDPEditorOption.STOREROWMODE, EDPStoreRowMode.DELETE_NONE.getModeStr());
//			}
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
					Integer rowNumber = insertRow(datensatz, edpEditor, rowNumbervorher);
					

					ArrayList<Feld> tabellenFelder = datensatzTabelle.getTabellenFelder();
					for (Feld feld : tabellenFelder) {
						writeField(datensatz, feld, edpEditor, rowNumber);
					
					}
				}
			}				
			}		
		}

	private void writeFieldsInEditor(Datensatz datensatz,
			DatensatzTabelle datensatzTabelle, EDPEditor edpEditor , String[] ignoreFields ) throws ImportitException {

		if (edpEditor.isActive()) {
//			EDPEditAction testaction = edpEditor.getEditAction(); 
			if (edpEditor.getEditAction() == EDPEditAction.NEW  ) {	
	//			Kopffelder schreiben
				
				List<Feld> kopfFelder = datensatz.getKopfFelder();
				for (Feld feld : kopfFelder) {
					
					writeField(datensatz, feld, edpEditor, 0);
					
					}
				
	//			eine Zeile TabelleFelder schreiben
	
				if (datensatzTabelle !=null && edpEditor.hasTablePart()) {
					Integer rowNumbervorher = edpEditor.getRowCount();
						Integer rowNumber = insertRow(datensatz, edpEditor, rowNumbervorher);
						

						ArrayList<Feld> tabellenFelder = datensatzTabelle.getTabellenFelder();
						for (Feld feld : tabellenFelder) {
							writeField(datensatz, feld, edpEditor, rowNumber);
						
						}
					
				}				
				
			}else if (edpEditor.getEditAction() == EDPEditAction.UPDATE) {
//				Kopffelder schreiben
				List<Feld> kopfFelder = datensatz.getKopfFelder();
				for (Feld feld : kopfFelder) {
					if (dontIgnoreField(feld, ignoreFields)) {
						writeField(datensatz, feld, edpEditor, 0);
					}
				}
//				Zeile aktualisieren
//				eine Zeile TabelleFelder schreiben
				
				if (datensatzTabelle !=null && edpEditor.hasTablePart()) {
						Integer rowNumber = edpEditor.getCurrentRow();
						ArrayList<Feld> tabellenFelder = datensatzTabelle.getTabellenFelder();
						for (Feld feld : tabellenFelder) {
								if (dontIgnoreField(feld, ignoreFields)) {
									writeField(datensatz, feld, edpEditor, rowNumber);	
								}	
						}
					
				}
			}
		}	
		
	}


	private Integer insertRow(Datensatz datensatz, EDPEditor edpEditor,
			Integer rowNumbervorher) throws ImportitException {
		try {
			if (rowNumbervorher == 0) {
				edpEditor.insertRow(1);
				return 1;
			}else {
				if (( datensatz.getTippkommando() != null && rowNumbervorher > 1) || (datensatz.getTippkommando() == null) ) {
					Integer newRowNumber = rowNumbervorher + 1;
					edpEditor.insertRow(newRowNumber);
					return newRowNumber;
				}else {
					return rowNumbervorher;

				}
					
			}
			
		} catch (InvalidRowOperationException e) {
			logger.error(e);
			throw new ImportitException("Die Zeilen konnten nicht eingefügt werden!" ,e );
		}
	}
	
	/**
	 * Prüfen, ob das @param feld in dem Array
	 * @param ignoreFields vorkommt
	 * @return wenn ja wird false zurückgegen, ansonsten true
	 */
	private boolean dontIgnoreField(Feld feld, String[] ignoreFields) {
		
		String fieldName = feld.getName();
		for (String ignoreField : ignoreFields) {
			if (ignoreField.equals(fieldName)) {
				return false;
			}
		}
		return true;
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
						
					
						
						if (edpEditor.fieldIsModifiable(rowNumber, feld.getName())) {
//									beschreibe das Feld 
							String datafieldval = feld.getValue();
							
							if (!(feld.getOption_dontChangeIfEqual() & 
									edpEditor.getFieldVal(rowNumber, feld.getName()).equals(datafieldval))) {
								edpEditor.setFieldVal(rowNumber, feld.getName(), datafieldval);
					
								logger.debug("Das Feld " + feld.getName() + " mit dem Wert " + datafieldval  + " wurde in Zeile " + rowNumber.toString() + "geschrieben" );
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
						logger.error(e);
						if (rowNumber == 0) {			
						throw new ImportitException("Das Kopffeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
								+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " nicht änderbar"  , e);
						}else {
							throw new ImportitException("Das Tabellenfeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
									+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " in Zeile " + rowNumber.toString() + " nicht änderbar"  , e);
						}
						
					}catch (CantReadFieldPropertyException e) {
						logger.error(e);
						if (rowNumber == 0) {
						throw new ImportitException("Für das Kopffeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
								+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " die Abasfeldeigenschaften nicht auslesbar"  , e);
						}else {
							throw new ImportitException("Für das Tabellenfeld " + feld.getName() + " ist in dem Datensatz " + datensatz.getValueOfKeyfield() 
									+ " für die Datenbank " + datensatz.getDatenbank() + ":" + datensatz.getGruppe() + " in Zeile " + rowNumber.toString() + " die Abasfeldeigenschaften nicht auslesbar"  , e);
							
					}	
					}
					
				}
				
			}else {
				logger.debug("Das Feld " + feld.getName() + " ist als SKIP-Feld gekennzeichnet");				
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
			Boolean includeError = false;
			for (Feld feld : kopfFelder) {
				if (!checkData(feld)) {
					includeError = true;
				}
				
			}
			List<DatensatzTabelle> tabellenZeilen = datensatz.getTabellenzeilen();
			for (DatensatzTabelle datensatzTabelle : tabellenZeilen) {
				ArrayList<Feld> tabfelder = datensatzTabelle.getTabellenFelder();
				for (Feld feld : tabfelder) {
					if (!checkData(feld)) {
						includeError = true;
					}
				}
			}
			if (includeError) {
				datensatz.createErrorReport();
			}
		}
		
		closeEdpSession();
	}

	private long getAbasFieldLength(String abasTyp){
		EDPEKSArtInfo edpeksartinfo = new EDPEKSArtInfo(abasTyp);
		return edpeksartinfo.getMaxLineLen();
	}
	
	private Boolean checkData(Feld feld) throws ImportitException {
		
		String value = feld.getValue();
//		falls Feld ist mit skip gekennzeichnet, nicht prüfen
		if (!feld.getOption_skip()) {
			logger.debug("checkData Feld " + feld.getName() + " Zeile "
					+ feld.getColNumber() + " AbasTyp " + feld.getAbasTyp()
					+ " Wert " + value);
			EDPEKSArtInfo edpeksartinfo = new EDPEKSArtInfo(feld.getAbasTyp());
			int datatyp = edpeksartinfo.getDataType();
			if (value != null) {
				if (!(feld.getOption_notEmpty() & value.isEmpty())) {
					if (datatyp == EDPTools.EDP_REFERENCE
							|| datatyp == EDPTools.EDP_ROWREFERENCE) {
						String edpErpArt = edpeksartinfo.getERPArt();
						if (edpErpArt.startsWith("V")) {

							if (edpErpArt.equals("VPK1")
									|| edpErpArt.equals("VPKS1")
									|| edpErpArt.equals("VPKT1")) {
								//							Verweis auf Fertigungslistenposition 
								if (value.startsWith("A ")) {
									//								Es ist ein Arbeitsgang
									EDPEKSArtInfo edpeksartinfoV = new EDPEKSArtInfo(
											"P7:0");
									checkVerweisFeld(feld, edpeksartinfoV);
								} else {
									//								Es ist ein Artikel, Fertigungsmittel oder Dienstleistung
									EDPEKSArtInfo edpeksartinfoV = new EDPEKSArtInfo(
											"P2:1.2.5");
									checkVerweisFeld(feld, edpeksartinfoV);
								}

							}

							//				Multiverweisfelder außer VPK1
							//				werden derzeit nicht überprüft.

						} else {
							//				normales Verweisfeld
							checkVerweisFeld(feld, edpeksartinfo);
						}

					} else if (datatyp == EDPTools.EDP_STRING) {
						Long fieldlength = edpeksartinfo.getMaxLen();
						Long valueLength = (long) value.length();
						if (fieldlength < valueLength) {
							feld.setError("Der Wert " + value + " ("
									+ valueLength
									+ "Zeichen) ist für das Feld "
									+ feld.getName() + " mit der Feldlänge "
									+ fieldlength.toString() + " zu lang ");
						}

					} else if (datatyp == EDPTools.EDP_INTEGER) {
						int integerDigits = edpeksartinfo.getIntegerDigits();
//						nur Prüfen wenn nicht leer oder <> 0
						if (value.length() > 0 && !value.equals("0")) {
							try {
								Integer intValue = new Integer(value);
								Integer valueLength = intValue.toString().length();
								if (integerDigits < valueLength) {
									feld.setError("Der Wert " + value
											+ "ist zu groß");
								}
							} catch (NumberFormatException e) {
								feld.setError("Der Wert "
										+ value
										+ " konnte nicht in einen Integer-Wert konvertiert werden");
							}
						}

					} else if (datatyp == EDPTools.EDP_DOUBLE) {
						int fractionDigits = edpeksartinfo.getFractionDigits();
						int integerDigits = edpeksartinfo.getIntegerDigits();
						
						if (value.length() > 0 && !value.equals("0")) {
							try {
								value = value.replaceAll(" ", "");
								BigDecimal bigDecimalValue = new BigDecimal(
										value);
								BigDecimal roundBigDValue = bigDecimalValue
										.setScale(fractionDigits,
												RoundingMode.HALF_UP);
								String roundBigDValueStr = roundBigDValue
										.toString();
								String compValue = fillvaluewithfractionDigit(
										value, fractionDigits);

								if (!roundBigDValueStr.equals(compValue)) {
									feld.setError("Das Runden auf die geforderten Nachkommastellen ergibt ein falsches Ergebnis org: "
											+ value
											+ " Vergleichswert "
											+ compValue
											+ " gerundeter Wert :"
											+ roundBigDValueStr);
								}

							} catch (NumberFormatException e) {
								feld.setError("Der Wert "
										+ value
										+ " konnte nicht in einen BigDezimal-Wert(Zahl mit Nachkommastellen) konvertiert werden");
							} catch (BadAttributeValueExpException e) {
								throw new ImportitException(
										"Es wurde ein falscher Übergabeparamter in der Programmierung übergeben",
										e);
							}

							String vorkommaStellen = "";
							if (value.contains(",")) {
								String[] stringTeile = value.split(",");
								if (stringTeile.length > 0) {
									vorkommaStellen = stringTeile[0];
								}
							} else if (value.contains(".")) {
								String[] stringTeile = value.split(".");
								if (stringTeile.length > 0) {
									vorkommaStellen = stringTeile[0];
								}

							} else {
								vorkommaStellen = value;
							}
							if (vorkommaStellen.length() > integerDigits) {
								feld.setError("Der Wert "
										+ value
										+ " hat zu viele Vorkommastellen für den AbasTyp "
										+ feld.getAbasTyp() + " für das Feld "
										+ feld.getName());

							}

						} else {

							//						Prüfung macht keinen Sinn bei lerrem Feld und Fehler wäre zu hart
						}

					} else if (datatyp == EDPTools.EDP_DATE) {
						if (!checkDataDate(feld)) {
							feld.setError("Der Wert "
									+ value
									+ " kann nicht in ein Abas-Datum gewandelt werden!");
						}

					} else if (datatyp == EDPTools.EDP_DATETIME
							|| datatyp == EDPTools.EDP_TIME
							|| datatyp == EDPTools.EDP_WEEK) {
						if (!checkDataDate(feld)) {
							feld.setError("Der Wert "
									+ value
									+ " kann nicht in ein Abas-Zeitformat gewandelt werden!");
						}
					}
				}
			} else {
				feld.setError("Der Wert ist null! Das ist nicht erlaubt");
			}
		}
		
		
		if (feld.getError().isEmpty()) {
			return true;
		}else {
			return false;
		}
		
		
	}

	/**
	 * @param feld
	 * @param value
	 * @param edpeksartinfo
	 */
	private void checkVerweisFeld(Feld feld, 
			EDPEKSArtInfo edpeksartinfo) {
		String value = feld.getValue();
		int databaseNumber = edpeksartinfo.getRefDatabaseNr();
		int groupNumber = edpeksartinfo.getRefGroupNr();
//		Die Prüfung soll nur ausgeführt werden wenn value <> "" ist
		if (!value.isEmpty()) {
			try {
				EDPQuery query = getEDPQueryVerweis(value,
						databaseNumber, groupNumber,
						feld.getColNumber());
				query.getLastRecord();
				int recordCount = query.getRecordCount();
				if (recordCount == 0) {
					feld.setError("Es wurde kein Datensatz für den Verweis "
							+ feld.getAbasTyp()
							+ "mit dem Wert "
							+ value + " gefunden!");
	
				} else if (recordCount > 1) {
					feld.setError("Es wurden mehrere Datensätze für den Verweis "
							+ feld.getAbasTyp()
							+ "mit dem Wert "
							+ value + " gefunden!");
	
				} else {
	
				}
	
			} catch (ImportitException e) {
				feld.setError("Es trat ein Fehler beim Prüfen des Verweises"
						+ feld.getAbasTyp()
						+ " mit dem Wert "
						+ value
						+ " auf");
	
			}
		}
	}

	/**
	 * @param value
	 * @param fractionDigits
	 * @return
	 * @throws BadAttributeValueExpException
	 * 
	 * Die Funktion soll den String der einen Zahl darstellt, mit den geforderten NachkommaStellen mit 0 auffüllen
	 * 
	 * Beispiel 4 geforderte Nachkommastellen 
	 * value = 1.5
	 * ergebniss =  1.5000
	 *  
	 *  
	 */
	private String fillvaluewithfractionDigit(String value, int fractionDigits)
			throws BadAttributeValueExpException {
 
		Double doubleValue = new Double(value);
		
		NumberFormat numberformat = new DecimalFormat("#.#########");
		String strdblValue = numberformat.format(doubleValue);
		
		String[] listofValue = strdblValue.split("\\.");
		String nullstring = fillStringWithMultipleValues("0", fractionDigits);
		if (listofValue.length > 1) {
			listofValue[1] = (listofValue[1] + nullstring).substring(0,
					fractionDigits);
			value = listofValue[0] + "." + listofValue[1];
		}else {
			value = listofValue[0] + "." + nullstring;
		}
		
		return value;
	}
		
	/**
	 * 
	 *
	 * @param value
	 * Es ist nur ein String mit einem Zeichen erlaubt
	 * @param numberofString
	 * @return einen String mit numberofString mal dem value Character
	 * @throws BadAttributeValueExpException 
	 * 
	 * 
	 */
	private String fillStringWithMultipleValues(String value,
			int numberofString) throws BadAttributeValueExpException {
		
		if (value.length()==1) {
			String multipleString = "";
			for (int i = 0; i < numberofString; i++) {
				multipleString = multipleString + value;
			}
			return multipleString;	
		}else {
			throw new BadAttributeValueExpException("Als Übergabe ist nur ein String mit einem Zeichen erlaubt!");
		}
		
	}

	private  Boolean checkDataDate(Feld feld) {
		String abastyp = feld.getAbasTyp();
		String value = feld.getValue();
		Boolean ergebnis=false;
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();

		String varnameErgebnis = "xtergebnis";

		if (!userTextBuffer.isVarDefined(varnameErgebnis)) {
			userTextBuffer.defineVar("Bool", varnameErgebnis);
		}
		userTextBuffer.setValue(varnameErgebnis, "0");
		
			String formelStr = "U|" +varnameErgebnis + " = F|isvalue( \""  + value + "\" , \"" + abastyp + "\")";
			FOe.formula(formelStr);
			ergebnis = userTextBuffer.getBooleanValue(varnameErgebnis);	
				
		
		return ergebnis;
	}
		
		
		
		
private EDPQuery getEDPQueryVerweis(String value, Integer database,
		Integer group, Integer rowNumber) throws ImportitException {
	
	if (!this.edpSession.isConnected()) {
		startEdpSession();
	}
	
	
	
	String[] fieldNames = {"id" , "nummer"};
	
	String tableName = "";
	
	
//	wenn die Gruppe nicht eindeutig wird eine -1 übergeben
	
	if (group == -1) {
		tableName= database.toString() + ":";
	}else {
		tableName= database.toString() + ":" + group.toString();	
	}
	
	EDPQuery query = this.edpSession.createQuery();
	String krit = "@noswd="  + value +  ";@englvar=true;@language=en;@database=" +database.toString();		
	StandardEDPSelectionCriteria criteria = new StandardEDPSelectionCriteria(krit);
	StandardEDPSelection edpcriteria = new StandardEDPSelection(tableName, criteria);
	edpcriteria.setDatabase(database.toString());
	
	try {

		query.startQuery(edpcriteria, fieldNames.toString());
		
	} catch (InvalidQueryException e) {
		closeEdpSession();
		throw new ImportitException( "fehlerhafter Selektionsstring: " + krit , e);
	}
	
	return query;
}

public void startTransaction() throws ImportitException {
	startEdpSession();
	try {
		this.edpSession.startTransaction();
	} catch (TransactionException e) {
		throw new ImportitException("Der Start der Transaction schlug fehl", e);
	}
}

public void commitTransaction() throws ImportitException {
	try {
		this.edpSession.commitTransaction();
	} catch (TransactionException e) {		
		throw new ImportitException("Das Commit der Transaction schlug fehl", e);
	}finally{
		closeEdpSession();
	}
	
}

public void abortTransaction() throws ImportitException {
	try {
		this.edpSession.abortTransaction();
	} catch (TransactionException e) {
		throw new ImportitException("Das Abbruch der Transaction schlug fehl", e);
		
	}catch(ConnectionLostException e){
		throw new ImportitException("Die Verbindung ist abgebrochen", e);
	}finally{
		closeEdpSession();
	}
	
}

}
