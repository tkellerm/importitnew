package de.abaspro.infosystem.importit;

import java.util.HashMap;

import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abaspro.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abaspro.utils.Util;

public class Vartab {

	HashMap<String, VartabField> englishVariables = new HashMap<>();
	HashMap<String, VartabField> germanVariables = new HashMap<>();

	public Vartab(EDPSessionHandler edpSessionHandler, Integer datenbank, Integer gruppe) throws ImportitException {
		EDPSession edpsession = edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
		EDPQuery query = edpsession.createQuery();
		int aliveFlag = EDPConstants.ALIVEFLAG_BOTH;
		String[] fieldNames = { "varName", "varNameNew", "inTab", "varType", "varTypeNew", "varLengthExt" };
		String key = "";
		String tableName = "12:26";
		String criteria = "0:grpDBDescr=(" + datenbank.toString() + ");0:grpGrpNo=" + gruppe.toString() + ";"
				+ ";@englvar=true;@language=en";
		try {
			query.startQuery(tableName, key, criteria, true, aliveFlag, true, true, fieldNames, 0, 10000);
			while (query.getNextRecord()) {
				VartabField vartabField = new VartabField(query);
				englishVariables.put(vartabField.getVarNameEnglish(), vartabField);
				germanVariables.put(vartabField.getActiveVarName(), vartabField);
			}
		} catch (InvalidQueryException e) {
			throw new ImportitException(Util.getMessage("err.edp.query.bad.selection.string", criteria), e);
		} finally {
			edpSessionHandler.freeEDPSession(edpsession);
		}
	}

	public VartabField checkVartabEnglish(String name) {
		return englishVariables.get(name);
	}

	public VartabField checkVartabGerman(String name) {
		return germanVariables.get(name);
	}

}
