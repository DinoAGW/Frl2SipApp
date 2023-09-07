package idCrawler;

import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ieManager.IeIdentifier;
import sql.MetadatensatzTable;
import utilities.ApiManager;
import utilities.Drive;
import utilities.Url;

public class KonsekutivCrawl {
	private static final int max = 5000;
	
	/*
	 * Schaut, ob zu der Datenmaske irgendwelche Modifizierungen vorliegen.
	 * Gibt zurück ob irgendwas Neues dabei war.
	 */
	private static boolean checkMoreForDate(String dateMask) throws Exception {
		final String url = "https://frl.publisso.de/find?q=isDescribedBy.modified:" + dateMask + "&format=json&from=0&until=" + max + "";
		Thread.sleep(1000);
		String apiAntwortJson = Url.getText(url);
		if (apiAntwortJson.length() == 2) {
			System.out.println("Nichts gefunden unter der Maske = '" + dateMask + "'");
			return false;
		}
		boolean ret = false;
		try {
			JSONObject obj = new JSONObject("{\"array\": " + apiAntwortJson + "}");
			JSONArray arr = obj.getJSONArray("array");
			if (arr.length() == max) {
				System.err.println("Suchanfrage hat das limit überschritten für dateMask = '" + dateMask + "'");
				throw new Exception();
			} else {
				System.out.println("Die Suchanfrage zur dateMask = '" + dateMask + "' ergab " + arr.length() + " Ergebnisse");
			}
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject innerObj = arr.getJSONObject(i);
				String innerApiAntwortJson = innerObj.toString(2);
				String id = ApiManager.json2id(innerApiAntwortJson);
				if (!innerObj.getString("contentType").contentEquals("file") && !innerObj.getString("contentType").contentEquals("part")) {
					Drive.saveStringToFile(innerApiAntwortJson, Drive.apiAntwort(id));
				}
				if (MetadatensatzTable.insertIdIntoDatabase(id)) {
					System.out.println("" + i + ") ID = '" + id + "' war noch nicht drin");
					ret = true;
				} else {
//					System.out.println("" + i + ") ID = '" + id + "' war schon drin");
				}
			}
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + apiAntwortJson + "'");
			throw e;
		}
		return ret;
	}
	
	/*
	 * aktualisiert die Metadatensätze bis gestern
	 */
	public static boolean makeUpToDate() throws Exception {
		boolean ret = false;
		String heute = LocalDateTime.now().toString().substring(0, 10);
		
		utilities.PropertiesManager prop = new utilities.PropertiesManager(Drive.propertyDateiPfad);
		String lastMakeUpToDate = prop.readStringFromProperty("lastMakeUpToDate");
		
		if (heute.contentEquals(lastMakeUpToDate)) {
			return ret;
		}
		
		int heuteJahr = Integer.parseInt(heute.substring(0, 4));
		int heuteMonat = Integer.parseInt(heute.substring(5, 7));
		int heuteTag = Integer.parseInt(heute.substring(8));
		
		int jahr = Integer.parseInt(lastMakeUpToDate.substring(0, 4));
		int monat = Integer.parseInt(lastMakeUpToDate.substring(5, 7));
		int tag = Integer.parseInt(lastMakeUpToDate.substring(8));
		
		while (true) {
			String formated = String.format("%04d-%02d-%02d", jahr, monat, tag);
			if (checkMoreForDate(formated.concat("*"))) {
				ret = true;
			}
			prop.saveStringToProperty("lastMakeUpToDate", formated);
			if ((tag==heuteTag) && (monat==heuteMonat) && (jahr==heuteJahr)) {
				break;
			}
			++tag;
			if (tag==32) {
				tag = 1;
				++monat;
			}
			if (monat==13) {
				monat = 1;
				++jahr;
			}
		}
		if (ret) {
			System.out.println("Es wurden neue Metadatensätze gefunden");
			IeIdentifier.scanForIes();
			DeepCrawl.deeperCrawl();
			PrivateLoader.allPrivateMetadataLoader();
		}
		return ret;
	}

	/*
	 * holt sich seit dem letzten Crawl alle neuen Datensätze, dabei kommen aber aus irgendeinen Grund nicht alle Part und nicht alle File Datensätze mit. Darum brauchts danach einen DeepCrawl
	 */
	public static void main(String[] args) throws Exception {
		makeUpToDate();
		System.out.println("Anzahl IDs in der Datenbank = " + MetadatensatzTable.countEntries());
		System.out.println("KonsekutivCrawl Ende");
	}

}
