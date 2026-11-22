package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Objects;

/**
 * Városi útvonalszakasz, amely parkolási díjjal és a menetidőt növelő dugó faktorral számol.
 */
public class VarosiSzakasz extends UtvonalSzakasz {

    private static final double ALAP_VAROSI_SEBESSEG_KMH = 35.0;

    private final double parkolasiDij;
    private final double dugoFaktor;

    /**
     * Létrehoz egy új városi útvonalszakaszt.
     *
     * @param nev            a szakasz neve
     * @param tavolsagKm     távolság km-ben (pozitív)
     * @param alapFogyasztas alapfogyasztás l/100 km-ben (pozitív)
     * @param uzemanyagAr    üzemanyagár Ft/l-ben (pozitív)
     * @param parkolasiDij   parkolási díj Ft-ban (nem negatív)
     * @param dugoFaktor     a forgalmi torlódást jelző szorzó (legalább 1.0)
     */
    @JsonCreator
    public VarosiSzakasz(
            @JsonProperty("nev") String nev,
            @JsonProperty("tavolsagKm") double tavolsagKm,
            @JsonProperty("alapFogyasztas") double alapFogyasztas,
            @JsonProperty("uzemanyagAr") double uzemanyagAr,
            @JsonProperty("parkolasiDij") double parkolasiDij,
            @JsonProperty("dugoFaktor") double dugoFaktor) {
        super(nev, tavolsagKm, alapFogyasztas, uzemanyagAr);
        ellenorizNemNegativSzam(parkolasiDij, "A parkolási díj nem lehet negatív.");
        if (Double.isNaN(dugoFaktor) || Double.isInfinite(dugoFaktor) || dugoFaktor < 1.0) {
            throw new ErvenytelenAdatException("A dugó faktor nem lehet 1.0-nál kisebb.");
        }

        this.parkolasiDij = parkolasiDij;
        this.dugoFaktor = dugoFaktor;
    }

    public double getParkolasiDij() {
        return parkolasiDij;
    }

    public double getDugoFaktor() {
        return dugoFaktor;
    }

    @Override
    public double szamoltKoltseg() {
        return alapUzemanyagKoltseg() + parkolasiDij;
    }

    @Override
    public double becsultMenetidoOra() {
        return (getTavolsagKm() / ALAP_VAROSI_SEBESSEG_KMH) * dugoFaktor;
    }

    @Override
    public String getSzakaszTipus() {
        return "Városi";
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        VarosiSzakasz that = (VarosiSzakasz) o;
        return Double.compare(that.parkolasiDij, parkolasiDij) == 0
                && Double.compare(that.dugoFaktor, dugoFaktor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parkolasiDij, dugoFaktor);
    }

    @Override
    public String toString() {
        return String.format("%s (parkolás: %.0f Ft, dugó faktor: %.2fx)",
                super.toString(), parkolasiDij, dugoFaktor);
    }
}
