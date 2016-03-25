package de.abaspro.infosystem.importit;

import java.util.HashMap;

import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.InvalidQueryException;

public class Vartab {
	 
	
	HashMap<String, VartabFeld> englVars;
	HashMap<String, VartabFeld> germanVars;
	 
	
	 public Vartab(EDPSession edpSession , Integer datenbank , Integer gruppe) throws ImportitException {
//		Es werden alle Felder aus Vartab geladen
		EDPQuery query = edpSession.createQuery(); 
		
		int aliveFlag = EDPConstants.ALIVEFLAG_BOTH;
		String[] fieldNames = {"varName" , "varNameNew" , "inTab" , "varType" , "varTypeNew" , "varLengthExt"};
		String key = "";
		String tableName = "12:26";
		this.englVars = new HashMap<String, VartabFeld>();
		this.germanVars = new HashMap<String, VartabFeld>();
		String krit = "0:grpDBDescr=(" + datenbank.toString() + ");0:grpGrpNo=" + gruppe.toString() +    ";" +  ";@englvar=true;@language=en";		
		
		try {
			query.startQuery(tableName, key, krit, true, aliveFlag, true, true, fieldNames, 0, 10000);
			while (query.getNextRecord()) {
				
				VartabFeld vartabFeld = new VartabFeld(query);
				this.englVars.put(vartabFeld.getVarNameEnglish(), vartabFeld);
				this.germanVars.put(vartabFeld.getAktivVarName(), vartabFeld);
			}
		
		} catch (InvalidQueryException e) {
			if (edpSession.isConnected()) {
				edpSession.endSession();
			}
			throw new ImportitException( "fehlerhafter Selektionsstring: " + krit , e);
		} 
		 
	}

	public VartabFeld checkVartabforEnglVarName(String name) {
		VartabFeld vartabFeld = englVars.get(name);
		
		return vartabFeld;
	}

	public VartabFeld checkVartabforGermanVarName(String name) {
		VartabFeld vartabFeld = germanVars.get(name);
		
		return vartabFeld;
	}
	
	
	
}
