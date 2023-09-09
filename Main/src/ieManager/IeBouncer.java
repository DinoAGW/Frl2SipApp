package ieManager;

import java.io.File;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONObject;

import sql.IeTable;
import sql.SqlManager;
import utilities.Drive;

public class IeBouncer {
	public static void bounce() throws Exception {
		ResultSet res = sql.SqlManager.INSTANCE
				.executeQuery("SELECT * FROM ieTable WHERE status=" + IeTable.status.get("Gefunden"));
		int max = 0; // zum Testen
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
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get("NichtPubPubPmd")
						+ " WHERE id='" + id + "';");
				System.err.println("PMD = " + id + " ist eine NichtPubPubPmd");
				continue;
			}
			tempStr = obj.optString("publishScheme");
			if (tempStr.length() == 0) {
				throw new Exception("publishScheme nicht definiert bei PMD = " + id);
			}
			if (!tempStr.contentEquals("public")) {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get("NichtPubPubPmd")
						+ " WHERE id='" + id + "';");
				System.err.println("PMD = " + id + " ist eine NichtPubPubPmd");
				continue;
			}
			
			// Falls Embargo läuft
			if (obj.has("embargoTime")) {
				tempArr = obj.optJSONArray("embargoTime");
				if (tempArr == null) {
					throw new Exception("embargoTime ist kein Array bei PMD = " + id);
				}
				if (tempArr.length() != 1) {
					throw new Exception("embargoTime hat eine unerwartete Länge von " + tempArr.length() + " bei PMD = " + id);
				}				
				tempStr = tempArr.optString(0);
				if (tempStr.length() > 0) {
					if (embargoLaeuft(tempStr, id)) {
						SqlManager.INSTANCE.executeUpdate(
								"UPDATE ieTable SET status=" + IeTable.status.get("Embargo") + " WHERE id='" + id + "';");
						System.err.println("PMD = " + id + " ist im Embargo");
						continue;
					}
				}
			}
			
			// Falls Kinderlos
			if (kinderlos(obj, id)) {
				SqlManager.INSTANCE.executeUpdate(
						"UPDATE ieTable SET status=" + IeTable.status.get("Kinderlos") + " WHERE id='" + id + "';");
				System.err.println("PMD = " + id + " ist Kinderlos");
				continue;
			}
			
			// Sonst bereit zum Builden
			SqlManager.INSTANCE.executeUpdate(
					"UPDATE ieTable SET status=" + IeTable.status.get("BereitZumBuilden") + " WHERE id='" + id + "';");

			--max;
			if (max == 0) {
				break;
			}
		}
	}

	private static boolean kinderlos(JSONObject obj, String id) throws Exception {
		JSONArray tempArr;
		String tempStr;

		// falls gelöscht, Ende
		tempStr = obj.optString("notification");
		if (tempStr.contains("gelöscht")) {
			return true;
		}

		// schaue, ob File
		tempStr = obj.optString("contentType");
		if (tempStr.length() == 0) {
			throw new Exception("Ein Datensatz unter PMD = " + id + " hat keinen contentType");
		}
		if (tempStr.contentEquals("file")) {
			return false;
		}

		// ansonsten geht es nach den hasParts
		tempArr = obj.optJSONArray("hasPart");
		if (tempArr == null) {
			return true;
		}
		for (int index = 0; index < tempArr.length(); ++index) {
			JSONObject part = tempArr.getJSONObject(index);
			tempStr = part.optString("@id");
			if (tempStr.startsWith("frl:")) {
				tempStr = tempStr.substring(4);
			} else {
				throw new Exception("$.hasPart[]{}.@id: beginnt nicht mit 'frl:'. @id = " + tempStr);
			}
			String apiAntwortJson = null;
			try {
				apiAntwortJson = utilities.Drive.loadFileToString(new File(Drive.apiAntwort(tempStr)));
			} catch (Exception e) {
				throw new Exception("Datensatz unter PMD = " + id + " fehlt auf der Festplatte: " + tempStr
						+ ". Empfehle PrivateLoader.allPrivateMetadataLoader() auszuführen.");
			}
			JSONObject partObj = new JSONObject(apiAntwortJson);
			if (!kinderlos(partObj, id)) {
				return false;
			}
		}
		return true;
	}

	private static boolean embargoLaeuft(String embargoTime, String id) throws Exception {
		if (embargoTime.length() != 10) {
			throw new Exception(
					"embargoTime = '" + embargoTime + "' hat eine unerwartet Länge von " + embargoTime.length() + " bei PMD = " + id);
		}
		int jahr;
		int monat;
		int tag;
		try {
			jahr = Integer.parseInt(embargoTime.substring(0, 4));
			monat = Integer.parseInt(embargoTime.substring(5, 7));
			tag = Integer.parseInt(embargoTime.substring(8));
		} catch (Exception e) {
			throw new Exception("Konnte embargoTime = " + embargoTime + " nicht parsen bei PMD = " + id);
		}
		if (!embargoTime.contentEquals(String.format("%04d-%02d-%02d", jahr, monat, tag))) {
			throw new Exception("embargoTime = " + embargoTime + " hat nicht das Format YYYY-MM-DD bei PMD = " + id);
		}

		String heute = LocalDateTime.now().toString().substring(0, 10);
		int heuteJahr = Integer.parseInt(heute.substring(0, 4));
		int heuteMonat = Integer.parseInt(heute.substring(5, 7));
		int heuteTag = Integer.parseInt(heute.substring(8));

		if (jahr > heuteJahr) {
			return true;
		} else if (jahr == heuteJahr) {
			if (monat > heuteMonat) {
				return true;
			} else if (monat == heuteMonat) {
				if (tag >= heuteTag) {
					return true;
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		bounce();
		System.out.println("IeBouncer Ende");
	}
}
