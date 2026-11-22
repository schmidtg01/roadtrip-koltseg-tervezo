package hu.pte.mik.roadtrip.service;

import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Költségmegosztási stratégia, amely a túra teljes költségét az egyes utasok által
 * megtett kilométerek arányában osztja szét.
 */
public class TavolsagAranyosStrategia implements KoltsegOsztasiStrategia {

    @Override
    public Map<Utas, Double> elosztKoltseg(TuraTerv tura) throws ErvenytelenAdatException {
        if (tura == null) {
            throw new ErvenytelenAdatException("A túraterv nem lehet null.");
        }
        List<Utas> utasok = tura.getUtasok();
        if (utasok.isEmpty()) {
            throw new ErvenytelenAdatException("A távolságarányos költségelosztáshoz legalább egy utas szükséges a túrában.");
        }

        double osszesUtasKm = utasok.stream()
                .mapToDouble(Utas::getMegtettKm)
                .sum();
        if (osszesUtasKm <= 0.0) {
            throw new ErvenytelenAdatException(
                    "A távolságarányos költségelosztáshoz az utasok összes megtett kilométerének 0-nál nagyobbnak kell lennie.");
        }

        double osszesKoltseg = tura.osszesKoltseg();
        Map<Utas, Double> elszamolas = new LinkedHashMap<>();
        for (Utas utas : utasok) {
            double reszesedes = osszesKoltseg * (utas.getMegtettKm() / osszesUtasKm);
            elszamolas.put(utas, reszesedes);
        }
        return Collections.unmodifiableMap(elszamolas);
    }

    @Override
    public String getNev() {
        return "Távolságarányos elosztás";
    }
}
