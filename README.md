# Frl2SipApp

## Einstieg

* Projekt als existing maven Project in Eclipse importieren
* Java 1.8
* hängt von Projekt MetsSipCreator ab
  * dort mal maven clean package install
  * dann hier maven build
* Setup ausführen
* idCrawler->KonsekutivCrawl ausführen um Datensätze auf die Festplatte zu laden
* ieManager->IeIdentifier ausführen um anhand der Datensätzen von der Festplatte die Datenbank mit IEs aufzubauen
* idCrawler->DeepCrawlausführen um weitere Datensätze auf der Festplatte nachzuladen
