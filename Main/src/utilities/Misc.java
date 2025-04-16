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
	
	public static void main(String[] args) throws Exception {
		String datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_0.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
		datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_1.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
		datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_2.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
		datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_3.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
		datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_4.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
		datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_5.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
		datei = "/app/FrlAnreicherung/6425518/content/airskinurogenital/sample_6e.tar.gz";
		System.out.println(md5sumOfFile(datei) + "  " + datei);
	}
}
