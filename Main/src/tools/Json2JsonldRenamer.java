package tools;

import java.io.File;

import utilities.Drive;

/*
 * Programm benennt alle .json Dateien des Caches in .jsonld um
 */
public class Json2JsonldRenamer {
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	
	private static void scan() throws Exception {
		int max = 0;
		for (File file : apiAntwortOrdner.listFiles()) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			if (file.getName().endsWith(".json")) {
				File dest = new File(file.getAbsolutePath().concat("ld"));
				System.out.println("Renaming " + file.getName() + " to " + dest.getName() + ".");
				file.renameTo(dest);
				--max;
				if (max == 0) {
					break;
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		scan();
		System.out.println("Json2JsonldRenamer Ende");

	}

}
