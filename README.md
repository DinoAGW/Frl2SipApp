# Frl2SipApp

## Einstieg

* Projekt als existing maven Project in Eclipse importieren
* Java 1.8
* hängt von Projekt MetsSipCreator ab
  * dort mal maven clean package install
  * dann hier maven build
* Setup ausführen
* in FRL_Properties im Home-Ordner noch username und Passwort für FRL API Account ergänzen
* idCrawler->KonsekutivCrawl ausführen um Datensätze auf die Festplatte zu laden
* ieManager->IeIdentifier ausführen um anhand der Datensätzen von der Festplatte die Datenbank mit IEs aufzubauen
* idCrawler->DeepCrawlausführen um weitere Datensätze auf der Festplatte nachzuladen

## maven
* mvn install:install-file -Dfile=Main/lib/dps-sdk-7.3.0.jar -DgroupId=com.exlibris -DartifactId=dps-sdk -Dversion=7.3.0 -Dpackaging=jar
* mvn install:install-file -Dfile=Main/lib/MetsSipCreator-0.2.8-jar-with-dependencies.jar -DgroupId=de.zbmed -DartifactId=MetsSipCreator -Dversion=0.2.8 -Dpackaging=jar
