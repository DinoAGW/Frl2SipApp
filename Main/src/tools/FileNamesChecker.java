package tools;

import java.io.File;

import org.json.JSONObject;

import utilities.Drive;

public class FileNamesChecker {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	public static final String fs = System.getProperty("file.separator");

	static void scan() throws Exception {
		for (File file : apiAntwortOrdner.listFiles()) {
			String filename = file.getName();
			if (filename.startsWith(".")) {
				continue;
			}
			try {
				String apiAntwortJson = Drive.loadFileToString(file);
				JSONObject obj = new JSONObject(apiAntwortJson);
				if (obj.has("notification")) {
					continue;
				}
				if (obj.getString("contentType").contentEquals("part")) {
					String title = obj.getJSONArray("title").getString(0);
					if (Drive.checkUnerlaubteZeichen(title)) {
						System.out.println("part(" + filename + "): '" + title + "'");
					}
				} else if (obj.getString("contentType").contentEquals("file")) {
					String fileLabel = obj.getJSONObject("hasData").getString("fileLabel");
					if (Drive.checkUnerlaubteZeichen(fileLabel)) {
						System.out.println("file(" + filename + "): '" + fileLabel + "'");
					}
				}
			} catch (Exception e) {
				System.err.println("Fehler bei Datei " + filename);
//				throw e;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		scan();
	}

}
