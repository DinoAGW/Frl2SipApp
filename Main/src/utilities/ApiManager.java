package utilities;

import java.io.File;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiManager {
	private static final String fs = System.getProperty("file.separator");

	/*
	 * fragt die API nach einem beliebigen Metadatensatz Nummer num, legt die
	 * Antwort ab und merkt sich die ID in der Datenbank. Return: ist hat alles
	 * geklappt?
	 */
	public static boolean findAny(int num) throws Exception {
		final String url = "https://frl.publisso.de/find?q=*&format=json&from=".concat(Integer.toString(num))
				.concat("&until=").concat(Integer.toString(num + 1));
		String apiAntwortJson = Url.getText(url);
//		System.out.println(apiAntwortJson);
		if (apiAntwortJson.length() == 2) {
			System.out.println("Nichts gefunden unter der Nummer = " + num);
			return false;
		}
		String id = null;
		try {
			JSONObject obj = new JSONObject(apiAntwortJson.substring(1, apiAntwortJson.length() - 1));
			// siehe hier:
			// https://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
//			String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//			JSONArray arr = obj.getJSONArray("posts"); // notice that `"posts": [...]`
//			for (int i = 0; i < arr.length(); i++)
//			{
//			    String post_id = arr.getJSONObject(i).getString("post_id");
//			    ......
//			}
			id = obj.getString("@id");
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + url + "'");
			throw e;
		}
		if (id.startsWith("frl:")) {
			id = id.substring(4);
		} else {
			System.err.println("ID beginnt nicht mit 'frl:'. ID = " + id);
			throw new Exception();
		}
		//System.out.println("" + num + ") ID = '" + id + "'");

		String apiAntwortDatei = Drive.apiAntwort(id);
		File apiAntwortFile = new File(apiAntwortDatei);
		if (apiAntwortFile.exists()) {
			apiAntwortFile.delete();
		}
		Drive.saveStringToFile(apiAntwortJson, apiAntwortDatei);

		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM idTable WHERE id = '" + id + "';");
		if (resultSet.next()) {
			System.out.println("" + num + ") ID = '" + id + "' war schon drin");
			SqlManager.INSTANCE.executeUpdate("UPDATE idTable SET found  = CURRENT_DATE() WHERE id = '" + id + "';");
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO idTable (id, found) VALUES ('" + id + "', CURRENT_DATE());");
		}

		return true;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Anzahl = " + Database.countEntries());
		findAny(1);
		System.out.println("Anzahl = " + Database.countEntries());
		findAny(1000000);
		System.out.println("ApiManager Ende");
	}

}
