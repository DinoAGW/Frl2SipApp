package sip;

import java.io.File;

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
		sip1 = new SIP();
		rep1 = sip1.newREP(null);
		everythingPublic = true;
		traverseIe(id, null, null);
	}

	private static void traverseIe(String id, String pfad, String parent) throws Exception {
		File file = new File(Drive.apiAntwort(id));
		String apiAntwortJson = Drive.loadFileToString(file);
		JSONObject obj = new JSONObject(apiAntwortJson);
		if (!obj.has("contentType")) {
			System.err.println("Datensatz ohne contentType: " + id + ".");
			throw new Exception();
		}
		if(!obj.has("accessScheme")) {
			System.err.println("Datensatz ohne accessScheme: " + id + ".");
			throw new Exception();
		}
		String accessScheme = obj.getString("accessScheme");
		if (!accessScheme.contentEquals("private") && !accessScheme.contentEquals("public")) {
			System.err.println("Datensatz " + id + " ist weder private, noch public: " + accessScheme);
			throw new Exception();
		}
		if (accessScheme.contentEquals("private")) {
			everythingPublic = false;
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
		if (obj.has("hasPart")) {
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
				if (obj.getString("contentType").contentEquals("part")) {
					if (!obj.has("title")) {
						System.err.println("Ein Part ohne title: " + id + ".");
						throw new Exception();
					}
					String title = obj.getString("title");
					traverseIe(innerId.substring(4), title.concat(fs), id);
				} else if (obj.getString("contentType").contentEquals("file")) {
					System.err.println("File-Datensatz sollte kein Part haben: " + id + ".");
					throw new Exception();
				} else {
					if (pfad != null) {
						System.err.println("Weiter unten sollte kein " + obj.getString("contentType") + " sein");
						throw new Exception();
					}
					traverseIe(innerId.substring(4), "", id);
				}
			}
		} else {
			System.out.println("File: " + pfad + obj.getJSONObject("hasData").getString("fileLabel"));
		}
	}

	public static void main(String[] args) throws Exception {
		generateOneSip("6407998");
	}

}
