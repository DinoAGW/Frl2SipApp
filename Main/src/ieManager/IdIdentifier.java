package ieManager;

import java.io.File;
import java.sql.ResultSet;

import org.json.JSONObject;

import sql.IeTable;
import sql.SqlManager;
import utilities.Drive;

public class IdIdentifier {
	public static boolean scanForIes() throws Exception {
		idCrawler.KonsekutivCrawl.makeUpToDate();
		boolean ret = false;
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM metadatensatzTable WHERE isRoot IS NULL");
		while (res.next()) {
			String id = res.getString("id");
			String apiAntwortJson = utilities.Drive.loadFileToString(new File(Drive.apiAntwort(id)));
			JSONObject obj = new JSONObject(apiAntwortJson);
			boolean isRoot = !obj.has("parentPid"); 
			if (isRoot) {
				if (IeTable.insertIdIntoDatabase(id)) {
					ret = true;
				}
			}
			SqlManager.INSTANCE.executeUpdate("UPDATE metadatensatzTable SET isRoot=" + isRoot + " WHERE id='" + id + "';");
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		if (scanForIes()) {
			System.out.println("Es wurden neue IEs identifiziert");
		}
		System.out.println("Anzahl = " + IeTable.countEntries());
		System.out.println("IdIdentifier Ende");
	}

}
