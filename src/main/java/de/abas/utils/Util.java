package de.abas.utils;

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

import org.apache.log4j.Logger;

import static de.abas.eks.jfop.remote.EKS.getFOPSessionContext;

public class Util {

	private static final  String MESSAGE_BASE = "de.abaspro.infosystem.importit.messages";

	private static final Locale locale = Locale.GERMAN;

	private static final Logger logger = Logger.getLogger(Util.class);

	private Util() {
		throw new IllegalStateException("Utility class");
	}

	public static Locale getLocale() {
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

	/**
	 * 
	 * @param tarFile tar file
	 * @param destFile destination directory
	 * @throws IOException Error if File not available
	 */
	public static void unTarFile(File tarFile, File destFile) throws IOException {

		try (FileInputStream fis = new FileInputStream(tarFile)) {
			if(tarFile.exists()) {
				try (TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {
					TarArchiveEntry tarEntry;

					// tarIn is a TarArchiveInputStream
					while ((tarEntry = tis.getNextTarEntry()) != null) {
						File outputFile = new File(destFile + File.separator + tarEntry.getName());

						if (tarEntry.isDirectory()) {

							logger.info("outputFile Directory ---- " + outputFile.getAbsolutePath());
							if (!outputFile.exists()) {
								outputFile.mkdirs();
							}
						} else {

							logger.info("outputFile File ---- " + outputFile.getAbsolutePath());
							outputFile.getParentFile().mkdirs();
							FileOutputStream fos = new FileOutputStream(outputFile);
							IOUtils.copy(tis, fos);
							fos.close();
						}
					}
				}
			}
		}


	}

}
