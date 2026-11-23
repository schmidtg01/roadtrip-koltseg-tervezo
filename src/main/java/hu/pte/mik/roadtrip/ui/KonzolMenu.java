package hu.pte.mik.roadtrip.ui;

import hu.pte.mik.roadtrip.domain.AutopalyaSzakasz;
import hu.pte.mik.roadtrip.domain.HegyiHagoSzakasz;
import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.domain.UtvonalSzakasz;
import hu.pte.mik.roadtrip.domain.VarosiSzakasz;
import hu.pte.mik.roadtrip.exception.AdatMentesException;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;
import hu.pte.mik.roadtrip.exception.TulTerheltJarmuException;
import hu.pte.mik.roadtrip.persistence.JsonTuraTarolo;
import hu.pte.mik.roadtrip.service.EgyenloElosztasStrategia;
import hu.pte.mik.roadtrip.service.TavolsagAranyosStrategia;
import hu.pte.mik.roadtrip.service.TuraKezeloService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Az Autós Túra- és Költségmegosztó Tervező magyar nyelvű konzolos menürendszere.
 */
public class KonzolMenu {

    private final TuraKezeloService turaKezeloService;
    private final JsonTuraTarolo turaTarolo;
    private final Path adatFajlUtvonal;
    private final Scanner scanner;
    private final List<TuraTerv> turak;
    private int aktivTuraIndex;

    public KonzolMenu(TuraKezeloService turaKezeloService, JsonTuraTarolo turaTarolo, Path adatFajlUtvonal) {
        this(turaKezeloService, turaTarolo, adatFajlUtvonal, new Scanner(System.in, StandardCharsets.UTF_8));
    }

    public KonzolMenu(
            TuraKezeloService turaKezeloService,
            JsonTuraTarolo turaTarolo,
            Path adatFajlUtvonal,
            Scanner scanner) {
        this.turaKezeloService = turaKezeloService;
        this.turaTarolo = turaTarolo;
        this.adatFajlUtvonal = adatFajlUtvonal;
        this.scanner = scanner;
        this.turak = new ArrayList<>();
        this.aktivTuraIndex = -1;
    }

    /**
     * Betölti a mentett adatokat (vagy mintaadatot hoz létre), majd elindítja a főmenü ciklust.
     */
    public void indit() {
        System.out.println("=========================================================");
        System.out.println("      Autós Túra- és Költségmegosztó Tervező");
        System.out.println("=========================================================");

        kezdetiAdatokBetoltese();

        boolean fut = true;
        while (fut) {
            foMenuKiirasa();
            Integer valasztas = olvasEgesz("Válasszon menüpontot (1-6): ", 1, 6);
            if (valasztas == null) {
                System.out.println("\nBemeneti csatorna lezárult, kilépés...");
                break;
            }

            switch (valasztas) {
                case 1 -> ujTuraLetrehozasa();
                case 2 -> aktivTuraKivalasztasaEsMegtekintese();
                case 3 -> utvonalSzakaszHozzaadasa();
                case 4 -> utasHozzaadasa();
                case 5 -> koltsegStrategiaValtasaEsElszamolas();
                case 6 -> {
                    adatokMentese();
                    System.out.println("Viszontlátásra! Biztonságos utazást kívánunk!");
                    fut = false;
                }
                default -> System.out.println("Érvénytelen választás.");
            }
        }
    }

    private void kezdetiAdatokBetoltese() {
        if (Files.exists(adatFajlUtvonal)) {
            try {
                List<TuraTerv> betoltott = turaTarolo.betolt(adatFajlUtvonal);
                turak.addAll(betoltott);
                if (!turak.isEmpty()) {
                    aktivTuraIndex = 0;
                    System.out.printf("Sikeresen betöltve %d túraterv a(z) '%s' fájlból.%n",
                            turak.size(), adatFajlUtvonal);
                    return;
                }
                System.out.printf("A(z) '%s' fájl nem tartalmazott túrát, mintatúra létrehozása...%n",
                        adatFajlUtvonal);
            } catch (AdatMentesException e) {
                System.out.println("Figyelmeztetés: nem sikerült betölteni a mentett adatokat: " + e.getMessage());
                System.out.println("Előre feltöltött mintatúrával indul a program.");
            }
        } else {
            System.out.printf("A(z) '%s' még nem létezik. Mintatúra létrehozása az azonnali kipróbáláshoz...%n",
                    adatFajlUtvonal);
        }

        turak.add(letrehozMintaTura());
        aktivTuraIndex = 0;
    }

