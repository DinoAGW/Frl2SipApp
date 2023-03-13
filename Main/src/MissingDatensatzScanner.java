import java.io.File;
import java.sql.ResultSet;

import utilities.ApiManager;
import utilities.Drive;

public class MissingDatensatzScanner {
	
	static void scanDatensaetze() throws Exception {
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM metadatensatzTable WHERE isRoot IS NULL");
		while (res.next()) {
			String id = res.getString("id");
			File file = new File(Drive.apiAntwort(id));
			if (!file.exists()) {
				try {
					ApiManager.saveId2File(id);
					System.out.println("fehlender Metadatensatz: " + id + " Korrigiert.");
				} catch (Exception e) {
					System.out.println("fehlender Metadatensatz: " + id + " Konnte nicht korrigiert werden.");
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		scanDatensaetze();
		System.out.println("MissingDatensatzScanner Ende...");
	}

}
