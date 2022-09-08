package idCrawler;

import java.sql.ResultSet;

import org.json.JSONException;
import org.json.JSONObject;

import sql.IdTable;
import sql.SqlManager;
import utilities.ApiManager;
import utilities.Drive;
import utilities.Url;

public class InitialCrawl {
	private static final String fs = System.getProperty("file.separator");
	
	/*
	 * fragt die API nach einem beliebigen Metadatensatz Nummer num, legt die
	 * Antwort ab und merkt sich die ID in der Datenbank. Return: wurde etwas gefunden?
	 */
	private static boolean findAny(int num) throws Exception {
		final String url = "https://frl.publisso.de/find?q=*&format=json&from=".concat(Integer.toString(num))
				.concat("&until=").concat(Integer.toString(num + 1));
		String apiAntwortJson = Url.getText(url);
//		System.out.println(apiAntwortJson);
		if (apiAntwortJson.length() == 2) {
			System.out.println("Nichts gefunden unter der Nummer = " + num);
			return false;
		}
		apiAntwortJson = apiAntwortJson.substring(1, apiAntwortJson.length() - 1);
		String id = ApiManager.json2id(apiAntwortJson);
		
		//System.out.println("" + num + ") ID = '" + id + "'");

		Drive.geheSicherDassOrdnerExistiert(Drive.apiAntwortPfad);
		
		try {
			JSONObject obj = new JSONObject(apiAntwortJson);
			apiAntwortJson = obj.toString(2);
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + apiAntwortJson + "'");
			throw e;
		}
		
		Drive.saveStringToFile(apiAntwortJson, Drive.apiAntwort(id));

		if (!IdTable.insertIdIntoDatabase(id)) {
			System.out.println("" + num + ") ID = '" + id + "' war schon drin");
		}

		return true;
	}

	public static void main(String[] args) throws Exception {
		int anz = 0;
		for ( int i = 38000; i < 40000; ++i) {
			if (findAny(i)) {
				++anz;
			}
		}
		System.out.println("Anzahl = " + anz);
		System.out.println("Anzahl = " + IdTable.countEntries());
		
		System.out.println("InitialCrawl Ende");
	}

}
