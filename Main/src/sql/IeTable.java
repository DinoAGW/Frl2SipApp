package sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class IeTable {
	
	//die nächsten beiden Zeilen sind anzupassen
	private static final String tabelle = "ieTable";
	private static final String columns = "( id VARCHAR(14), status int, PRIMARY KEY (id) )";
	
	public static final Map<String, Integer> status = new HashMap<>();
	
	static {
		status.put("Gefunden", 10);
		status.put("NichtPubPubPmd", 12);
		status.put("Embargo", 14);
		status.put("Kinderlos", 16);
		status.put("BereitZumBuilden", 30);
//		status.put()
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
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + " WHERE " + key + " = '" + value + "';");
		return resultSet.next();
	}
	
	public static boolean insertIdIntoDatabase(String id) throws SQLException {
		if (checkIfEntryIsInDatabase("id", id)) {
			return false;
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle + " (id, status) VALUES ('" + id + "', " + status.get("Gefunden") + ");");
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
	
	public static void main(String[] args) throws SQLException {
//		leereTabelle();
		makeExistent();
		System.out.println("Die Datenbank hat " + countEntries() + " Einträge");
		System.out.println("IeTable Ende");
	}
}
