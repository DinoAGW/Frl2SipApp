package tools;


import java.io.File;
import java.io.IOException;

import utilities.Drive;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class isDescribedBy_modified_Scanner {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad.concat("_1"));

	public static void scanContent(String pfad) throws IOException {
		System.out.println("MetadatenContenScan für '" + pfad + "'");
		int min=29998989;
		int max = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			--max;
			if (max == 0) {
				break;
			}
//			System.out.println(file.getName());
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration()
					.addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			net.minidev.json.JSONArray arr = json.read(pfad);
			for (int i = 0; i < arr.size(); ++i) {
				String str = (String) arr.get(i);
				int jahr = Integer.parseInt(str.substring(0, 4));
				int monat = Integer.parseInt(str.substring(5, 7));
				int tag = Integer.parseInt(str.substring(8, 10));
				int cmp = 10000*jahr + 100*monat + tag;
				if (cmp < min) {
					min = cmp;
				}
//				System.out.println(cmp);
			}
		}
		System.out.println(min);
	}

	public static void main(String[] args) throws IOException {
		scanContent("$.isDescribedBy.modified");
		System.out.println("MetadataContentScanner Ende...");
	}

}
