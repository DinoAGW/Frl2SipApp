package sip;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import metsSipCreator.FILE;
import metsSipCreator.REP;
import metsSipCreator.SIP;
import utilities.ApiManager;
import utilities.Drive;

/*
 * ist für das Packen einer SIP verantwortlich
 */
public class SipPacker {
	private static String inst = "PROD";

	private static final String fs = System.getProperty("file.separator");

	static SIP sip1;
	static REP rep1;
	private static boolean externeFD = false;
	private static boolean ignoreMissingMD5 = false;
	private static boolean logMissingMD5 = false;
	private static boolean ignoreUnerlaubteZeichenInDateinamen = false;
	private static boolean everythingPublic;
	private static int tempFileName;

	/*
	 * Gibt für die jeweilige Instanz die ID der ZB MED Staff only AR-Policy aus
	 */
	private static String getARPolicyIdForInst(String inst) throws Exception {
		if (inst.contentEquals("DEV")) {
			return "433120";
		} else if (inst.contentEquals("TEST")) {
			return "1349113";
		} else if (inst.contentEquals("PROD")) {
			return "4963332";
		} else {
			throw new Exception("Inst " + inst + " ungültig");
		}
	}

	public static void setInst(String value) throws Exception {
		if (value.contentEquals("DEV") || value.contentEquals("TEST") || value.contentEquals("PROD")) {
			inst = value;
		} else {
			throw new Exception("Inst " + value + " ungültig");
		}
	}

	/*
	 * generiert eine SIP und speichert sie im Verzeichnis
	 * bin/<inst>_<heute:yyyy_mm_dd>_<id>
	 */
	public static void generateOneSip(String id) throws Exception {
		System.out.println("Verarbeite id " + id + " ...");
		String heute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
		String sipTarget = "bin" + fs + inst + "_" + heute + "_" + id;
		if (externeFD) {
			sipTarget = "/app/FrlAnreicherung/" + fs + inst + "_" + heute + "_" + id;
		}
		File sip = new File(sipTarget);
		File temp = new File("bin" + fs + "temp");
		if (!temp.exists())
			temp.mkdirs();
		if (sip.exists()) {
			FileUtils.deleteDirectory(sip);
		}
		tempFileName = 0;
		sip1 = new SIP();
		rep1 = sip1.newREP(null);
		everythingPublic = true;
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		JSONObject mainObj = new JSONObject(apiAntwortJson);
		traverseIe(id, null, null, mainObj);
//		System.out.println("everythingPublic = " + everythingPublic);
		addMetadata(id, mainObj);
		sip1.deploy(sipTarget);
		FileUtils.deleteDirectory(temp);
	}

	/*
	 * ist hauptverantwortlich um Metadaten zu der SIP hinzuzufügen. Ausnahmen sind
	 * die Metadaten, die File-Datensatz als Quelle haben oder File-Metadaten sind
	 */
	private static void addMetadata(String id, JSONObject mainObj) throws Exception {
		ArrayList<JSONObject> objList = new ArrayList<>();
		objList.add(mainObj);
		ArrayList<JSONObject> tempObj;
		ArrayList<String> tempStr;

		// Zeile 2.0
		sip1.setUserDefined("A", "FRL_" + id);

		// Zeile 3.0
		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "modified");
		if (tempStr.size() != 1) {
			throw new Exception(id);
		}
		for (String str : tempStr) {
			str = str.substring(0, 4).concat(str.substring(5, 7)).concat(str.substring(8, 10));
			sip1.setUserDefined("B", str);
		}

		// Zeile 4.0
		tempStr = getString(objList, "@id");
		addMetadata("dc:identifier", tempStr, true, true, id);

		// Zeile 5.0
		tempStr = getString(objList, "alternative");
		addMetadata("dcterms:alternative", tempStr, false, false, id);

		// Zeile 6.0
		tempStr = getString(objList, "bibliographicCitation");
		addMetadata("dcterms:bibliographicCitation", tempStr, false, true, id);

