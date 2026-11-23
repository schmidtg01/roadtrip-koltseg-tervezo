package hu.pte.mik.roadtrip.persistence;

import hu.pte.mik.roadtrip.domain.AutopalyaSzakasz;
import hu.pte.mik.roadtrip.domain.HegyiHagoSzakasz;
import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.domain.UtvonalSzakasz;
import hu.pte.mik.roadtrip.domain.VarosiSzakasz;
import hu.pte.mik.roadtrip.exception.AdatMentesException;
import hu.pte.mik.roadtrip.exception.TulTerheltJarmuException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("JsonTuraTarolo JSON mentés és visszatöltés tesztelése")
class JsonTuraTaroloTest {

    private static final double EPSZILON = 0.0001;

    @Test
    @DisplayName("Polimorf szakaszokat tartalmazó túralista hiba nélkül menthető és visszatölthető JSON-ból")
    void mentesEsBetoltesPolimorfSzakaszokkal(@TempDir Path tempDir)
            throws TulTerheltJarmuException, AdatMentesException {
        JsonTuraTarolo tarolo = new JsonTuraTarolo();
        Path fajl = tempDir.resolve("teszt-turak.json");

        TuraTerv erdely = new TuraTerv("Erdélyi körút", "Skoda Octavia Combi", 5, 6.8, 620.0);
        erdely.szakaszHozzaadasa(new AutopalyaSzakasz("M5", 235.0, 6.8, 620.0, 6400.0, 125.0));
        erdely.szakaszHozzaadasa(new HegyiHagoSzakasz("Transzfogarasi", 90.0, 6.8, 620.0, 1450, 28));
        erdely.szakaszHozzaadasa(new VarosiSzakasz("Kolozsvár", 18.0, 6.8, 620.0, 2500.0, 1.45));
        erdely.utasHozzaadasa(new Utas("Kovács Péter", 343.0));
        erdely.utasHozzaadasa(new Utas("Nagy Anna", 180.0));

        List<TuraTerv> eredeti = List.of(erdely);
        tarolo.ment(eredeti, fajl);

        List<TuraTerv> betoltott = tarolo.betolt(fajl);
        assertEquals(1, betoltott.size());

        TuraTerv visszatoltottTura = betoltott.get(0);
        assertEquals(erdely, visszatoltottTura);
        assertEquals(erdely.osszesKoltseg(), visszatoltottTura.osszesKoltseg(), EPSZILON);
        assertEquals(erdely.osszesMenetidoOra(), visszatoltottTura.osszesMenetidoOra(), EPSZILON);

        List<UtvonalSzakasz> szakaszok = visszatoltottTura.getSzakaszok();
        assertInstanceOf(AutopalyaSzakasz.class, szakaszok.get(0));
        assertInstanceOf(HegyiHagoSzakasz.class, szakaszok.get(1));
        assertInstanceOf(VarosiSzakasz.class, szakaszok.get(2));
    }

    @Test
    @DisplayName("Nem létező JSON fájl betöltésekor AdatMentesException dobódik")
    void nemLetezoFajlBetolteseAdatMentesExceptiontDob(@TempDir Path tempDir) {
        JsonTuraTarolo tarolo = new JsonTuraTarolo();
        Path nemLetezo = tempDir.resolve("nem-letezik.json");

        assertThrows(AdatMentesException.class, () -> tarolo.betolt(nemLetezo));
    }

    @Test
    @DisplayName("Sérült JSON tartalom esetén AdatMentesException dobódik")
    void serultJsonFajlBetolteseAdatMentesExceptiontDob(@TempDir Path tempDir) throws IOException {
        JsonTuraTarolo tarolo = new JsonTuraTarolo();
        Path hibasFajl = tempDir.resolve("serult.json");
        Files.writeString(hibasFajl, "{ nem valid json [", StandardCharsets.UTF_8);

        assertThrows(AdatMentesException.class, () -> tarolo.betolt(hibasFajl));
    }
}
