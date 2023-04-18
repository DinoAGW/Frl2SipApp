package ieManager;

import java.io.File;
import java.sql.ResultSet;

import org.json.JSONObject;

import sql.IeTable;
import sql.MetadatensatzTable;
import sql.SqlManager;
import utilities.Drive;

public class IeIdentifier {
	public static boolean scanForIes() throws Exception {
		idCrawler.KonsekutivCrawl.makeUpToDate();
		boolean ret = false;
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM metadatensatzTable WHERE isRoot IS NULL");
		int insg = 0;
		while (res.next()) {
			String id = res.getString("id");
			String apiAntwortJson = null;
			try {
				apiAntwortJson = utilities.Drive.loadFileToString(new File(Drive.apiAntwort(id)));
			} catch (Exception e) {
				System.err.println("Datensatz fehlt auf der Festplatte: " + id);
				continue;
			}
			JSONObject obj = new JSONObject(apiAntwortJson);
			boolean isRoot = !(obj.getString("contentType").contentEquals("file")
					|| obj.getString("contentType").contentEquals("part"));
			if (isRoot) {
				if (IeTable.insertIdIntoDatabase(id)) {
					ret = true;
				}
			}
			SqlManager.INSTANCE.executeUpdate("UPDATE metadatensatzTable SET isRoot=" + isRoot + " WHERE id='" + id + "';");
			++insg;
		}
		System.out.println("Metadatensätze kontrolliert = " + insg);
		return ret;
	}

	public static void main(String[] args) throws Exception {
		IeTable.makeExistent();
		System.out.println("Anzahl Metadatensätze vorher = " + MetadatensatzTable.countEntries());
		System.out.println("Anzahl IEs vorher = " + IeTable.countEntries());
		if (scanForIes()) {
			System.out.println("Es wurden neue IEs identifiziert");
		}
		System.out.println("Anzahl Metadatensätze nachher = " + MetadatensatzTable.countEntries());
		System.out.println("Anzahl IEs nachher = " + IeTable.countEntries());
		System.out.println("IdIdentifier Ende");
	}

}
