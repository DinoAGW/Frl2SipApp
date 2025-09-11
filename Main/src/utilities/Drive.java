package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;

import com.opencsv.CSVReader;

/*
 * diese Klasse vereint möglichst alles, was mit Dateizugriff auf der Festplatte zu tun hat (um eine Übersicht zu haben)
 * und bietet auch ein paar Dateioperationen, die immer wieder genutzt werden
 */
public class Drive {
	public static final String fs = System.getProperty("file.separator");
	public static final String osName = System.getProperty("os.name");
	public static final String home = System.getProperty("user.home");
	public static final String dbPath = home.concat(fs).concat(".databases").concat(fs).concat("Frl2SipApp");
	public static final String workspace = home.concat(fs).concat("workspace");
	public static final String frl2SipAppWorkspace = workspace.concat(fs).concat("Frl2SipApp");
	public static final String apiAntwortPfad = frl2SipAppWorkspace.concat(fs).concat("apiAntworten");
	public static final String crawlPfad = osName.contentEquals("Linux") ? "/app/FrlAnreicherung/"
			: frl2SipAppWorkspace.concat(fs).concat("crawls").concat(fs);
	public static final String propertyDateiPfad = home.concat(fs).concat("FRL_Properties.txt");

	public static String apiAntwort(String id) throws Exception {
		if (id.startsWith("frl:")) {
			throw new Exception("Es wird die ID ohne das Präfix \"frl:\" erwartet");
		}
		return apiAntwortPfad.concat(fs).concat(id).concat(".jsonld");
	}

	public static String crawl(String id) throws Exception {
		if (id.startsWith("frl:")) {
			throw new Exception("Es wird die ID ohne das Präfix \"frl:\" erwartet");
		}
		return crawlPfad.concat(id).concat(fs);
	}

	public static void saveStringToFile(String str, String datei) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(datei)) {
			out.println(str);
		}
	}

	public static String loadFileToString(File file) throws Exception {
		if (!file.exists()) {
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

	public static String[][] readCsvFileMehrspaltig(File file) throws Exception {
		List<String[]> data = new Stack<>();
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				data.add(nextLine);
			}
		}
		String[][] ret = new String[data.size()][];
		for (int i = 0; i < data.size(); ++i) {
			ret[i] = data.get(i);
		}
		return ret;
	}

	public static String[] readCsvFileEinspaltig(File file) throws Exception {
		String[][] data = readCsvFileMehrspaltig(file);
		String[] ret = new String[data.length];
		for (int i = 0; i < data.length; ++i) {
			ret[i] = data[i][0];
		}
		return ret;
	}

	public static boolean checkUnerlaubteZeichen(String dateiname) {
		boolean unerlaubt = false;
		if (dateiname.indexOf('/') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('\\') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf(':') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('*') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('?') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('"') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('<') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('>') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('|') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('[') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf(']') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('%') != -1) {
			unerlaubt = true;
		}
		if (dateiname.indexOf('\n') != -1) {
			unerlaubt = true;
		}
		return unerlaubt;
	}
}
