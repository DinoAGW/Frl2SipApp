package utilities;

import java.io.File;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import idCrawler.PrivateLoader;

public class ApiManager {
	public static final String fs = System.getProperty("file.separator");
	
	public static String json2id(String apiAntwortJson) throws Exception {
		String id = null;
		try {
			JSONObject obj = new JSONObject(apiAntwortJson);
			id = obj.getString("@id");
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + apiAntwortJson + "'");
			throw e;
		}
		if (id.startsWith("frl:")) {
			id = id.substring(4);
		} else {
			System.err.println("ID beginnt nicht mit 'frl:'. ID = " + id);
			throw new Exception();
		}
		return id;
	}
	
	/*
	 * ruft den Datensatz zu einer bestimmten ID über resource ab
	 * (das nötigenfalls auch mit Zugangsdaten ApiAccount)
	 * und speichert diese im Cache
	 */
	public static void saveId2File(String id) throws Exception {
		if (id.startsWith("frl:")) {
			throw new Exception("ID wird ohne Prefix erwartet: " + id);
		}
		String url = "https://frl.publisso.de/resource/frl:".concat(id).concat(".json2");
		String stringApiAntwortJson = null;
		try {
			stringApiAntwortJson = Url.getText(url);
		} catch (Exception e) {
			System.err.println("Fehler beim Laden der URL '" + url + "'");
			throw e;
		}
		JSONObject innerApiAntwortJson;
		try {
			innerApiAntwortJson = new JSONObject(stringApiAntwortJson);
		} catch (Exception e) {
			System.err.println("Fehler beim Verarbeiten des Datensatzes '" + id
					+ "'. Versuche API Account");
			PrivateLoader.privateMetadataLoader(id);
			innerApiAntwortJson = new JSONObject(Drive.loadFileToString(new File(Drive.apiAntwort(id))));
		}
		Drive.saveStringToFile(innerApiAntwortJson.toString(2), Drive.apiAntwort(id));
	}
	
	public static void saveDataOfId2File(String id, String file) throws Exception {
		PropertiesManager prop = new PropertiesManager(Drive.propertyDateiPfad);
		String user = prop.readStringFromProperty("user");
		String passwort = prop.readStringFromProperty("passwort");
		
		String command = "curl -u " + user + ":" + passwort + " --ssl-no-revoke https://frl.publisso.de/resource/frl:" + id + "/data --output " + file;
//		System.out.println("curl command = " + command);
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
		Process process = processBuilder.start();
		int size = 0;
		byte[] buffer = new byte[1024];
		InputStream inputStream;
		
//		System.out.println("Process ErrorStream:");
		inputStream = process.getErrorStream();
		while((size = inputStream.read(buffer)) != -1) {
//			System.out.write(buffer, 0, size);
		}
//		System.out.println("Process OutputStream:");
		inputStream = process.getInputStream();
		while((size = inputStream.read(buffer)) != -1) {
//			System.out.write(buffer, 0, size);
		}
		
		process.waitFor();
		int exitCode;
		if ((exitCode = process.exitValue())!=0) {
			throw new Exception("CURL endete mit exitCode = " + exitCode + " bei id = " + id + " und file = " + file);
		}
	}
	
	public static String getPmdOfDatensatz(String id) throws Exception {
		String apiAntwortJson = utilities.Drive.loadFileToString(new File(Drive.apiAntwort(id)));
		JSONObject obj = new JSONObject(apiAntwortJson);
		if(!obj.has("parentPid")) {
			return id;
		} else {
			String parent = obj.getString("parentPid");
			if (!parent.startsWith("frl:")) {
				throw new Exception("Das sollte nicht sein");
			}
			return getPmdOfDatensatz(parent.substring(4));
		}
	}
	
	public static void saveId2FileRecursively(String id) throws Exception {
		saveId2File(id);
		String apiAntwortJson = utilities.Drive.loadFileToString(new File(Drive.apiAntwort(id)));
		JSONObject obj = new JSONObject(apiAntwortJson);
		if (obj.has("notification")) {
			if (!obj.getString("notification").contentEquals("Dieses Objekt wurde gelöscht")) {
				throw new Exception(
						"Ungewöhnliche Notification : " + obj.getString("notification") + " bei id " + id + ".");
			}
		} else {
			JSONArray jarr = obj.optJSONArray("hasPart");
			if (jarr != null) {
				for (int i = 0; i < jarr.length(); ++i) {
					JSONObject hasPart = jarr.optJSONObject(i);
					if (hasPart == null) {
						throw new Exception("Datensatz " + id + " hat einen ungültigen hasPart Nummer " + i);
					}
					String hasPartId = hasPart.optString("@id");
					if (!hasPartId.startsWith("frl:")) {
						throw new Exception("@id beginnt nicht mit 'frl:': '" + hasPartId + "'");
					}
					hasPartId = hasPartId.substring(4);
					saveId2FileRecursively(hasPartId);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		saveId2File("4369687");
//		saveId2File("6472794");
//		saveId2File("6453491");
//		saveId2File("6453498");
//		saveId2File("6453493");
//		saveId2File("6472795");
//		saveId2File("6453492");
//		saveId2File("6453495");
//		saveId2File("6472793");
//		saveId2File("6472801");
//		saveId2File("6472798");
//		saveId2File("6453494");
//		saveId2File("6472792");
//		saveId2File("6472797");
//		saveId2File("6453499");
//		saveId2File("6472800");
//		saveId2File("6484054");
//		saveId2File("6483996");
//		saveId2File("6483864");
//		saveId2File("6488518");
//		saveId2File("6488501");
//		saveId2File("6488513");
//		saveId2File("6488498");
//		saveId2File("6488520");
//		saveId2File("6488506");
//		saveId2File("6488510");
//		saveId2File("6488503");
//		saveId2File("6488515");
//		saveId2File("6488499");
//		saveId2File("6488519");
//		saveId2File("6488507");
//		saveId2File("6488502");
//		saveId2File("6488505");
//		saveId2File("6488517");
//		saveId2File("6488512");
//		saveId2File("6488500");
//		saveId2File("6488516");
//		saveId2File("6488511");
//		saveId2File("6488509");
//		saveId2File("6472545");
//		saveId2File("6472545");
//		saveDataOfId2File("6408009", "bin" + fs + "HK_image.jpg");
//		saveId2File("6488405");
		System.out.println("ApiManager Ende");
	}

}
