

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

import utilities.Drive;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class AutorenvertragScanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	public static void scanFiles() throws IOException {
		int max = 0;
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (!obj.getString("contentType").contentEquals("part")) {
				continue;
			}
			--max;
			if (max == 0) {
				break;
			}
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			net.minidev.json.JSONArray arr = json.read("$.hasData.fileLabel");
			System.out.println(file.getName());
			for (int i = 0; i < arr.size(); ++i) {
				String str = (String) arr.get(i);
//				if (str.contains("vertrag")) {
					System.out.println(file.getName() + ": " + str);
//				} else {
//					System.out.println("nicht: " + file.getName() + ": " + str);
//				}
			}
		}
	}
	
	public static void scanMD() throws IOException {
		int max = 0;
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file") || obj.getString("contentType").contentEquals("part")) {
				continue;
			}
			--max;
			if (max == 0) {
				break;
			}
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			net.minidev.json.JSONArray arr = json.read("$.hasPart[*].prefLabel");
			for (int i = 0; i < arr.size(); ++i) {
				String str = (String) arr.get(i);
				if (str.contains("vertrag")) {
					System.out.println(file.getName() + ": " + str);
				} else {
//					System.out.println("nicht: " + file.getName() + ": " + str);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Scan Files...");
		scanFiles();
		System.out.println("Scan Metadaten...");
		scanMD();
		System.out.println("AutorenvertragScanner Ende...");
	}

}
