package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.Drive;
import utilities.Url;

public class PmdFindToResource {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	public static void convertFindToResource() throws Exception {
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
			System.out.println("Bearbeite Datei: " + file.getName());
			String id = obj.optString("@id");
			if (!id.startsWith("frl:")) {
				throw new Exception("@id beginnt nicht mit 'frl:': '" + id + "'");
			}
			String url = "https://frl.publisso.de/resource/".concat(id).concat(".json2");
			String stringApiAntwortJson = Url.getText(url);// versucht sich den Datensatz herunter zu laden
			Thread.sleep(1000);
			try {// speichert das aber nur ab, wenn es eine gültige Json ist. z.B. weil Datensatz
					// private ist.
				obj = new JSONObject(stringApiAntwortJson);
			} catch (Exception e) {
				System.err.println("Fehler beim Verarbeiten von id '" + id + "'. Antwort wird nicht gespeichert.");
				continue;
			}
			Drive.saveStringToFile(obj.toString(2), Drive.apiAntwort(id.substring(4)));
		}
	}

	public static void main(String[] args) throws Exception {
		convertFindToResource();
		System.out.println("PmdFindToResource Ende");
	}
}
