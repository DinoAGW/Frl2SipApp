import java.io.File;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import sql.IeTable;
import sql.SqlManager;
import sql.VorbereitungFehlerfaelle;
import utilities.ApiManager;
import utilities.Drive;
import utilities.PropertiesManager;
import utilities.Url;

/*
 * Diese Klasse ist dafür da, den lokalen Cache auf dem neusten Stand zu bringen.
 */
public class Vorbereitung {
	/*
	 * Programm erkennt, wenn Limit zu klein ist. gibt dann Fehlermeldung aus, dass
	 * das Limit überschritten wurde Programm erkennt auch, wenn das Limit zu groß
	 * ist gibt dann eine Fehlermeldung aus, dass Server eine 500 HTTP response
	 * zurück gab 5000 hat sehr lange keine Probleme gemacht am 12.12.2023 zu klein,
	 * probiere mal 6000 am 27.09.2025 zu klein, probiere mal 7000 immer noch zu
	 * klein, probiere mal 8000
	 */
	private static final int max = 8000;
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
	private static String setzeFortNach = null;

	/*
	 * löscht den Cache und stellt lastMakeUpToDate zurück
	 */
	public static void setzeZurueck() throws Exception {
		System.out.println("Lösche API Antworten aus dem Cache...");
		if (apiAntwortOrdner.exists()) {
			FileUtils.deleteDirectory(apiAntwortOrdner);
		}
		apiAntwortOrdner.mkdirs();
		PropertiesManager prop = new PropertiesManager(Drive.propertyDateiPfad);
		prop.saveStringToProperty("lastMakeUpToDate", "2017-12-04");
		IeTable.leereTabelle();
	}

	/*
	 * Geht alle Tage zwischen lastMakeUpToDate (einschließlich) bis Abruftag
	 * (ausschließlich) durch um für jeden der Tage jeweils nach Neuerungen zu
	 * suchen und speichert den Tag anschließend in lastMakeUpToDate
	 */
	private static void scan() throws Exception {
		String heute = LocalDateTime.now().toString().substring(0, 10);
		utilities.PropertiesManager prop = new utilities.PropertiesManager(Drive.propertyDateiPfad);
		String lastMakeUpToDate = prop.readStringFromProperty("lastMakeUpToDate");

		int heuteJahr = Integer.parseInt(heute.substring(0, 4));
		int heuteMonat = Integer.parseInt(heute.substring(5, 7));
		int heuteTag = Integer.parseInt(heute.substring(8));

		int jahr = Integer.parseInt(lastMakeUpToDate.substring(0, 4));
		int monat = Integer.parseInt(lastMakeUpToDate.substring(5, 7));
		int tag = Integer.parseInt(lastMakeUpToDate.substring(8));

		while (!(tag == heuteTag && monat == heuteMonat && jahr == heuteJahr)) {
			// sucht die Aktualisierungen dieses Tages
			findModifiedPMDsForDate(lastMakeUpToDate.concat("*"));

			++tag;
			if (tag == 32) {
				tag = 1;
				++monat;
			}
			if (monat == 13) {
				monat = 1;
				++jahr;
			}
			lastMakeUpToDate = String.format("%04d-%02d-%02d", jahr, monat, tag);
			prop.saveStringToProperty("lastMakeUpToDate", lastMakeUpToDate);
		}
	}

