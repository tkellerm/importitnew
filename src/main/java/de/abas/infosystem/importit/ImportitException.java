package de.abas.infosystem.importit;

import org.apache.commons.lang3.exception.ExceptionUtils;


public class ImportitException extends Exception {

	private static final long serialVersionUID = 66964017367429334L;

	public ImportitException(String message) {
		super(message);

	}

	public ImportitException(String message , Exception e) {
		super(message + "\n" + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));

	}


}
