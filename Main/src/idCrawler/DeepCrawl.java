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
	
	public static boolean deepCrawl() throws Exception {
		System.out.println("deepCrawl-ing...");
		String pfad = "$.hasPart[*].@id";
		int insg = 0;
		int max = 0;
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
			if (!obj.has("contentType")) {
				System.err.println("Datei " + file.getName() + " hat keinen contentType");
				continue;
			}
			if (obj.getString("contentType").contentEquals("file") ) {
				continue;
			}

			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			// holt sich ein Array aller Json Objekte, die zu diesem Pfad passen
			net.minidev.json.JSONArray arr = json.read(pfad);
			for (int i = 0; i < arr.size(); ++i) {
				String str = (String) arr.get(i);//sollten alles Strings sein
//				System.out.println(file.getName() + ": " + str);
				String id = str;
				if (id.startsWith("frl:")) {
					id = id.substring(4);
				} else {
					throw new Exception("ID beginnt nicht mit 'frl:'. ID = " + id);
				}
				//fügt alle IDs, die in irgendwelchen Dateien im Pfad gefunden wurden zur MetadatensatzTabelle hinzu
				if (MetadatensatzTable.insertIdIntoDatabase(id)) {
					++insg;
					System.out.println(insg + ") " + file.getName() + " #" + (i+1) + " ID = '" + id + "' war noch nicht drin");
					String url = "https://frl.publisso.de/resource/".concat(str).concat(".json2");
					String stringApiAntwortJson = Url.getText(url);//versucht sich den Datensatz herunter zu laden
					Thread.sleep(1000);
					JSONObject innerApiAntwortJson = null;
					try {//speichert das aber nur ab, wenn es eine gültige Json ist. z.B. weil Datensatz private ist.
						innerApiAntwortJson = new JSONObject(stringApiAntwortJson);
					} catch (Exception e) {
						System.err.println("Fehler beim Verarbeiten von id '" + str + "'. Antwort wird nicht gespeichert.");
						continue;
					}
					Drive.saveStringToFile(innerApiAntwortJson.toString(2), Drive.apiAntwort(id));
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
	
	public static void deeperCrawl() throws Exception {
		boolean nochmal = true;
		while (nochmal) {
			nochmal = deepCrawl();
		}
		System.out.println("Gefunden = " + insginsg);
		if (insginsg>0) {
			PrivateLoader.allPrivateMetadataLoader();
		}
	}

	
	public static void main(String[] args) throws Exception {
		deeperCrawl();
		System.out.println("DeepCrawl Ende");
	}

}