		// Zeile 7.0
		tempObj = getObject(objList, "containedIn");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dcterms:isPartOf", tempStr, false, false, id);

		// Zeile 8.0
		tempObj = getObject(objList, "collectionTwo");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dcterms:isPartOf", tempStr, false, false, id);

		// Zeile 9.0
		tempObj = getObject(objList, "natureOfContent");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:type", tempStr, false, false, id);

		// Zeile 10.0
		tempObj = getObject(objList, "rdftype");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:type", tempStr, true, false, id);

		// Zeile 11.0
		boolean existAutorin = false;
		ArrayList<JSONObject> contribution = getObject(objList, "contribution");
		for (JSONObject obj : contribution) {
			ArrayList<JSONObject> contributionElement = new ArrayList<>();
			contributionElement.add(obj);

			// stelle fest ob role->label == Autor/in
			tempObj = getObject(contributionElement, "role");
			tempStr = getString(tempObj, "label");
			if (tempStr.size() != 1) {
				throw new Exception("PMD (" + id + ") hat ungleich 1 viele role->label in einer contribution");
			}
			String xPathKey = tempStr.get(0).contentEquals("Autor/in") ? "dc:creator" : "dc:contributor";
			if (tempStr.get(0).contentEquals("Autor/in")) {
				existAutorin = true;// wird später benötigt
			}

			tempObj = getObject(contributionElement, "agent");
			tempStr = getString(tempObj, "prefLabel");
			addMetadata(xPathKey, tempStr, true, true, id);
		}

		// Zeile 12.0
		tempObj = getObject(objList, "contributor");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:contributor", tempStr, false, false, id);

		// Zeile 13.0
		if (!existAutorin) {
			tempObj = getObject(objList, "creator");
			tempStr = getString(tempObj, "prefLabel");
			addMetadata("dc:creator", tempStr, false, false, id);
		}

		// Zeile 14.0
		tempObj = getObject(objList, "ddc");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:subject", tempStr, false, false, id);

		// Zeile 15-18
		// insgesamt mindestens 1 soll gelten
		int count = 0;

		// 15.2
		tempStr = getString(objList, "bibo:doi");
		for (String test : tempStr) {
			if (test.startsWith("10.")) {
				++count;
				sip1.addMetadata("dc:identifier@dcterms:URI", test);
			} else {
				throw new Exception("PMD.bibo:doi beginnt falsch: " + tempStr.get(0));
			}
		}

		// 16.2
		tempStr = getString(objList, "doi");
		for (String test : tempStr) {
			if (test.startsWith("10.")) {
				++count;
				sip1.addMetadata("dc:identifier@dcterms:URI", test);
			} else {
				throw new Exception("PMD.doi beginnt falsch: " + tempStr.get(0));
			}
		}

		// 17.1
		tempObj = getObject(objList, "publisherVersion");
		tempStr = getString(tempObj, "prefLabel");
		for (String test : tempStr) {
			if (test.contains("doi.org/10.")) {
				++count;
				sip1.addMetadata("dcterms:isVersionOf", test);
			}
		}

		// 18.1
		tempObj = getObject(objList, "isLike");
		tempStr = getString(tempObj, "@id");
		for (String test : tempStr) {
			if (test.contains("doi.org/10.")) {
				++count;
				sip1.addMetadata("dcterms:isVersionOf", test);
			}
		}

		if ((count == 0) && (!isInExceptionList(id))) {
			throw new Exception("bei PMD ist keine DOI gefunden worden: " + id);
		}

		// Zeile 19.0
		tempObj = getObject(objList, "editor");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:contributor", tempStr, false, false, id);

		// Zeile 20.0
		tempStr = getString(objList, "embargoTime");
		addMetadata("dcterms:available", tempStr, false, true, id);

		// Zeile 21.0
		tempObj = getObject(objList, "exampleOfWork");
		tempStr = getString(tempObj, "variantNameForTheWork");
		addMetadata("dcterms:alternative", tempStr, false, true, id);

