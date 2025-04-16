import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Stack;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utilities.Drive;
import utilities.Misc;
import utilities.Url;

public class ExterneFDCrawler {
	public static final String fs = System.getProperty("file.separator");

	private static List<String> content = new Stack<String>();;

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
		if (new File(dateiPfad).exists())
			return;
		InputStream in = new URL(linkString).openStream();
		Files.copy(in, Paths.get(dateiPfad), StandardCopyOption.REPLACE_EXISTING);
		in.close();
	}

	private static void recursiveCrawl(String url, String pfad, String hauptPfad) throws Exception {
		Document doc = Url.getWebsite(url);
		Element elem = doc.child(0);
		Elements links = elem.getElementsByTag("a");
		for (Element link : links) {
			String href = link.attr("href");
			if (href.startsWith("?"))
				continue;
			String linkString = url.concat(href);
			if (linkString.endsWith("/")) {
				String unterpfad = pfad.concat(href.substring(0, href.length() - 1)).concat(fs);
				new File(unterpfad).mkdir();
				recursiveCrawl(linkString, unterpfad, hauptPfad);
			} else {
				String dateiPfad = pfad.concat(href);
				System.out.println("Datei: " + linkString + " -> " + dateiPfad);
				downloadFile(linkString, dateiPfad);
				content.add(dateiPfad);
			}
		}
	}

	private static void calculateMd5sums(String hauptPfad) throws Exception {
		File md5sums = new File(hauptPfad.concat("md5sums.txt"));
		md5sums.createNewFile();
		PrintWriter md5sumsWriter = new PrintWriter(md5sums, "UTF-8");
		for (String dateiPfad : content) {
			md5sumsWriter.println(Misc.md5sumOfFile(dateiPfad) + "  " + dateiPfad.substring(hauptPfad.length()));
		}
		md5sumsWriter.close();
	}

	private static void checkVorhanden(String hauptPfad) throws Exception {
		for (String dateiPfad : content) {
			if (!new File(dateiPfad).exists())
				throw new Exception("Datei " + dateiPfad + " fehlt");
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
		recursiveCrawl(url, contentPfad, hauptPfad);
		checkVorhanden(hauptPfad);
		calculateMd5sums(hauptPfad);
	}

	public static void main(String[] args) throws Exception {
//		crawl("frl:6424451");
		crawl("frl:6425518");
		System.out.println("ExterneFDCrawler Ende");
	}

}
