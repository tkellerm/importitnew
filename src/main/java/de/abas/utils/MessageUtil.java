package de.abas.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.abas.eks.jfop.remote.EKS.getFOPSessionContext;

public class MessageUtil {

	private static final  String MESSAGE_BASE = "de.abas.infosystem.importit.messages";

	private static final Locale locale = Locale.GERMAN;

	private MessageUtil() {
		throw new IllegalStateException("Utility class");
	}

	private static Locale getLocale() {
		try {
			return getFOPSessionContext().getOperatingLangLocale();
		} catch (final NullPointerException e) {
			return locale;
		}
	}

	public static String getMessage(String key) {
		final ResourceBundle rb = ResourceBundle.getBundle(MESSAGE_BASE, getLocale());
		return rb.getString(key);
	}

	public static String getMessage(String key, Object... params) {
		final ResourceBundle rb = ResourceBundle.getBundle(MESSAGE_BASE, getLocale());
		return MessageFormat.format(rb.getString(key), params);
	}

}
