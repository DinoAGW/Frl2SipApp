package utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Url {
	public static String getText(String url) throws Exception {
		StringBuilder response = new StringBuilder();

		int versuche = 0;
		while (true) {
			try {
				URL website = new URL(url);
				URLConnection connection = website.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null)
					response.append(inputLine);

				in.close();
				break;
			} catch (Exception e) {
				++versuche;
				if (versuche < 10) {
					System.err.println("Url.getText(" + url + ") nicht geklappt. Versuche erneut...");
					Thread.sleep(1000);
				} else {
					throw e;
				}
			}
		}
		return response.toString();
	}
}
