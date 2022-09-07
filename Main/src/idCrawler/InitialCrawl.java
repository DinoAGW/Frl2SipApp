package idCrawler;

import utilities.ApiManager;
import utilities.Database;

public class InitialCrawl {
	private static final String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws Exception {
		int anz = 0;
		for ( int i = 30000; i < 40000; ++i) {
			if (ApiManager.findAny(i)) {
				++anz;
			}
		}
		System.out.println("Anzahl = " + anz);
		System.out.println("Anzahl = " + Database.countEntries());
		
		System.out.println("InitialCrawl Ende");
	}

}
