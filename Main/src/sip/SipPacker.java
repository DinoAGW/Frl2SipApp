package sip;

import java.io.File;
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
import utilities.Url;

public class SipPacker {
	private static final String fs = System.getProperty("file.separator");

	static SIP sip1;
	static REP rep1;
	private static boolean everythingPublic;
	private static int tempFileName;

	public static void generateOneSip(String id) throws Exception {
		System.out.println("Verarbeite id " + id + " ...");
		String heute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_"));
		File sip = new File("bin" + fs + heute + id);
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
		traverseIe(id, null, null);
//		System.out.println("everythingPublic = " + everythingPublic);
		addMetadata(id);
		sip1.deploy("bin" + fs + heute + id);
		FileUtils.deleteDirectory(temp);
	}

	private static void addMetadata(String id) throws Exception {
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		ArrayList<JSONObject> objList = new ArrayList<>();
		JSONObject mainObj = new JSONObject(apiAntwortJson);
		objList.add(mainObj);
		ArrayList<JSONObject> tempObj;
		ArrayList<String> tempStr;

		sip1.setUserDefined("A", "FRL_" + id);

		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "modified");
		if (tempStr.size() != 1) {
			throw new Exception(id);
		}
		for (String str : tempStr) {
			str = str.substring(0, 4).concat(str.substring(5, 7)).concat(str.substring(8, 10));
			sip1.setUserDefined("B", str);
		}

		tempStr = getString(objList, "@id");
		addMetadata("dc:identifier", tempStr, true, true, id);

		tempStr = getString(objList, "alternative");
		addMetadata("dcterms:alternative", tempStr, false, false, id);

		tempStr = getString(objList, "bibliographicCitation");
		addMetadata("dcterms:bibliographicCitation", tempStr, false, true, id);

		tempObj = getObject(objList, "containedIn");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dcterms:isPartOf", tempStr, false, false, id);

		tempObj = getObject(objList, "collectionTwo");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dcterms:isPartOf", tempStr, false, false, id);

		tempObj = getObject(objList, "natureOfContent");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:type", tempStr, false, false, id);

