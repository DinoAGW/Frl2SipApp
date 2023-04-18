package sip;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import metsSipCreator.REP;
import metsSipCreator.SIP;
import utilities.Drive;

public class SipPacker {
	private static final String fs = System.getProperty("file.separator");

	static SIP sip1;
	static REP rep1;
	private static boolean everythingPublic;

	public static void generateOneSip(String id) throws Exception {
		File sip = new File("bin" + fs + id);
		if (sip.exists()) {
			FileUtils.deleteDirectory(sip);
		}
		sip1 = new SIP();
		rep1 = sip1.newREP(null);
		everythingPublic = true;
		traverseIe(id, null, null);
		System.out.println("everythingPublic = " + everythingPublic);
		addMetadata(id);
		sip1.deploy("bin" + fs + id);
	}

	private static void addMetadata(String id) throws Exception {
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		ArrayList<JSONObject> objList = new ArrayList<>();
		objList.add(new JSONObject(apiAntwortJson));
		ArrayList<JSONObject> tempObj;
		ArrayList<String> tempStr;

		sip1.setUserDefined("A", id);

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
		addMetadata("dcterms:IsPartOf", tempStr, false, false, id);

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

		// Zeile 17-20
//		//insgesamt 1 soll gelten
//		int count = 0;
//		
//		//bibo->doi
//		tempStr = getString(objList, "bibo:doi");

		tempObj = getObject(objList, "editor");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:contributor", tempStr, false, false, id);

		tempStr = getString(objList, "embargoTime");
		addMetadata("dcterms:available", tempStr, false, true, id);

		tempObj = getObject(objList, "exampleOfWork");
		tempStr = getString(tempObj, "variantNameForTheWork");
		addMetadata("dcterms:alternative", tempStr, false, true, id);

		tempStr = getString(objList, "extent");
		addMetadata("dc:extent", tempStr, false, true, id);

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
		addMetadata("dc:modified", tempStr, true, true, id);

		tempStr = getString(objList, "issued");
		addMetadata("dcterms:Issued", tempStr, false, true, id);

		tempStr = getString(objList, "publicationYear");
		addMetadata("dcterms:Issued", tempStr, false, true, id);

		// Zeile 36/37

		tempObj = getObject(objList, "language");
		tempStr = getString(tempObj, "prefLabel");
		addMetadata("dc:language", tempStr, false, false, id);

		tempObj = getObject(objList, "license");
		tempStr = getString(tempObj, "@id");
		addMetadata("dcterms:accessRights", tempStr, true, false, id);

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
			if (tempStr.size() != 1) {
				throw new Exception("PMD (" + id + ") hat ungleich 1 Kandidaten für TeilB in lv:isPartOf");
			}
			TeilB = tempStr.get(0);

			sip1.addMetadata("dcterms:isPartOf", TeilA + ", " + TeilB);
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
		addMetadata("dcterms:DateCopyrighted", tempStr, false, true, id);

		sip1.addMetadata("dcterms:license", "ZBMED_FRL_v1_Verträge_oder_Lizenz_oder_Policy_ab_31.01.2007");

		boolean istZuMappen = everythingPublic;
		tempObj = getObject(objList, "rdfType");
		tempStr = getString(tempObj, "prefLabel");
		for (String str : tempStr) {
			if (str.contains("Abschlussarbeit")) {
				istZuMappen = false;
			}
		}
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
		
		tempStr = getString(tempObj, "hbzId");
		if (tempStr.size()>1) {
			throw new Exception("PMD (" + id + ") hat zu viele hbzIds");
		} else if (tempStr.size() == 1) {
			sip1.setCMS("HBZ01", tempStr.get(0));
		}
	}

	private static void addMetadata(String xPathKey, ArrayList<String> tempStr, boolean minOne, boolean maxOne,
			String id) throws Exception {
		if (minOne && tempStr.size() < 1) {
			throw new Exception("PMD (" + id + ") hat an einer Stelle zu wenig Elemente");
		}
		if (maxOne && tempStr.size() > 1) {
			for (String str : tempStr) {
				System.err.println(str);
			}
			throw new Exception("PMD (" + id + ") hat an einer Stelle zu viele Elemente");
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
//		Thread.sleep(1000);
		// Lade die json-Datei zu der ID von der Festplatte
		File file = new File(Drive.apiAntwort(id));
//		System.out.println("Pfad = " + letzterPfad);
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
			pfad = id.concat("_").concat(title).concat(fs);
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
		rep1.newFile(Drive.apiAntwort(id), pfad, null);

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
			//TODO: Datei herunterladen + hinzufügen

			if (accessScheme.contentEquals("private")) {
				//TODO: accessRightsPolicy ZB MED Staff Only
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
				System.out.println(id + " " + istZuMappen);
			}

		}
	}

	public static void main(String[] args) throws Exception {
		generateOneSip("6407998");
		System.out.println("SipPacker Ende");
	}

}
