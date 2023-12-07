package ru.askurkin.file_scaner.setting;

public class SysFolders {
	private String folderName;
	private String path;
	private String baseName;
	private String schemaName;

	public String getFolderName() {
			return folderName;
	}

	public String getPath() {
		return path;
	}

	public String getBaseName() {
		return baseName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public SysFolders(String name, String baseName, String schemaName, String path) {
		this.folderName = name;
		this.baseName = baseName;
		this.schemaName = schemaName;
		this.path = path;
	}

	@Override
	public String toString() {
		return folderName + "{path='" + path + "}";
	}
}
