package de.abas.infosystem.importit.dataprocessing;

import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.infosystem.importit.ImportitException;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EDPSessionPool implements Runnable {

    private static String edpLogFile = "java/log/importit21edp.log";
    Queue<EDPSession> fifo = new ConcurrentLinkedQueue<>();
    protected Logger logger = Logger.getLogger(EDPSessionPool.class);
    private static final int MAX_ACTIVE_EDPSESSIONS = 20;

    private boolean activePool = true;
    private Integer sleeptime;
    private static final Integer MAX_SLEEP_TIME = 2000;

    public EDPSessionPool(EDPSessionHandler edpSessionHandler) {
        this.edpSessionHandler = edpSessionHandler;
        this.sleeptime = 0;
    }

    private EDPSessionHandler edpSessionHandler;

    //TODO: Use Commons Pool
    @Override
    public void run() {

        while (this.activePool) {
            try {
                fillqueue();
                if (fifo.size() == MAX_ACTIVE_EDPSESSIONS) {
                    Thread.sleep(sleeptime);
                }
            } catch (Exception e) {
                logger.error("run ", e);

            }

        }
        logger.info(MessageUtil.getMessage("info.edpSessionPool.closeConnections"));
        while (!this.fifo.isEmpty()) {
            EDPSession edpSession = fifo.remove();
            closeEDPSession(edpSession);
        }

    }

    public synchronized EDPSession getEDPSession() {
        EDPSession edpSession = null;

        while (this.fifo.isEmpty()) {
            logger.error("WAIT FOR SESSION " + Thread.currentThread().getState().toString());
// TODO finde andere Lösung
        }
        try {
            edpSession = this.fifo.remove();
            logger.info(
                    MessageUtil.getMessage("info.edpSessionPool.getEDPSession", edpSession.getSessionTag(), this.fifo.size()));
        } catch (NoSuchElementException e) {
            logger.error(e);
            edpSession = getEDPSession();
        }
        return edpSession;

    }

    public synchronized void addEDPsession(EDPSession edpSession) {
        if (edpSession.isConnected()) {
            this.fifo.add(edpSession);
            logger.info(
                    MessageUtil.getMessage("info.edpSessionPool.addEDPSession", edpSession.getSessionTag(), this.fifo.size()));
        }
    }

    private EDPSession createSession(EDPSessionHandler edpSessionHandler, EDPVariableLanguage variableLanguage)
            throws ImportitException {

        EDPSession edpSession = EDPFactory.createEDPSession();

        try {
            edpSession.beginSession(edpSessionHandler.getServer(), edpSessionHandler.getPort(),
                    edpSessionHandler.getClient(), edpSessionHandler.getPassword(), "ImportIt_21");
            edpSession.loggingOn(edpLogFile);
            logger.info(MessageUtil.getMessage("info.edp.session.begin", edpSession.getSessionTag()));
            edpSession.setVariableLanguage(variableLanguage);
        } catch (CantBeginSessionException e) {
            logger.error(MessageUtil.getMessage("err.edp.session.start", e));
            throw new ImportitException(MessageUtil.getMessage("err.edp.session.start", e));
        }

        return edpSession;
    }

    private void fillqueue() {
        boolean newSession = false;
        while (this.fifo.size() < MAX_ACTIVE_EDPSESSIONS) {
            EDPSession createSession = null;
            try {
                createSession = createSession(edpSessionHandler, EDPVariableLanguage.ENGLISH);
                if (createSession.isConnected()) {
                    fifo.add(createSession);
                }
                this.sleeptime = 0;
                newSession = true;
                logger.info(MessageUtil.getMessage("info.EDPHandler.fillqueue", fifo.size()));
            } catch (ImportitException | NullPointerException e) {
                // hier ein Logeintrag aus, da bei Init schon geprüft wird ob es
                // klappt
                logger.error(e);
            }
        }
        if (!newSession) {
            ascendingSleepTime();

        }
    }

    private void ascendingSleepTime() {
        if (this.sleeptime <= MAX_SLEEP_TIME) {
            this.sleeptime++;
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

    public synchronized void closeSessions() {
        this.activePool = false;

    }

}