    private TuraTerv letrehozMintaTura() {
        TuraTerv minta = new TuraTerv(
                "Erdélyi körút",
                "Skoda Octavia Combi",
                5,
                6.8,
                620.0
        );

        minta.szakaszHozzaadasa(new AutopalyaSzakasz(
                "M5 - M43 autópálya (Budapest - Nagylak)",
                235.0,
                minta.getAlapFogyasztas(),
                minta.getUzemanyagAr(),
                6400.0,
                125.0
        ));
        minta.szakaszHozzaadasa(new HegyiHagoSzakasz(
                "Transzfogarasi út (Bâlea-tó)",
                90.0,
                minta.getAlapFogyasztas(),
                minta.getUzemanyagAr(),
                1450,
                28
        ));
        minta.szakaszHozzaadasa(new VarosiSzakasz(
                "Kolozsvár belvárosi áthaladás",
                18.0,
                minta.getAlapFogyasztas(),
                minta.getUzemanyagAr(),
                2500.0,
                1.45
        ));

        try {
            minta.utasHozzaadasa(new Utas("Kovács Péter", 343.0));
            minta.utasHozzaadasa(new Utas("Nagy Anna", 343.0));
            minta.utasHozzaadasa(new Utas("Szabó Gábor", 180.0));
        } catch (TulTerheltJarmuException e) {
            // A mintatúra férőhelye 5 fő, így ez a kivétel itt nem fordulhat elő.
            throw new IllegalStateException("Váratlan hiba a mintatúra létrehozásakor.", e);
        }

        return minta;
    }

    private void foMenuKiirasa() {
        System.out.println();
        System.out.println("---------------------------------------------------------");
        TuraTerv aktiv = getAktivTura();
        if (aktiv != null) {
            System.out.printf("Aktív túra: %s (%s) | Összes túra: %d db%n",
                    aktiv.getTuraNev(), aktiv.getJarmuNev(), turak.size());
        } else {
            System.out.println("Aktív túra: (nincs kiválasztva)");
        }
        System.out.printf("Aktív költségosztási stratégia: %s%n",
                turaKezeloService.getStrategia().getNev());
        System.out.println("---------------------------------------------------------");
        System.out.println("1) Új túraterv létrehozása");
        System.out.println("2) Aktív túra kiválasztása és részleteinek megtekintése");
        System.out.println("3) Útvonalszakasz hozzáadása az aktív túrához");
        System.out.println("4) Utas hozzáadása az aktív túrához");
        System.out.println("5) Költségmegosztási stratégia váltása és elszámolás");
        System.out.println("6) Adatok mentése (turak.json) és kilépés");
    }

    private void ujTuraLetrehozasa() {
        System.out.println("\n--- Új túraterv létrehozása ---");
        String turaNev = olvasNemUresSzoveg("Túra neve: ");
        if (turaNev == null) {
            return;
        }
        String jarmuNev = olvasNemUresSzoveg("Autó típusa / neve: ");
        if (jarmuNev == null) {
            return;
        }
        Integer ferohelyek = olvasEgesz("Férőhelyek száma (fő, min. 1): ", 1, 50);
        if (ferohelyek == null) {
            return;
        }
        Double alapFogyasztas = olvasValosSzam("Alapfogyasztás (liter/100 km): ", 0.0, true);
        if (alapFogyasztas == null) {
            return;
        }
        Double uzemanyagAr = olvasValosSzam("Üzemanyagár (Ft/liter): ", 0.0, true);
        if (uzemanyagAr == null) {
            return;
        }

        try {
            TuraTerv ujTura = new TuraTerv(turaNev, jarmuNev, ferohelyek, alapFogyasztas, uzemanyagAr);
            turak.add(ujTura);
            aktivTuraIndex = turak.size() - 1;
            System.out.printf("Sikeresen létrehozva és aktívvá téve: %s (%s).%n",
                    ujTura.getTuraNev(), ujTura.getJarmuNev());
        } catch (ErvenytelenAdatException e) {
            System.out.println("Hiba a túra létrehozása során: " + e.getMessage());
        }
    }

