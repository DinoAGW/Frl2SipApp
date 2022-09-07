package idCrawler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utilities.ApiManager;
import utilities.Database;
import utilities.Drive;
import utilities.Url;

public class KonsekutivCrawl {
	
	private static boolean checkMoreForDate(String dateMask) throws Exception {
		final String url = "https://frl.publisso.de/find?q=created:" + dateMask + "&format=json&from=0&until=10000";
		String apiAntwortJson = Url.getText(url);
		if (apiAntwortJson.length() == 2) {
			System.out.println("Nichts gefunden unter der Maske = '" + dateMask + "'");
			return false;
		}
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
				if (Database.insertIdIntoDatabase(id)) {
					System.out.println("" + i + ") ID = '" + id + "' war noch nicht drin");
				}
			}
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + apiAntwortJson + "'");
			throw e;
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		checkMoreForDate("2022-09-*");
		System.out.println("KonsekutivCrawl Ende");
	}

}
