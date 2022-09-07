package utilities;

import java.io.File;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import idCrawler.InitialCrawl;

public class ApiManager {
	private static final String fs = System.getProperty("file.separator");

	
	
	public static String json2id(String apiAntwortJson) throws Exception {
		String id = null;
		try {
			JSONObject obj = new JSONObject(apiAntwortJson);
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
			System.out.println("JSON Fehler bei '" + apiAntwortJson + "'");
			throw e;
		}
		if (id.startsWith("frl:")) {
			id = id.substring(4);
		} else {
			System.err.println("ID beginnt nicht mit 'frl:'. ID = " + id);
			throw new Exception();
		}
		return id;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("ApiManager Ende");
	}

}
