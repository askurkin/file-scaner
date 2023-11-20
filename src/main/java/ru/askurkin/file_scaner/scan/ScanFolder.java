package ru.askurkin.file_scaner.scan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScanFolder {
	private String name;
	private String path;
	private Map<String, FolderFile> folderFiles;
	private static final Logger logger = LogManager.getLogger(ScanFolder.class);

	public Map<String, FolderFile> getFiles() {
		return folderFiles;
	}

	public FolderFile getFolderFile(String fileName) {
		return folderFiles.get(fileName);
	}

	public String getName() {
		return name;
	}

	public String getPath(String fileName) {
		return path + "\\" + fileName;
	}

	public ScanFolder() {
		this.path = "";
		folderFiles = new HashMap<>();
	}

	public ScanFolder(String name, String path) {
		this.name = name;
		this.path = path;
		folderFiles = new HashMap<>();
	}

	public void addNewestFile(String fileName, FolderFile newestFile) {
		if (!folderFiles.containsKey(fileName)) {
			folderFiles.put(fileName, newestFile);
			return;
		}

		FolderFile folderFile = folderFiles.get(fileName);
		if (folderFile.getLastModified() < newestFile.getLastModified() && folderFile.getSize() != newestFile.getSize()) {
			folderFiles.put(fileName, newestFile);
		}
	}

	public void scanFiles(Set<String> paternFiles) {
		scanFiles(path, paternFiles);
	}

	public void scanFiles(String pathScan, Set<String> paternFiles) {
		File dir = new File(pathScan);
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				FolderFile folderFile = new FolderFile(file.getPath(), file.lastModified(), file.length());
				if (paternFiles.contains(folderFile.getName())) {
					folderFiles.put(folderFile.getName(), folderFile);
					logger.trace(folderFile);
				}
			}
			if (file.isDirectory()) {
				scanFiles(file.getPath(), paternFiles);
			}
		}
		logger.debug(pathScan + " found " + folderFiles.size() + " files");
	}
}
