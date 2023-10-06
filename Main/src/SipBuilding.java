import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import ieManager.IeBouncer;
import sql.IeTable;
import sql.SqlManager;
import utilities.Drive;

public class SipBuilding {
	private static final String fs = System.getProperty("file.separator");

	public static void bewerteDatenpakete(String report) throws Exception {
		ResultSet res = sql.SqlManager.INSTANCE
				.executeQuery("SELECT * FROM ieTable WHERE status=" + IeTable.status.get("Gefunden") + ";");
		int max = 2; // zum Testen
		File reportFile = new File(report);
		FileWriter fr = new FileWriter(reportFile, true);
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
				System.err.println("PMD = " + id + " ist Kinderlos");
				continue;
			}

			System.out.println("SIP kann gebildet werden: " + id);
			try {
//				SipPacker.generateOneSip(id);
//				SqlManager.INSTANCE.executeUpdate(
//						"UPDATE ieTable SET status=" + IeTable.status.get("Gebuildet") + " WHERE id='" + id + "';");
				fr.append(id + "\n");
			} catch (Exception e) {
				System.err.println("Fehler bei SIP " + id);
				System.err.println(e);
			}

			--max;
			if (max == 0) {
				break;
			}
		}
		fr.close();
	}

	public static void main(String[] args) throws Exception {
//		IeBouncer.rebuildDatabase();
		IeBouncer.clearStatus();
		bewerteDatenpakete("bin" + fs + "Report1.txt");
		System.out.println("SipBuilding Ende");
	}
}
