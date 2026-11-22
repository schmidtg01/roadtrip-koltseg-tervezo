package hu.pte.mik.roadtrip.service;

import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.domain.UtvonalSzakasz;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A túratervek üzleti műveleteit összefogó szolgáltatásosztály.
 * A Strategy minta kontextusaként kezeli a költségmegosztási stratégiát,
 * valamint különböző szempontok szerinti szakaszrendezést biztosít.
 */
public class TuraKezeloService {

    private KoltsegOsztasiStrategia strategia;

    /**
     * Létrehozza a szolgáltatást az alapértelmezett egyenlő költségmegosztási stratégiával.
     */
    public TuraKezeloService() {
        this(new EgyenloElosztasStrategia());
    }

    /**
     * Létrehozza a szolgáltatást a megadott kezdeti költségmegosztási stratégiával.
     *
     * @param kezdetiStrategia a kezdetben aktív stratégia (nem lehet null)
     */
    public TuraKezeloService(KoltsegOsztasiStrategia kezdetiStrategia) {
        setStrategia(kezdetiStrategia);
    }

    /**
     * Visszaadja az aktuálisan beállított költségmegosztási stratégiát.
     *
     * @return az aktív stratégia
     */
    public KoltsegOsztasiStrategia getStrategia() {
        return strategia;
    }

    /**
     * Futásidőben lecseréli az aktuális költségmegosztási stratégiát.
     *
     * @param strategia az új költségmegosztási stratégia (nem lehet null)
     */
    public void setStrategia(KoltsegOsztasiStrategia strategia) {
        if (strategia == null) {
            throw new ErvenytelenAdatException("A költségosztási stratégia nem lehet null.");
        }
        this.strategia = strategia;
    }

    /**
     * Kiszámítja az utasonkénti költségelszámolást az aktuálisan beállított stratégia alapján.
     *
     * @param tura a vizsgált túraterv
     * @return utasonkénti fizetendő összeg Ft-ban
     */
    public Map<Utas, Double> koltsegElszamolas(TuraTerv tura) {
        if (tura == null) {
            throw new ErvenytelenAdatException("A túraterv nem lehet null.");
        }
        return strategia.elosztKoltseg(tura);
    }

    /**
     * A túra útvonalszakaszait növekvő sorrendbe rendezi a számított költségük alapján.
     *
     * @param tura a vizsgált túraterv
     * @return költség szerint növekvő sorrendbe rendezett szakaszlista
     */
    public List<UtvonalSzakasz> rendezSzakaszokKoltsegSzerint(TuraTerv tura) {
        return rendezSzakaszokKoltsegSzerint(tura, false);
    }

    /**
     * A túra útvonalszakaszait lambda kifejezéssel rendezi a számított költségük alapján.
     *
     * @param tura     a vizsgált túraterv
     * @param csokkeno {@code true} esetén csökkenő, egyébként növekvő sorrend
     * @return költség szerint rendezett szakaszlista
     */
    public List<UtvonalSzakasz> rendezSzakaszokKoltsegSzerint(TuraTerv tura, boolean csokkeno) {
        ellenorizTura(tura);
        List<UtvonalSzakasz> rendezett = new ArrayList<>(tura.getSzakaszok());
        rendezett.sort((s1, s2) -> csokkeno
                ? Double.compare(s2.szamoltKoltseg(), s1.szamoltKoltseg())
                : Double.compare(s1.szamoltKoltseg(), s2.szamoltKoltseg()));
        return Collections.unmodifiableList(rendezett);
    }

    /**
     * A túra útvonalszakaszait növekvő sorrendbe rendezi a becsült menetidejük alapján.
     *
     * @param tura a vizsgált túraterv
     * @return menetidő szerint növekvő sorrendbe rendezett szakaszlista
     */
    public List<UtvonalSzakasz> rendezSzakaszokMenetidoSzerint(TuraTerv tura) {
        return rendezSzakaszokMenetidoSzerint(tura, false);
    }

    /**
     * A túra útvonalszakaszait lambda kifejezéssel rendezi a becsült menetidejük alapján.
     *
     * @param tura     a vizsgált túraterv
     * @param csokkeno {@code true} esetén csökkenő, egyébként növekvő sorrend
     * @return menetidő szerint rendezett szakaszlista
     */
    public List<UtvonalSzakasz> rendezSzakaszokMenetidoSzerint(TuraTerv tura, boolean csokkeno) {
        ellenorizTura(tura);
        List<UtvonalSzakasz> rendezett = new ArrayList<>(tura.getSzakaszok());
        rendezett.sort((s1, s2) -> csokkeno
                ? Double.compare(s2.becsultMenetidoOra(), s1.becsultMenetidoOra())
                : Double.compare(s1.becsultMenetidoOra(), s2.becsultMenetidoOra()));
        return Collections.unmodifiableList(rendezett);
    }

    /**
     * A túra útvonalszakaszait távolság szerint növekvő sorrendben adja vissza
     * az {@link UtvonalSzakasz#compareTo(UtvonalSzakasz)} természetes rendezése alapján.
     *
     * @param tura a vizsgált túraterv
     * @return távolság szerint rendezett szakaszlista
     */
    public List<UtvonalSzakasz> rendezSzakaszokTavolsagSzerint(TuraTerv tura) {
        ellenorizTura(tura);
        return tura.getSzakaszokTavolsagSzerintRendezve();
    }

    private static void ellenorizTura(TuraTerv tura) {
        if (tura == null) {
            throw new ErvenytelenAdatException("A túraterv nem lehet null.");
        }
    }
}
