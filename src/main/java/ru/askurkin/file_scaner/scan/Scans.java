package ru.askurkin.file_scaner.scan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.askurkin.file_scaner.setting.SysFolders;
import ru.askurkin.file_scaner.setting.Setting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Scans {

	Map<String, ScanFolder> scanFolders;
	Setting setting;
	ScanFolder idealFolder;
	public static ExecutorService serv;
	private static final Logger logger = LogManager.getLogger(Scans.class);

	public Scans(Setting setting) {
		this.setting = setting;
		scanFolders = new HashMap<>();
	}

	public void scanAllFolders() {
		List<Future> futures = new ArrayList<>();
		serv = Executors.newFixedThreadPool(4);
		for (SysFolders folder : setting.getSysFolders()) {
			Future f = serv.submit(() -> {
				ScanFolder scanFolder = new ScanFolder(folder.getName(), folder.getPath());
				scanFolder.scanFiles(setting.getFiles());
				scanFolders.put(folder.getName(), scanFolder);
				logger.info(scanFolder.getName() + " scan completed, found " + scanFolder.getFiles().size() + " files");
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
				Proccess.copyFile(idealFile, new FolderFile(toFolder.getPath(fileName)));
			});
		}
	}
}
