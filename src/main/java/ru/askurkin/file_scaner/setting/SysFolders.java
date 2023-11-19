package ru.askurkin.file_scaner.setting;

public class SysFolders {
	private String name;
	private String path;

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return name + "{path='" + path + "}";
	}
}
