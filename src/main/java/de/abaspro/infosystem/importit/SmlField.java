package de.abaspro.infosystem.importit;

import de.abas.ceks.jedp.EDPQuery;
import de.abaspro.utils.Util;

public class SmlField {

	private static String varNameVarName = "varName";
	private static String varNameVarType = "typeOfAdditionalVar";

	private String varName;
	private String varType;

	public SmlField(EDPQuery query) {
		if (query != null) {
			this.varName = query.getField(varNameVarName);

			this.varType = query.getField(varNameVarType);

		} else
			throw new NullPointerException(Util.getMessage("vartab.field.query.not.defined"));
	}

	public String getType() {

		return this.varType;

	}

	public String getVarName() {

		return this.varName;

	}

}
