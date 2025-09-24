package tools;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import sip.SipPacker;
import utilities.ApiManager;

public class MissingMD5s {

	private static final String fs = System.getProperty("file.separator");

	static List<String> readIDs(File csvFile) throws Exception {
		if (!csvFile.exists()) {
			throw new Exception("CSV-Datei nicht gefunden");
		}
		List<String> lines = Files.readAllLines(csvFile.toPath());
		return lines;
	}

	static void fix1() throws Exception {
		List<String> lines = readIDs(new File("bin" + fs + "MissingMD5s.txt"));
//		List<String> all = Files.readAllLines(new File("bin" + fs + "Report1.txt").toPath());
//		for (String line: lines) {
//			System.out.println(line);
//			if (!all.remove(line)) {
//				throw new Exception(line + " konnte nicht entfernt werden");
//			}
//		}
//		FileWriter fr = new FileWriter(new File("bin" + fs + "Report2.txt"), true);
//		for (String line: all) {
//			fr.append(line + "\n");
//		}
//		fr.close();
	}

	static void fix2() throws Exception {
		List<String> lines = readIDs(new File("bin" + fs + "MissingMD5s.txt"));
		List<String> all = Files.readAllLines(new File("bin" + fs + "Report1.txt").toPath());
		int anz = 0;
		for (String line : lines) {
			String pmd = ApiManager.getPmdOfDatensatz(line);
			ApiManager.saveId2FileRecursively(pmd, 0);
			if (!all.contains(pmd))
				continue;
			System.out.println((++anz) + ") " + line + " -> " + pmd);
			SipPacker.generateOneSip(pmd);
		}
	}

	public static void main(String[] args) throws Exception {
//		ApiManager.saveId2FileRecursively("6484054");
		fix2();
		System.out.println("fertig");
	}

}
