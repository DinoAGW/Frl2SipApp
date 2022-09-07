package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

	public static int countEntries() throws SQLException {
		int anz = 0;
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM idTable;");
		while (resultSet.next()) {
			++anz;
		}
		return anz;
	}
	
	public static boolean checkIfEntryIsInDatabase(String key, String value) throws SQLException {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM idTable WHERE " + key + " = '" + value + "';");
		return resultSet.next();
	}
	
	public static boolean insertIdIntoDatabase(String id) throws SQLException {
		if (checkIfEntryIsInDatabase("id", id)) {
			SqlManager.INSTANCE.executeUpdate("UPDATE idTable SET found  = CURRENT_DATE() WHERE id = '" + id + "';");
			return false;
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO idTable (id, found) VALUES ('" + id + "', CURRENT_DATE());");
			return true;
		}
	}
	
	public static void main(String[] args) throws SQLException {
		System.out.println("Anzahl = " + countEntries());
	}

}
