package utilities;

public class ApiManager {
	private static final String fs = System.getProperty("file.separator");

	/*
	 * fragt die API nach einem beliebigen Metadatensatz Nummer num, legt die
	 * Antwort ab und merkt sich die ID in der Datenbank
	 */
	public static boolean findAny(int num) throws Exception {
		final String url = "https://frl.publisso.de/find?q=*&format=json&from=".concat(Integer.toString(num))
				.concat("&until=").concat(Integer.toString(num + 1));
		String apiAntwort = Url.getText(url);
		System.out.println(apiAntwort);
		return true;
	}

	public static void main(String[] args) throws Exception {
		findAny(0);
		findAny(1000000);
		System.out.println("ApiManager Ende");
	}

}
