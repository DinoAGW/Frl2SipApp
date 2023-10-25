package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class PrivateScanner {
	private static final String fs = System.getProperty("file.separator");
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	public static void scan() throws Exception {
		int max = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (!obj.getString("accessScheme").contentEquals("public")) {
				System.out.println(
						obj.getString("accessScheme") + " " + obj.getString("contentType") + ": " + file.getName());
				--max;
				if (max == 0) {
					break;
				}
			}
		}
	}

	public static void scan2() throws Exception {
		int max = 0;
		System.out.println("File-Datensatz,$.title[0],$.hasData.fileLabel,PMD->$.embargoTime[?]->Anzahl");
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (!obj.getString("accessScheme").contentEquals("private")) {
				continue;
			}
			if (!obj.getString("contentType").contentEquals("file")) {
				continue;
			}
			if (obj.has("notification")) {
				continue;
			}
			String line;
			try {
				line = "" + file.getName() + ",\"" + obj.getJSONArray("title").getString(0).replace("\"", "\"\"").replace("\n", "\\n")
						+ "\",\"" + obj.getJSONObject("hasData").getString("fileLabel").replace("\"", "\"\"") + "\",";
			} catch (Exception e) {
				System.err.println("Fehler bei Datei: " + file.getName());
//				throw e;
				continue;
			}
			JSONObject obj2 = obj;
			File file2 = file;
			while (obj2.has("parentPid")) {
				String parent = obj2.getString("parentPid");
				if (!parent.startsWith("frl:")) {
					throw new Exception("parentPid startet nicht mit \"frl:\": " + file2.getName());
				}
				file2 = new File(Drive.apiAntwort(parent.substring(4)));
				obj2 = new JSONObject(Drive.loadFileToString(file2));
			}
			if (obj2.has("embargoTime")) {
				line += obj2.getJSONArray("embargoTime").length();
			} else {
				line += "NA";
			}
			System.out.println(line);
			--max;
			if (max == 0) {
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		scan2();
		System.out.println("PrivateScanner Ende");
	}

}
