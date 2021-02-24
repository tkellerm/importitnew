package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.*;
import de.abas.infosystem.importit.ImportitException;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

public class EDPSessionHandler  {

	protected Logger logger = Logger.getLogger("EDPSessionHandlerSpezial");
	private static final String EDP_LOG_FILE = "java/log/importitedp.log";

	private EDPSession edpSession;
	private String server;
	private Integer port;
	private String client;
	private String password;
//TODO: STRING für Passwort anders machen
	private boolean activeTransaction;

	private Thread sessionPoolThread;

	private EDPSessionPool edpSessionPool;

	private static EDPSessionHandler instance;

	private EDPSessionHandler() {
		super();
		this.server = null;
		this.port = null;
		this.client = null;
		this.password = null;
		this.activeTransaction = false;
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
		this.edpSession = createSession(this.server, this.port, this.client, this.password
		);
		this.edpSessionPool = new EDPSessionPool(this);
		this.sessionPoolThread = new Thread(this.edpSessionPool);
		this.sessionPoolThread.start();

	}

	public EDPSession getEDPSession(EDPVariableLanguage varLang)  {

		EDPSession edpSessionLocal = this.edpSessionPool.getEDPSession();

		changeVariableLanguage(edpSessionLocal, varLang);
		return edpSessionLocal;
	}

	public EDPSession getEDPSessionWriteData(EDPVariableLanguage varLang) {
		if (this.activeTransaction) {
			return getTransactionEdpSession(varLang);
		} else {
			return getEDPSession(varLang);
		}
	}

	private synchronized EDPSession getTransactionEdpSession(EDPVariableLanguage varLang) {
		try {
			if (!this.edpSession.isConnected()) {
				this.edpSession = createSession(this.server, this.port, this.client, this.password
				);
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
			logger.info(MessageUtil.getMessage("info.edp.session.edpvarlangchanged", varLang.toString()));
		} catch (CantReadSettingException e) {
			logger.error(MessageUtil.getMessage("info.edp.session.edpvarlangchanged", varLang.toString()), e);
		}

	}

	public void freeEDPSession(EDPSession edpSession) {
		if (edpSession != this.edpSession) {
			this.edpSessionPool.addEDPsession(edpSession);
		}
		logger.info(MessageUtil.getMessage("info.edp.session.release"));

	}

	public void startTransaction() throws ImportitException {

		try {
			this.edpSession.startTransaction();
			this.activeTransaction = true;
			logger.info(MessageUtil.getMessage("info.transaction.start"));
		} catch (TransactionException e) {
			logger.error(e);
			throw new ImportitException(MessageUtil.getMessage("err.transaction.start"), e);
		}

	}

	public void abortTransaction() throws ImportitException {

		try {
			this.edpSession.abortTransaction();

			logger.info(MessageUtil.getMessage("info.transaction.cancel"));
		} catch (TransactionException e) {
			throw new ImportitException(MessageUtil.getMessage("err.transaction.cancel"), e);

		} catch (ConnectionLostException e) {
			throw new ImportitException(MessageUtil.getMessage("err.edp.connection.cancel"), e);
		} finally {
			this.activeTransaction = false;
		}

	}

	public void commitTransaction() throws ImportitException {
		try {
			this.edpSession.commitTransaction();

			logger.info(MessageUtil.getMessage("info.transaction.commit"));
		} catch (TransactionException e) {
			throw new ImportitException(MessageUtil.getMessage("err.transaction.commit"), e);
		} finally {
			this.activeTransaction = false;
		}
	}

	private void closeEDPSession(EDPSession edpSession) {
		if (edpSession.isConnected()) {
			edpSession.endSession();
			logger.info(MessageUtil.getMessage("info.edp.session.closed", edpSession.getSessionTag()));
		} else {
			logger.error(MessageUtil.getMessage("err.edp.session.lost", edpSession.getSessionTag()));
		}
	}

	private EDPSession createSession(String server, int port, String client, String password) throws ImportitException {

		EDPSession edpSessionLocal = EDPFactory.createEDPSession();

		try {
			edpSessionLocal.beginSession(server, port, client, password, "ImportIt_21");
			edpSessionLocal.loggingOn(EDP_LOG_FILE);
			logger.info(MessageUtil.getMessage("info.edp.session.begin", edpSessionLocal.getSessionTag()));
			edpSessionLocal.setVariableLanguage(EDPVariableLanguage.ENGLISH);
		} catch (CantBeginSessionException e) {
			logger.error(MessageUtil.getMessage("err.edp.session.start", e));
			throw new ImportitException(MessageUtil.getMessage("err.edp.session.start", e));
		}

		return edpSessionLocal;
	}

	public Boolean isTransactionActive() {
		return activeTransaction;
	}

	public synchronized void closeAllConnections() {

		if ((this.edpSession != null) && (this.edpSession.isConnected())) {
				closeEDPSession(this.edpSession);
			}
		if (this.edpSessionPool != null) {
			this.edpSessionPool.closeSessions();
			while (this.sessionPoolThread.getState() != Thread.State.TERMINATED) {
				logger.debug(MessageUtil.getMessage("debug.EDPSessionHandler.waitEndThread"));
			}

		}
		logger.info(MessageUtil.getMessage("info.EDPSessionHandler.allSessions.closed"));
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
