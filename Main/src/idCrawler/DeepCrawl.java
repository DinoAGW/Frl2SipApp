package idCrawler;

import java.io.File;

import org.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import sql.MetadatensatzTable;
import utilities.ApiManager;
import utilities.Drive;
import utilities.Url;

public class DeepCrawl {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	public static void deepCrawl(String pfad) throws Exception {
		int max = 30;
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file") || obj.getString("contentType").contentEquals("part")) {
				continue;
			}

			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			net.minidev.json.JSONArray arr = json.read(pfad);
			for (int i = 0; i < arr.size(); ++i) {
				String str = (String) arr.get(i);
//				System.out.println(file.getName() + ": " + str);
				String id = str;
				if (id.startsWith("frl:")) {
					id = id.substring(4);
				} else {
					System.err.println("ID beginnt nicht mit 'frl:'. ID = " + id);
					throw new Exception();
				}
				if (MetadatensatzTable.insertIdIntoDatabase(id)) {
					System.out.println(file.getName() + " #" + i + ") ID = '" + id + "' war noch nicht drin");
					String url = "https://frl.publisso.de/resource/".concat(str).concat(".json2");
					Thread.sleep(1000);
					String innerApiAntwortJson = Url.getText(url);
					Drive.saveStringToFile(innerApiAntwortJson, Drive.apiAntwort(id));
				} else {
//					System.out.println("" + i + ") ID = '" + id + "' war schon drin");
				}
			}

			--max;
			if (max == 0) {
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		deepCrawl("$.hasPart[*].@id");
		System.out.println("DeepCrawl Ende");
	}

}
