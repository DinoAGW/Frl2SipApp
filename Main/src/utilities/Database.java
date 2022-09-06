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
	
	public static void main(String[] args) throws SQLException {
		System.out.println("Anzahl = " + countEntries());
	}

}
