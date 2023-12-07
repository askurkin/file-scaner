package ru.askurkin.file_scaner.scan;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Proccess {
	private static final Logger logger = LogManager.getLogger(Proccess.class);

	private static void copyFile(FolderFile inFile, FolderFile outFile) {
		if (inFile.equals(outFile)) {
			throw new RuntimeException("Copy " + inFile.getPath() + " to self.");
		}
		String version = inFile.getNewVersion();
		boolean setVer = false;
		try (BufferedReader in = new BufferedReader(new FileReader(inFile.getPath()));
			 PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile.getPath())))) {
			String buff = in.readLine();
			while (buff != null) {
				String buffTrim = buff.trim().toLowerCase();
				if (!setVer) {
					if (!buff.contains(version)) {
						if (buffTrim.equals("is") || buffTrim.equals("as") || buffTrim.contains(" is") || buffTrim.contains(" as") || buffTrim.contains(" trigger")) {
							buff = buff + " " + version;
							setVer = true;
						}
					} else {
						setVer = true;
					}
				}
				out.println(buff);
				buff = in.readLine();
			}
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void replace(FolderFile folderFileOld, FolderFile folderFileNew) {
		if (folderFileOld.equals(folderFileNew) || folderFileOld.isUniqueVersion()) {
			return;
		}

		if (questions(folderFileNew, folderFileOld)) {
			if (folderFileOld.existsBackup()) {
				folderFileOld.deleteBackup();
				//throw new RuntimeException("Backup " + folderFileOld.getName() + " is exists, try delete backup!!");
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
		folderFile.restore();
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
		if (fromFile.equals(toFile) || !fromFile.exists() || fromFile.length() == toFile.length()) {
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

	public static void compile(FolderFile folderFile, String schemaName, String baseName, String basePass) {
		String sqlSrypt = null;
		try {
			sqlSrypt = new String(Files.readAllBytes(Paths.get(folderFile.getPath())), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (folderFile.getPath().contains("Views") && sqlSrypt.lastIndexOf("/") == sqlSrypt.length() - 1) {
			sqlSrypt = sqlSrypt.substring(0, sqlSrypt.length() - 2);
		}

		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		try (Connection connection = DriverManager.getConnection("jdbc:oracle:oci:" + schemaName + "/" + basePass + "@" + baseName)) {
			try (Statement statement = connection.createStatement()) {
				statement.execute(sqlSrypt);
				logger.info(folderFile + " compile " + schemaName + "@" + baseName);
			} catch (SQLException ex) {
				logger.error(folderFile + ":" + schemaName + "@" + baseName + ":" + ex.getMessage());
				throw new RuntimeException(ex.getMessage());
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
