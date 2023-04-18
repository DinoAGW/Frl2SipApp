import java.io.File;

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
