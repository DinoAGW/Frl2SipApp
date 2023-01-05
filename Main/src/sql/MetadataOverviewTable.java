package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MetadataOverviewTable {

	private static final String tabelle1 = "metadataOverviewTable1";
	private static final String columns1 = "( signatur VARCHAR(100), anz int )";
	private static final String tabelle2 = "metadataOverviewTable2";
	private static final String columns2 = "( signatur VARCHAR(100), beispiel VARCHAR(14), min int, max int )";
	
	public static void addMetadataInTable1(String signatur) throws SQLException {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle1 + " WHERE signatur = '" + signatur + "';");
		if (resultSet.next()) {
			SqlManager.INSTANCE.executeUpdate("UPDATE " + tabelle1 + " SET anz = anz+1 WHERE signatur = '" + signatur + "';");
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle1 + " (signatur, anz) VALUES ('" + signatur + "', 1);");
		}
	}
	
	public static void uebernehme(String id, boolean first) throws SQLException {
		//setze min auf 0, falls nicht vorkommt
		if (!first) {
			ResultSet resultSet2 = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle2 + " WHERE min > 0;");
			while (resultSet2.next()) {
				String signatur = resultSet2.getString("signatur");
				ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle1 + " WHERE signatur = '" + signatur + "';");
				if (!resultSet.next()) {
					SqlManager.INSTANCE.executeUpdate("UPDATE " + tabelle2 + " SET min = 0 WHERE signatur = '" + signatur + "';");
				}
			}
		}
		//aktualisiere den Rest
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle1 + ";");
		while (resultSet.next()) {
			String signatur = resultSet.getString("signatur");
			int anz = resultSet.getInt("anz");
			if (first) {
				SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle2 + " (signatur, beispiel, min, max) VALUES ('" + signatur + "', '" + id + "', " + anz + ", " + anz + ");");
			} else {
				ResultSet resultSet2 = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle2 + " WHERE signatur = '" + signatur + "';");
				if (resultSet2.next()) {
					int min = resultSet2.getInt("min");
					if (anz < min) {
						min = anz;
					}
					int max = resultSet2.getInt("max");
					if (anz > max) {
						max = anz;
					}
					SqlManager.INSTANCE.executeUpdate("UPDATE " + tabelle2 + " SET min = " + min + ", max = " + max + " WHERE signatur = '" + signatur + "';");
				} else {
					SqlManager.INSTANCE.executeUpdate("INSERT INTO " + tabelle2 + " (signatur, beispiel, min, max) VALUES ('" + signatur + "', '" + id + "', 0, " + anz + ");");
				}
			}
		}
		//leere Tabelle1
		try {
			SqlManager.INSTANCE.executeUpdate("DROP TABLE IF EXISTS " + tabelle1);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to delete Tables", e);
		}
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE " + tabelle1 + columns1 + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}
	
	public static void printout() throws SQLException {
		ResultSet resultSet2 = SqlManager.INSTANCE.executeQuery("SELECT * FROM " + tabelle2 + ";");
		while (resultSet2.next()) {
			String signatur = resultSet2.getString("signatur");
			String beispiel = resultSet2.getString("beispiel");
			int min = resultSet2.getInt("min");
			int max = resultSet2.getInt("max");
			System.out.println(signatur + " [" + min + "-" + max + "] Beispiel: " + beispiel);
		}
	}
	
	public static void makeExistent() {
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS " + tabelle1 + columns1 + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS " + tabelle2 + columns2 + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}
	
	public static void leereTabelle1() {
		System.out.println("Lösche Datenbank1...");
		try {
			SqlManager.INSTANCE.executeUpdate("DROP TABLE IF EXISTS " + tabelle1);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to delete Tables", e);
		}
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE " + tabelle1 + columns1 + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}
	
	public static void leereTabelle2() {
		System.out.println("Lösche Datenbank2...");
		try {
			SqlManager.INSTANCE.executeUpdate("DROP TABLE IF EXISTS " + tabelle2);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to delete Tables", e);
		}
		try {
			SqlManager.INSTANCE.executeUpdate("CREATE TABLE " + tabelle2 + columns2 + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}

	public static void main(String[] args) {
		leereTabelle1();
		leereTabelle2();
//		makeExistent();
		System.out.println("MetadataOverviewTable Ende");
	}

}