		tempObj = getObject(objList, "rdftype");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:type", tempStr, true, false, id);

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
				existAutorin = true;
			}

			tempObj = getObject(contributionElement, "agent");
			tempStr = getString(tempObj, "prefLabel");
			addMetadata(xPathKey, tempStr, true, true, id);
		}

		tempObj = getObject(objList, "contributor");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:contributor", tempStr, false, false, id);

		if (!existAutorin) {
			tempObj = getObject(objList, "creator");
			tempStr = getString(tempObj, "prefLabel");
			addMetadata("dc:creator", tempStr, false, false, id);
		}

		tempObj = getObject(objList, "ddc");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:subject", tempStr, false, false, id);

		// Zeile 15-18
		// insgesamt 1 soll gelten
		int count = 0;

		// doi vorgezogen, weil entscheidend für andere Einträge
		tempStr = getString(objList, "doi");
		if (tempStr.size() > 0) {
			++count;
			if (tempStr.size() > 1) {
				throw new Exception("PMD (" + id + ") hat an einer Stelle zu viele Elemente");
			} else {
				String doi = tempStr.get(0);
				if (doi == null || !doi.startsWith("10.")) {
					throw new Exception("PMD.doi beginnt falsch: " + doi);
				}
				sip1.addMetadata("dcterms:URI", doi);
			}
		} else { // nur, falls doi nicht vorhanden ist
			// $.bibo:doi[]:
			tempStr = getString(objList, "bibo:doi");
			if (tempStr.size() > 0 && !tempStr.get(0).startsWith("10.")) {
				throw new Exception("PMD.bibo:doi beginnt falsch: " + tempStr.get(0));
			}
			count += tempStr.size();
			addMetadata("dcterms:URI", tempStr, false, true, id);

			// $.publisherVersion[]{}.prefLabel:
			tempObj = getObject(objList, "publisherVersion");
			tempStr = getString(tempObj, "prefLabel");
			for (String test : tempStr) {
				if (test.contains("doi.org/10.")) {
					++count;
					sip1.addMetadata("dcterms:isVersionOf", test);
				}
			}

			// $.isLike[]{}.@id:
			tempObj = getObject(objList, "isLike");
			tempStr = getString(tempObj, "@id");
			for (String test : tempStr) {
				if (test.contains("doi.org/10.")) {
					++count;
					sip1.addMetadata("dcterms:isVersionOf", test);
				}
			}
		}
		if (count != 1) {
//			throw new Exception("bei PMD sind ungleich 1 DOIs gefunden worden: " + count);
			System.err.println("bei PMD sind ungleich 1 DOIs gefunden worden: " + count);
		}

		tempObj = getObject(objList, "editor");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:contributor", tempStr, false, false, id);

		tempStr = getString(objList, "embargoTime");
		addMetadata("dcterms:available", tempStr, false, true, id);

		tempObj = getObject(objList, "exampleOfWork");
		tempStr = getString(tempObj, "variantNameForTheWork");
		addMetadata("dcterms:alternative", tempStr, false, true, id);

		tempStr = getString(objList, "extent");
		addMetadata("dcterms:extent", tempStr, false, true, id);

		tempObj = getObject(objList, "hasVersion");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:identifier", tempStr, false, true, id);

		tempStr = getString(objList, "hbzId");
		addMetadata("dc:identifier", tempStr, false, true, id);

		tempObj = getObject(objList, "institution");
		tempStr = getString(tempObj, "@id");
		addMetadata("dcterms:isPartOf", tempStr, false, false, id);

		tempStr = getString(objList, "Isbn");
		addMetadata("dc:identifier", tempStr, false, false, id);

		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "created");
		addMetadata("dcterms:created", tempStr, true, true, id);

		tempObj = getObject(objList, "isDescribedBy");
		tempStr = getString(tempObj, "modified");
		addMetadata("dcterms:modified", tempStr, true, true, id);

		tempStr = getString(objList, "issued");
		addMetadata("dcterms:Issued", tempStr, false, true, id);

		tempStr = getString(objList, "publicationYear");
		addMetadata("dcterms:Issued", tempStr, false, true, id);

		tempObj = getObject(objList, "publication");
		tempStr = getString(tempObj, "publishedBy");
		addMetadata("dc:publisher", tempStr, false, false, id);

		tempObj = getObject(objList, "language");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:language", tempStr, false, false, id);

		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "@id");
		addMetadata("dcterms:accessRights", tempStr, false, false, id);

		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "note");
		addMetadata("dc:rights", tempStr, false, true, id);

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
					if (TeilA != null) {
						throw new Exception("PMD (" + id + ") hat zu viele Kandidaten für TeilA in lv:isPartOf");
					}
					TeilA = str;
				}
			}
			tempObj = getObject(lv_isPartOfElement, "hasSuperordinate");
			tempStr = getString(tempObj, "prefLabel");
			for (String str : tempStr) {
				if (!str.contains("lobid")) {
					if (TeilA != null) {
						throw new Exception("PMD (" + id + ") hat zu viele Kandidaten für TeilA in lv:isPartOf");
					}
					TeilA = str;
				}
			}
			if (TeilA == null) {
				throw new Exception("PMD (" + id + ") hat keinen Kandidaten für TeilA in lv:isPartOf");
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

		tempObj = getObject(objList, "medium");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:format", tempStr, false, false, id);

		tempStr = getString(objList, "P60489");
		addMetadata("dc:publisher", tempStr, false, true, id);

		tempObj = getObject(objList, "subject");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:subject", tempStr, false, false, id);

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

		tempStr = getString(objList, "urn");
		addMetadata("dc:identifier", tempStr, false, false, id);

		tempStr = getString(objList, "yearOfCopyright");
		addMetadata("dcterms:dateCopyrighted", tempStr, false, true, id);

		sip1.addMetadata("dcterms:license", "ZBMED_FRL_v1_Verträge_oder_Lizenz_oder_Policy_ab_31.01.2007");

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
			String str = mainObj.optString("note", null);
			if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
				istZuMappen = true;
			}
		}
		if (istZuMappen) {
			sip1.addMetadata("dcterms:accessRights", "Retraction");
		}

		istZuMappen = everythingPublic;
		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "@id");
		if (tempStr.size() > 0) {
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

		tempStr = getString(objList, "hbzId");
		if (tempStr.size() > 1) {
			throw new Exception("PMD (" + id + ") hat zu viele hbzIds");
		} else if (tempStr.size() == 1) {
			sip1.setCMS("HBZ01", tempStr.get(0));
		}
	}

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

	private static void traverseIe(String id, String letzterPfad, String parent) throws Exception {
		// Lade die json-Datei zu der ID von der Festplatte
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		JSONObject obj = new JSONObject(apiAntwortJson);
		if (obj.has("notification")) {
			if (!obj.getString("notification").contentEquals("Dieses Objekt wurde gelöscht")) {
				throw new Exception(
						"Ungewöhnliche Notification : " + obj.getString("notification") + " bei id " + id + ".");
			}
//			System.out.println("Objekt wurde gelöscht: " + id + ".");
			return;
		}
		if (!obj.has("contentType")) {
			System.err.println("Datensatz ohne contentType: " + id + ".");
			throw new Exception();
		}
		if (!obj.has("accessScheme")) {
			System.err.println("Datensatz ohne accessScheme: " + id + ".");
			throw new Exception();
		}
		String accessScheme = obj.getString("accessScheme");
		if (!accessScheme.contentEquals("private") && !accessScheme.contentEquals("public")) {
			System.err.println("Datensatz " + id + " ist weder private, noch public: " + accessScheme);
			throw new Exception();
		}
		if (parent != null) {
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

		String pfad = null;
		if (obj.getString("contentType").contentEquals("part")) {
			if (!obj.has("title")) {
				System.err.println("Ein Part ohne title: " + id + ".");
				throw new Exception();
			}
			String title = obj.getJSONArray("title").getString(0);
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
//		System.out.println("Füge File " + id + " unter '" + pfad + "' hinzu");
		// Füge json-Datei hinzu
		rep1.newFile(Drive.apiAntwort(id), "SourceMD".concat(fs).concat(pfad));

		if (obj.has("hasPart")) {
			if (obj.getString("contentType").contentEquals("file")) {
				System.err.println("File-Datensatz sollte kein Part haben: " + id + ".");
				throw new Exception();
			}

			JSONArray jarr = obj.getJSONArray("hasPart");
			for (int i = 0; i < jarr.length(); ++i) {
				JSONObject innerObj = jarr.getJSONObject(i);
				if (!innerObj.has("@id")) {
					System.err.println("hasPart ohne @id im Datensatz " + id + ".");
					throw new Exception();
				}
				String innerId = innerObj.getString("@id");
				if (!innerId.startsWith("frl:")) {
					System.err.println("Frl ID in " + id + " beginnt nicht mit 'frl:' " + innerId + ".");
					throw new Exception();
				}

				traverseIe(innerId.substring(4), pfad, id);
			}
		} else {
			if (!obj.has("hasData")) {
				throw new Exception("File ohne hasData: " + id + ".");
			}
//			System.out.println("File: " + letzterPfad + obj.getJSONObject("hasData").getString("fileLabel"));
//			System.out.println(pfad);
			ApiManager.saveDataOfId2File(id,
					"bin".concat(fs).concat("temp").concat(fs).concat(Integer.toString(tempFileName)));
			FILE tempFile = rep1
					.newFile("bin".concat(fs).concat("temp").concat(fs).concat(Integer.toString(tempFileName)),
							pfad.concat(obj.getJSONObject("hasData").getString("fileLabel")))
					.setLabel(id.concat("_").concat(obj.getJSONObject("hasData").getString("fileLabel")));
			++tempFileName;

			String atId = obj.optString("@id");
			if (atId == null) {
				throw new Exception("Datei " + id + " hat keine @id");
			}
			tempFile.addMetadata("dc:identifier", atId);

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
				tempFile.setARPolicy("433120", "ZB MED_STAFF only");
				everythingPublic = false;
				boolean istZuMappen = true;
				JSONArray arr = obj.optJSONArray("note");
				if (arr != null) {
					for (int i = 0; i < arr.length(); ++i) {
						String str = arr.optString(i);
						if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
							istZuMappen = false;
						}
					}
				} else {
					String str = obj.optString("note", null);
					if (str != null && (str.contains("zurückgezogen") || str.contains("gesperrt"))) {
						istZuMappen = false;
					}
				}
				if (istZuMappen) {
					sip1.addMetadata("dc:rights", "Datei_Rechtsgrundlage für die Veröffentlichung " + id);
				}

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
		}
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

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
			line = line.substring(0, line.length()-6);
			lines.set(index, line);
		}
		Files.write(csvFile.toPath(), lines);
	}

	public static void main(String[] args) throws Exception {
//		generateOneSip("6407998");
//		generateOneSip("5670012");
//		generateOneSip("6422475");
//		generateOneSip("6413012");
//		generateOneSip("6421582");
//		generateOneSip("6405195");
//		generateOneSip("6415350");
//		generateOneSip("6428346");
//		generateOneSip("6400295");
//		generateOneSip("6401771");
//		generateOneSip("3222678");
//		generateOneSip("6434372");//Embargo
//		generateOneSip("5085526");//zurückgezogen
//		generateOneSip("6422445");//bibo:doi
//		generateOneSip("6410749");
//		generateOneSip("6424992");
//		clearCsv("bin" + fs + "Test-Datensaetze_2023-06-25.csv");
		generateSipsFromCsv("bin" + fs + "Test-Datensaetze_2023-06-25.csv");
		System.out.println("SipPacker Ende");
	}

}
