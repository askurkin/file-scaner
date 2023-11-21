package ru.askurkin.file_scaner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.askurkin.file_scaner.scan.Scans;
import ru.askurkin.file_scaner.setting.Setting;

public class AppMain {
	private static final String APP_NAME = "file-scaner";
	private static final String APP_INFO = "Синхронизация файлов по разным папкам\n" +
			"\n>" + APP_NAME + " " + CMD_PARAMS.sync + " [params] - запуск синхронизации" +
			"\n>" + APP_NAME + " " + CMD_PARAMS.cp + "=<Folder> [params] - копирование всех items в указанную папку" +
			"\n>" + APP_NAME + " " + CMD_PARAMS.restore + " [params] - копирование всех items в указанную папку" +
			"\n params:" +
			"\n* " + CMD_PARAMS.folders + "=" + CMD_PARAMS.folders + ".json - настройка по папкам для сканирования" +
			"\n* " + CMD_PARAMS.files + "=" + CMD_PARAMS.files + ".json - настройка для повторяющихся файлов" +
			"\n* " + CMD_PARAMS.dir + "=<directory> - обновить только в одной директории";
	private static final Logger logger = LogManager.getLogger(AppMain.class);

	public static void main(String[] args) {
		Setting setting = new Setting();
		CMD_PARAMS cmd;

		try {
			cmd = CMD_PARAMS.valueOf(args[0]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println(APP_INFO);
			return;
		} catch (IllegalArgumentException ex) {
			System.out.println(APP_INFO);
			return;
		}

		setting.loadFolders(getParam(args, CMD_PARAMS.folders));
		setting.loadFiles(getParam(args, CMD_PARAMS.files));
		setting.setFilterDir(getParam(args, CMD_PARAMS.dir));

		Scans scans = new Scans(setting);
		scans.scanAllFolders();

		if (cmd == CMD_PARAMS.sync) {
			System.out.println("Синхронизация");
			scans.sync();
		}

		if (cmd == CMD_PARAMS.restore) {
			System.out.println("Восстановление");
			scans.restore();
		}

		if (cmd == CMD_PARAMS.cp && !setting.getFilterDir().isEmpty()) {
			System.out.println("Копирование");
			scans.copy(setting.getFilterDir());
		}

	}

	private static String getParam(String[] args, CMD_PARAMS param) {
		String result = new String();
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String[] params = args[i].split("=");
				CMD_PARAMS set = CMD_PARAMS.valueOf(params[0]);
				if (set == param && (set.equals(CMD_PARAMS.sync) || set.equals(CMD_PARAMS.cp))) {
					return set.toString();
				} else if (set == param && params.length > 1) {
					result = params[1];
				}
			}
		}

		if (param == CMD_PARAMS.files || param == CMD_PARAMS.folders) {
			if (result.isEmpty()) {
				result = param.toString() + ".json";
			}

			if (!result.endsWith(".json")) {
				throw new RuntimeException("File setting must be *.json");
			}
		}

		logger.info(param + " = " + result);

		return result;
	}
}
