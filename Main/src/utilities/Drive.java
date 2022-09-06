package utilities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Drive {
	public static final String fs = System.getProperty("file.separator");
	public static final String home = System.getProperty("user.home");
	public static final String dbPath = home.concat(fs).concat(".databases").concat(fs).concat("Frl2SipApp");
	public static final String apiAntwortPfad = home.concat(fs).concat("workspace").concat(fs).concat("Frl2SipApp").concat(fs).concat("apiAntworten");
	
	public static String apiAntwort(String id) {
		return apiAntwortPfad.concat(fs).concat(id).concat(".json");
	}
	
	public static void saveStringToFile(String str, String datei) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(datei)) {
		    out.println(str);
		}
	}
}
