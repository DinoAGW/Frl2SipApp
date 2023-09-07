package idCrawler;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;

import utilities.Drive;
import utilities.PropertiesManager;

public class PrivateLoader {
	
	public static void privateMetadataLoader(String id) throws Exception {
		System.out.println("Lade Metadaten zur ID " + id + " herunter.");
		PropertiesManager prop = new PropertiesManager(Drive.propertyDateiPfad);
		String user = prop.readStringFromProperty("user");
		String passwort = prop.readStringFromProperty("passwort");
		String command = "curl -u " + user + ":" + passwort + " https://frl.publisso.de/resource/frl:" + id + ".json2 --output " + Drive.apiAntwort(id);
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
		Process process = processBuilder.start();
		int size = 0;
		byte[] buffer = new byte[1024];
		InputStream inputStream;
		
//		System.out.println("Process ErrorStream:");
		inputStream = process.getErrorStream();
		while((size = inputStream.read(buffer)) != -1) {
//			System.out.write(buffer, 0, size);
		}
//		System.out.println("Process OutputStream:");
		inputStream = process.getInputStream();
		while((size = inputStream.read(buffer)) != -1) {
//			System.out.write(buffer, 0, size);
		}
		
		process.waitFor();
		int exitCode;
		if ((exitCode = process.exitValue())!=0) {
			System.err.println("CURL endete mit exitCode = " + exitCode);
		}
	}
	
	public static void allPrivateMetadataLoader() throws Exception {
		System.out.println("allPrivateMetadataLoader wird ausgeführt...");
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM metadatensatzTable");
		while (res.next()) {
			String id = res.getString("id");
			File apiAntwort = new File(Drive.apiAntwort(id));
			if(!apiAntwort.exists()) {
				privateMetadataLoader(id);
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		privateMetadataLoader("6449071");
		allPrivateMetadataLoader();
		System.out.println("PrivateLoader Ende");
	}

}
