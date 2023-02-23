

import java.io.File;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import sql.MetadataOverviewTable;
import utilities.ApiManager;
import utilities.Drive;

public class MetadataOverviewCreator {
	
	private static void traverseJson(JSONObject obj, String signatur) throws Exception {
		String newSignatur = null;
		for(String key:obj.keySet()) {
			if (obj.get(key) instanceof String) {
				newSignatur = signatur.concat("." + key + ": String");
			} else if(obj.get(key) instanceof JSONObject) {
				newSignatur = signatur.concat("." + key + "{}");
				traverseJson(obj.getJSONObject(key), newSignatur);
			} else if(obj.get(key) instanceof JSONArray) {
				newSignatur = signatur.concat("." + key + "[]");
				JSONArray arr = obj.getJSONArray(key);
				String newnewSignatur = null;
				for (Object innerObj : arr) {
					if (innerObj instanceof String) {
						newnewSignatur = newSignatur.concat(": String");
					} else if (innerObj instanceof JSONObject) {
						newnewSignatur = newSignatur.concat("{}");
						traverseJson((JSONObject)innerObj, newnewSignatur);
					} else {
						System.err.println(innerObj.getClass());
						throw new Exception();
					}
					addMetadata(newnewSignatur);
				}
			} else {
				System.err.println("Key \"" + key + "\" mit Klasse " + obj.query("/"+key).getClass() + ".");
				throw new Exception();
			}
			addMetadata(newSignatur);
		}
	}
	
	private static void addMetadata(String signatur) throws SQLException {
//		System.out.println(signatur);
		MetadataOverviewTable.addMetadataInTable1(signatur);
	}
	
	private static void scan() throws Exception {
		MetadataOverviewTable.leereTabelle1();
		MetadataOverviewTable.leereTabelle2();
		boolean first = true;
		File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
		for(File file: apiAntwortOrdner.listFiles()) {
			if(!file.isFile()) {
				System.out.println("Datei " + file.getAbsolutePath() + file.getName() + " habe ich nicht erwartet");
				continue;
			}
			try {
				String apiAntwortJson = Drive.loadFileToString(file);
				String id = ApiManager.json2id(apiAntwortJson);
				JSONObject obj = new JSONObject(apiAntwortJson);
				if (obj.has("parentPid")) {
					continue;
				}
//				System.out.println("ID = " + id);
				traverseJson(obj, "$");
				MetadataOverviewTable.uebernehme(id, first);
				first = false;
//				break;
			} catch(Exception e) {
				System.err.println("Fehler bei Datei " + file.getName());
				throw e;
			}
		}
	}
	
	private static void scanFortsetzen(String ab) throws Exception {
		boolean gefunden = false;
		File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
		for(File file: apiAntwortOrdner.listFiles()) {
			if(!file.isFile()) {
				System.out.println("Datei " + file.getAbsolutePath() + file.getName() + " habe ich nicht erwartet");
				continue;
			}
			if (!gefunden) {
				if (file.getName().equals(ab)) {
					gefunden = true;
					continue;
				} else {
					continue;
				}
			}
			try {
				String apiAntwortJson = Drive.loadFileToString(file);
				String id = ApiManager.json2id(apiAntwortJson);
				JSONObject obj = new JSONObject(apiAntwortJson);
				if (obj.has("parentPid")) {
					continue;
				}
//				System.out.println("ID = " + id);
				traverseJson(obj, "$");
				MetadataOverviewTable.uebernehme(id, false);
//				break;
			} catch(Exception e) {
				System.err.println("Fehler bei Datei " + file.getName());
				throw e;
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		scanFortsetzen(".directory");
		System.out.println("Los gehts...");
		MetadataOverviewTable.printout();
		System.out.println("MetadataOverviewCreator Ende");
	}

}
