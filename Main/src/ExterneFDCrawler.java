import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utilities.Drive;
import utilities.Misc;
import utilities.Url;

public class ExterneFDCrawler {
	public static final String fs = System.getProperty("file.separator");
	
	static void downloadPseudoFile(String linkString, String dateiPfad) throws Exception {
		File datei = new File(dateiPfad);
		try {
			datei.createNewFile();
		} catch (Exception e) {
			throw new Exception("Fehler beim erstellen der Datei " + datei);
		}
		PrintWriter writer = new PrintWriter(datei, "UTF-8");
		writer.println(linkString);
		writer.close();
	}
	
	static void downloadFile(String linkString, String dateiPfad) throws Exception {
		InputStream in = new URL(linkString).openStream();
		Files.copy(in, Paths.get(dateiPfad), StandardCopyOption.REPLACE_EXISTING);
	}

	static void recursiveCrawl(String url, String pfad, String hauptPfad, PrintWriter md5sums) throws Exception {
		Document doc = Url.getWebsite(url);
		Element content = doc.child(0);
		Elements links = content.getElementsByTag("a");
		for (Element link : links) {
			String href = link.attr("href");
			if (href.startsWith("?"))
				continue;
			String linkString = url.concat(href);
			if (linkString.endsWith("/")) {
				pfad = pfad.concat(href.substring(0, href.length() - 1)).concat(fs);
				new File(pfad).mkdir();
				recursiveCrawl(linkString, pfad, hauptPfad, md5sums);
			} else {
				String dateiPfad = pfad.concat(href);
				downloadPseudoFile(linkString, dateiPfad);
				md5sums.println(Misc.md5sumOfFile(dateiPfad) + "  " + pfad.substring(hauptPfad.length()).concat(href));
				System.out.println("Datei: " + linkString + " -> " + dateiPfad);
			}
		}
	}

	static void crawl(String frlId) throws Exception {
		if (!frlId.startsWith("frl:")) {
			throw new Exception("frl-ID muss mit \"frl:\" beginnen");
		}
		String url = "https://frl.publisso.de/data/" + frlId + "/";
		String hauptPfad = Drive.crawl(frlId.substring(4));
		String contentPfad = hauptPfad.concat("content").concat(fs);
		File pfadFile = new File(contentPfad);
		pfadFile.mkdirs();
		File md5sums = new File(hauptPfad.concat("md5sums.txt"));
		md5sums.createNewFile();
		PrintWriter md5sumsWriter = new PrintWriter(md5sums, "UTF-8");
		recursiveCrawl(url, contentPfad, hauptPfad, md5sumsWriter);
		md5sumsWriter.close();
	}

	public static void main(String[] args) throws Exception {
		crawl("frl:6424451");
	}

}
