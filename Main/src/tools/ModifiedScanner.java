package tools;
import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class ModifiedScanner {
	private static final String fs = System.getProperty("file.separator");
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	public static void scan() throws Exception {
		int min = Integer.MAX_VALUE;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject mainObj = new JSONObject(apiAntwortJson);
			if (mainObj.getString("contentType").contentEquals("file") || mainObj.getString("contentType").contentEquals("part")) {
				continue;
			}
			JSONObject tempObj = mainObj.optJSONObject("isDescribedBy");
			if (tempObj == null) {
				throw new Exception("kein isDescribedBy vorhanden");
			}
			String tempStr = tempObj.optString("modified");
			if (tempStr == null) {
				throw new Exception("kein modified vorhanden");
			}
			int jahr = Integer.parseInt(tempStr.substring(0, 4));
			int monat = Integer.parseInt(tempStr.substring(5, 7));
			int tag = Integer.parseInt(tempStr.substring(8, 10));
			int alsEineZahl = jahr*10000+monat*100+tag;
			if (alsEineZahl<min) {
				min = alsEineZahl;
			}
		}
		System.out.println("Minimum liegt bei: " + min);
	}

	public static void main(String[] args) throws Exception {
		scan();
		System.out.println("ModifiedScanner Ende");
	}

}
