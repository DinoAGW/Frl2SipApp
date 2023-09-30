package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@Deprecated
public class MetadatensatzTable {
	
	//die nächsten beiden Zeilen sind anzupassen
	private static final String tabelle = "metadatensatzTable";
	private static final String columns = "( id VARCHAR(14), found DATE, isRoot BOOLEAN, PRIMARY KEY (id) )";
	
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

	public static int countEntries() throws SQLException {
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + ";");
		while (resultSet.next()) {
			++anz;
		}
		return anz;
	}
	
	public static boolean checkIfEntryIsInDatabase(String key, String value) throws SQLException {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + " WHERE " + key + " = '" + value + "';");
		return resultSet.next();
	}
	
	public static boolean insertIdIntoDatabase(String id) throws SQLException {
		if (checkIfEntryIsInDatabase("id", id)) {
			SqlManager.INSTANCE.executeUpdate("UPDATE " + tabelle + " SET found = CURRENT_DATE() WHERE id = '" + id + "';");
			return false;
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle + " (id, found) VALUES ('" + id + "', CURRENT_DATE());");
			return true;
		}
	}
	
	public static void clearIsRoot() throws SQLException {
		SqlManager.INSTANCE.executeUpdate("UPDATE " + tabelle + " SET isRoot = NULL;");
	}
	
	public static void main(String[] args) throws SQLException {
		makeExistent();
//		clearIsRoot();
		System.out.println("Anzahl = " + countEntries());
	}

}
