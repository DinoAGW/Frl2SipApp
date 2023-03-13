package idCrawler;

import java.io.File;

import org.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import sql.MetadatensatzTable;
import utilities.Drive;
import utilities.Url;

public class DeepCrawl {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	static int insginsg = 0;
	
	public static boolean deepCrawl(String pfad) throws Exception {
		int insg = 0;
		int max = 80;
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = null;
			try {
				obj = new JSONObject(apiAntwortJson);
			} catch (Exception e) {
				System.err.println("Fehler bei Datei " + file.getName());
				throw e;
			}
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
					++insg;
					System.out.println(insg + ") " + file.getName() + " #" + (i+1) + " ID = '" + id + "' war noch nicht drin");
					String url = "https://frl.publisso.de/resource/".concat(str).concat(".json2");
					String stringApiAntwortJson = Url.getText(url);
					JSONObject innerApiAntwortJson = new JSONObject(stringApiAntwortJson);
					Drive.saveStringToFile(innerApiAntwortJson.toString(2), Drive.apiAntwort(id));
					Thread.sleep(1000);
				} else {
//					System.out.println("" + i + ") ID = '" + id + "' war schon drin");
				}
			}

			--max;
			if (max == 0) {
				break;
			}
		}
		insginsg += insg;
		return (insg > 0);
	}
	
	public static void deeperCrawl(String pfad) throws Exception {
		boolean nochmal = true;
		while (nochmal) {
			nochmal = deepCrawl(pfad);
		}
	}

	public static void main(String[] args) throws Exception {
		deeperCrawl("$.hasPart[*].@id");
		System.out.println("Gefunden = " + insginsg);
		System.out.println("DeepCrawl Ende");
	}

}
