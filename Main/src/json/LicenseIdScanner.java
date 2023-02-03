package json;

import java.io.File;

import org.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import utilities.Drive;

public class LicenseIdScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	public static void scanContent(String pfad) throws Exception {
		int max_work = 0;
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
//				System.err.println("versteckte Datei entdeckt");
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file") || obj.getString("contentType").contentEquals("part")) {
//				System.err.println(obj.getString("contentType") + " entdeckt: " + file.getPath());
				continue;
			}
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			net.minidev.json.JSONArray arr = json.read(pfad);

			if (arr.size() == 0) {
				continue;
			}
			
			if (arr.size()>1) {
				System.out.println("https://frl.publisso.de/resource/frl:" + file.getName() + "2 hat " + arr.size() + " Lizenztext-IDs");
				
				--max_work;
				if (max_work == 0) {
					break;
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		scanContent("$.license[*].@id");
		System.out.println("LicenseIdScanner Ende");
	}

}
