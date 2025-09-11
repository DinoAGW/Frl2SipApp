package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Klasse zur Verwaltung der VorbereitungFehlerfaelle Datenbank
 */
public class VorbereitungFehlerfaelle {

	// die nächsten beiden Zeilen sind anzupassen
	private static final String tabelle = "fehlerFaelle";
	private static final String columns = "( id VARCHAR(14), PRIMARY KEY (id) )";

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
	 * Falls eine id bereits in der Datenbank ist, gebe false zurück, sonst füge ein
	 * und gebe true zurück
	 */
	public static boolean insertIdIntoDatabase(String id) throws Exception {
		if (checkIfEntryIsInDatabase("id", id)) {
			return false;
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle + " (id) VALUES ('" + id + "');");
			return true;
		}
	}

	/*
	 * Falls eine id nicht in der Datenbank ist, gebe false zurück, sonst entferne
	 * und gebe true zurück
	 */
	public static boolean removeIdFromDatabase(String id) throws Exception {
		if (!checkIfEntryIsInDatabase("id", id)) {
			return false;
		} else {
			SqlManager.INSTANCE.executeUpdate("DELETE FROM " + tabelle + " WHERE id = '" + id + "';");
			return true;
		}
	}

	public static int countEntries() throws Exception {
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + ";");
		while (resultSet.next()) {
			++anz;
		}
		return anz;
	}

	public static int printEntries() throws Exception {
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + ";");
		while (resultSet.next()) {
			String id = resultSet.getString("id");
			ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM ieTable WHERE id='" + id + "';");
			if (res.next()) {
				int status = res.getInt("status");
				System.out.println((++anz) + ") " + id + " hat Status = " + status);
			} else {
				System.out.println((++anz) + ") " + id + " ist nicht in der Datenbank");
			}
		}
		return anz;
	}

	public static int printForGitlab() throws Exception {
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle + ";");
		while (resultSet.next()) {
			String id = resultSet.getString("id");
			++anz;
			System.out.println("- [ ] " + id);
		}
		return anz;
	}

	public static void main(String[] args) throws Exception {
//		leereTabelle();
		makeExistent();
//		removeIdFromDatabase("6501228");
		System.out.println("Die Datenbank hat " + countEntries() + " Einträge");
//		printEntries();
		printForGitlab();
		System.out.println("VorbereitungFehlerfaelle Ende");
	}
}
