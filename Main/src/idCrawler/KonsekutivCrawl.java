package idCrawler;

import java.io.IOException;
import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sql.IdTable;
import utilities.ApiManager;
import utilities.Drive;
import utilities.Url;

public class KonsekutivCrawl {
	
	/*
	 * Schaut, ob zu der Datenmaske irgendwelche Modifizierungen vorliegen.
	 * Gibt zurück ob irgendwas Neues dabei war.
	 */
	private static boolean checkMoreForDate(String dateMask) throws Exception {
		final String url = "https://frl.publisso.de/find?q=modified:" + dateMask + "&format=json&from=0&until=10000";
		String apiAntwortJson = Url.getText(url);
		if (apiAntwortJson.length() == 2) {
			System.out.println("Nichts gefunden unter der Maske = '" + dateMask + "'");
			return false;
		}
		boolean ret = false;
		try {
			JSONObject obj = new JSONObject("{\"array\": " + apiAntwortJson + "}");
			JSONArray arr = obj.getJSONArray("array");
			if (arr.length()>=9999) {
				System.err.println("Suchanfrage hat das limit überschritten für dateMask = '" + dateMask + "'");
				throw new Exception();
			} else {
				System.out.println("Die Suchanfrage zur dateMask = '" + dateMask + "' ergab " + arr.length() + " Ergebnisse");
			}
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject innerObj = arr.getJSONObject(i);
				String innerApiAntwortJson = innerObj.toString(2);
				String id = ApiManager.json2id(innerApiAntwortJson);
				Drive.saveStringToFile(innerApiAntwortJson, Drive.apiAntwort(id));
				if (IdTable.insertIdIntoDatabase(id)) {
					System.out.println("" + i + ") ID = '" + id + "' war noch nicht drin");
					ret = true;
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
		String heute = LocalDateTime.now().toString().substring(0, 10);
		int heuteJahr = Integer.parseInt(heute.substring(0, 4));
		int heuteMonat = Integer.parseInt(heute.substring(5, 7));
		int heuteTag = Integer.parseInt(heute.substring(8));
		
		utilities.PropertiesManager prop = new utilities.PropertiesManager(Drive.propertyDateiPfad);
		int jahr = Integer.parseInt(prop.readStringFromProperty("jahr"));
		int monat = Integer.parseInt(prop.readStringFromProperty("monat"));
		int tag = Integer.parseInt(prop.readStringFromProperty("tag"));
		
		boolean ret = false;
		while (true) {
			++tag;
			if (tag==32) {
				tag = 0;
				++monat;
			}
			if (monat==13) {
				monat = 0;
				++jahr;
			}
			if ((tag==heuteTag) && (monat==heuteMonat) && (jahr==heuteJahr)) {
				break;
			}
			String formated = String.format("%04d-%02d-%02d*", jahr, monat, tag);
			if (checkMoreForDate(formated)) {
				ret = true;
			}
			prop.saveStringToProperty("jahr", Integer.toString(jahr));
			prop.saveStringToProperty("monat", Integer.toString(monat));
			prop.saveStringToProperty("tag", Integer.toString(tag));
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		if (makeUpToDate()) {
			System.out.println("Es wurden neue Metadatensätze gefunden");
		}
		System.out.println("Anzahl IDs in der Datenbank = " + IdTable.countEntries());
		System.out.println("KonsekutivCrawl Ende");
	}

}
