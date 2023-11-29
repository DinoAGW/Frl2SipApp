package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.ApiManager;
import utilities.Drive;
import utilities.Url;

public class PmdFindToResource {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	public static void convertFindToResource() throws Exception {
		int count = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file")
					|| obj.getString("contentType").contentEquals("part")) {
				continue;
			}
//			System.out.println("Bearbeite Datei: " + file.getName());
			String id = obj.optString("@id");
			if (!id.startsWith("frl:")) {
				throw new Exception("@id beginnt nicht mit 'frl:': '" + id + "'");
			}
			ApiManager.saveId2File(id.substring(4));
			Thread.sleep(1000);
			if (++count % 100 == 0) {
				System.out.println(count + " Dateien verarbeitet... (" + file.getName() + ")");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		convertFindToResource();
		System.out.println("PmdFindToResource Ende");
	}
}
