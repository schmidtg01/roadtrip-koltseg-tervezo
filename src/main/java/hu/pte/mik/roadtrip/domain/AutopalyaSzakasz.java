package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Autópályás útvonalszakasz, amely a nagyobb tempó miatt +15% fogyasztási felárral
 * és opcionális matricadíjjal számol.
 */
public class AutopalyaSzakasz extends UtvonalSzakasz {

    private static final double FOGYASZTASI_SZORZO = 1.15;

    private final double matricaDij;
    private final double atlagSebesseg;

    /**
     * Létrehoz egy új autópálya-szakaszt.
     *
     * @param nev            a szakasz neve
     * @param tavolsagKm     távolság km-ben (pozitív)
     * @param alapFogyasztas alapfogyasztás l/100 km-ben (pozitív)
     * @param uzemanyagAr    üzemanyagár Ft/l-ben (pozitív)
     * @param matricaDij     autópálya-matrica díja Ft-ban (nem negatív)
     * @param atlagSebesseg  átlagsebesség km/h-ban (pozitív)
     */
    @JsonCreator
    public AutopalyaSzakasz(
            @JsonProperty("nev") String nev,
            @JsonProperty("tavolsagKm") double tavolsagKm,
            @JsonProperty("alapFogyasztas") double alapFogyasztas,
            @JsonProperty("uzemanyagAr") double uzemanyagAr,
            @JsonProperty("matricaDij") double matricaDij,
            @JsonProperty("atlagSebesseg") double atlagSebesseg) {
        super(nev, tavolsagKm, alapFogyasztas, uzemanyagAr);
        ellenorizNemNegativSzam(matricaDij, "A matricadíj nem lehet negatív.");
        ellenorizPozitivSzam(atlagSebesseg, "Az átlagsebességnek pozitív számnak kell lennie.");

        this.matricaDij = matricaDij;
        this.atlagSebesseg = atlagSebesseg;
    }

    public double getMatricaDij() {
        return matricaDij;
    }

    public double getAtlagSebesseg() {
        return atlagSebesseg;
    }

    @Override
    public double szamoltKoltseg() {
        return alapUzemanyagKoltseg() * FOGYASZTASI_SZORZO + matricaDij;
    }

    @Override
    public double becsultMenetidoOra() {
        return getTavolsagKm() / atlagSebesseg;
    }

    @Override
    @JsonIgnore
    public String getSzakaszTipus() {
        return "Autópálya";
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        AutopalyaSzakasz that = (AutopalyaSzakasz) o;
        return Double.compare(that.matricaDij, matricaDij) == 0
                && Double.compare(that.atlagSebesseg, atlagSebesseg) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), matricaDij, atlagSebesseg);
    }

    @Override
    public String toString() {
        return String.format("%s (matrica: %.0f Ft, átlagsebesség: %.0f km/h)",
                super.toString(), matricaDij, atlagSebesseg);
    }
}
