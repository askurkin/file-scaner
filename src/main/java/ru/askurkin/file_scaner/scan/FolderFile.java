package ru.askurkin.file_scaner.scan;

import java.io.File;
import java.sql.Timestamp;
import java.util.Objects;

public class FolderFile extends File {
	private String name;
	public final static String backup = "backup";
	private String backupPath;

	public String getName() {
		return name;
	}

	public long getSize() {
		return super.length() - getSchema().length();
	}

	public String getBackupPath() {
		return backupPath;
	}

	public FolderFile(String path) {
		super(path);
		this.name = getFileNameWithDir(path);
		this.backupPath = path.replace(this.name, "") + backup + "\\" + this.name;
	}

	public String getSchema() {
		String schema = super.getPath().replace("\\" + name, "");
		return " " + schema.substring(schema.lastIndexOf("\\", schema.lastIndexOf("\\") - 1) + 1, schema.lastIndexOf("\\")).toLowerCase() + ".";
	}

	private String getFileNameWithDir(String path) {
		return path.substring(path.lastIndexOf("\\", path.lastIndexOf("\\") - 1) + 1);
	}

	public boolean existsBackup() {
		return new File(backupPath).exists();
	}

	public boolean restore() {
		super.delete();
		return new File(backupPath).renameTo(this);
	}

	public boolean backup() {
		File mkDirs = new File(backupPath.substring(0, backupPath.lastIndexOf("\\")));
		mkDirs.mkdirs();
		return super.renameTo(new File(backupPath));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		FolderFile that = (FolderFile) o;
		return Objects.equals(super.getPath(), that.getPath()) || getSize() == that.getSize() || this.length() == that.length();
	}

	@Override
	public String toString() {
		return String.format("%1$s; size: %2$d bytes; modified: %3$tY-%3$tm-%3$td %3$tH:%3$tM",
//				name,
				super.getPath(), super.length(), new Timestamp(super.lastModified()));
	}
}
