package de.abaspro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import de.abas.eks.jfop.remote.EKS;

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

	/**
	 * 
	 * @param tarFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void unTarFile(File tarFile, File destFile) throws IOException {
		FileInputStream fis = new FileInputStream(tarFile);
		boolean exists = tarFile.exists();
		TarArchiveInputStream tis = new TarArchiveInputStream(fis);
		TarArchiveEntry tarEntry = null;

		// tarIn is a TarArchiveInputStream
		while ((tarEntry = tis.getNextTarEntry()) != null) {
			File outputFile = new File(destFile + File.separator + tarEntry.getName());

			if (tarEntry.isDirectory()) {

				System.out.println("outputFile Directory ---- " + outputFile.getAbsolutePath());
				if (!outputFile.exists()) {
					outputFile.mkdirs();
				}
			} else {
				// File outputFile = new File(destFile + File.separator +
				// tarEntry.getName());
				System.out.println("outputFile File ---- " + outputFile.getAbsolutePath());
				outputFile.getParentFile().mkdirs();
				// outputFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(outputFile);
				IOUtils.copy(tis, fos);
				fos.close();
			}
		}
		tis.close();
	}

}
