import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class NonPmdDeleter {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	public static void deleteAllPartsAndFiles () throws Exception {
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file") || obj.getString("contentType").contentEquals("part")) {
				file.delete();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		deleteAllPartsAndFiles();
		System.out.println("NonPmdDeleter Ende");
	}
}
