package ru.askurkin.file_scaner.scan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Objects;

public class FolderFile extends File {
	private String sysFileName;
	private String backupPath;
	private String version;

	public String getSysFileName() {
		return sysFileName;
	}

	public String getBackupPath() {
		return backupPath;
	}

	public FolderFile(String path) {
		super(path);
		this.sysFileName = String.format("%1$s\\%2$s", new File(path).getParentFile().getName(), new File(path).getName().toLowerCase());
		String subPath = new File(path).getParentFile().getName();
		this.backupPath = path.replace(subPath, "backup\\" + subPath);
		this.version = "";
	}

	public boolean existsBackup() {
		return new File(backupPath).exists();
	}

	public boolean deleteBackup() {
		return new File(backupPath).delete();
	}

	public boolean restore() {
		super.delete();
		return new File(backupPath).renameTo(this);
	}

	public boolean backup() {
		File mkDirs = new File(backupPath).getParentFile();
		mkDirs.mkdirs();
		return super.renameTo(new File(backupPath));
	}

	public String getVersion() {
		if (!version.isEmpty()) {
			return version;
		}
		String textVersion = "version ";
		String textComment = "--";
		try (BufferedReader in = new BufferedReader(new FileReader(super.getPath()))) {
			String buff = in.readLine();
			while (buff != null) {
				if (buff.contains(textComment) && buff.toLowerCase().contains(textVersion) && buff.toLowerCase().indexOf(textVersion) - buff.indexOf(textComment) < 5) {
					version = buff.toLowerCase().substring(buff.toLowerCase().indexOf(textVersion)).replace(textVersion, "").trim();
					if (!version.isEmpty() && version.length() > 4) {
						break;
					} else {
						version = "";
					}
				}
				buff = in.readLine();
			}
		} catch (IOException e) {
		}
		return version;
	}

	public boolean isUniqueVersion() {
		if (version.isEmpty()) {
			getVersion();
		}
		if (version.isEmpty()) {
			return false;
		}
		if (version.contains(" ")) {
			return true;
		}
		return false;
	}

	public String getNewVersion() {
		String ver = getVersion();
		if (ver.isEmpty()) {
			return String.format("-- version %1$tY.%1$tm.%1$td", new Timestamp(super.lastModified()));
		}
		return "-- version " + ver;
	}

	public long compare(FolderFile folderFile) {
		if (this.equals(folderFile)) {
			return 0;
		}
		if (this.getVersion().isEmpty() && !folderFile.getVersion().isEmpty()) {
			return -1; // текущий хуже
		}
		if (!this.getVersion().isEmpty() && folderFile.getVersion().isEmpty()) {
			return 1; // текущий лучше
		}
		if (!this.getVersion().isEmpty() && !folderFile.getVersion().isEmpty()) {
			if (this.getVersion().equals(folderFile.getVersion())) {
				return super.length() - folderFile.length();
			}
			return this.getVersion().compareToIgnoreCase(folderFile.getVersion());
		}
		return super.length() - folderFile.length();
	}

	public boolean isNoVersions() {
		return super.getPath().contains("Sequences") || super.getPath().contains("Indexes") || super.getPath().contains("Tables") || super.getPath().contains("SchedulerJobs") || super.getPath().contains("Types");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FolderFile that = (FolderFile) o;
		if (isNoVersions()) {
			return true;
		}
		return Objects.equals(super.getPath(), that.getPath()) || this.length() == that.length() || this.length() == that.length() + 1 || this.length() + 1 == that.length();
	}

	@Override
	public String toString() {
		return String.format("%1$s; size: %2$d bytes; modified: %3$tY-%3$tm-%3$td %3$tH:%3$tM (%4$s)", super.getPath(), super.length(), new Timestamp(super.lastModified()), this.getVersion());
	}
}
