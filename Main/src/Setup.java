import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import sql.IeTable;
import sql.MetadataOverviewTable;
import sql.MetadatensatzTable;
import utilities.Drive;

/*
 * dieses Programm ist dafür da, den PC darauf vorzubereiten für das FRL benutzt zu werden,
 * wenn das Projekt gerade frisch von Github heruntergeladen wurde.
 * Im Anschluss muss noch Username und Passwort der FRL API eingetragen werden (siehe Ausgabe)
 * 
 * Ist so programmiert, dass es nichts falsch macht (falls man es später aus versehen erneut ausführt, passiert nichts)
 */
public class Setup {
	public static final String fs = System.getProperty("file.separator");

	public static void main(String[] args) throws Exception {
		System.out.println("Starte Setup...");
		System.out.println("Kopiere FRL_Properties.txt in den HomeOrdner, falls dort noch nicht vorhanden...");
		File prop = new File(Drive.propertyDateiPfad);
		if (!prop.exists()) {
			File defaultFile = new File("..".concat(fs).concat("Material").concat(fs).concat("FRL_Properties.txt"));
			Files.copy(defaultFile.toPath(), prop.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Sie müssen aber noch Username und Passwort selbst eintragen");
		}
		System.out.println("Lege Unterordner workspace\\Frl2SipApp\\apiAntworten\\ im HomePfad an...");
		File apiAntworten = new File(Drive.apiAntwortPfad);
		apiAntworten.mkdirs();
		System.out.println("Lege Datenbanken an...");
		IeTable.makeExistent();
		MetadataOverviewTable.makeExistent();

		System.out.println("Setup Ende");
	}

}
