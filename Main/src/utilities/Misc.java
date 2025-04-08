package utilities;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Misc {
	public static String md5sumOfFile(String file) throws Exception {
		try (InputStream is = Files.newInputStream(Paths.get(file))) {
		    return org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
		} catch (Exception e) {
			System.err.println("Fehler beim Lesen der Datei " + file);
			throw e;
		}
	}
}
