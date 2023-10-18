import java.io.File;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import idCrawler.PrivateLoader;
import sql.IeTable;
import sql.SqlManager;
import utilities.Drive;
import utilities.PropertiesManager;
import utilities.Url;

public class Vorbereitung {
	/*
	 * Programm erkennt, wenn Limit zu klein ist.
	 * gibt dann Fehlermeldung aus, dass das Limit überschritten wurde
	 * Programm erkennt auch, wenn das Limit zu groß ist
	 * gibt dann eine Fehlermeldung aus, dass Server eine 500 HTTP response zurück gab
	 * 5000 hat sehr lange keine Probleme gemacht, war nur am 12.12.2023 zu klein
	 * probiere mal 10000...
	 */
	private static final int max = 10000;
	static File apiAntwortOrdner = new File(Drive.apiAntwortPfad);

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

		while (!((tag == heuteTag) && (monat == heuteMonat) && (jahr == heuteJahr))) {
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

	private static void findModifiedPMDsForDate(String dateMask) throws Exception {
		final String url = "https://frl.publisso.de/find?q=isDescribedBy.modified:" + dateMask
				+ "&format=json&from=0&until=" + max + "";
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
			try {
				ladeBaum(innerObj, id);
			} catch (Exception e) {
				System.err.println("Fehler bei API-Antwort #" + i + " für dateMask " + dateMask + ":");
				throw e;
			}
			verwalteDBbeiAktualisierterPMD(id);
			if (i % 1000 == 0) {
				System.out.println(i + " Ergebnisse abgearbeitet.");
			}
		}
	}

	private static void ladeBaum(JSONObject innerObj, String id) throws Exception {
		if (innerObj.has("notification")) {
			if (!innerObj.getString("notification").contentEquals("Dieses Objekt wurde gelöscht")) {
				throw new Exception(
						"Ungewöhnliche Notification : " + innerObj.getString("notification") + " bei id " + id + ".");
			}
		} else {
			JSONArray jarr = innerObj.optJSONArray("hasPart");
			if (jarr != null) {
				for (int i = 0; i < jarr.length(); ++i) {
					JSONObject hasPart = jarr.optJSONObject(i);
					if (hasPart == null) {
						throw new Exception("Datensatz " + id + " hat einen ungültigen hasPart Nummer " + i);
					}
					String hasPartId = hasPart.optString("@id");
					if (!hasPartId.startsWith("frl:")) {
						throw new Exception("@id beginnt nicht mit 'frl:': '" + hasPartId + "'");
					}
					String url = "https://frl.publisso.de/resource/".concat(hasPartId).concat(".json2");
					hasPartId = hasPartId.substring(4);
					String datensatz = Url.getText(url);
					Thread.sleep(1000);
					JSONObject child = null;
					try {//speichert das aber nur ab, wenn es eine gültige Json ist. z.B. weil Datensatz private ist.
						child = new JSONObject(datensatz);
					} catch (Exception e) {
						System.err.println("Fehler beim Verarbeiten des hasPart Datensatzes '" + hasPartId + "'. Versuche API Account");
						PrivateLoader.privateMetadataLoader(hasPartId);
						child = new JSONObject(Drive.loadFileToString(new File(Drive.apiAntwort(hasPartId))));
					}
					ladeBaum(child, hasPartId);
				}
			}
		}
		Drive.saveStringToFile(innerObj.toString(2), Drive.apiAntwort(id));
	}

	private static void verwalteDBbeiAktualisierterPMD(String id) throws Exception {
		ResultSet res = sql.SqlManager.INSTANCE.executeQuery("SELECT * FROM ieTable WHERE id='" + id + "';");
		if (res.first()) {
			int status = res.getInt("status");
			if (status < IeTable.status.get("Gebuildet")) {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get("Gefunden") + ";");
			} else {
				SqlManager.INSTANCE.executeUpdate("UPDATE ieTable SET status=" + IeTable.status.get("OutOfDate") + ";");
			}
		} else {
			SqlManager.INSTANCE.executeUpdate("INSERT INTO ieTable (id, status) VALUES ('" + id + "', " + IeTable.status.get("Gefunden") + ");");
		}
	}

	public static void main(String[] args) throws Exception {
//		setzeZurueck();
		scan();
		System.out.println("Vorbereitung Ende");
	}
}
