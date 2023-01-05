package json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

import utilities.Url;

public class MetadataParser {
	
	private static String frlUrl(String id) {
		return "https://frl.publisso.de/resource/frl:".concat(id).concat(".json2");
	}
	
	private static String getJsonString(String url) throws Exception {
		String ret = Url.getText(url);
		try {
			JSONObject obj = new JSONObject(ret);
			ret = obj.toString(2);
		} catch (JSONException e) {
			System.out.println("JSON Fehler bei '" + ret + "'");
			throw e;
		}
		return ret;
	}
	
	private static String frlJsonString(String id) throws Exception {
		return getJsonString(frlUrl(id));
	}
	
	private static JSONObject frlJson(String id) throws Exception {
		String jsonString = frlJsonString(id);
		return new JSONObject(jsonString);
	}
	
	private static ArrayList<Object> getArray(ArrayList<JSONObject> in, String key) {
		ArrayList<Object> ret = new ArrayList<Object>();
		for(JSONObject jobj: in) {
			JSONArray arr = jobj.getJSONArray(key);
			for(Object obj: arr) {
				ret.add(obj);
			}
		}
		return ret;
	}
	
	private static ArrayList<Object> getValue(ArrayList<Object> in, String key) {
		ArrayList<Object> ret = new ArrayList<Object>();
		for (Object obj: in) {
			if (obj instanceof JSONObject) {
				ret.add(((JSONObject) obj).get(key));
			} else {
				System.out.println("Object " + obj + " ist von der Klasse = " + obj.getClass());
			}
		}
		return ret;
	}
	
//	public static ArrayList<Object> parse(String key) {
//		int p = key.indexOf('.');
//		String id = null;
//		if (p == -1) {
//			id = key;
//		} else {
//			id = key.substring(0, p);
//		}
//		ArrayList<Object> ret = frlJson(id);
//		while (p != -1) {
//			key = key.substring(p+1);
//			byte[] byteKey = key.getBytes();
//			if (byteKey[0]=='[') {
//				int anz = 1;
//				int pos = 1;
//				while (anz >= 1) {
//					if (byteKey[pos] == '[') {
//						++anz;
//					} else if (byteKey[pos] == ']') {
//						--anz;
//					}
//					++pos;
//				}
//				for (Object obj: ret) {
//					ArrayList<Object> temp = new ArrayList<Object>();
//					for (((JSONObject)obj). getJSONArray(key))
//				}
//				ret = ((JSONObject[])ret).ge
//			}
//			
//			p = key.indexOf('.');
//			String subkey = null;
//			if (p == -1) {
//				subkey = key;
//			} else {
//				subkey = key.substring(0, p);
//			}
//			if (subkey.startsWith("[")) {
//				
//			}
//		}
//		if (p==-1) {
//			ArrayList<Object> frlJson
//		}
//		String id = key.substring
//	}
	
	private static void printOverview(JSONObject obj) {
		for(String key:obj.keySet()) {
			if (obj.get(key) instanceof String) {
				System.out.println("\"" + key + "\": String");
			} else if(obj.get(key) instanceof JSONObject) {
				System.out.println("\"" + key + "\": {" + obj.getJSONObject(key).keySet() + "}");
			} else if(obj.get(key) instanceof JSONArray) {
				System.out.println("\"" + key + "\": [" + obj.getJSONArray(key).length() + "]");
			} else {
				System.out.println("Key \"" + key + "\" mit Klasse " + obj.query("/"+key).getClass() + ".");
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		printOverview(frlJson("6434833"));
		printOverview(frlJson("6431699"));
//		System.out.println(frlJson("6434833").query("/isDescribedBy/createdBy"));
		System.out.println(JsonPath.parse(frlJsonString("6431699")).read("$.contribution[?(@.role[0].label != 'Autor/in')].agent[0].label", List.class));//?(@.rolle[1].label=='Autor/in')
//		System.out.println(obj.get(0));
	}

}
