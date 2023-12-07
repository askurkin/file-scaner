package ru.askurkin.file_scaner.scan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.askurkin.file_scaner.setting.SysFolders;
import ru.askurkin.file_scaner.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Scans {

	private Map<String, ScanFolder> scanFolders;
	private Setting setting;
	private ScanFolder idealFolder;
	public static ExecutorService serv;
	private static final Logger logger = LogManager.getLogger(Scans.class);

	public Scans(Setting setting) {
		this.setting = setting;
		scanFolders = new ConcurrentHashMap<>();
	}

	public void scanAllFolders() {
		List<Future> futures = new ArrayList<>();
		serv = Executors.newFixedThreadPool(4);
		for (SysFolders folder : setting.getSysFolders()) {
			Future f = serv.submit(() -> {
				ScanFolder scanFolder = new ScanFolder(folder);
				scanFolder.scanFiles(setting.getSysFilesSet());
				scanFolders.put(folder.getFolderName(), scanFolder);
				logger.info(scanFolder.getFolderName() + " scan completed, found " + scanFolder.getFiles().size() + " files");
			});
			futures.add(f);
		}

		for (Future future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
		serv.shutdown();
		logger.info("Scan all folder complited");

		idealFolder = createIdealFolder();
	}

	public ScanFolder createIdealFolder() {
		ScanFolder newest = new ScanFolder();
		scanFolders.forEach((folderName, scanFolder) -> {
			scanFolder.getFiles().forEach(newest::addNewestFile);
		});

		return newest;
	}

	public void sync() {
		scanFolders.forEach((folderName, scanFolder) -> {
			if (setting.checkFilterDir(folderName)) {
				scanFolder.getFiles().forEach((fileName, folderFile) -> Proccess.replace(folderFile, idealFolder.getFolderFile(fileName)));
			}
		});
	}

	public void restore() {
		scanFolders.forEach((folderName, scanFolder) -> {
			if (setting.checkFilterDir(folderName)) {
				scanFolder.getFiles().forEach((fileName, folderFile) -> Proccess.restore(folderFile));
			}
		});
	}

	public void copy(String toDir) {
		ScanFolder toFolder = scanFolders.get(toDir);
		if (toFolder != null) {
			idealFolder.getFiles().forEach((fileName, idealFile) -> {
				Proccess.copy(idealFile, toFolder.getPath(fileName));
			});
		}
	}

	public void compile(String basePass) {
		scanFolders.forEach((folderName, scanFolder) -> {
			if (setting.checkFilterDir(folderName)) {
				String baseName = scanFolder.getBaseName();
				String schemaName = scanFolder.getSchemaName();
				scanFolder.getFiles().forEach((fileName, folderFile) -> {
					if (!folderFile.isNoVersions()) {
						Proccess.compile(folderFile, schemaName, baseName, basePass);
					}
				});
			}
		});
	}
}
