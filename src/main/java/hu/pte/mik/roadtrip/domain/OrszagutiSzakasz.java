package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Objects;

/**
 * Normál országúti útvonalszakasz, amely matrica- és parkolási díj nélkül,
 * az alap üzemanyagköltség forgalmi szorzóval módosított értékével és
 * megadható átlagsebességgel (alapértelmezetten 90 km/h) számol.
 */
public class OrszagutiSzakasz extends UtvonalSzakasz {

    public static final double ALAP_ORSZAGUTI_SEBESSEG_KMH = 90.0;
    public static final double ALAP_FORGALMI_SZORZO = 1.0;
    public static final double MIN_FORGALMI_SZORZO = 1.0;
    public static final double MAX_FORGALMI_SZORZO = 1.3;

    private final double atlagSebesseg;
    private final double forgalmiSzorzo;

    /**
     * Létrehoz egy új normál országúti szakaszt.
     *
     * @param nev            a szakasz neve
     * @param tavolsagKm     távolság km-ben (pozitív)
     * @param alapFogyasztas alapfogyasztás l/100 km-ben (pozitív)
     * @param uzemanyagAr    üzemanyagár Ft/l-ben (pozitív)
     * @param atlagSebesseg  átlagsebesség km/h-ban (pozitív)
     * @param forgalmiSzorzo forgalmi fogyasztási szorzó (1.0 és 1.3 között)
     */
    @JsonCreator
    public OrszagutiSzakasz(
            @JsonProperty("nev") String nev,
            @JsonProperty("tavolsagKm") double tavolsagKm,
            @JsonProperty("alapFogyasztas") double alapFogyasztas,
            @JsonProperty("uzemanyagAr") double uzemanyagAr,
            @JsonProperty("atlagSebesseg") double atlagSebesseg,
            @JsonProperty("forgalmiSzorzo") double forgalmiSzorzo) {
        super(nev, tavolsagKm, alapFogyasztas, uzemanyagAr);
        ellenorizPozitivSzam(atlagSebesseg, "Az átlagsebességnek pozitív számnak kell lennie.");
        if (Double.isNaN(forgalmiSzorzo)
                || Double.isInfinite(forgalmiSzorzo)
                || forgalmiSzorzo < MIN_FORGALMI_SZORZO
                || forgalmiSzorzo > MAX_FORGALMI_SZORZO) {
            throw new ErvenytelenAdatException("A forgalmi szorzónak 1.0 és 1.3 között kell lennie.");
        }

        this.atlagSebesseg = atlagSebesseg;
        this.forgalmiSzorzo = forgalmiSzorzo;
    }

    /**
     * Kényelmi konstruktor alapértelmezett 90 km/h átlagsebességgel és 1.0 forgalmi szorzóval.
     */
    public OrszagutiSzakasz(String nev, double tavolsagKm, double alapFogyasztas, double uzemanyagAr) {
        this(nev, tavolsagKm, alapFogyasztas, uzemanyagAr, ALAP_ORSZAGUTI_SEBESSEG_KMH, ALAP_FORGALMI_SZORZO);
    }

    public double getAtlagSebesseg() {
        return atlagSebesseg;
    }

    public double getForgalmiSzorzo() {
        return forgalmiSzorzo;
    }

    @Override
    public double szamoltKoltseg() {
        return alapUzemanyagKoltseg() * forgalmiSzorzo;
    }

    @Override
    public double becsultMenetidoOra() {
        return getTavolsagKm() / atlagSebesseg;
    }

    @Override
    @JsonIgnore
    public String getSzakaszTipus() {
        return "Országúti";
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        OrszagutiSzakasz that = (OrszagutiSzakasz) o;
        return Double.compare(that.atlagSebesseg, atlagSebesseg) == 0
                && Double.compare(that.forgalmiSzorzo, forgalmiSzorzo) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), atlagSebesseg, forgalmiSzorzo);
    }

    @Override
    public String toString() {
        return String.format("%s (átlagsebesség: %.0f km/h, forgalmi szorzó: %.2fx)",
                super.toString(), atlagSebesseg, forgalmiSzorzo);
    }
}
