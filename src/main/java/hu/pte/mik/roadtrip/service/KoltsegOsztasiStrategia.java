package hu.pte.mik.roadtrip.service;

import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.domain.Utas;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Map;

/**
 * A Strategy tervezési minta közös interfésze, amely különböző költségmegosztási
 * elszámolási módok futásidejű cseréjét teszi lehetővé.
 */
public interface KoltsegOsztasiStrategia {

    /**
     * Kiszámítja, hogy az adott túraterv teljes költségéből melyik utasnak
     * mekkora összeget kell fizetnie Ft-ban.
     *
     * @param tura a vizsgált túraterv
     * @return utasonkénti fizetendő összeg Ft-ban
     * @throws ErvenytelenAdatException ha a túraterv érvénytelen vagy a stratégia nem alkalmazható
     */
    Map<Utas, Double> elosztKoltseg(TuraTerv tura) throws ErvenytelenAdatException;

    /**
     * Visszaadja a stratégia ember által olvasható magyar megnevezését.
     *
     * @return a stratégia neve
     */
    String getNev();
}
