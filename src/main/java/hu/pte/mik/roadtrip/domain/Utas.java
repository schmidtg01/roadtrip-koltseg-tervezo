package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Objects;

/**
 * Az autós túrán részt vevő utast reprezentáló domain osztály.
 */
public class Utas {

    private final String nev;
    private double megtettKm;

    /**
     * Létrehoz egy új utast érvényesített adatokkal.
     *
     * @param nev       az utas neve (nem lehet üres)
     * @param megtettKm az utas által a túrán megtett távolság kilométerben (nem lehet negatív)
     */
    @JsonCreator
    public Utas(
            @JsonProperty("nev") String nev,
            @JsonProperty("megtettKm") double megtettKm) {
        if (nev == null || nev.isBlank()) {
            throw new ErvenytelenAdatException("Az utas neve nem lehet üres.");
        }
        ellenorizMegtettKm(megtettKm);
        this.nev = nev.trim();
        this.megtettKm = megtettKm;
    }

    /**
     * Kényelmi konstruktor 0 km kezdeti megtett távolsággal.
     *
     * @param nev az utas neve (nem lehet üres)
     */
    public Utas(String nev) {
        this(nev, 0.0);
    }

    private static void ellenorizMegtettKm(double megtettKm) {
        if (Double.isNaN(megtettKm) || Double.isInfinite(megtettKm) || megtettKm < 0.0) {
            throw new ErvenytelenAdatException("A megtett távolság nem lehet negatív vagy érvénytelen szám.");
        }
    }

    public String getNev() {
        return nev;
    }

    public double getMegtettKm() {
        return megtettKm;
    }

    public void setMegtettKm(double megtettKm) {
        ellenorizMegtettKm(megtettKm);
        this.megtettKm = megtettKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Utas that)) {
            return false;
        }
        return Double.compare(that.megtettKm, megtettKm) == 0
                && Objects.equals(nev, that.nev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nev, megtettKm);
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f km)", nev, megtettKm);
    }
}
