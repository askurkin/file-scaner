package ru.askurkin.file_scaner.scan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Proccess {
	private static final Logger logger = LogManager.getLogger(Proccess.class);

	private static void copyFile(FolderFile inFile, FolderFile outFile) {
		if (inFile.equals(outFile)) {
			throw new RuntimeException("Copy " + inFile.getPath() + " to self.");
		}
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile.getPath()))); DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile.getPath())))) {
			String buff = in.readLine();
			while (buff != null) {
				out.writeBytes(buff.replace(inFile.getSchema(), outFile.getSchema()) + "\n");
				buff = in.readLine();
			}
			out.flush();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void replace(FolderFile folderFileOld, FolderFile folderFileNew) {
		// файл уже свежий
		if (folderFileOld.equals(folderFileNew)) {
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

	public static void copy(FolderFile folderFile, String newFileName) {
		FolderFile newFile = new FolderFile(newFileName);

		if (newFile.exists()) {
			if (questions(folderFile, newFile)) {
				newFile.backup();
				copyFile(folderFile, newFile);
			}
		} else {
			copyFile(folderFile, newFile);
		}
	}

	public static boolean questions(FolderFile fromFile, FolderFile toFile) {
		if (fromFile.getPath().equals(toFile.getPath()) || !fromFile.exists()) {
			logger.warn("Не доступно " + fromFile + " => " + toFile);
			return false;
		}

		Scanner scanner = new Scanner(System.in);
		System.out.println("From: " + fromFile);
		System.out.println("To  : " + toFile);
		System.out.print("Заменить фалы [Y/N]: ");
		String response = scanner.next();
		if (response.toUpperCase().startsWith("Y") || response.toUpperCase().startsWith("Д")) {
			return true;
		}
		return false;
	}
}
