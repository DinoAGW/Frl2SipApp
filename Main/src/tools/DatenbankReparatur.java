package tools;

import sql.IeTable;
import sql.SqlManager;

public class DatenbankReparatur {
	private static void setzeAlleVonStatusAufStatus(String von, String nach) throws Exception {
		int count = SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get(nach) + " WHERE status=" + IeTable.status.get(von) + ";");
		System.out.println("Es wurden " + count + " Einträge von '" + von + "' nach '" + nach + "' geändert.");
	}

	public static void main(String[] args) throws Exception {
		setzeAlleVonStatusAufStatus("OutOfDate", "Temporär");
		System.out.println("Datenbank Reparatur Ende");
	}
}
