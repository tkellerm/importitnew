package de.abaspro.infosystem.importit.dataprocessing;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.utils.Util;

public class EDPSessionPool implements Runnable {

	private static String edpLogFile = "java/log/importit21edp.log";
	Queue<EDPSession> fifo = new ConcurrentLinkedQueue<EDPSession>();
	protected Logger logger = Logger.getLogger(EDPSessionPool.class);
	private static final int MAX_ACTIVE_EDPSESSIONS = 50;

	private Boolean activePool = true;

	public EDPSessionPool(EDPSessionHandler edpSessionHandler) {
		this.edpSessionHandler = edpSessionHandler;
	}

	private EDPSessionHandler edpSessionHandler;

	@Override
	public void run() {
		long zaehler = 0;
		while (this.activePool) {
			try {

				zaehler++;
				fillqueue();
			} catch (Exception e) {
				logger.error(e);
			}
			// if (zaehler > 10000000) {
			// logger.debug("Thread aktiv " + zaehler + "queue size: " +
			// this.fifo.size());
			// zaehler = 0;
			// }
		}
		logger.info(Util.getMessage("info.edpSessionPool.closeConnections"));
		Integer xiof = fifo.size();
		while (this.fifo.size() > 0) {
			EDPSession edpSession = fifo.remove();
			closeEDPSession(edpSession);
		}

	}

	public EDPSession getEDPSession() {
		EDPSession edpSession = null;

		while (this.fifo.size() == 0) {
			logger.error("WAIT FOR SESSION " + Thread.currentThread().getState().toString());

		}
		try {
			edpSession = this.fifo.remove();
			logger.info(
					Util.getMessage("info.edpSessionPool.getEDPSession", edpSession.getSessionTag(), this.fifo.size()));
		} catch (NoSuchElementException e) {
			logger.error(e);
			edpSession = getEDPSession();
		}
		return edpSession;

	}

	public void addEDPsession(EDPSession edpSession) {
		if (edpSession.isConnected()) {
			this.fifo.add(edpSession);
			logger.info(
					Util.getMessage("info.edpSessionPool.addEDPSession", edpSession.getSessionTag(), this.fifo.size()));
		}
	}

	private EDPSession createSession(EDPSessionHandler edpSessionHandler, EDPVariableLanguage variableLanguage)
			throws ImportitException {

		EDPSession edpSession = EDPFactory.createEDPSession();

		try {
			edpSession.beginSession(edpSessionHandler.getServer(), edpSessionHandler.getPort(),
					edpSessionHandler.getClient(), edpSessionHandler.getPassword(), "ImportIt_21");
			edpSession.loggingOn(edpLogFile);
			logger.info(Util.getMessage("info.edp.session.begin", edpSession.getSessionTag()));
			edpSession.setVariableLanguage(variableLanguage);
		} catch (CantBeginSessionException e) {
			logger.error(Util.getMessage("err.edp.session.start", e));
			throw new ImportitException(Util.getMessage("err.edp.session.start", e));
		}

		return edpSession;
	}

	private void fillqueue() {

		while (this.fifo.size() < MAX_ACTIVE_EDPSESSIONS) {
			EDPSession createSession = null;
			try {
				createSession = createSession(edpSessionHandler, EDPVariableLanguage.ENGLISH);
				if (createSession.isConnected()) {
					fifo.add(createSession);
				}
				logger.info(Util.getMessage("info.EDPHandler.fillqueue", fifo.size()));
			} catch (ImportitException e) {
				// hier ein Logeintrag aus, da bei Init schon geprüft wird ob es
				// klappt
				logger.error(e);
			} catch (NullPointerException e) {
				logger.error(e);
			}
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

	public void closeSessions() {
		this.activePool = false;

	}

}
