package tools;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import ieManager.IeBouncer;
import utilities.ApiManager;
import utilities.Drive;

public class PostprintScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	private static void scan() throws Exception {
		int num = 0;
		String setzeFortNach = "6475969";
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith("."))
				continue;
			if (setzeFortNach != null) {
				if (file.getName().contentEquals(setzeFortNach.concat(".jsonld"))) {
					System.out.println("Mache weiter bei Datei " + file.getName());
					setzeFortNach = null;
				}
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file")
					|| obj.getString("contentType").contentEquals("part")) {
				continue;
			}
			JSONArray jarr = obj.optJSONArray("publicationStatus");
			if (jarr == null)
				continue;
			String test = jarr.getJSONObject(0).getString("prefLabel");
//			String test = jarr.getJSONObject(0).getString("@id");
			if (!test.contains("postprint") && !test.contains("Postprint"))
				continue;
			if (IeBouncer.kinderlos(obj, file.getName()))
				continue;
			if (enthaeltNutzungsvereinbarung(obj))
				continue;
			System.out.println((++num) + " " + file.getName());
		}
	}

	private static boolean enthaeltNutzungsvereinbarung(JSONObject obj) throws Exception {
		String id = obj.getString("@id");
		if (obj.has("notification")) {
//			System.err.println("gelöschter Datensatz: " + id);
			return false;
		}
		if (obj.getString("contentType").contentEquals("file")) {
			JSONArray titles = obj.optJSONArray("title");
			if (titles == null) {
				System.err.println("File hat kein title: " + id);
				return false;
			}
			if (obj.getJSONArray("title").length() != 1) {
				throw new Exception(id + " hat ungleich 1 titles " + obj.getJSONArray("title").length());
			}
			if (obj.getJSONArray("title").getString(0).contains("Nutzungsvereinbarung")) {
				return true;
			} else {
				return false;
			}
		} else {
			JSONArray jarr = obj.optJSONArray("hasPart");
			if (jarr == null)
				throw new Exception("Hat kein Part: " + id);
			for (int i = 0; i < jarr.length(); ++i) {
				JSONObject innerObj = jarr.getJSONObject(i);
				String innerId = ladeHasPartReferenzertenDatensatzInDenCache(innerObj, id);
				JSONObject child = ladeDatensatzVonCache(innerId);
				if (enthaeltNutzungsvereinbarung(child))
					return true;
			}
			return false;
		}
	}

	private static JSONObject ladeDatensatzVonCache(String id) throws Exception {
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		return new JSONObject(apiAntwortJson);
	}

	private static String ladeHasPartReferenzertenDatensatzInDenCache(JSONObject innerObj, String id) throws Exception {
		if (!innerObj.has("@id")) {
			throw new Exception("hasPart ohne @id im Datensatz " + id + ".");
		}
		String innerId = innerObj.getString("@id");
		if (!innerId.startsWith("frl:")) {
			throw new Exception("Frl ID in " + id + " beginnt nicht mit 'frl:' " + innerId + ".");
		}
		innerId = innerId.substring(4);
		ApiManager.saveId2File(innerId);
		JSONObject child = new JSONObject(Drive.loadFileToString(new File(Drive.apiAntwort(innerId))));
		Drive.saveStringToFile(child.toString(2), Drive.apiAntwort(innerId));
		return innerId;
	}

	public static void main(String[] args) throws Exception {
		scan();
		System.out.println("PostprintScanner Ende");
	}
}
