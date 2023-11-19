package ru.askurkin.file_scaner.scan;

import java.io.File;

public class FolderFile {
	private String name;
	private String path;
	public final static String backup = "backup";
	private String backupPath;
	private long lastModified;
	private long size;

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public long getLastModified() {
		return lastModified;
	}

	public long getSize() {
		return size - getSchema().length();
	}

	public FolderFile(String path, long lastModified, long size) {
		this.path = path;
		this.name = getFileNameWithDir(path);
		this.backupPath = path.replace(this.name, "") + backup + "\\" + this.name;
		this.lastModified = lastModified;
		this.size = size;
	}

	public String getSchema() {
		String schema = path.replace("\\" + name, "");
		return " " + schema.substring(schema.lastIndexOf("\\", schema.lastIndexOf("\\") - 1) + 1, schema.lastIndexOf("\\")).toLowerCase() + ".";
	}

	private String getFileNameWithDir(String path) {
		return path.substring(path.lastIndexOf("\\", path.lastIndexOf("\\") - 1) + 1);
	}

	public File getFile() {
		return new File(path);
	}

	public File getBackupFile() {
		File mkDirs = new File(backupPath.substring(0, backupPath.lastIndexOf("\\")));
		mkDirs.mkdirs();

		return new File(backupPath);
	}

	@Override
	public String toString() {
		return name + "\n{lastModified=" + lastModified + ", size=" + size + ", path=" + path + ", backupPath=" + backupPath + " '}";
	}
}
