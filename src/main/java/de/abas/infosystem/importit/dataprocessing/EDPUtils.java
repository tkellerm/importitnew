package de.abas.infosystem.importit.dataprocessing;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.ServerActionException;

public class EDPUtils {

	private EDPUtils(){
		throw new IllegalStateException("Utility class");
	}

	 protected static void releaseQuery(EDPQuery query, Logger logger) {

		if (query != null) {
			try {
				query.release();
			} catch (ServerActionException e) {
				logger.error(e);
			}
		}

	}

	 protected static void releaseEDPEditor(EDPEditor edpEditor, Logger logger) {

		if (edpEditor.isActive()) {
			try {
				edpEditor.release();
			} catch (ServerActionException e) {
				logger.error(e);
			}
		}

	}

	protected static String replaceCharPlusBlank(String sign, String value) {
		String regexpr = "^[" + sign + "]\\s";
		value = value.replaceAll(regexpr, "");
		return value;
	}

}