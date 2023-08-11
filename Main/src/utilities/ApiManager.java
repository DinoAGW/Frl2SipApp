package utilities;

import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiManager {
	public static final String fs = System.getProperty("file.separator");
	
	public static String json2id(String apiAntwortJson) throws Exception {
		String id = null;
		try {
			JSONObject obj = new JSONObject(apiAntwortJson);
			// siehe hier:
			// https://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
//			String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//			JSONArray arr = obj.getJSONArray("posts"); // notice that `"posts": [...]`
//			for (int i = 0; i < arr.length(); i++)
//			{
//			    String post_id = arr.getJSONObject(i).getString("post_id");
//			    ......
//			}
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
	
	public static void saveId2File(String id) throws Exception {
		String url = "https://frl.publisso.de/resource/frl:".concat(id).concat(".json2");
		String stringApiAntwortJson = null;
		try {
			stringApiAntwortJson = Url.getText(url);
		} catch (Exception e) {
			System.err.println("Fehler beim Laden der URL '" + url + "'");
			throw new Exception();
		}
		JSONObject innerApiAntwortJson = new JSONObject(stringApiAntwortJson);
		Drive.saveStringToFile(innerApiAntwortJson.toString(2), Drive.apiAntwort(id));
	}
	
	public static void saveDataOfId2File(String id, String file) throws Exception {
		PropertiesManager prop = new PropertiesManager(Drive.propertyDateiPfad);
		String user = prop.readStringFromProperty("user");
		String passwort = prop.readStringFromProperty("passwort");
		
		String command = "curl -u " + user + ":" + passwort + " https://frl.publisso.de/resource/frl:" + id + "/data --output " + file;
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
		Process process = processBuilder.start();
		int size = 0;
		byte[] buffer = new byte[1024];
		InputStream inputStream;
//		System.out.println("Process ErrorStream:");
//		inputStream = process.getErrorStream();
//		System.out.println("Process OutputStream:");
//		inputStream = process.getInputStream();
//		while((size = inputStream.read(buffer)) != -1) {
//			System.out.write(buffer, 0, size);
//		}
		process.waitFor();
		int exitCode;
		if ((exitCode = process.exitValue())!=0) {
			System.err.println("CURL endete mit exitCode = " + exitCode);
		}
	}

	public static void main(String[] args) throws Exception {
//		saveId2File("6407998");
		saveDataOfId2File("6408009", "bin" + fs + "HK_image.jpg");
		System.out.println("ApiManager Ende");
	}

}
