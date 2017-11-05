package de.abaspro.infosystem.importit.dataprocessing;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.ConnectionLostException;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.TransactionException;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.utils.Util;

public class EDPSessionHandler {

	private static final int MAX_ACTIVE_EDPSESSIONS = 3;

	private static String edpLogFile = "java/log/importit21edp.log";

	protected Logger logger = Logger.getLogger(EDPSessionHandler.class);

	private EDPSession edpSession;
	private String server;
	private Integer port;
	private String client;
	private String password;
	private Boolean edpsessionOnWork;
	private Boolean activeTransaction;

	Queue<EDPSession> fifo = new LinkedList<EDPSession>();

	private static EDPSessionHandler instance;

	private EDPSessionHandler() {
		this.server = null;
		this.port = null;
		this.client = null;
		this.password = null;
		this.edpsessionOnWork = false;
		this.activeTransaction = false;
	}

	public static synchronized EDPSessionHandler getInstance() {
		if (EDPSessionHandler.instance == null) {
			EDPSessionHandler.instance = new EDPSessionHandler();
		}
		return EDPSessionHandler.instance;
	}

	public void initSession(String server, Integer port, String client, String password) throws ImportitException {
		this.server = server;
		this.port = port;
		this.client = client;
		this.password = password;
		this.edpSession = createSession(this.server, this.port, this.client, this.password,
				EDPVariableLanguage.ENGLISH);
		fillqueue();
	}

	public Boolean isInit() {
		if (this.server == null) {
			return false;
		} else {
			return true;
		}
	}

	public EDPSession getEDPSession(EDPVariableLanguage varLang) {

		EDPSession edpSession = this.fifo.remove();
		changeVariableLanguage(edpSession, varLang);
		return edpSession;
	}

	public EDPSession getEDPSessionWriteData(EDPVariableLanguage varLang) {
		if (this.activeTransaction) {
			return getTransactionEdpSession(varLang);
		} else {
			return getEDPSession(varLang);
		}
	}

	private EDPSession getTransactionEdpSession(EDPVariableLanguage varLang) {
		try {
			if (!this.edpSession.isConnected()) {
				this.edpSession = createSession(this.server, this.port, this.client, this.password,
						EDPVariableLanguage.ENGLISH);
			}
			changeVariableLanguage(this.edpSession, varLang);

		} catch (ImportitException e) {
			logger.error(e);
		}
		return this.edpSession;
	}

	private void changeVariableLanguage(EDPSession edpSession, EDPVariableLanguage varLang) {
		try {

			if (!edpSession.getVariableLanguage().equals(varLang)) {
				edpSession.setVariableLanguage(varLang);
			}
			logger.info(Util.getMessage("info.edp.session.edpvarlangchanged", varLang.toString()));
		} catch (CantReadSettingException e) {
			logger.error(Util.getMessage("info.edp.session.edpvarlangchanged", varLang.toString()), e);
		}

	}

	public void freeEDPSession(EDPSession edpSession) {
		if (edpSession != this.edpSession) {
			closeEDPSession(edpSession);
		}
		this.edpsessionOnWork = false;
		logger.info(Util.getMessage("info.edp.session.release"));
		fillqueue();
	}

	private void fillqueue() {
		while (this.fifo.size() < MAX_ACTIVE_EDPSESSIONS) {
			EDPSession createSession = null;
			try {
				createSession = createSession(this.server, this.port, this.client, this.password,
						EDPVariableLanguage.ENGLISH);
				if (createSession.isConnected()) {
					fifo.add(createSession);
				}
			} catch (ImportitException e) {
				// hier ein Logeintrag aus, da bei Init schon geprüft wird ob es
				// klappt
				logger.error(e);
			}
		}
		logger.info(Util.getMessage("info.EDPHandler.fillqueue", fifo.size()));
	}

	public void startTransaction() throws ImportitException {

		try {
			this.edpSession.startTransaction();
			this.activeTransaction = true;
			logger.info(Util.getMessage("info.transaction.start"));
		} catch (TransactionException e) {
			logger.error(e);
			throw new ImportitException(Util.getMessage("err.transaction.start"), e);
		}

	}

	public void abortTransaction() throws ImportitException {

		try {
			this.edpSession.abortTransaction();

			logger.info(Util.getMessage("info.transaction.cancel"));
		} catch (TransactionException e) {
			throw new ImportitException(Util.getMessage("err.transaction.cancel"), e);

		} catch (ConnectionLostException e) {
			throw new ImportitException(Util.getMessage("err.edp.connection.cancel"), e);
		} finally {
			this.activeTransaction = false;
			this.edpsessionOnWork = false;
		}

	}

	public void commitTransaction() throws ImportitException {
		try {
			this.edpSession.commitTransaction();

			logger.info(Util.getMessage("info.transaction.commit"));
		} catch (TransactionException e) {
			throw new ImportitException(Util.getMessage("err.transaction.commit"), e);
		} finally {
			this.activeTransaction = false;
			this.edpsessionOnWork = false;
		}
	}

	private void closeEDPSession(EDPSession edpSession) {
		if (edpSession.isConnected()) {
			edpSession.endSession();
			logger.info(Util.getMessage("info.edp.session.closed", edpSession.getSessionTag()));
		} else {
			logger.error(Util.getMessage("err.edp.session.lost", edpSession.getSessionTag()));
		}
	}

	private EDPSession createSession(String server, int port, String client, String password,
			EDPVariableLanguage variableLanguage) throws ImportitException {

		EDPSession edpSession = EDPFactory.createEDPSession();

		try {
			edpSession.beginSession(server, port, client, password, "ImportIt_21");
			edpSession.loggingOn(edpLogFile);
			logger.info(Util.getMessage("info.edp.session.begin", edpSession.getSessionTag()));
			edpSession.setVariableLanguage(variableLanguage);
		} catch (CantBeginSessionException e) {
			logger.error(Util.getMessage("err.edp.session.start", e));
			throw new ImportitException(Util.getMessage("err.edp.session.start", e));
		}

		return edpSession;
	}

	public Boolean getActiveTransaction() {
		return activeTransaction;
	}

	public void closeAllConnections() {
		if (this.edpSession != null) {
			if (this.edpSession.isConnected()) {
				closeEDPSession(this.edpSession);
			}
		}
		if (fifo != null) {
			while (fifo.size() > 0) {
				EDPSession edpSession2 = fifo.remove();
				if (edpSession2 != null) {
					if (edpSession2.isConnected()) {
						closeEDPSession(edpSession2);
					}
				}
			}

		}
	}

}
