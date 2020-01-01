package de.abaspro.infosystem.importit.dataprocessing;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import de.abaspro.infosystem.importit.ProgressListener;
import de.abaspro.utils.Util;

public class ProgressManager implements Runnable {

	protected Logger logger = Logger.getLogger(ProgressManager.class);

	private int zaehler;
	private String text;
	private int size;
	private boolean active = true;

	Queue<String> messageQueue = new ConcurrentLinkedQueue<String>();

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
		messageQueue.add(Util.getMessage(text, zaehler, size));
		logger.debug("sendprogess: " + Util.getMessage(text, zaehler, size));
	}

	private void sendProgress(String message) {
		logger.debug("send message: " + message);
		if (message != null) {
			for (ProgressListener pl : this.progressListener)
				pl.edpProgress(message);
		}
	}

	public void stop() {
		this.active = false;
	}

	@Override
	public void run() {
		while (active) {
			if (!messageQueue.isEmpty()) {
				while (!messageQueue.isEmpty()) {
					sendProgress(messageQueue.poll());
					if (!active) {
						while (messageQueue.isEmpty()) {
							messageQueue.poll();
							if (messageQueue.size() == 1) {
								sendProgress(messageQueue.poll());
							}
						}
					}
				}

			}
		}

	}

}
