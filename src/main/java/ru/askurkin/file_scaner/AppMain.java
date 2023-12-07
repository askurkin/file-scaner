package ru.askurkin.file_scaner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.askurkin.file_scaner.scan.Scans;
import ru.askurkin.file_scaner.setting.Setting;

public class AppMain {
	private static final String APP_NAME = "file-scaner";
	private static final String APP_INFO = "Синхронизация файлов по разным папкам\n" +
			"\n>" + APP_NAME + " " + cmd2str(CMD_PARAMS.SYNC) + " [params] - запуск синхронизации" +
			"\n>" + APP_NAME + " " + cmd2str(CMD_PARAMS.CP) + "=<Folder> [params] - копирование всех items в указанную папку" +
			"\n>" + APP_NAME + " " + cmd2str(CMD_PARAMS.RESTORE) + " [params] - копирование всех items в указанную папку" +
			"\n>" + APP_NAME + " " + cmd2str(CMD_PARAMS.COMPILE) + " [params|"+cmd2str(CMD_PARAMS.PASS)+"] - компилирование объектов в БД" +
			"\n params:" +
			"\n* " + cmd2str(CMD_PARAMS.FOLDERS) + "=" + cmd2str(CMD_PARAMS.FOLDERS) + ".json - настройка по папкам для сканирования" +
			"\n* " + cmd2str(CMD_PARAMS.FILES) + "=" + cmd2str(CMD_PARAMS.FILES) + ".json - настройка для повторяющихся файлов" +
			"\n* " + cmd2str(CMD_PARAMS.DIR) + "=<directory> - обновить только в одной директории" +
			"\n* " + cmd2str(CMD_PARAMS.PASS) + "=<password> - пароль для доступа к бд";
	private static final Logger logger = LogManager.getLogger(AppMain.class);

	public static String cmd2str(CMD_PARAMS param) {
		return param.toString().toLowerCase();
	}

	public static void main(String[] args) {
		Setting setting = new Setting();
		CMD_PARAMS cmd;

		try {
			cmd = CMD_PARAMS.valueOf(args[0].toUpperCase());
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println(APP_INFO);
			return;
		} catch (IllegalArgumentException ex) {
			System.out.println(APP_INFO);
			return;
		}

		setting.loadFolders(getParam(args, CMD_PARAMS.FOLDERS));
		setting.loadFiles(getParam(args, CMD_PARAMS.FILES));
		setting.setFilterDir(getParam(args, CMD_PARAMS.DIR));
		setting.setBasePass(getParam(args, CMD_PARAMS.PASS));

		Scans scans = new Scans(setting);
		scans.scanAllFolders();

		if (cmd == CMD_PARAMS.SYNC) {
			System.out.println("Синхронизация");
			scans.sync();
		}

		if (cmd == CMD_PARAMS.RESTORE) {
			System.out.println("Восстановление");
			scans.restore();
		}

		if (cmd == CMD_PARAMS.CP && !setting.getFilterDir().isEmpty()) {
			System.out.println("Копирование");
			scans.copy(setting.getFilterDir());
		}

		if (cmd == CMD_PARAMS.COMPILE && !setting.getBasePass().isEmpty()) {
			System.out.println("Компиляция всех объектов");
			scans.compile(setting.getBasePass());
		}

	}

	private static String getParam(String[] args, CMD_PARAMS param) {
		String result = new String();
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String[] params = args[i].split("=");
				CMD_PARAMS set = CMD_PARAMS.valueOf(params[0].toUpperCase());
				if (set == param && (set.equals(CMD_PARAMS.SYNC) || set.equals(CMD_PARAMS.CP))) {
					return set.toString();
				} else if (set == param && params.length > 1) {
					result = params[1];
				}
			}
		}

		if (param == CMD_PARAMS.FILES || param == CMD_PARAMS.FOLDERS) {
			if (result.isEmpty()) {
				result = cmd2str(param) + ".json";
			}

			if (!result.endsWith(".json")) {
				throw new RuntimeException("File setting must be *.json");
			}
		}

		logger.info(param + " = " + result);

		return result;
	}
}
