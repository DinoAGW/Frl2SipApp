package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class HasDataSizeScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	private static void scanForSize() throws Exception {
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
			if (!obj.has("contentType")) {
				System.err.println("Datei " + file.getName() + " hat keinen contentType");
				continue;
			}
			if (!obj.getString("contentType").contentEquals("file")) {
				continue;
			}
			
			String tempStr;
			JSONObject tempObj;

			tempObj = obj.optJSONObject("hasData");
			if (tempObj == null) {
				System.err.println("Kein hasData vorhanden bei Datei " + file.getName());
				continue;
			}

			tempStr = tempObj.optString("size");
			if (tempStr.length() == 0) {
				System.err.println("hat kein size");
				continue;
			}
			
			System.out.println(file.getName() + ": " + tempStr);
		}
	}

	public static void main(String[] args) throws Exception {
		scanForSize();
		System.out.println("HasDataSizeScanner Ende");
	}
}
