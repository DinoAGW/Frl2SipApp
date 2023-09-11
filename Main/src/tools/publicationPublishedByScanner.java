package tools;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import utilities.Drive;

public class publicationPublishedByScanner {
	private static final String fs = System.getProperty("file.separator");
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	public static void scan() throws Exception {
		int max = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file") || obj.getString("contentType").contentEquals("part")) {
				continue;
			}
			JSONArray jarr = obj.optJSONArray("publication");
			if (jarr == null) {
				continue;
			}
			obj = jarr.getJSONObject(0);
			jarr = obj.optJSONArray("publishedBy");
			if (jarr == null) {
				continue;
			}
			if (jarr.length()>2) {
				System.out.println("PMD " + file.getName() + " hat " + jarr.length() + " $.publication[]{}.publishedBy[].");
			}
			if (--max == 0) {
				break;
			}
//			if (obj.)
		}
	}
	
	public static void main(String[] args) throws Exception {
		 scan();
		 System.out.println("publicationPublishedByScanner Ende");
	}
}
