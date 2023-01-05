package json;

import java.io.File;

import org.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import utilities.Drive;

public class HbzIdValidator {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	public static void scanContent(String pfad) throws Exception {
		int max_work = 0;
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for(File file: apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			String apiAntwortJson = Drive.loadFileToString(file);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.has("parentPid")) {
				continue;
			}
			DocumentContext json = JsonPath.parse(file, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST));
			net.minidev.json.JSONArray arr = json.read(pfad);
			if (arr.size() == 0) {
				continue;
			}
//			System.out.println(json.read(pfad).toString());
			
			String str = (String) arr.get(0);
			if (!str.startsWith("HT0")) {
				System.err.println("hbzId beginnt nicht mit HT0: '" + str + "' in " + file.getName());
				throw new Exception();
			}
			int num = Integer.parseInt(str.substring(2));
			if (!str.contentEquals("HT0" + num)) {
				System.err.println("hbzId hat nicht die Form HT0<nummer>: '" + str + "' in " + file.getName());
				throw new Exception();
			}
			if (num < min) {
				min = num;
			}
			if (num > max) {
				max = num;
			}

			--max_work;
			if (max_work == 0) {
				break;
			}
		}
		System.out.println("Die Spanne geht von " + min + " bis " + max);
	}

	public static void main(String[] args) throws Exception {
		//scanContent("$.license[*].note");
		scanContent("$.hbzId[*]");
		System.out.println("HbzIdValidator Ende");
	}

}
