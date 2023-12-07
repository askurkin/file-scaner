package ru.askurkin.file_scaner.setting;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Setting {

	private List<SysFolders> sysFolders;
	private Set<String> files;
	private String filterDir;
	private Gson gson;
	private String basePass;

	private static final Logger logger = LogManager.getLogger(Setting.class);

	public List<SysFolders> getSysFolders() {
		return sysFolders;
	}

	public Set<String> getSysFilesSet() {
		return files;
	}

	public void setFilterDir(String filterDir) {
		this.filterDir = filterDir;
	}

	public String getFilterDir() {
		return filterDir;
	}

	public String getBasePass() {
		return basePass;
	}

	public void setBasePass(String basePass) {
		this.basePass = basePass;
	}

	public Setting() {
		sysFolders = new ArrayList<>();
		files = new HashSet<>();
		gson = new Gson();
	}

	public boolean checkFilterDir(String dirName) {
		if (filterDir.isEmpty()) {
			return true;
		}
		if (filterDir.equals(dirName)) {
			return true;
		}
		return false;
	}

	private String loadJson(String fileName) {
		StringBuilder json = new StringBuilder();

		try (InputStreamReader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName)))) {
			int ch = in.read();
			while (ch != -1) {
				json.append((char) ch);
				ch = in.read();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File " + fileName + " not found.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		logger.trace(json);
		return json.toString();
	}


	public void loadFolders(String fileName) {
		String json = loadJson(fileName);

		sysFolders = gson.fromJson(json, new TypeToken<ArrayList<SysFolders>>() {
		}.getType());

		logger.debug(sysFolders);
	}

	public void loadFiles(String fileName) {
		String json = loadJson(fileName);

		List<SysFiles> sysFiles = gson.fromJson(json, new TypeToken<ArrayList<SysFiles>>() {
		}.getType());

		for (SysFiles sysFile : sysFiles) {
			files.addAll(sysFile.getList());
		}

		logger.debug(sysFiles);
	}
}
