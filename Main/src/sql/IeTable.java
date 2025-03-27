package sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/*
 * Klasse zur Verwaltung der IeTable Datenbank
 */
public class IeTable {

	// die nächsten beiden Zeilen sind anzupassen
	private static final String tabelle = "ieTable";
	private static final String columns = "( id VARCHAR(14), status int, PRIMARY KEY (id) )";

	public static final Map<String, Integer> status = new HashMap<>();

	static {
		status.put("Gefunden", 10);
		status.put("NichtArchivierungswürdig", 12);
//		status.put("Embargo", 14);
		status.put("PolicyPublikationen", 15);
//		status.put("BereitZumBuilden", 30); wird nicht mehr benötigt
		status.put("Gebuildet", 60);
		status.put("OutOfDate", 80);
		status.put("Temporär", 101);
	}

	public static void makeExistent() {
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS " + tabelle + columns + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}

	public static void leereTabelle() {
		System.out.println("Lösche Datenbank...");
		try {
			SqlManager.INSTANCE.executeUpdate("DROP TABLE IF EXISTS " + tabelle);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to delete Tables", e);
		}
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE " + tabelle + columns + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}

	public static boolean checkIfEntryIsInDatabase(String key, String value) throws SQLException {
		ResultSet resultSet = SqlManager.INSTANCE
				.executeQuery("SELECT * FROM " + tabelle + " WHERE " + key + " = '" + value + "';");
		return resultSet.next();
	}

	/*
	 * Falls eine id bereits in der Datenbank ist, gebe false zurück, sonst füge als
	 * Gefunden ein und gebe true zurück
	 */
	public static boolean insertIdIntoDatabase(String id) throws SQLException {
		if (checkIfEntryIsInDatabase("id", id)) {
			return false;
		} else {
			SqlManager.INSTANCE.executeUpdate(
					"INSERT INTO " + tabelle + " (id, status) VALUES ('" + id + "', " + status.get("Gefunden") + ");");
			return true;
		}
	}

	public static int countEntries() throws SQLException {
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + ";");
		while (resultSet.next()) {
			++anz;
		}
		return anz;
	}

	/*
	 * zeigt den Status einer IE als Nummer. Übersetzung, siehe status
	 */
	public static void zeigeEintrag(String id) throws Exception {
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM ieTable WHERE id='" + id + "';");
		while (res.next()) {
			int status = res.getInt("status");
			System.out.println("ID = '" + id + "' hat Status = " + status + ".");
		}
	}

	/*
	 * zählt alle Einträge der Datenbank, die einen gewissen Status(als Nummernwert)
	 * haben
	 */
	public static void zaehleEintraege(int status) throws Exception {
		int count = 0;
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM ieTable WHERE status=" + status + ";");
		while (res.next()) {
			++count;
		}
		System.out.println("Anzahl der IEs mit Status = " + status + " ist gleich " + count);
	}

	/*
	 * zählt alle Einträge der Datenbank, die einen gewissen Status(als Bezeichner)
	 * haben
	 */
	public static void zaehleEintraege(String aStatus) throws Exception {
		int count = 0;
		ResultSet res = sql.SqlManager.INSTANCE
				.executeQuery("SELECT * FROM ieTable WHERE status=" + status.get(aStatus) + ";");
		while (res.next()) {
			++count;
		}
		System.out.println("Anzahl der IEs mit Status = " + aStatus + " ist gleich " + count);
	}

	public static void set(String id, String aStatus) throws Exception {
		SqlManager.INSTANCE
				.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get(aStatus) + " WHERE id='" + id + "';");
	}

	public static void main(String[] args) throws Exception {
//		leereTabelle();
//		makeExistent();
		System.out.println("Die Datenbank hat " + countEntries() + " Einträge");
//		set("6369570", "Gefunden");
//		zeigeEintrag("6369570");
//		zeigeEintrag("6433321");
//		zeigeEintrag("6480733");
		zeigeEintrag("6502900");
//		zeigeEintrag("4369687");
//		set("4369687", "OutOfDate");
//		set("4369687", "Gefunden");
//		set("6433321", "Gefunden");
//		zeigeEintrag("6433321");
//		zeigeEintrag("4369687");
//		zaehleEintraege("NichtArchivierungswürdig");
//		zaehleEintraege("Gefunden");
		System.out.println("IeTable Ende");
	}
}
