package de.abaspro.utils.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
 
public class DirEdit {
 
	public static  void copyDir(File quelle, File ziel) throws FileNotFoundException, IOException {
 
		File[] files = quelle.listFiles();
		File newFile = null;
		ziel.mkdirs();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				newFile = new File(ziel.getAbsolutePath() + System.getProperty("file.separator") + files[i].getName());
				if (files[i].isDirectory()) {
					copyDir(files[i], newFile);
				}
				else {
					copyFile(files[i], newFile);
				}
			}
		}
	}
 
//	public static void copyFile(File file, File ziel) throws FileNotFoundException, IOException {
// 
//		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
//		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(ziel, true));
//		int bytes = 0;
//		while ((bytes = in.read()) != -1) {
//			out.write(bytes);
//		}
//		in.close();
//		out.close();
//	}
 
	public static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(in).getChannel();
            if (!out.exists()) {
				boolean test = out.isDirectory();
				
				out.createNewFile();
			}
            outChannel = new FileOutputStream(out).getChannel();
            
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null)
                    outChannel.close();
            } catch (IOException e) {}
        }
    } 
	
	public static void deleteDir(File dir) {
 
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDir(files[i]);
				}
				else {
					files[i].delete();
				}
			}
			dir.delete();
		}
	}
 
	public static ArrayList<File> searchFile(File dir, String find) {
 
		File[] files = dir.listFiles();
		ArrayList<File> matches = new ArrayList<File> ();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equalsIgnoreCase(find)) { 
					matches.add(files[i]);
				}
				if (files[i].isDirectory()) {
					matches.addAll(searchFile(files[i], find)); 
				}
			}
		}
		return matches;
	}
 
	public static void listDir(File dir) {
 
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				System.out.print(files[i].getAbsolutePath());
				if (files[i].isDirectory()) {
					System.out.print(" (Ordner)\n");
					listDir(files[i]); 
				}
				else {
					System.out.print(" (Datei)\n");
				}
			}
		}
	}
 
	public static long getDirSize(File dir) {
 
		long size = 0;
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					size += getDirSize(files[i]);
				}
				else {
					size += files[i].length();
				}
			}
		}
		return size;
	}
}