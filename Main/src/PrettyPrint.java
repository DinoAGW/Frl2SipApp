import java.io.File;

import org.json.JSONObject;

import utilities.ApiManager;
import utilities.Drive;

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
				throw e;
			}
<<<<<<< HEAD
//			if (apiAntwortJson.length() == obj.toString(2).length() + 2) {
				System.out.println(apiAntwortJson.contentEquals(obj.toString(2) + "\r\n"));
				System.out.println((apiAntwortJson.charAt(apiAntwortJson.length()-1)+0) + " " + apiAntwortJson.length());
				System.out.println("[" + apiAntwortJson.substring(apiAntwortJson.length()-3) + "]");
				System.out.println((obj.toString(2).charAt(obj.toString(2).length()-1)+0) + " " + obj.toString(2).length());
				System.out.println("[" + obj.toString(2).substring(apiAntwortJson.length()-3) + "]");
//			}
=======
			String id = ApiManager.json2id(apiAntwortJson);
			if (!file.getName().contentEquals(id + ".json")) {
				System.err.println("Unterschied entdeckt: " + file.getName() + " und " + id + ".json");
				throw new Exception();
			}
			if (!apiAntwortJson.contentEquals(obj.toString(2) + newline)) {
				Drive.saveStringToFile(obj.toString(2), file.getAbsolutePath());
				++insg;
				System.out.println(insg + ") " + file.getName() + " geändert");
			}
>>>>>>> dc1e96edc0e19bc9973a40ef7eab05bed09b4d9b

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
