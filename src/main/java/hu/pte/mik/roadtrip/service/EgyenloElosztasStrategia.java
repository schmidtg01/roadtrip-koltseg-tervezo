package hu.pte.mik.roadtrip.service;

import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Költségmegosztási stratégia, amely a túra teljes költségét egyenlő arányban
 * osztja el a részt vevő utasok között.
 */
public class EgyenloElosztasStrategia implements KoltsegOsztasiStrategia {

    @Override
    public Map<Utas, Double> elosztKoltseg(TuraTerv tura) throws ErvenytelenAdatException {
        if (tura == null) {
            throw new ErvenytelenAdatException("A túraterv nem lehet null.");
        }
        List<Utas> utasok = tura.getUtasok();
        if (utasok.isEmpty()) {
            throw new ErvenytelenAdatException("Az egyenlő költségelosztáshoz legalább egy utas szükséges a túrában.");
        }

        double osszesKoltseg = tura.osszesKoltseg();
        double fejenkentiKoltseg = osszesKoltseg / utasok.size();

        Map<Utas, Double> elszamolas = new LinkedHashMap<>();
        for (Utas utas : utasok) {
            elszamolas.put(utas, fejenkentiKoltseg);
        }
        return Collections.unmodifiableMap(elszamolas);
    }

    @Override
    public String getNev() {
        return "Egyenlő elosztás";
    }
}