    private void aktivTuraKivalasztasaEsMegtekintese() {
        if (turak.isEmpty()) {
            System.out.println("\nMég nincs egyetlen túraterv sem. Először hozzon létre egyet!");
            return;
        }

        System.out.println("\n--- Elérhető túratervek ---");
        for (int i = 0; i < turak.size(); i++) {
            TuraTerv tura = turak.get(i);
            String jelolo = (i == aktivTuraIndex) ? " [AKTÍV]" : "";
            System.out.printf("%d) %s%s%n", i + 1, tura, jelolo);
        }

        Integer valasztott = olvasEgesz(
                String.format("Melyik túrát szeretné kiválasztani és megtekinteni? (1-%d): ", turak.size()),
                1, turak.size());
        if (valasztott == null) {
            return;
        }

        aktivTuraIndex = valasztott - 1;
        TuraTerv aktiv = getAktivTura();

        System.out.println("\nRendezési szempont az útvonalszakaszokhoz:");
        System.out.println("1) Távolság szerint (alapértelmezett)");
        System.out.println("2) Költség szerint (növekvő)");
        System.out.println("3) Menetidő szerint (növekvő)");
        System.out.println("4) Felvitel sorrendjében");

        Integer rendezes = olvasEgesz("Válasszon rendezést (1-4): ", 1, 4);
        if (rendezes == null) {
            return;
        }

        List<UtvonalSzakasz> szakaszok = switch (rendezes) {
            case 1 -> turaKezeloService.rendezSzakaszokTavolsagSzerint(aktiv);
            case 2 -> turaKezeloService.rendezSzakaszokKoltsegSzerint(aktiv);
            case 3 -> turaKezeloService.rendezSzakaszokMenetidoSzerint(aktiv);
            default -> aktiv.getSzakaszok();
        };

        turaReszletekKiirasa(aktiv, szakaszok);
    }

    private void turaReszletekKiirasa(TuraTerv tura, List<UtvonalSzakasz> megjelenitendoSzakaszok) {
        System.out.println("\n=========================================================");
        System.out.printf("Túra neve:       %s%n", tura.getTuraNev());
        System.out.printf("Jármű:           %s (férőhelyek: %d/%d fő, szabad: %d fő)%n",
                tura.getJarmuNev(), tura.getUtasok().size(), tura.getFerohelyekSzama(),
                tura.getSzabadFerohelyekSzama());
        System.out.printf("Fogyasztás / ár: %.1f l/100 km | %.0f Ft/liter%n",
                tura.getAlapFogyasztas(), tura.getUzemanyagAr());
        System.out.printf("Össztávolság:    %.1f km%n", tura.osszesTavolsagKm());
        System.out.printf("Összköltség:     %.0f Ft%n", tura.osszesKoltseg());
        System.out.printf("Becsült menetidő: %s (%.2f óra)%n",
                formazIdo(tura.osszesMenetidoOra()), tura.osszesMenetidoOra());

        System.out.println("\nÚtvonalszakaszok:");
        if (megjelenitendoSzakaszok.isEmpty()) {
            System.out.println("  (Még nincs felvett útvonalszakasz.)");
        } else {
            for (int i = 0; i < megjelenitendoSzakaszok.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, megjelenitendoSzakaszok.get(i));
            }
        }

