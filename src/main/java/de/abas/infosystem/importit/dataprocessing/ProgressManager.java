package de.abas.infosystem.importit.dataprocessing;

import de.abas.infosystem.importit.ProgressListener;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
//TODO check and remvove queue and multi threading
public class ProgressManager implements Runnable{

	protected Logger logger = Logger.getLogger(ProgressManager.class);

	private int zaehler;
	private final String text;
	private final int size;
	private boolean active = true;
	Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
	private List<ProgressListener> progressListener;


	public ProgressManager(String proptext, int size, List<ProgressListener> progressListener) {
		super();
		this.zaehler = 0;
		this.text = proptext;
		this.size = size;
		this.progressListener = progressListener;
	}

	public void sendProgress() {
		zaehler++;
		sendProgress(MessageUtil.getMessage(text, zaehler, size));
		logger.debug("sendprogess: " + MessageUtil.getMessage(text, zaehler, size));
	}

	private void sendProgress(String message) {
		logger.debug("send message: " + message);
		if (message != null) {
			for (ProgressListener pl : this.progressListener)
				pl.edpProgress(message);
		}
	}

	public void stop() {
		logger.info("ProgressMananger send stop");
		this.active = false;
	}

	@Override
	public void run() {
		while (active) {
			if (!messageQueue.isEmpty()) {
				while (!messageQueue.isEmpty()) {
					sendProgress(messageQueue.poll());
				}

			}
		}
		logger.debug("send last messages " + messageQueue.size());
		while (!messageQueue.isEmpty()) {
			messageQueue.poll();
			if (messageQueue.size() == 1) {
				logger.debug("send last message");
				sendProgress(messageQueue.poll());
			}
		}
		logger.info("ProgressMananger run -process end");
	}
}
