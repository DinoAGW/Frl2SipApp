import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class PrettyPrint {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	static void makePretty() throws Exception {
		int max = 10;
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
//			if (apiAntwortJson.length() == obj.toString(2).length() + 2) {
				System.out.println(apiAntwortJson.contentEquals(obj.toString(2) + "\r\n"));
				System.out.println((apiAntwortJson.charAt(apiAntwortJson.length()-1)+0) + " " + apiAntwortJson.length());
				System.out.println("[" + apiAntwortJson.substring(apiAntwortJson.length()-3) + "]");
				System.out.println((obj.toString(2).charAt(obj.toString(2).length()-1)+0) + " " + obj.toString(2).length());
				System.out.println("[" + obj.toString(2).substring(apiAntwortJson.length()-3) + "]");
//			}

			--max;
			if (max == 0) {
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		makePretty();
	}
}
