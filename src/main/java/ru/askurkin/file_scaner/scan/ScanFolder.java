package ru.askurkin.file_scaner.scan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.askurkin.file_scaner.setting.SysFolders;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScanFolder {
	private String folderName;
	private String path;
	private String baseName;
	private String schemaName;
	private Map<String, FolderFile> folderFiles;
	private static final Logger logger = LogManager.getLogger(ScanFolder.class);

	public Map<String, FolderFile> getFiles() {
		return folderFiles;
	}

	public FolderFile getFolderFile(String fileName) {
		return folderFiles.get(fileName);
	}

	public String getFolderName() {
		return folderName;
	}

	public String getPath(String fileName) {
		return path + "\\" + fileName;
	}

	public String getBaseName() {
		return baseName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public ScanFolder() {
		this.path = "";
		folderFiles = new HashMap<>();
	}

	public ScanFolder(SysFolders sysFolders) {
		this.folderName = sysFolders.getFolderName();
		this.path = sysFolders.getPath();
		this.schemaName = sysFolders.getSchemaName();
		this.baseName = sysFolders.getBaseName();
		folderFiles = new HashMap<>();
	}

	public void addNewestFile(String fileName, FolderFile newestFile) {
		if (newestFile.isUniqueVersion()) {
			return;
		}
		if (!folderFiles.containsKey(fileName)) {
			folderFiles.put(fileName, newestFile);
			return;
		}

		FolderFile folderFile = folderFiles.get(fileName);
		if (folderFile.compare(newestFile) < 0) {
			folderFiles.put(fileName, newestFile);
		}
	}

	public void scanFiles(Set<String> sysFilesSet) {
		scanFiles(path, sysFilesSet);
	}

	public void scanFiles(String pathScan, Set<String> sysFilesSet) {
		File dir = new File(pathScan);
		logger.trace("scan = " + pathScan);
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				FolderFile folderFile = new FolderFile(file.getPath());
				if (sysFilesSet.contains(folderFile.getSysFileName())) {
					folderFiles.put(folderFile.getSysFileName(), folderFile);
					logger.trace(folderFile);
				}
			}
			if (file.isDirectory()) {
				scanFiles(file.getPath(), sysFilesSet);
			}
			logger.trace("file = " + file.getPath());
		}
		logger.trace(pathScan + " found " + folderFiles.size() + " files");
	}
}
