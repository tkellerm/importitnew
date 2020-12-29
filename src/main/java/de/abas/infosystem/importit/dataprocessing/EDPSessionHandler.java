package de.abas.infosystem.importit.dataprocessing;

import de.abas.utils.Util;
import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.CantReadSettingException;
import de.abas.ceks.jedp.ConnectionLostException;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.TransactionException;
import de.abas.infosystem.importit.ImportitException;

public class EDPSessionHandler extends Thread {

	protected Logger logger = Logger.getLogger("EDPSessionHandlerSpezial");
	private static String edpLogFile = "java/log/importit21edp.log";

	private EDPSession edpSession;
	private String server;
	private Integer port;
	private String client;
	private String password;

	private Boolean edpsessionOnWork;
	private Boolean activeTransaction;

	private Thread sessionPoolThread;

	private boolean activeHandler;

	private EDPSessionPool edpSessionPool;

	private static EDPSessionHandler instance;

	private EDPSessionHandler() {
		this.server = null;
		this.port = null;
		this.client = null;
		this.password = null;
		this.edpsessionOnWork = false;
		this.activeTransaction = false;
		this.activeHandler = true;
		this.sessionPoolThread = null;
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
		this.edpSessionPool = new EDPSessionPool(this);
		this.sessionPoolThread = new Thread(this.edpSessionPool);
		this.sessionPoolThread.start();

	}

	public Boolean isInit() {
		if (this.server == null) {
			return false;
		} else {
			return true;
		}
	}

	public EDPSession getEDPSession(EDPVariableLanguage varLang) throws ImportitException {

		EDPSession edpSession = this.edpSessionPool.getEDPSession();

		changeVariableLanguage(edpSession, varLang);
		return edpSession;
	}

	public EDPSession getEDPSessionWriteData(EDPVariableLanguage varLang) throws ImportitException {
		if (this.activeTransaction) {
			return getTransactionEdpSession(varLang);
		} else {
			return getEDPSession(varLang);
		}
	}

	private synchronized EDPSession getTransactionEdpSession(EDPVariableLanguage varLang) {
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
			this.edpSessionPool.addEDPsession(edpSession);
		}
		logger.info(Util.getMessage("info.edp.session.release"));

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

	public synchronized void closeAllConnections() {
		this.activeHandler = false;

		if (this.edpSession != null) {
			if (this.edpSession.isConnected()) {
				closeEDPSession(this.edpSession);
			}
		}
		if (this.edpSessionPool != null) {
			this.edpSessionPool.closeSessions();
			while (this.sessionPoolThread.getState() != State.TERMINATED) {
				logger.debug(Util.getMessage("debug.EDPSessionHandler.waitEndThread"));
			}

		}
		logger.info(Util.getMessage("info.EDPSessionHandler.allSessions.closed"));
	}

	public String getServer() {
		return server;
	}

	public Integer getPort() {
		return port;
	}

	public String getClient() {
		return client;
	}

	public String getPassword() {
		return password;
	}

}
