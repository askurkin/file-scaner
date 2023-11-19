package ru.askurkin.file_scaner.scan;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Proccess {
	private static final Logger logger = LogManager.getLogger(Proccess.class);

	public static void copyFile(File inFile, File outFile) {
		try (FileInputStream in = new FileInputStream(inFile.getPath());
			 FileOutputStream out = new FileOutputStream(outFile.getPath())) {
			int ch = in.read();
			while (ch != -1) {
				out.write(ch);
				ch = in.read();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void replace(FolderFile folderFileOld, FolderFile folderFileNew) {
		// файл уже свежий
		if (folderFileOld.getPath().equals(folderFileNew.getPath())
				|| folderFileOld.getSize() == folderFileNew.getSize()) {
			return;
		}
		File file = folderFileOld.getFile();
		File fileBak = folderFileOld.getBackupFile();
		File fileNew = new File(folderFileNew.getPath());

		if (questions(fileNew, file)) {
			if (fileBak.exists()) {
				throw new RuntimeException("Backup " + fileBak.getName() + " is exists, try delete backup!!");
			}
			file.renameTo(fileBak);
			copyFile(fileNew, file);
		}
	}

	public static void restore(FolderFile folderFile) {
		File file = folderFile.getFile();
		File fileBak = folderFile.getBackupFile();
		if (!fileBak.exists()) {
			logger.warn("File " + fileBak.getPath() + " not exists");
			return;
		}

		if (questions(fileBak, file)) {
			file.delete();
			fileBak.renameTo(file);
		}
	}

	public static boolean questions(File fromFile, File toFile) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("From: " + fileInfo(fromFile));
		System.out.println("To  : " + fileInfo(toFile));
		System.out.print("Заменить фалы [Y/N]:");
		String response = scanner.next();
		if (response.toUpperCase().startsWith("Y") || response.toUpperCase().startsWith("Д")) {
			return true;
		}
		return false;
	}

	public static String fileInfo(File file) {
		return String.format("%1$s; size: %2$d bytes; modified: %3$tY-%3$tm-%3$td %3$tH:%3$tM",
				file.getName(),
				file.length(),
				new Timestamp(file.lastModified())
		);
	}
}
