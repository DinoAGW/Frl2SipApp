package werkstatt;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import utilities.Drive;

public class AendereApiAntwort {

	public static void main(String[] args) throws IOException {
		File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
		for(File file: apiAntwortOrdner.listFiles()) {
			String content = Drive.loadFileToString(file);
			if (content.startsWith("{")) {
				continue;
			}
			try {
				JSONObject obj = new JSONObject(content.substring(1, content.length() - 1));
				content = obj.toString(2);
			} catch (JSONException e) {
				System.out.println("JSON Fehler bei '" + content + "'");
				throw e;
			}
			Drive.saveStringToFile(content, file.getPath());
		}
		System.out.println("AendereApiAntwort fertig");
	}

}