		// Zeile 22.0
		tempStr = getString(objList, "extent");
		addMetadata("dcterms:extent", tempStr, false, true, id);

		// Zeile 23.0
		tempObj = getObject(objList, "hasVersion");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:identifier", tempStr, false, true, id);

		// Zeile 23b.0
		tempStr = getString(objList, "nextVersion");
		addMetadata("dcterms:hasVersion", tempStr, false, true, id);

		// Zeile 23c.0
		tempStr = getString(objList, "previousVersion");
		addMetadata("dcterms:isVersionOf", tempStr, false, true, id);

		// Zeile 24.0
		tempStr = getString(objList, "hbzId");
		addMetadata("dc:identifier", tempStr, false, true, id);

		// Zeile 25.0
		tempObj = getObject(objList, "institution");
		tempStr = getString(tempObj, "@id");
		addMetadata("dcterms:isPartOf", tempStr, false, false, id);

		// Zeile 26.0
		tempStr = getString(objList, "Isbn");
		addMetadata("dc:identifier", tempStr, false, false, id);

		// Zeile 27.0
		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "created");
		addMetadata("dcterms:created", tempStr, true, true, id);

		// Zeile 28.0
		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "modified");
		addMetadata("dcterms:modified", tempStr, true, true, id);

		// Zeile 29.0
		tempStr = getString(objList, "issued");
		addMetadata("dcterms:issued", tempStr, false, true, id);

		// Zeile 30.0
		tempStr = getString(objList, "publicationYear");
		addMetadata("dcterms:issued", tempStr, false, true, id);

		// Zeile 31.0
		tempObj = getObject(objList, "publication");
		tempStr = getString(tempObj, "publishedBy");
		addMetadata("dc:publisher", tempStr, false, false, id);

		// Zeile 31b.0
		tempStr = getString(objList, "regal:publishedBy");
		addMetadata("dc:publisher", tempStr, false, false, id);

		// Zeile 32.0
		tempObj = getObject(objList, "language");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:language", tempStr, false, false, id);

		// Zeile 33.0
		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "@id");
		addMetadata("dcterms:accessRights", tempStr, false, false, id);

		// Zeile 34.0
		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "note");
		addMetadata("dc:rights", tempStr, false, true, id);

		// Zeile 35.1
		ArrayList<JSONObject> lv_isPartOf = getObject(objList, "lv:isPartOf");
		for (JSONObject obj : lv_isPartOf) {
			ArrayList<JSONObject> lv_isPartOfElement = new ArrayList<>();
			lv_isPartOfElement.add(obj);
			String TeilA = null;
			String TeilB = null;

			// ermittle TeilA
			tempObj = getObject(lv_isPartOfElement, "hasSuperordinate");
			tempStr = getString(tempObj, "label");
			for (String str : tempStr) {
				if (!str.contains("lobid")) {
					TeilA = str;
				}
			}
			if (TeilA == null) {
				tempObj = getObject(lv_isPartOfElement, "hasSuperordinate");
				tempStr = getString(tempObj, "prefLabel");
				for (String str : tempStr) {
					if (!str.contains("lobid")) {
						TeilA = str;
					}
				}
			}
			if (TeilA == null) {
				continue;
			}

			// ermittle TeilB
			tempStr = getString(lv_isPartOfElement, "numbering");
			if (tempStr.size() > 1) {
				throw new Exception("PMD (" + id + ") hat mehr als 1 Kandidaten für TeilB in lv:isPartOf");
			} else if (tempStr.size() == 1) {
				TeilB = tempStr.get(0);
			}

			if (TeilB == null) {
				sip1.addMetadata("dcterms:isPartOf", TeilA);
			} else {
				sip1.addMetadata("dcterms:isPartOf", TeilA + ", " + TeilB);
			}
		}

		// Zeile 36.0
		tempObj = getObject(objList, "medium");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:format", tempStr, false, false, id);

		// Zeile 37.0
		tempStr = getString(objList, "P60489");
		addMetadata("dc:publisher", tempStr, false, true, id);

		// Zeile 38
		tempObj = getObject(objList, "subject");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:subject", tempStr, false, false, id);

		// Zeile 38b.0
		tempObj = getObject(objList, "subject");
		tempStr = getString(tempObj, "notation");
		for (String str : tempStr) {
			sip1.addMetadata("dc:subject", "ddc:" + str);
		}

		// Zeile 39.0
		tempStr = getString(objList, "title");
		if (tempStr.size() != 1) {
			throw new Exception("PMD (" + id + ") hat ungleich 1 title");
		}
		String title = tempStr.get(0);
		tempStr = getString(objList, "otherTitleInformation");
		if (tempStr.size() > 1) {
			throw new Exception("PMD (" + id + ") hat mehr als 1 otherTitleInformation");
		}
		if (tempStr.size() == 1) {
			title = title + ":" + tempStr.get(0);
		}
		sip1.addMetadata("dc:title", title);

		// Zeile 40.0
		tempStr = getString(objList, "urn");
		addMetadata("dc:identifier", tempStr, false, false, id);

		// Zeile 41.0
		tempStr = getString(objList, "yearOfCopyright");
		addMetadata("dcterms:dateCopyrighted", tempStr, false, true, id);

		// Zeile 41b.0
		tempObj = getObject(objList, "fundingId");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:contributor", tempStr, false, false, id);

		// Zeile 42.2
		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "created");
		String created = new String(tempStr.get(0));
		int test = 10000 * Integer.parseInt(created.substring(0, 4)) + 100 * Integer.parseInt(created.substring(5, 7))
				+ Integer.parseInt(created.substring(8, 10));
		if (test < 20240503) {
			sip1.addMetadata("dcterms:license", "ZBMED_FRL_v1_Verträge_oder_Lizenz_oder_Policy_ab_31.01.2007");
		} else {
			sip1.addMetadata("dcterms:license", "ZBMED_FRL_v2_Verträge_oder_Lizenz_oder_Policy_ab_03.05.2024");
		}

		// Zeile 45.2
		boolean istZuMappen = false;
		JSONArray arr = mainObj.optJSONArray("note");
		if (arr != null) {
			for (int i = 0; i < arr.length(); ++i) {
				String str = arr.optString(i);
				if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
					istZuMappen = true;
				}
			}
		} else {
			String str = mainObj.optString("note", null); // Sollte niemals vorkommen
			if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
				istZuMappen = true;
			}
			if (str != null) {
				throw new Exception("Keine Nicht-Array-note erwartet. id = " + id);
			}
		}
		arr = mainObj.optJSONArray("additionalNotes");
		if (arr != null) {
			for (int i = 0; i < arr.length(); ++i) {
				String str = arr.optString(i);
				if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
					istZuMappen = true;
				}
			}
		} else {
			String str = mainObj.optString("additionalNotes", null); // Sollte niemals vorkommen
			if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
				istZuMappen = true;
			}
			if (str != null) {
				throw new Exception("Keine Nicht-Array-additionalNotes erwartet. id = " + id);
			}
		}
		if (istZuMappen) {
			sip1.addMetadata("dcterms:accessRights", "Retraction");
		}

		// Zeile 46.2
		istZuMappen = true;
		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "@id");
		for (String str : tempStr) {
			if (str.contains("creativecommons"))
				istZuMappen = false;
		}
		if (istZuMappen) {
			sip1.addMetadata("dc:rights",
					"Dieses Dokument darf zu eigenen wissenschaftlichen Zwecken und zum Privatgebrauch gespeichert und kopiert werden."
							+ " Sie dürfen dieses Dokument nicht für öffentliche oder kommerzielle Zwecke vervielfältigen,"
							+ " öffentlich ausstellen, aufführen, vertreiben oder anderweitig nutzen. // This document may be saved"
							+ " and copied for your personal and scholarly purposes. You may not copy it for public or commercial purposes,"
							+ " exhibit, perform, distribute or otherwise use the document in public.");
		}

		// Zeile 50.0 und 51.0
		tempStr = getString(objList, "hbzId");
		if (tempStr.size() > 1) {
			throw new Exception("PMD (" + id + ") hat zu viele hbzIds");
		} else if (tempStr.size() == 1) {
			sip1.setCMS("HBZ01", tempStr.get(0));
		}
	}

	/*
	 * Nimmt eine Liste an Strings und speichert sie als Metadatum des im xPathKey
	 * angegebenem Schlüssels Prüft dabei ggf je nach minOne oder maxOne ob die
	 * Erwartungen erfüllt wurden
	 */
	private static void addMetadata(String xPathKey, ArrayList<String> tempStr, boolean minOne, boolean maxOne,
			String id) throws Exception {
		if (minOne && tempStr.size() < 1) {
			throw new Exception("PMD (" + id + ") hat an einer Stelle zu wenig Elemente. xPathKey = " + xPathKey);
		}
		if (maxOne && tempStr.size() > 1) {
			for (String str : tempStr) {
				System.err.println(str);
			}
			throw new Exception("PMD (" + id + ") hat an einer Stelle zu viele Elemente. xPathKey = " + xPathKey);
		}
		for (String str : tempStr) {
			sip1.addMetadata(xPathKey, str);
		}
	}

	/*
	 * geht eine Liste von Objekten durch und sucht jeweils in ihnen ein Objekt zum
	 * gegebenem Schlüssel und gibt am Ende eine Liste dieser gefundenen Objekte
	 * zurück
	 */
	private static ArrayList<JSONObject> getObject(ArrayList<JSONObject> objList, String key) {
		ArrayList<JSONObject> ret = new ArrayList<>();
		for (JSONObject obj : objList) {
			JSONObject tempObj = obj.optJSONObject(key);
			if (tempObj != null) {
				ret.add(tempObj);
			}
			JSONArray tempArr = obj.optJSONArray(key);
			if (tempArr != null) {
				for (int i = 0; i < tempArr.length(); ++i) {
					tempObj = tempArr.optJSONObject(i);
					if (tempObj != null) {
						ret.add(tempObj);
					}
				}
			}
		}
		return ret;
	}

	/*
	 * geht eine Liste von Objekten durch und sucht jeweils in ihnen ein String oder
	 * ein Array von Strings mit dem gegebenem Schlüssel und gibt eine Liste dieser
	 * gefundenen Strings zurück
	 */
	private static ArrayList<String> getString(ArrayList<JSONObject> objList, String key) {
		ArrayList<String> ret = new ArrayList<>();
		for (JSONObject obj : objList) {
			String str;
			JSONArray arr = obj.optJSONArray(key);
			if (arr != null) {
				for (int i = 0; i < arr.length(); ++i) {
					str = arr.optString(i);
					if (str != null) {
						ret.add(str);
					}
				}
			} else {
				str = obj.optString(key, null);
				if (str != null) {
					ret.add(str);
				}
			}
		}
		return ret;
	}

	/*
	 * hangelt sich den Baum der Datensätze entlang um die Pfade zu ermitteln, die
	 * Dateien herunter zu laden und Metadaten zu den Dateien hinzuzufügen (bzw der
	 * Files der SIP)
	 */
	private static void traverseIe(String id, String letzterPfad, String parent, JSONObject mainObj) throws Exception {
		// Lade die json-Datei zu der ID von der Festplatte
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		JSONObject obj = new JSONObject(apiAntwortJson);

		boolean geloescht = false;
		if (obj.has("notification")) {
			if (!obj.getString("notification").contentEquals("Dieses Objekt wurde gelöscht")) {
				throw new Exception(
						"Ungewöhnliche Notification : " + obj.getString("notification") + " bei id " + id + ".");
			}
			geloescht = true;
		}

		// Ein paar Erwartungsprüfungen
		if (!obj.has("contentType")) {
			System.err.println("Datensatz ohne contentType: " + id + ".");
			throw new Exception();
		}
		// prüfe accessScheme
		if (!obj.has("accessScheme")) {
			System.err.println("Datensatz ohne accessScheme: " + id + ".");
			throw new Exception();
		}
		String accessScheme = obj.getString("accessScheme");
		if (!accessScheme.contentEquals("private") && !accessScheme.contentEquals("public")) {
			System.err.println("Datensatz " + id + " accessScheme ist weder private, noch public: " + accessScheme);
			throw new Exception();
		}
		// prüfe publishScheme
		if (!obj.has("publishScheme")) {
			System.err.println("Datensatz ohne publishScheme: " + id + ".");
			throw new Exception();
		}
		String publishScheme = obj.getString("publishScheme");
		if (!publishScheme.contentEquals("private") && !publishScheme.contentEquals("public")) {
			System.err.println("Datensatz " + id + " publishScheme ist weder private, noch public: " + publishScheme);
			throw new Exception();
		}
		if (parent != null && !geloescht) {
			if (!obj.has("parentPid")) {
				System.err.println("Kind hat keine Eltern: " + id + ".");
				throw new Exception();
			}
			if (!"frl:".concat(parent).contentEquals(obj.getString("parentPid"))) {
				System.err.println(
						"Kind " + id + " von " + parent + " hat als Parent " + obj.getString("parentPid") + ".");
				throw new Exception();
			}
		}

		// berechne Pfad
		String pfad = null;
		if (obj.getString("contentType").contentEquals("part")) {
			if (!obj.has("title")) {
				System.err.println("Ein Part ohne title: " + id + ".");
				throw new Exception();
			}
			String title = obj.getJSONArray("title").getString(0);
			if (Drive.checkUnerlaubteZeichen(title)) {
				if (ignoreUnerlaubteZeichenInDateinamen) {
					System.err.println("Ordnername '" + title + "' hat unerlaubte Zeichen");
				} else {
					throw new Exception("Ordnername '" + title + "' hat unerlaubte Zeichen");
				}
			}
			if ((letzterPfad.length() == 0) && title.contentEquals("SourceMD")) {
				throw new Exception("Konflikt mit einer \"SourceMD\" Part und dem gleichnamigem Ordner: " + id + ".");
			}
			pfad = letzterPfad.concat(title).concat(fs);
		} else if (obj.getString("contentType").contentEquals("file")) {
			pfad = letzterPfad;
		} else {
			if (letzterPfad != null) {
				System.err.println("Weiter unten sollte kein " + obj.getString("contentType") + " sein");
				throw new Exception();
			}
			pfad = "";
		}

		// Füge jsonld-Datei hinzu
		rep1.newFile(Drive.apiAntwort(id), "SourceMD".concat(fs).concat(pfad));

		// tue nichts weiter, wenn gelöscht
		if (geloescht) {
			return;
		}
		// tue nichts weiter falls publishScheme=private
		if (publishScheme.contentEquals("private")) {
			return;
		}

		if (obj.has("hasPart")) {
			if (obj.getString("contentType").contentEquals("file")) {
				throw new Exception("File-Datensatz sollte kein Part haben: " + id + ".");
			}

			JSONArray jarr = obj.getJSONArray("hasPart");

			for (int i = 0; i < jarr.length(); ++i) {
				JSONObject innerObj = jarr.getJSONObject(i);
				if (!innerObj.has("@id")) {
					throw new Exception("hasPart ohne @id im Datensatz " + id + ".");
				}
				String innerId = innerObj.getString("@id");
				if (!innerId.startsWith("frl:")) {
					throw new Exception("Frl ID in " + id + " beginnt nicht mit 'frl:' " + innerId + ".");
				}

				traverseIe(innerId.substring(4), pfad, id, mainObj);
			}
		} else if (obj.has("hasData")) {
//			System.out.println("File: " + letzterPfad + obj.getJSONObject("hasData").getString("fileLabel"));
//			System.out.println(pfad);
			ApiManager.saveDataOfId2File(id,
					"bin".concat(fs).concat("temp").concat(fs).concat(Integer.toString(tempFileName)));
			String Dateiname = obj.getJSONObject("hasData").getString("fileLabel");
			// Nur um sicher zu gehen
			if ((pfad.length() == 0) && Dateiname.contentEquals("SourceMD")) {
				throw new Exception("Konflikt mit einer \"SourceMD\" Datei und dem gleichnamigem Ordner: " + id + ".");
			}
			if (Drive.checkUnerlaubteZeichen(Dateiname)) {
				if (ignoreUnerlaubteZeichenInDateinamen) {
					System.err.println("Dateiname '" + Dateiname + "' hat unerlaubte Zeichen");
				} else {
					throw new Exception("Dateiname '" + Dateiname + "' hat unerlaubte Zeichen");
				}
			}
			// reporte jsonld Dateien, weil wir jsonld-Fortmaterkennungsfehler in Rosetta
			// ignorieren
			if (Dateiname.endsWith(".jsonld")) {
				FileWriter fr = new FileWriter(new File("bin" + fs + "JSONLDs.txt"), true);
				fr.append(id + "\n");
				fr.close();
			}
			String md5sum = null;
			try {
				md5sum = obj.getJSONObject("hasData").getJSONObject("checksum").getString("checksumValue");
			} catch (Exception e) {
				throw new Exception("md5-Summe konnte nicht ermittelt werden bei " + id);
			}
			FILE tempFile = rep1
					.newFile("bin".concat(fs).concat("temp").concat(fs).concat(Integer.toString(tempFileName)),
							pfad.concat(Dateiname))
					.setLabel(id.concat("_").concat(obj.getJSONObject("hasData").getString("fileLabel")));
			if (md5sum == null || md5sum.length() < 32) {
				if (ignoreMissingMD5) {
					System.err.println("md5-Summe fehlt bei " + id);
				} else {
					if (logMissingMD5) {
						FileWriter fr = new FileWriter(new File("bin" + fs + "MissingMD5s.txt"), true);
						fr.append(id + "\n");
						fr.close();
					}
					throw new Exception("md5-Summe fehlt bei " + id);
				}
			} else {
				tempFile.setMd5sum(md5sum);
			}
			++tempFileName;

			// Zeile 47.0 der Mappingtabelle
			String atId = obj.optString("@id");
			if (atId == null) {
				throw new Exception("Datei " + id + " hat keine @id");
			}
			tempFile.addMetadata("dc:identifier", atId);

			// Zeile 48.0 der Mappingtabelle
			JSONArray jarr = obj.optJSONArray("title");
			if (jarr == null) {
				throw new Exception("Datei " + id + " hat kein title Array");
			}
			if (jarr.length() != 1) {
				throw new Exception("Datei " + id + " hat ungleich 1 title: " + jarr.length());
			}
			String title = jarr.getString(0);
			tempFile.addMetadata("dc:title", title);

			if (accessScheme.contentEquals("private")) {
				// Zeile 49.0 der Mappingtabelle
				tempFile.setARPolicy(getARPolicyIdForInst(inst), "ZB MED_STAFF only");
				everythingPublic = false;

				boolean istZuMappen;
				JSONArray arr;

				// Zeile 44.1 der Mappingtabelle (bzgl. Nutzungsvereinbarung)
				istZuMappen = true;
				arr = obj.optJSONArray("title");
				if (arr != null) {
					for (int i = 0; i < arr.length(); ++i) {
						String str = arr.optString(i);
						if (str != null && !str.contains("Nutzungsvereinbarung")) {
							istZuMappen = false;
						}
					}
				} else {
					String str = obj.optString("title", null);
					if (str != null && !str.contains("Nutzungsvereinbarung")) {
						istZuMappen = false;
					}
				}
				if (istZuMappen) {
					sip1.addMetadata("dc:rights", "Datei_Nutzungsvereinbarung " + id);
				}
			}
		} else {
			String Dateiname = "/app/FrlAnreicherung/" + id + "/Data.zip";
			FILE tempFile = rep1.newFile(Dateiname, pfad.concat("/Data.zip")).setLabel(id.concat("_Data.zip"))
					.setMoveMode(true);
			++tempFileName;
			if (new File(Dateiname).exists()) {
				System.err.println("Externe Forschungsdaten? " + id + ".");
			} else {
				throw new Exception(
						"Kein hasPart, kein hasData, aber auch keine externen Forschungsdaten -> das darf nicht sein");
			}
		}
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/*
	 * Für eine gegebene csv-Datei: gehe Zeile für Zeile durch und wenn dort nur
	 * eine Zahl steht, bild die SIP zu dieser ID und speichere in die csv dahinter
	 * ab, ob Fehler oder fertig
	 */
	private static void generateSipsFromCsv(String csv) throws Exception {
		File csvFile = new File(csv);
		if (!csvFile.exists()) {
			throw new Exception("CSV-Datei nicht gefunden");
		}
		List<String> lines = Files.readAllLines(csvFile.toPath());
		for (int index = 0; index < lines.size(); ++index) {
			String line = lines.get(index);
			if (isNumeric(line)) {
				try {
					generateOneSip(line);
				} catch (Exception e) {
					lines.set(index, line + "Fehler");
					Files.write(csvFile.toPath(), lines);
					throw e;
				}
				lines.set(index, line + "fertig");
				Files.write(csvFile.toPath(), lines);
			}
		}
	}

	private static void clearCsv(String csv) throws Exception {
		File csvFile = new File(csv);
		if (!csvFile.exists()) {
			throw new Exception("CSV-Datei nicht gefunden");
		}
		List<String> lines = Files.readAllLines(csvFile.toPath());
		for (int index = 0; index < lines.size(); ++index) {
			String line = lines.get(index);
			if (isNumeric(line)) {
				continue;
			}
			if (!line.endsWith("fertig")) {
				throw new Exception("Nicht implementiert");
			}
			line = line.substring(0, line.length() - 6);
			lines.set(index, line);
		}
		Files.write(csvFile.toPath(), lines);
	}

	private static boolean isInExceptionList(String id) {
		if (id.contentEquals("6402620"))
			return true;
		if (id.contentEquals("6408553"))
			return true;
		if (id.contentEquals("6402311"))
			return true;
		if (id.contentEquals("6441265"))
			return true;
		if (id.contentEquals("6441267"))
			return true;
		return false;
	}

	private static void generateListOfSIPs(String csvFile) throws Exception {
		File list = new File(csvFile);
		if (!list.exists() && !list.isFile())
			throw new Exception("Datei " + list + " existiert nicht oder ist keine Datei");

		String[] data = Drive.readCsvFileEinspaltig(list);

		for (int i = 0; i < data.length; ++i) {
//			System.out.println("Builde: " + data[i]);
			generateOneSip(data[i]);
		}
	}

	public static void main(String[] args) throws Exception {
		String sipId = "6402743";
		ApiManager.saveId2FileRecursively(sipId);
		generateOneSip(sipId);

//		externeFD = true;
//		String sipId = "6425518";
//		generateOneSip(sipId);
//		IeTable.zeigeEintrag(sipId);

//		ignoreMissingMD5 = true;
//		generateListOfSIPs(Drive.home + fs + "workspace" + fs + "Testdaten_Ingest_Policy-Embargo-Publikationen.csv");

//		inst = "TEST";
//		generateOneSip("6415606");
//		generateOneSip("6415609");
//		generateOneSip("6415621");
//		generateOneSip("6415612");
//		generateOneSip("6511274");

//		clearCsv("bin" + fs + "Test-Datensaetze_2023-06-25.csv");
//		generateSipsFromCsv("bin" + fs + "Test-Datensaetze_2023-06-25.csv");
//		generateSipsFromCsv("bin" + fs + "Test-Datensaetze_2023-10-17.csv");
		System.out.println("SipPacker Ende");
	}

}