        System.out.println("\nUtasok:");
        if (tura.getUtasok().isEmpty()) {
            System.out.println("  (Még nincs felvett utas.)");
        } else {
            for (int i = 0; i < tura.getUtasok().size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, tura.getUtasok().get(i));
            }
        }
        System.out.println("=========================================================");
    }

    private void utvonalSzakaszHozzaadasa() {
        TuraTerv aktiv = getAktivTura();
        if (aktiv == null) {
            System.out.println("\nNincs aktív túra! Először hozzon létre vagy válasszon ki egy túrát.");
            return;
        }

        System.out.printf("\n--- Útvonalszakasz hozzáadása (%s) ---%n", aktiv.getTuraNev());
        System.out.println("1) Autópálya szakasz (+15% fogyasztási felár, matricadíj)");
        System.out.println("2) Hegyi hágó szakasz (szintemelkedés és hajtűkanyarok)");
        System.out.println("3) Városi szakasz (parkolási díj és dugó faktor)");

        Integer tipus = olvasEgesz("Válasszon szakasztípust (1-3): ", 1, 3);
        if (tipus == null) {
            return;
        }

        String nev = olvasNemUresSzoveg("Szakasz neve: ");
        if (nev == null) {
            return;
        }
        Double tavolsag = olvasValosSzam("Távolság (km): ", 0.0, true);
        if (tavolsag == null) {
            return;
        }

        try {
            UtvonalSzakasz ujSzakasz;
            switch (tipus) {
                case 1 -> {
                    Double matricaDij = olvasValosSzam("Matricadíj (Ft, legalább 0): ", 0.0, false);
                    if (matricaDij == null) {
                        return;
                    }
                    Double atlagSebesseg = olvasValosSzam("Átlagsebesség (km/h): ", 0.0, true);
                    if (atlagSebesseg == null) {
                        return;
                    }
                    ujSzakasz = new AutopalyaSzakasz(
                            nev, tavolsag, aktiv.getAlapFogyasztas(), aktiv.getUzemanyagAr(),
                            matricaDij, atlagSebesseg);
                }
                case 2 -> {
                    Integer szintEmelkedes = olvasEgesz("Szintemelkedés (méter, legalább 0): ", 0, 20000);
                    if (szintEmelkedes == null) {
                        return;
                    }
                    Integer hajtukanyarok = olvasEgesz("Hajtűkanyarok száma (db, legalább 0): ", 0, 500);
                    if (hajtukanyarok == null) {
                        return;
                    }
                    ujSzakasz = new HegyiHagoSzakasz(
                            nev, tavolsag, aktiv.getAlapFogyasztas(), aktiv.getUzemanyagAr(),
                            szintEmelkedes, hajtukanyarok);
                }
                case 3 -> {
                    Double parkolasiDij = olvasValosSzam("Parkolási díj (Ft, legalább 0): ", 0.0, false);
                    if (parkolasiDij == null) {
                        return;
                    }
                    Double dugoFaktor = olvasValosSzam("Dugó faktor (legalább 1.0, pl. 1.4): ", 1.0, false);
                    if (dugoFaktor == null) {
                        return;
                    }
                    ujSzakasz = new VarosiSzakasz(
                            nev, tavolsag, aktiv.getAlapFogyasztas(), aktiv.getUzemanyagAr(),
                            parkolasiDij, dugoFaktor);
                }
                default -> {
                    return;
                }
            }

            aktiv.szakaszHozzaadasa(ujSzakasz);
            System.out.println("Szakasz sikeresen hozzáadva: " + ujSzakasz);
        } catch (ErvenytelenAdatException e) {
            System.out.println("Hiba a szakasz hozzáadása során: " + e.getMessage());
        }
    }

    private void utasHozzaadasa() {
        TuraTerv aktiv = getAktivTura();
        if (aktiv == null) {
            System.out.println("\nNincs aktív túra! Először hozzon létre vagy válasszon ki egy túrát.");
            return;
        }

        System.out.printf("\n--- Utas hozzáadása (%s | szabad helyek: %d/%d) ---%n",
                aktiv.getTuraNev(), aktiv.getSzabadFerohelyekSzama(), aktiv.getFerohelyekSzama());

        String nev = olvasNemUresSzoveg("Utas neve: ");
        if (nev == null) {
            return;
        }
        Double megtettKm = olvasValosSzam(
                String.format("Utas által megtett távolság (km, túra össztávja: %.1f km): ",
                        aktiv.osszesTavolsagKm()),
                0.0, false);
        if (megtettKm == null) {
            return;
        }

        try {
            Utas ujUtas = new Utas(nev, megtettKm);
            aktiv.utasHozzaadasa(ujUtas);
            System.out.println("Utas sikeresen hozzáadva: " + ujUtas);
        } catch (TulTerheltJarmuException e) {
            System.out.println("Nem sikerült hozzáadni az utast: " + e.getMessage());
        } catch (ErvenytelenAdatException e) {
            System.out.println("Érvénytelen utasadat: " + e.getMessage());
        }
    }

    private void koltsegStrategiaValtasaEsElszamolas() {
        TuraTerv aktiv = getAktivTura();
        if (aktiv == null) {
            System.out.println("\nNincs aktív túra! Először hozzon létre vagy válasszon ki egy túrát.");
            return;
        }

        System.out.println("\n--- Költségmegosztási stratégia és elszámolás ---");
        System.out.println("Jelenlegi stratégia: " + turaKezeloService.getStrategia().getNev());
        System.out.println("1) Egyenlő elosztás (minden utas azonos összeget fizet)");
        System.out.println("2) Távolságarányos elosztás (megtett km arányában)");

        Integer valasztas = olvasEgesz("Válasszon stratégiát (1-2): ", 1, 2);
        if (valasztas == null) {
            return;
        }

        if (valasztas == 1) {
            turaKezeloService.setStrategia(new EgyenloElosztasStrategia());
        } else {
            turaKezeloService.setStrategia(new TavolsagAranyosStrategia());
        }

        System.out.printf("%nAktív stratégia beállítva: %s%n", turaKezeloService.getStrategia().getNev());

        try {
            Map<Utas, Double> elszamolas = turaKezeloService.koltsegElszamolas(aktiv);
            System.out.printf("Túra teljes költsége: %.0f Ft%n", aktiv.osszesKoltseg());
            System.out.println("Utasonkénti fizetendő összegek:");
            for (Map.Entry<Utas, Double> bejegyzes : elszamolas.entrySet()) {
                Utas utas = bejegyzes.getKey();
                System.out.printf("  - %-20s (%.1f km): %10.0f Ft%n",
                        utas.getNev(), utas.getMegtettKm(), bejegyzes.getValue());
            }
        } catch (ErvenytelenAdatException e) {
            System.out.println("Nem számítható ki az elszámolás: " + e.getMessage());
        }
    }

    private void adatokMentese() {
        try {
            turaTarolo.ment(turak, adatFajlUtvonal);
            System.out.printf("Sikeresen elmentve %d túraterv a(z) '%s' fájlba.%n",
                    turak.size(), adatFajlUtvonal);
        } catch (AdatMentesException e) {
            System.out.println("Hiba történt az adatok mentése közben: " + e.getMessage());
        }
    }

    private TuraTerv getAktivTura() {
        if (aktivTuraIndex < 0 || aktivTuraIndex >= turak.size()) {
            return null;
        }
        return turak.get(aktivTuraIndex);
    }

    private String olvasNemUresSzoveg(String uzenet) {
        while (true) {
            System.out.print(uzenet);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String sor = scanner.nextLine().trim();
            if (!sor.isEmpty()) {
                return sor;
            }
            System.out.println("Hiba: a mező nem lehet üres, kérem adjon meg egy érvényes szöveget!");
        }
    }

    private Integer olvasEgesz(String uzenet, int min, int max) {
        while (true) {
            System.out.print(uzenet);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String sor = scanner.nextLine().trim();
            try {
                int ertek = Integer.parseInt(sor);
                if (ertek < min || ertek > max) {
                    System.out.printf("Hiba: kérem %d és %d közötti egész számot adjon meg!%n", min, max);
                    continue;
                }
                return ertek;
            } catch (NumberFormatException e) {
                System.out.println("Hiba: érvénytelen bemenet, kérem egész számot írjon be!");
            }
        }
    }

    private Double olvasValosSzam(String uzenet, double min, boolean szigoruMin) {
        while (true) {
            System.out.print(uzenet);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String sor = scanner.nextLine().trim().replace(',', '.');
            try {
                double ertek = Double.parseDouble(sor);
                if (Double.isNaN(ertek) || Double.isInfinite(ertek)) {
                    System.out.println("Hiba: kérem valós számot adjon meg!");
                    continue;
                }
                if (szigoruMin ? ertek <= min : ertek < min) {
                    String feltetel = szigoruMin ? "nagyobb" : "nagyobb vagy egyenlő";
                    System.out.printf("Hiba: az értéknek %.1f-nél %snak kell lennie!%n", min, feltetel);
                    continue;
                }
                return ertek;
            } catch (NumberFormatException e) {
                System.out.println("Hiba: érvénytelen bemenet, kérem számot írjon be (pl. 12.5)!");
            }
        }
    }

    private static String formazIdo(double ora) {
        long osszesPerc = Math.round(ora * 60.0);
        long orak = osszesPerc / 60;
        long percek = osszesPerc % 60;
        return String.format("%d óra %d perc", orak, percek);
    }
}
