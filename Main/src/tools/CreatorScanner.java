package tools;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import utilities.Drive;

public class CreatorScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	private static void scanFor(String zeichen) throws Exception {
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("part")
					|| obj.getString("contentType").contentEquals("file")) {
				continue;
			}

			String tempStr;
			JSONObject tempObj;
			JSONArray tempJarr;

			tempJarr = obj.optJSONArray("subject");
			if (tempJarr == null) {
				continue;
			}
			for (int i = 0; i < tempJarr.length(); ++i) {
				tempObj = tempJarr.getJSONObject(i);
				tempStr = tempObj.optString("prefLabel");
				if (tempStr.contains(zeichen)) {
					System.out.println(file.getName() + " -> " + tempStr);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		scanFor("�");
	}
}
