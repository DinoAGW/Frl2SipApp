package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import utilities.ApiManager;

/*
 * Klasse zur Verwaltung der VorbereitungFehlerfaelle Datenbank
 */
public class DatensatzLogbuch {

	private static final int maxLaenge = 500;
	// die nächsten beiden Zeilen sind anzupassen
	private static final String tabelle = "datensatzLogbuch";
	private static final String columns = "( id VARCHAR(14), root VARCHAR(14), level VARCHAR(7), meldung VARCHAR("
			+ maxLaenge + "))";

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
	public static void insertIdIntoDatabase(String id, String level, String meldung) throws Exception {
		String root = ApiManager.getPmdOfDatensatz(id);
		SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle + " (id, root, level, meldung) VALUES ('" + id
				+ "', '" + root + "', '" + level + "', '" + meldung + "');");
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

	public static int printEntries(String id_arg) throws Exception {
		String root_arg = ApiManager.getPmdOfDatensatz(id_arg);
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE
				.executeQuery("SELECT * FROM " + tabelle + " WHERE 'root' = '" + root_arg + "';");
		while (resultSet.next()) {
			String id = resultSet.getString("id");
			String root = resultSet.getString("root");
			String level = resultSet.getString("level");
			String meldung = resultSet.getString("meldung");
			System.out.printf("ID %s->%s (%7s) %s%n", root, id, level, meldung);
			++anz;
		}
		return anz;
	}

	public static String Fehlermeldung(String id_arg) throws Exception {
		String root_arg = ApiManager.getPmdOfDatensatz(id_arg);
		String ret = null;
		ResultSet resultSet = SqlManager.INSTANCE
				.executeQuery("SELECT * FROM " + tabelle + " WHERE 'root' = '" + root_arg + "';");
		while (resultSet.next()) {
			String id = resultSet.getString("id");
			String root = resultSet.getString("root");
			String level = resultSet.getString("level");
			String meldung = resultSet.getString("meldung");
			if (level.contentEquals("ERROR")) {
				ret = String.format("ID %s->%s (%7s) %s%n", root, id, level, meldung);
			}
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
//		leereTabelle();
		makeExistent();
		System.out.println("Die Datenbank hat " + countEntries() + " Einträge");
		printEntries("1997529");
		System.out.println("DatensatzLogbuch Ende");
	}
}
