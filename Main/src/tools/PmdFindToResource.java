package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.ApiManager;
import utilities.Drive;

public class PmdFindToResource {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	private static int count = 0;

	public static void convertFindToResource() throws Exception {
		boolean gefunden = false;
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
			if (id.contentEquals("frl:6455396"))
			{
				if (!gefunden) {
					System.out.println("Mache weiter mit count = " + count);
				}
				gefunden = true;
			}
			if (!gefunden) {
				++count;
				continue;
			}
			repariere(id.substring(4));
			if (++count % 100 == 0) {
				System.out.println(count + " Dateien verarbeitet... (" + file.getName() + ")");
			}
		}
		System.out.println("Insgesamt " + count + " PMDs");
	}
	
	private static void repariere(String id) throws Exception {
		try {
			ApiManager.saveId2File(id);
		} catch (Exception e) {
			System.err.println(count + " Fehler beim Verarbeiten der id " + id);
			throw e;
		}
		Thread.sleep(1000);
	}

	public static void main(String[] args) throws Exception {
//		repariere("5957918");
		convertFindToResource();
		System.out.println("PmdFindToResource Ende");
	}
}
