package ru.askurkin.file_scaner.scan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Proccess {
	private static final Logger logger = LogManager.getLogger(Proccess.class);

	public static void copyFile(FolderFile inFile, FolderFile outFile) {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile.getPath())));
			 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile.getPath())))) {
			String buff = in.readLine();
			while (buff != null) {
				out.writeBytes(buff.replace(inFile.getSchema(), outFile.getSchema()) + "\n");
				buff = in.readLine();
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
				|| folderFileOld.getSize() == folderFileNew.getSize()
				|| folderFileOld.length() == folderFileNew.length()) {
			return;
		}

		if (questions(folderFileNew, folderFileOld)) {
			if (folderFileOld.existsBackup()) {
				throw new RuntimeException("Backup " + folderFileOld.getName() + " is exists, try delete backup!!");
			}
			folderFileOld.backup();
			copyFile(folderFileNew, folderFileOld);
		}
	}

	public static void restore(FolderFile folderFile) {
		FolderFile backupFile = new FolderFile(folderFile.getBackupPath());
		if (!folderFile.existsBackup()) {
			logger.warn("File " + backupFile.getPath() + " not exists");
			return;
		}

		if (questions(backupFile, folderFile)) {
			folderFile.restore();
		}
	}

	public static boolean questions(FolderFile fromFile, FolderFile toFile) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("From: " + fileInfo(fromFile));
		System.out.println("To  : " + fileInfo(toFile));
		System.out.print("Заменить фалы [Y/N]: ");
		String response = scanner.next();
		if (response.toUpperCase().startsWith("Y") || response.toUpperCase().startsWith("Д")) {
			return true;
		}
		return false;
	}

	public static String fileInfo(FolderFile folderFile) {
		return String.format("%1$s; size: %2$d bytes; modified: %3$tY-%3$tm-%3$td %3$tH:%3$tM",
				folderFile.getName(),
				folderFile.length(),
				new Timestamp(folderFile.lastModified())
		);
	}
}
