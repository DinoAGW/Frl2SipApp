package idCrawler;

import org.json.JSONException;
import org.json.JSONObject;

import utilities.ApiManager;
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
			// siehe hier:
			// https://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
//			String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//			JSONArray arr = obj.getJSONArray("posts"); // notice that `"posts": [...]`
//			for (int i = 0; i < arr.length(); i++)
//			{
//			    String post_id = arr.getJSONObject(i).getString("post_id");
//			    ......
//			}
//			id = obj.getString("@id");
			System.out.println(obj.getJSONArray("array").length());
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + apiAntwortJson + "'");
			throw e;
		}
//		System.out.println(apiAntwortJson);
		
		return true;
	}

	public static void main(String[] args) throws Exception {
		checkMoreForDate("2022-09-*");
		System.out.println("KonsekutivCrawl Ende");
	}

}
