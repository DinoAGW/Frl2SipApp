import java.io.File;
import java.io.PrintWriter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utilities.Drive;
import utilities.Url;

public class ExterneFDCrawler {
	public static final String fs = System.getProperty("file.separator");

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
				File datei = new File(pfad.concat(href));
				try {
					datei.createNewFile();
				} catch (Exception e) {
					throw new Exception("Fehler beim erstellen der Datei " + datei);
				}
				PrintWriter writer = new PrintWriter(datei, "UTF-8");
				writer.println(linkString);
				writer.close();
				md5sums.println(datei.toString().substring(hauptPfad.length()));
				System.out.println("Datei: " + linkString + " -> " + datei);
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
