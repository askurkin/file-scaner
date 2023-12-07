package ru.askurkin.file_scaner.setting;

import java.util.ArrayList;
import java.util.List;

public class SysFiles {
	private String subFolder;
	private List<String> files;

	public List<String> getList() {
		List<String> list = new ArrayList<>();
		for (String file : files) {
			list.add(subFolder + "\\" + file.toLowerCase());
		}

		return list;
	}

	public SysFiles(String subFolder, List<String> files) {
		this.subFolder = subFolder;
		this.files = files;
	}

	@Override
	public String toString() {
		return subFolder + ":{" + files.toString() + "}";
	}
}