	/*
	 * Ruft die FRL-Schnittstelle auf um nach aktualisierten PMDs zu suchen und lädt
	 * zu allen dann jeweils PMD und alle darunter hängenden Datensätze herunter.
	 * 
	 * Aktualisiert die Datenbank entsprechend: falls der Datensatz unbekannt war,
	 * wird sie als Gefunden vermerkt falls der Datensatz Gefunden war, bleibt sie
	 * es falls der Datensatz Gebuildet war, wird sie als OutOfDate vermerkt
	 */
	private static void findModifiedPMDsForDate(String dateMask) throws Exception {
		/*
		 * Die Suche muss auf ein Maximum eingeschränkt werden, da sonst standardmässig
		 * nur 10 Ergebnisse angezeigt werden (Siehe Kommentare bei der max Variable)
		 */
		final String url = "https://frl.publisso.de/find?q=NOT%20contentType:file%20AND%20NOT%20contentType:part%20AND%20isDescribedBy.modified:"
				+ dateMask + "&format=json&from=0&until=" + max + "";
		Thread.sleep(1000);
		String apiAntwortJson = Url.getText(url);
		if (apiAntwortJson.length() == 2) {
			System.out.println("Nichts gefunden unter der Maske = '" + dateMask + "'");
			return;
		}

		JSONObject obj = new JSONObject("{\"array\": " + apiAntwortJson + "}");
		JSONArray arr = obj.getJSONArray("array");
		if (arr.length() == max) {
			System.err.println("Suchanfrage hat das limit überschritten für dateMask = '" + dateMask + "'");
			throw new Exception();
		} else {
			System.out
					.println("Die Suchanfrage zur dateMask = '" + dateMask + "' ergab " + arr.length() + " Ergebnisse");
		}
		if (setzeFortNach != null) {
			System.out.println("Skippe alle Publikationen bis inklusive " + setzeFortNach + " ...");
		}
		for (int i = 0; i < arr.length(); ++i) {
			JSONObject innerObj = arr.getJSONObject(i);
			if (innerObj.getString("contentType").contentEquals("file")
					|| innerObj.getString("contentType").contentEquals("part")) {
				continue;
			}
			String id = innerObj.optString("@id");
			if (!id.startsWith("frl:")) {
				throw new Exception("@id beginnt nicht mit 'frl:': '" + id + "'");
			}
			id = id.substring(4);
			if (setzeFortNach != null) {
				if (id.contentEquals(setzeFortNach)) {
					setzeFortNach = null;
					System.out.println("Publikation " + id + " gefunden");
					continue;
				} else {
//					System.out.println("Überspringe: " + id);
					continue;
				}
			}
			try {
				bearbeiteId(id);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				VorbereitungFehlerfaelle.insertIdIntoDatabase(id);
			}
			if ((i % 250 == 0) && (i > 0)) {
				System.out.println(i + " Ergebnisse abgearbeitet.");
			}
		}
		if (setzeFortNach != null) {
			throw new Exception("ID " + setzeFortNach + " konnte nicht gefunden werden");
		}
	}

	private static void rescanFehlerfaelle() throws Exception {
		ResultSet resultSet = SqlManager.INSTANCE.executeQuery("SELECT * FROM fehlerFaelle;");
		while (resultSet.next()) {
			String id = resultSet.getString("id");
			try {
				bearbeiteId(id);
				VorbereitungFehlerfaelle.removeIdFromDatabase(id);
				System.out.println("ID " + id + " ist nun erfolgreich durchgelaufen");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private static void bearbeiteId(String id) throws Exception {
		try {
			ApiManager.saveId2FileRecursively(id, 0);
		} catch (Exception e) {
//			e.printStackTrace();
			throw new Exception("Fehler bei API-Antwort " + id);
		}
		verwalteDBbeiAktualisierterPMD(id);
	}

	/*
	 * aktualisiert die IeTable Datenbank
	 */
	private static void verwalteDBbeiAktualisierterPMD(String id) throws Exception {
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM ieTable WHERE id='" + id + "';");
		if (res.first()) {
			int status = res.getInt("status");
			if (status < IeTable.status.get("Gebuildet")) {
				SqlManager.INSTANCE.executeUpdate(
						"UPDATE ieTable SET status=" + IeTable.status.get("Gefunden") + " WHERE id='" + id + "';");
			} else {
				SqlManager.INSTANCE.executeUpdate(
						"UPDATE ieTable SET status=" + IeTable.status.get("OutOfDate") + " WHERE id='" + id + "';");
			}
		} else {
			SqlManager.INSTANCE.executeUpdate(
					"INSERT INTO ieTable (id, status) VALUES ('" + id + "', " + IeTable.status.get("Gefunden") + ");");
		}
	}

	public static void main(String[] args) throws Exception {
//		setzeZurueck();
//		bearbeiteId("6478755");
		System.out.println("Rescan der Fehlerfälle...");
		rescanFehlerfaelle();
		System.out.println("Rescan Ende. Scan weiter...");
		scan();
		System.out.println("Scan Ende. Fehlerfaelle nun:");
		VorbereitungFehlerfaelle.printEntries();
		System.out.println("Vorbereitung Ende");
	}
}
