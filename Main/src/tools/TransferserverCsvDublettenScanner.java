package tools;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utilities.Drive;
import utilities.Transferserver;

public class TransferserverCsvDublettenScanner {
	public static final String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws Exception {
		loescheDubletten();
		System.out.println("DublettenScanner Ende");
	}

	private static void loescheDubletten() throws Exception {
		Set<String> ids = extractIds();
		Transferserver ts = new Transferserver();
		try {
			String tsPfad = "/exchange/lza/lza-zbmed/test/frl/";
			List<String> remoteFiles = ts.ls(tsPfad);
			for (String remoteFile : remoteFiles) {
				if (!remoteFile.startsWith("PROD_2025")) {
					continue;
				}
				String id = remoteFile.substring(16, remoteFile.length() - 1);
				if (ids.contains(id)) {
					System.out.println(id + " ist zu löschen...");
					ts.removeFolder(tsPfad + remoteFile);
				} else {
					System.out.println(id + " ist nicht in den LogFiles");
				}
			}
		} finally {
			ts.disconnect();
		}
	}

	private static Set<String> extractIds() throws Exception {
		Set<String> ids = new HashSet<>();
		List<String> lines = readLines(new File(Drive.workspace + fs + "2025_09_03 KonsolOutput.txt"));
		for (String line : lines) {
			if (line.startsWith("Verarbeite id ") && line.endsWith(" ...")) {
				ids.add(line.substring(14, line.length() - 4));
			}
		}
		lines = readLines(new File(Drive.workspace + fs + "2025_09_04 KonsolOutput.txt"));
		for (String line : lines) {
			if (line.startsWith("Verarbeite id ") && line.endsWith(" ...")) {
				ids.add(line.substring(14, line.length() - 4));
			}
		}
		return ids;
	}

	static List<String> readLines(File csvFile) throws Exception {
		if (!csvFile.exists()) {
			throw new Exception("CSV-Datei nicht gefunden");
		}
		List<String> lines = Files.readAllLines(csvFile.toPath());
		return lines;
	}
}
