package tools;
import java.io.File;

import org.json.JSONObject;

import utilities.ApiManager;
import utilities.Drive;
import utilities.Url;

public class PrettyPrint {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	static String newline = System.getProperty("line.separator");
	
	static void makePretty() throws Exception {
		int insg = 0;
		int max = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = null;
			try {
				obj = new JSONObject(apiAntwortJson);
			} catch (Exception e) {
				System.err.println("Fehler bei Datei " + file.getName());
				ApiManager.saveId2File(file.getName().substring(0, file.getName().indexOf('.')));
				continue;
			}
			if (!obj.has("contentType")) {
				System.err.println("Hat kein ContentType: " + file.getName());
				ApiManager.saveId2File(file.getName().substring(0, file.getName().indexOf('.')));
			}
			String id = ApiManager.json2id(apiAntwortJson);
			if (!file.getName().contentEquals(id + ".jsonld")) {
				System.err.println("Unterschied entdeckt: " + file.getName() + " und " + id + ".jsonld");
				throw new Exception();
			}
			if (!apiAntwortJson.contentEquals(obj.toString(2) + newline)) {
				Drive.saveStringToFile(obj.toString(2), file.getAbsolutePath());
				++insg;
				System.out.println(insg + ") " + file.getName() + " geändert");
			}

			--max;
			if (max == 0) {
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		makePretty();
		System.out.println("PrettyPrint Ende");
	}
}
