package hu.pte.mik.roadtrip.domain;

import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;
import hu.pte.mik.roadtrip.exception.TulTerheltJarmuException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TuraTerv és Utas üzleti szabályok tesztelése")
class TuraTervTest {

    private static final double EPSZILON = 0.0001;

    @Test
    @DisplayName("Férőhely túllépésekor a TuraTerv TulTerheltJarmuException kivételt dob")
    void utasHozzaadasaFerohelyFelettTulTerheltJarmuExceptiontDob() throws TulTerheltJarmuException {
        TuraTerv tura = new TuraTerv("Balatoni hétvége", "Mazda MX-5", 2, 7.5, 610.0);
        tura.utasHozzaadasa(new Utas("Kiss Bence", 200.0));
        tura.utasHozzaadasa(new Utas("Varga Dóra", 200.0));

        assertEquals(0, tura.getSzabadFerohelyekSzama());
        assertThrows(TulTerheltJarmuException.class,
                () -> tura.utasHozzaadasa(new Utas("Tóth Márk", 200.0)));
    }

    @Test
    @DisplayName("Érvénytelen túra- és utasadatokra ErvenytelenAdatException dobódik")
    void ervenytelenTuraEsUtasAdatokraErvenytelenAdatExceptiontDob() {
        assertThrows(ErvenytelenAdatException.class,
                () -> new TuraTerv("", "Audi A4", 5, 6.5, 600.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new TuraTerv("Alpok", "Audi A4", 0, 6.5, 600.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new TuraTerv("Alpok", "Audi A4", 5, -6.5, 600.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new Utas("   ", 100.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new Utas("Kovács Anna", -10.0));
    }

    @Test
    @DisplayName("TuraTerv pontosan összesíti a különböző szakasztípusok adatait")
    void turaOsszesitesekPolimorfSzakaszokkal() {
        TuraTerv tura = new TuraTerv("Teszt túra", "VW Golf", 4, 7.0, 600.0);
        UtvonalSzakasz autopalya = new AutopalyaSzakasz("M1", 100.0, 7.0, 600.0, 3000.0, 100.0);
        UtvonalSzakasz varosi = new VarosiSzakasz("Győr", 35.0, 7.0, 600.0, 1000.0, 2.0);
        UtvonalSzakasz orszaguti = new OrszagutiSzakasz("82-es főút", 90.0, 7.0, 600.0, 90.0, 1.0);
        tura.szakaszHozzaadasa(autopalya);
        tura.szakaszHozzaadasa(varosi);
        tura.szakaszHozzaadasa(orszaguti);

        assertEquals(225.0, tura.osszesTavolsagKm(), EPSZILON);
        assertEquals(4000.0, tura.osszesExtraDij(), EPSZILON);
        assertEquals(tura.osszesKoltseg() - 4000.0, tura.osszesUzemanyagKoltseg(), EPSZILON);
        assertEquals(
                autopalya.szamoltKoltseg() + varosi.szamoltKoltseg() + orszaguti.szamoltKoltseg(),
                tura.osszesKoltseg(),
                EPSZILON);
        assertEquals(4.0, tura.osszesMenetidoOra(), EPSZILON);
    }
}
