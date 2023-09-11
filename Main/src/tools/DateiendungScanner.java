package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class DateiendungScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	private static void scanFor(String dateiendung) throws Exception {
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (!(obj.getString("contentType").contentEquals("file"))) {
				continue;
			}
			
			String tempStr;
			JSONObject tempObj;
			
			tempObj = obj.optJSONObject("hasData");
			if (tempObj==null) {
				System.err.println("Kein hasData vorhanden bei Datei " + file.getName());
				continue;
			}
			
			tempStr = tempObj.optString("fileLabel");
			if (tempStr.length()==0) {
				System.err.println("hat kein fileLabel");
				continue;
			}
			
			if (tempStr.endsWith(dateiendung)) {
				System.out.println("File zur ID " + file.getName() + " hat die Dateiendung " + dateiendung);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		scanFor(".zip");
		System.out.println("DateiendungScanner Ende");
	}

}
