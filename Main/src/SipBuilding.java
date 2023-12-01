import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import ieManager.IeBouncer;
import sip.SipPacker;
import sql.IeTable;
import sql.SqlManager;
import utilities.Drive;

public class SipBuilding {
	private static final String fs = System.getProperty("file.separator");
	private static final boolean trockenModus = false;
	private static final boolean zeigeKinderlose = false;
	private static String reason; 

	public static void bewerteDatenpakete(String report) throws Exception {
		if (trockenModus) {
			System.out.println("Trockenmodus...");
		}
		int count = 0;
		ResultSet res = sql.SqlManager.INSTANCE
				.executeQuery("SELECT * FROM ieTable WHERE status=" + IeTable.status.get("Gefunden") + ";");
		int max = 0; // zum Testen
		File reportFile = new File(report);
		@SuppressWarnings("resource")
		FileWriter fr = new FileWriter(reportFile, true);
		File abort = new File("bin" + fs + "abort");
		while (res.next()) {
			String id = res.getString("id");
			String apiAntwortJson = null;
			try {
				apiAntwortJson = utilities.Drive.loadFileToString(new File(Drive.apiAntwort(id)));
			} catch (Exception e) {
				throw new Exception("Datensatz fehlt auf der Festplatte: " + id
						+ ". Empfehle PrivateLoader.allPrivateMetadataLoader() auszuführen.");
			}
			JSONObject obj = new JSONObject(apiAntwortJson);
			JSONArray tempArr;
			String tempStr;
			// schaue, ob aussortiert werden muss

			// Falls PMD nicht Pub;Pub
			tempStr = obj.optString("accessScheme");
			if (tempStr.length() == 0) {
				throw new Exception("accessScheme nicht definiert bei PMD = " + id);
			}
			if (!tempStr.contentEquals("public")) {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status="
						+ IeTable.status.get("NichtArchivierungswürdig") + " WHERE id='" + id + "';");
				System.err.println("PMD = " + id + " ist eine NichtPubPubPmd");
				continue;
			}
			tempStr = obj.optString("publishScheme");
			if (tempStr.length() == 0) {
				throw new Exception("publishScheme nicht definiert bei PMD = " + id);
			}
			if (!tempStr.contentEquals("public")) {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status="
						+ IeTable.status.get("NichtArchivierungswürdig") + " WHERE id='" + id + "';");
				System.err.println("PMD = " + id + " ist eine NichtPubPubPmd");
				continue;
			}

			// Falls Kinderlos
			if (IeBouncer.kinderlos(obj, id)) {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status="
						+ IeTable.status.get("NichtArchivierungswürdig") + " WHERE id='" + id + "';");
				if (zeigeKinderlose) {
					System.err.println("PMD = " + id + " ist Kinderlos");
				}
				continue;
			}
			
			//Falls PolicyPublikationen
			if (policyPublication(obj)) {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status="
						+ IeTable.status.get("PolicyPublikationen") + " WHERE id='" + id + "';");
				System.err.println(Integer.toString(++count) + ") PMD = " + id + " ist eine PolicyPublikation. " + reason);
				continue;
			}

			try {
				if (!trockenModus) {
//					System.out.println("SIP wird gebildet: " + id);
					SipPacker.generateOneSip(id);
					SqlManager.INSTANCE.executeUpdate(
							"UPDATE ieTable SET status=" + IeTable.status.get("Gebuildet") + " WHERE id='" + id + "';");
					fr.append(id + "\n");
					fr.flush();
				}
			} catch (Exception e) {
				System.err.println("Fehler bei SIP frl:" + id);
				System.err.println(e);
			}

			--max;
			if (max == 0) {
				System.out.println("Maximum erreicht. Beende mich.");
				break;
			}
			if (abort.exists()) {
				System.out.println("Abort-Datei entdeckt. Beende mich.");
				abort.delete();
				break;
			}
		}
		fr.close();
	}
	
	private static boolean policyPublication(JSONObject obj) throws Exception {
		//Falls PMD note "zurückgezogen" oder "gesperrt" enthält, ist alles gut
		String tempStr;
		JSONArray arr = obj.optJSONArray("note");
		if (arr != null) {
			for (int i = 0; i < arr.length(); ++i) {
				tempStr = arr.optString(i);
				if (tempStr != null && (tempStr.contains("zurückgezogen") || tempStr.contains("gesperrt"))) {
					return false;
				}
			}
		}
		//Falls es einen File-Datensatz mit accessScheme=private gibt,
		//aber keine Nutzungsvereinbarung, dann böse, sonst alles gut
		return (checkPolicyPublication(obj) == 1);
	}
	
	/*
	 * unterscheidet drei Fälle:
	 * Ausgabe = 0, wenn keine private Datei zu finden ist
	 * Ausgabe = 1, wenn mindestens eine Datei privat aber keine Nutzungsvereinbarung
	 * Ausgabe = 2, wenn mindestens eine Nutzungsvereinbarung dabei ist
	 */
	private static int checkPolicyPublication(JSONObject obj) throws Exception {
		//Nur nicht-gelöschte Publikationen beachten
		if (obj.has("notification")) {
			return 0;
		}
		//Hier geht es um die File-Datensätze
		if (obj.getString("contentType").contentEquals("file")) {
			if (obj.getString("accessScheme").contentEquals("public")) {
				return 0;
			}
			String title = obj.getJSONArray("title").getString(0);
			if (title.contains("Nutzungsvereinbarung")) {
				return 2;
			}
			reason = obj.getString("@id") + ": " + title;
			return 1;
		}
		//Sonst für alle hasPart-Datensätze...
		JSONArray jarr = obj.getJSONArray("hasPart");
		int ret = 0;
		for (int i = 0; i < jarr.length(); ++i) {
			JSONObject innerObj = jarr.getJSONObject(i);
			String innerId = innerObj.getString("@id").substring(4);
			File file = new File(Drive.apiAntwort(innerId));
			String apiAntwortJson = Drive.loadFileToString(file);
			int retHasPart = checkPolicyPublication(new JSONObject(apiAntwortJson)); 
			if (retHasPart > ret) {
				ret = retHasPart;
			}
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
//		IeBouncer.rebuildDatabase();
//		IeBouncer.clearStatus();
		bewerteDatenpakete("bin" + fs + "Report1.txt");
		System.out.println("SipBuilding Ende");
	}
}
