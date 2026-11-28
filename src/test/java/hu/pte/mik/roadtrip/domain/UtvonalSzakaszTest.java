package hu.pte.mik.roadtrip.domain;

import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Útvonalszakaszok polimorf számításai és validációi")
class UtvonalSzakaszTest {

    private static final double EPSZILON = 0.0001;

    @Test
    @DisplayName("AutopalyaSzakasz +15% fogyasztási felárral, matricadíjjal és átlagsebességgel számol")
    void autopalyaSzakaszPolimorfKoltsegEsMenetido() {
        UtvonalSzakasz szakasz = new AutopalyaSzakasz("M7 autópálya", 200.0, 7.0, 600.0, 6400.0, 125.0);

        // Alap üzemanyag: (200 / 100) * 7.0 * 600.0 = 8400 Ft
        // +15% felárral: 8400 * 1.15 = 9660 Ft + 6400 Ft matrica = 16060 Ft
        assertEquals(16060.0, szakasz.szamoltKoltseg(), EPSZILON);
        assertEquals(1.6, szakasz.becsultMenetidoOra(), EPSZILON);
        assertEquals("Autópálya", szakasz.getSzakaszTipus());
    }

    @Test
    @DisplayName("HegyiHagoSzakasz szintemelkedés miatt magasabb fogyasztással és lassabb tempóval számol")
    void hegyiHagoSzakaszPolimorfKoltsegEsMenetido() {
        UtvonalSzakasz szakasz = new HegyiHagoSzakasz("Transzfogarasi", 100.0, 8.0, 600.0, 1000, 10);

        // Alap üzemanyag: 1.0 * 8.0 * 600.0 = 4800 Ft
        // Szintemelkedés felár: 1 + (1000 / 100) * 0.03 = 1.30 -> 6240 Ft
        // Effektív sebesség: 55.0 - 10 * 0.8 - (1000 / 500.0) * 2.0 = 43.0 km/h
        assertEquals(6240.0, szakasz.szamoltKoltseg(), EPSZILON);
        assertEquals(100.0 / 43.0, szakasz.becsultMenetidoOra(), EPSZILON);

        // Extrém sok hajtűkanyar esetén a minimális sebesség 20 km/h marad
        HegyiHagoSzakasz extremHago = new HegyiHagoSzakasz("Stelvio", 40.0, 8.0, 600.0, 1800, 60);
        assertEquals(20.0, extremHago.getBecsultAtlagSebessegKmh(), EPSZILON);
        assertEquals(2.0, extremHago.becsultMenetidoOra(), EPSZILON);
    }

    @Test
    @DisplayName("VarosiSzakasz parkolási díjjal és dugó faktorral növelt menetidővel számol")
    void varosiSzakaszPolimorfKoltsegEsMenetido() {
        UtvonalSzakasz szakasz = new VarosiSzakasz("Belváros", 35.0, 8.0, 600.0, 1800.0, 1.5);

        // Alap üzemanyag: 0.35 * 8.0 * 600.0 = 1680 Ft + 1800 Ft parkolás = 3480 Ft
        // Menetidő: (35.0 / 35.0) * 1.5 = 1.5 óra
        assertEquals(3480.0, szakasz.szamoltKoltseg(), EPSZILON);
        assertEquals(1800.0, szakasz.extraDijKoltseg(), EPSZILON);
        assertEquals(1680.0, szakasz.uzemanyagKoltseg(), EPSZILON);
        assertEquals(1.5, szakasz.becsultMenetidoOra(), EPSZILON);
    }

    @Test
    @DisplayName("OrszagutiSzakasz extra díj nélkül, forgalmi szorzóval és átlagsebességgel számol")
    void orszagutiSzakaszPolimorfKoltsegEsMenetido() {
        UtvonalSzakasz alap = new OrszagutiSzakasz("6-os főút", 90.0, 7.0, 600.0);
        // Alapértelmezett: 90 km/h, 1.0 szorzó -> 0.9 * 7.0 * 600.0 = 3780 Ft, 1.0 óra
        assertEquals(3780.0, alap.szamoltKoltseg(), EPSZILON);
        assertEquals(0.0, alap.extraDijKoltseg(), EPSZILON);
        assertEquals(3780.0, alap.uzemanyagKoltseg(), EPSZILON);
        assertEquals(1.0, alap.becsultMenetidoOra(), EPSZILON);
        assertEquals("Országúti", alap.getSzakaszTipus());

        UtvonalSzakasz forgalmas = new OrszagutiSzakasz("66-os főút", 80.0, 7.5, 600.0, 80.0, 1.2);
        // Alap üzemanyag: 0.8 * 7.5 * 600.0 = 3600 Ft * 1.2 = 4320 Ft, 1.0 óra
        assertEquals(4320.0, forgalmas.szamoltKoltseg(), EPSZILON);
        assertEquals(1.0, forgalmas.becsultMenetidoOra(), EPSZILON);
    }

    @Test
    @DisplayName("Útvonalszakaszok természetes rendezése Comparable alapján távolság szerint történik")
    void szakaszokTermeszetesRendezeseTavolsagSzerint() {
        UtvonalSzakasz hosszu = new AutopalyaSzakasz("Autópálya", 250.0, 7.0, 600.0, 5000.0, 130.0);
        UtvonalSzakasz rovid = new VarosiSzakasz("Város", 15.0, 7.0, 600.0, 1200.0, 1.3);
        UtvonalSzakasz kozepes = new HegyiHagoSzakasz("Hágó", 80.0, 7.0, 600.0, 900, 12);

        List<UtvonalSzakasz> lista = new ArrayList<>(List.of(hosszu, rovid, kozepes));
        Collections.sort(lista);

        assertEquals(List.of(rovid, kozepes, hosszu), lista);
        assertTrue(rovid.compareTo(hosszu) < 0);
    }

    @Test
    @DisplayName("Érvénytelen szakaszparaméterek esetén ErvenytelenAdatException dobódik")
    void ervenytelenSzakaszAdatokraKiveteltDob() {
        assertThrows(ErvenytelenAdatException.class,
                () -> new AutopalyaSzakasz(" ", 100.0, 7.0, 600.0, 1000.0, 120.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new AutopalyaSzakasz("M1", 0.0, 7.0, 600.0, 1000.0, 120.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new AutopalyaSzakasz("M1", 100.0, 7.0, 600.0, -500.0, 120.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new HegyiHagoSzakasz("Hágó", 50.0, 7.0, 600.0, -100, 5));
        assertThrows(ErvenytelenAdatException.class,
                () -> new HegyiHagoSzakasz("Hágó", 50.0, 7.0, 600.0, 500, -1));
        assertThrows(ErvenytelenAdatException.class,
                () -> new VarosiSzakasz("Város", 20.0, 7.0, 600.0, 1000.0, 0.9));
        assertThrows(ErvenytelenAdatException.class,
                () -> new OrszagutiSzakasz("Főút", 50.0, 7.0, 600.0, 0.0, 1.0));
        assertThrows(ErvenytelenAdatException.class,
                () -> new OrszagutiSzakasz("Főút", 50.0, 7.0, 600.0, 90.0, 0.95));
        assertThrows(ErvenytelenAdatException.class,
                () -> new OrszagutiSzakasz("Főút", 50.0, 7.0, 600.0, 90.0, 1.35));
    }
}
