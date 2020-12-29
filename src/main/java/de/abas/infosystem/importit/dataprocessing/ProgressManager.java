package de.abas.infosystem.importit.dataprocessing;

import java.util.List;

import de.abas.utils.Util;
import de.abas.infosystem.importit.ProgressListener;

public class ProgressManager {

	private int zaehler;
	private String text;
	private int size;

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
		sendProgress(Util.getMessage(text, zaehler, size));

	}

	private void sendProgress(String message) {
		for (ProgressListener pl : this.progressListener)
			pl.edpProgress(message);
	}

}
