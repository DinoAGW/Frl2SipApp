package tools;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import utilities.Drive;

public class ArticleScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	public static final String fs = System.getProperty("file.separator");
	
	public static void main(String[] args) throws Exception {
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (!obj.getString("contentType").contentEquals("article")) {
				continue;
			}

			String ausgabe = file.getName().substring(0, file.getName().length()-5);
			
			JSONArray jarr = obj.getJSONArray("containedIn");
			
			for (int i = 0; i < jarr.length(); ++i) {
				JSONObject jobj = jarr.getJSONObject(i);
				ausgabe += ";" + jobj.getString("@id"); 
			}
			
			System.out.println(ausgabe);
		}
	}
}
