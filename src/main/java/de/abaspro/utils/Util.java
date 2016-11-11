package de.abaspro.utils;

import de.abas.eks.jfop.remote.EKS;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Util {

    private final static String MESSAGE_BASE = "de.abaspro.infosystem.importit.messages";

    private static Locale locale = Locale.GERMAN;

    public static Locale getLocale() {
        try {
            return EKS.getFOPSessionContext().getOperatingLangLocale();
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
