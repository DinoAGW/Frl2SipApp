package tools;


import java.io.File;
import java.util.LinkedList;

import org.json.JSONObject;

import utilities.Drive;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class MetadataContentScanner_Files {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

	public static LinkedList<String> scanContent(String pfad) throws Exception {
		System.out.println("MetadatenContenScan für '" + pfad + "'");
		LinkedList<String> ret = new LinkedList<String>();
		int max = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			--max;
			if (max == 0) {
				break;
			}
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (!obj.getString("contentType").contentEquals("file")
					|| obj.getString("contentType").contentEquals("part")) {
				continue;
			}
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration()
					.addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
//			System.out.println(json.read(pfad).toString());
			net.minidev.json.JSONArray arr = json.read(pfad);
			for (int i = 0; i < arr.size(); ++i) {
				String str = (String) arr.get(i);
				if (!ret.contains(str)) {
					ret.add(str);
				}
//				System.out.println(arr.get(i));
			}
		}
		for (String str : ret) {
			System.out.println(str);
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		LinkedList<String> ret = scanContent("$.hasData.format");
		System.out.println("MetadataContentScanner Ende...");
	}

}
