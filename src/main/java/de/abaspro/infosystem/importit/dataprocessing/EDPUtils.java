package de.abaspro.infosystem.importit.dataprocessing;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.ServerActionException;

public class EDPUtils {

	static protected void releaseQuery(EDPQuery query, Logger logger) {

		try {
			query.release();
		} catch (ServerActionException e) {
			logger.error(e);
		}

	}

	static protected void releaseEDPEditor(EDPEditor edpEditor, Logger logger) {

		if (edpEditor.isActive()) {
			try {
				edpEditor.release();
			} catch (ServerActionException e) {
				logger.error(e);
			}
		}

	}
}