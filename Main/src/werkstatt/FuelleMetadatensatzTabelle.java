package werkstatt;

import java.io.File;

import sql.MetadatensatzTable;
import utilities.ApiManager;
import utilities.Drive;

public class FuelleMetadatensatzTabelle {

	public static void main(String[] args) throws Exception {
		File apiAntwortOrdner = new File(Drive.apiAntwortPfad);
		for(File file: apiAntwortOrdner.listFiles()) {
			String apiAntwortJson = Drive.loadFileToString(file);
			String id = ApiManager.json2id(apiAntwortJson);
			MetadatensatzTable.insertIdIntoDatabase(id);
		}
		System.out.println("Anzahl = " + sql.MetadatensatzTable.countEntries());
		System.out.println("FuelleMetadatensatzTabelle fertig");
	}

}
