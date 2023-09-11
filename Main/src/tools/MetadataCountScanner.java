package tools;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import utilities.Drive;

public class MetadataCountScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	public static LinkedList<String> scanCount(String pfad, int count) throws Exception {
		LinkedList<String> ret = new LinkedList<String>();
		int max = 0;
		for(File file: apiAntwortOrdner.listFiles()) {
			--max;
			if (max == 0) {
				break;
			}
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.has("parentPid")) {
				continue;
			}
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
//			System.out.println(json.read(pfad).toString());
			net.minidev.json.JSONArray arr = json.read(pfad);
//			if (arr. .containsKey("parentPid")) {
//				continue;
//			}
			if (arr.size() == count) {
				ret.add(file.getName());
			}
		}
		for (String str: ret) {
			System.out.println(str);
		}
		return ret;
	}
	
	public static LinkedList<String> content(String id, String pfad) throws IOException {
		LinkedList<String> ret = new LinkedList<String>();
		
		File file = new File(id);
		DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
		net.minidev.json.JSONArray arr = json.read(pfad);
		for (int i = 0; i < arr.size(); ++i) {
			String str = (String) arr.get(i);
			if (!ret.contains(str)) {
				ret.add(str);
			}
//			System.out.println(str);
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		LinkedList<String> ret = scanCount("$.bibo:doi[*]", 2);
		System.out.println(ret.toString());
		System.out.println("MetadataCountScanner Ende...");
	}

}
