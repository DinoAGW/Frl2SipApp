package tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.HashSet;

import utilities.Drive;

public class IdListenVergleich {
	static final String fs = System.getProperty("file.separator");
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	static File frlIDs = new File(Drive.home.concat(fs).concat("workspace").concat(fs).concat("Frl2SipApp")
			.concat(fs).concat("2023-02-22_frlId_oai_harvested_parents.txt"));
	static File frlIDs_deleted = new File(Drive.home.concat(fs).concat("workspace").concat(fs).concat("Frl2SipApp")
			.concat(fs).concat("2023-02-22_frlId_oai_harvested_deleted_parents.txt"));
	
	static void unterIDsAberNichtInDerIdListe() throws Exception {
		int nummer = 0;
		HashSet<String> ids_in_list = new HashSet<>();
		
		BufferedReader br = new BufferedReader(new FileReader(frlIDs));
		String line;
		while ((line = br.readLine())!=null) {
			ids_in_list.add(line.substring(4));
		}
		br.close();
		
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM metadatensatzTable WHERE isRoot=True");
		while (res.next()) {
			String id = res.getString("id");
			if (!ids_in_list.contains(id)) {
				++nummer;
				System.out.println(nummer + ") ID " + id + " fehlt");
			}
		}
	}
	
	static void inDerIdListeAberNichtUnterIDs() throws Exception {
		int nummer = 0;
		HashSet<String> ids_in_list = new HashSet<>();
		
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM metadatensatzTable WHERE isRoot=True");
		while (res.next()) {
			String id = res.getString("id");
			ids_in_list.add(id);
		}

		HashSet<String> fehlend = new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader(frlIDs));
		String line;
		while ((line = br.readLine())!=null) {
			if (!ids_in_list.contains(line.substring(4))) {
				++nummer;
				System.out.println(nummer + ") ID " + line.substring(4) + " fehlt");
				fehlend.add(line.substring(4));
			}
		}
		br.close();
		
		HashSet<String> deleted = new HashSet<>();
		br = new BufferedReader(new FileReader(frlIDs_deleted));
		while ((line = br.readLine())!=null) {
			deleted.add(line.substring(4));
		}
		
		for (String id : deleted) {
			if (!fehlend.contains(id)) {
				System.out.println("Unter den Gelöschten, aber nicht unter den Fehlenden: " + id);
			}
		}
		
		for (String id : fehlend) {
			if (!deleted.contains(id)) {
				System.out.println("Unter den Fehlenden, aber nicht unter den Gelöschten: " + id);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		inDerIdListeAberNichtUnterIDs();
		System.out.println("IdListenVergleich Ende");
	}

}
