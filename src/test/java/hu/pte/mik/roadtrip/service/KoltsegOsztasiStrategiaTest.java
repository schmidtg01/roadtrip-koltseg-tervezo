package hu.pte.mik.roadtrip.service;

import hu.pte.mik.roadtrip.domain.AutopalyaSzakasz;
import hu.pte.mik.roadtrip.domain.HegyiHagoSzakasz;
import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.domain.UtvonalSzakasz;
import hu.pte.mik.roadtrip.domain.VarosiSzakasz;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;
import hu.pte.mik.roadtrip.exception.TulTerheltJarmuException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Strategy minta (költségmegosztás) és TuraKezeloService tesztelése")
class KoltsegOsztasiStrategiaTest {

    private static final double EPSZILON = 0.0001;

    @Test
    @DisplayName("EgyenloElosztasStrategia pontosan egyenlő részekre osztja a költséget")
    void egyenloElosztasStrategiaPontosSzamitas() throws TulTerheltJarmuException {
        TuraTerv tura = letrehozMintatura();
        KoltsegOsztasiStrategia strategia = new EgyenloElosztasStrategia();

        Map<Utas, Double> elszamolas = strategia.elosztKoltseg(tura);
        double vartFejenkent = tura.osszesKoltseg() / 3.0;

        assertEquals(3, elszamolas.size());
        for (Double osszeg : elszamolas.values()) {
            assertEquals(vartFejenkent, osszeg, EPSZILON);
        }
        double osszegzett = elszamolas.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(tura.osszesKoltseg(), osszegzett, EPSZILON);
    }

    @Test
    @DisplayName("TavolsagAranyosStrategia pontosan a megtett kilométerek arányában oszt")
    void tavolsagAranyosStrategiaPontosSzamitas() throws TulTerheltJarmuException {
        TuraTerv tura = new TuraTerv("Dél-dunántúli út", "Ford Focus", 4, 10.0, 600.0);
        // Alap üzemanyag: 100 km * 10 l/100km * 600 Ft/l = 6000 Ft + 4000 Ft parkolás = 10000 Ft
        tura.szakaszHozzaadasa(new VarosiSzakasz("Pécs környéke", 100.0, 10.0, 600.0, 4000.0, 1.0));

        Utas peter = new Utas("Péter", 100.0);
        Utas anna = new Utas("Anna", 60.0);
        Utas gabor = new Utas("Gábor", 40.0);
        tura.utasHozzaadasa(peter);
        tura.utasHozzaadasa(anna);
        tura.utasHozzaadasa(gabor);

        // Összes utas-km = 200 km -> Péter 50% (5000 Ft), Anna 30% (3000 Ft), Gábor 20% (2000 Ft)
        KoltsegOsztasiStrategia strategia = new TavolsagAranyosStrategia();
        Map<Utas, Double> elszamolas = strategia.elosztKoltseg(tura);

        assertEquals(5000.0, elszamolas.get(peter), EPSZILON);
        assertEquals(3000.0, elszamolas.get(anna), EPSZILON);
        assertEquals(2000.0, elszamolas.get(gabor), EPSZILON);
    }

    @Test
    @DisplayName("Stratégiák ErvenytelenAdatException kivételt dobnak üres utaslista vagy 0 össz-km esetén")
    void strategiakHibaAgakTesztelese() throws TulTerheltJarmuException {
        TuraTerv uresTura = new TuraTerv("Üres", "Opel Astra", 5, 7.0, 600.0);

        KoltsegOsztasiStrategia egyenlo = new EgyenloElosztasStrategia();
        KoltsegOsztasiStrategia aranyos = new TavolsagAranyosStrategia();

        assertThrows(ErvenytelenAdatException.class, () -> egyenlo.elosztKoltseg(uresTura));
        assertThrows(ErvenytelenAdatException.class, () -> aranyos.elosztKoltseg(uresTura));

        uresTura.utasHozzaadasa(new Utas("Nulla Km Utas", 0.0));
        assertThrows(ErvenytelenAdatException.class, () -> aranyos.elosztKoltseg(uresTura));
    }

    @Test
    @DisplayName("TuraKezeloService támogatja a futásidejű stratégiacserét és a lambda rendezést")
    void turaKezeloServiceStrategiaCsereEsLambdaRendezes() throws TulTerheltJarmuException {
        TuraTerv tura = letrehozMintatura();
        TuraKezeloService service = new TuraKezeloService(new EgyenloElosztasStrategia());

        Utas elsoUtas = tura.getUtasok().get(0);
        Utas harmadikUtas = tura.getUtasok().get(2);

        Map<Utas, Double> egyenloEredmeny = service.koltsegElszamolas(tura);
        assertEquals(egyenloEredmeny.get(elsoUtas), egyenloEredmeny.get(harmadikUtas), EPSZILON);

        // Futásidejű csere távolságarányos stratégiára
        service.setStrategia(new TavolsagAranyosStrategia());
        Map<Utas, Double> aranyosEredmeny = service.koltsegElszamolas(tura);
        assertEquals(aranyosEredmeny.get(elsoUtas) / 2.0, aranyosEredmeny.get(harmadikUtas), EPSZILON);

        // Lambda rendezés költség és menetidő szerint
        List<UtvonalSzakasz> koltsegSzerint = service.rendezSzakaszokKoltsegSzerint(tura);
        for (int i = 0; i < koltsegSzerint.size() - 1; i++) {
            assertEquals(true,
                    koltsegSzerint.get(i).szamoltKoltseg() <= koltsegSzerint.get(i + 1).szamoltKoltseg());
        }

        List<UtvonalSzakasz> menetidoSzerint = service.rendezSzakaszokMenetidoSzerint(tura);
        for (int i = 0; i < menetidoSzerint.size() - 1; i++) {
            assertEquals(true,
                    menetidoSzerint.get(i).becsultMenetidoOra() <= menetidoSzerint.get(i + 1).becsultMenetidoOra());
        }
    }

    private TuraTerv letrehozMintatura() throws TulTerheltJarmuException {
        TuraTerv tura = new TuraTerv("Erdély", "Skoda Octavia", 5, 7.0, 600.0);
        tura.szakaszHozzaadasa(new AutopalyaSzakasz("M5", 200.0, 7.0, 600.0, 6400.0, 125.0));
        tura.szakaszHozzaadasa(new HegyiHagoSzakasz("Transzfogarasi", 80.0, 7.0, 600.0, 1200, 20));
        tura.szakaszHozzaadasa(new VarosiSzakasz("Kolozsvár", 15.0, 7.0, 600.0, 2000.0, 1.5));

        tura.utasHozzaadasa(new Utas("Péter", 200.0));
        tura.utasHozzaadasa(new Utas("Anna", 200.0));
        tura.utasHozzaadasa(new Utas("Gábor", 100.0));
        return tura;
    }
}
