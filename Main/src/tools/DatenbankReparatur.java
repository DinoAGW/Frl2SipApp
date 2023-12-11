package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.List;

import org.json.JSONObject;

import sip.SipPacker;
import sql.IeTable;
import sql.SqlManager;
import utilities.Drive;

public class DatenbankReparatur {
	private static final String fs = System.getProperty("file.separator");

	private static void setzeAlleVonStatusAufStatus(String von, String nach) throws Exception {
		int count = SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get(nach)
				+ " WHERE status=" + IeTable.status.get(von) + ";");
		System.out.println("Es wurden " + count + " Einträge von '" + von + "' nach '" + nach + "' geändert.");
	}

	private static void bearbeiteIEsAusReport() throws Exception {
		int max = 0;
		int count = 0;

		File reportFile = new File("bin" + fs + "Report1.txt");
		if (!reportFile.exists()) {
			throw new Exception("Report-Datei nicht gefunden");
		}

		BufferedReader reader = new BufferedReader(new FileReader(reportFile));
		String line;
		while ((line = reader.readLine()) != null) {
			if (!SipPacker.isNumeric(line)) {
				throw new Exception("Report-Zeile hat einen ungültigen Wert = '" + line + "'.");
			}
			File pmd = new File(Drive.apiAntwort(line));
			String apiAntwortJson = Drive.loadFileToString(pmd);
			JSONObject obj = new JSONObject(apiAntwortJson);
			if (obj.getString("contentType").contentEquals("file")
					|| obj.getString("contentType").contentEquals("part")) {
				throw new Exception("Es sollten nur PMDs im Report sein: " + line);
			}
			JSONObject isDescribedBy = obj.optJSONObject("isDescribedBy");
			if (isDescribedBy == null) {
				throw new Exception("Kein isDescribedBy gefunden");
			}
			String modified = isDescribedBy.optString("modified", null);
			if (modified == null) {
				throw new Exception("Kein modified gefunden");
			}
			modified = modified.substring(0, 10);

			int modInt = Integer.parseInt(modified.substring(0, 4) + modified.substring(5, 7) + modified.substring(8));

			if (modInt >= 20231129) {
				++count;
				System.out.println("OutOfDate-Kandidat: " + line);
				SqlManager.INSTANCE.executeUpdate(
						"UPDATE ieTable SET status=" + IeTable.status.get("OutOfDate") + " WHERE id='" + line + "';");
			} else {
				SqlManager.INSTANCE.executeUpdate(
						"UPDATE ieTable SET status=" + IeTable.status.get("Gebuildet") + " WHERE id='" + line + "';");
			}

			if (--max == 0) {
				System.out.println("Maximum erreicht, beende mich nun.");
				break;
			}
		}
		System.out.println("Insgesamt: " + count);
	}

	private static void zeigeDatenbankUebersicht() throws Exception {
		IeTable.zaehleEintraege(IeTable.status.get("Gefunden"));
		IeTable.zaehleEintraege(IeTable.status.get("NichtArchivierungswürdig"));
		IeTable.zaehleEintraege(IeTable.status.get("PolicyPublikationen"));
		IeTable.zaehleEintraege(IeTable.status.get("Gebuildet"));
		IeTable.zaehleEintraege(IeTable.status.get("OutOfDate"));
		IeTable.zaehleEintraege(IeTable.status.get("Temporär"));
	}

	public static void main(String[] args) throws Exception {
//		setzeAlleVonStatusAufStatus("Temporär", "Gefunden");
//		bearbeiteIEsAusReport();
		zeigeDatenbankUebersicht();
		System.out.println("Datenbank Reparatur Ende");
	}
}
