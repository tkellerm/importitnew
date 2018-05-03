package de.abaspro.infosystem.importit;

import java.util.HashMap;

import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abaspro.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abaspro.utils.Util;

public class SmlTab {

	HashMap<String, SmlField> variables = new HashMap<>();

	public SmlTab(EDPSessionHandler edpSessionHandler, String smlNumber) throws ImportitException {
		EDPSession edpsession = edpSessionHandler.getEDPSession(EDPVariableLanguage.ENGLISH);
		EDPQuery query = edpsession.createQuery();
		int aliveFlag = EDPConstants.ALIVEFLAG_BOTH;
		String[] fieldNames = { "varName", "typeOfAdditionalVar", "varType", "width" };
		String key = "";

		String tableName = "12:24";
		String criteria = "0:idno=" + smlNumber + ";type=(2);@englvar=true;@language=en";

		try {
			query.startQuery(tableName, key, criteria, true, aliveFlag, true, true, fieldNames, 0, 10000);
			while (query.getNextRecord()) {
				SmlField smlField = new SmlField(query);

				variables.put(smlField.getVarName(), smlField);
			}
		} catch (InvalidQueryException e) {
			throw new ImportitException(Util.getMessage("err.edp.query.bad.selection.string", criteria), e);
		} finally {
			edpSessionHandler.freeEDPSession(edpsession);
		}
	}

	public SmlField checkSmlTab(String name) {
		return this.variables.get(name);
	}

}
