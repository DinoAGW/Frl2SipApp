package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Drive {
	public static final String fs = System.getProperty("file.separator");
	public static final String home = System.getProperty("user.home");
	public static final String dbPath = home.concat(fs).concat(".databases").concat(fs).concat("Frl2SipApp");
	public static final String apiAntwortPfad = home.concat(fs).concat("workspace").concat(fs).concat("Frl2SipApp")
			.concat(fs).concat("apiAntworten");
	public static final String propertyDateiPfad = home.concat(fs).concat("FRL_Properties.txt");

	public static String apiAntwort(String id) {
		return apiAntwortPfad.concat(fs).concat(id).concat(".json");
	}

	public static void saveStringToFile(String str, String datei) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(datei)) {
			out.println(str);
		}
	}

	public static String loadFileToString(File file) throws Exception {
		if(!file.exists()) {
			throw new Exception("Datei " + file.getAbsolutePath() + " existiert nicht.");
		}
		Charset encoding = Charset.defaultCharset();
		byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
		return new String(encoded, encoding);
	}

	public static void geheSicherDassOrdnerExistiert(String ziel) {
		File zielFile = new File(ziel);
		if (!zielFile.exists()) {
			zielFile.mkdirs();
		}
	}

	public static void loescheFallsExistiert(String ziel) {
		File zielFile = new File(ziel);
		if (zielFile.exists()) {
			zielFile.delete();
		}
	}
}
