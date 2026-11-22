package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Objects;

/**
 * Hegyi hágó útvonalszakasz, ahol a szintemelkedés növeli az üzemanyag-fogyasztást,
 * a hajtűkanyarok és az emelkedés pedig csökkentik az átlagsebességet.
 */
public class HegyiHagoSzakasz extends UtvonalSzakasz {

    private static final double FOGYASZTAS_FELAR_100M_ENKENT = 0.03;
    private static final double ALAP_HEGYI_SEBESSEG_KMH = 55.0;
    private static final double KANYAR_SEBESSEG_CSOKKENES_KMH = 0.8;
    private static final double EMELKEDES_SEBESSEG_CSOKKENES_500M = 2.0;
    private static final double MIN_HEGYI_SEBESSEG_KMH = 20.0;

    private final int szintEmelkedesMeter;
    private final int hajtukanyarokSzama;

    /**
     * Létrehoz egy új hegyi hágó szakaszt.
     *
     * @param nev                 a szakasz neve
     * @param tavolsagKm          távolság km-ben (pozitív)
     * @param alapFogyasztas      alapfogyasztás l/100 km-ben (pozitív)
     * @param uzemanyagAr         üzemanyagár Ft/l-ben (pozitív)
     * @param szintEmelkedesMeter összesített szintemelkedés méterben (nem negatív)
     * @param hajtukanyarokSzama  hajtűkanyarok száma (nem negatív)
     */
    @JsonCreator
    public HegyiHagoSzakasz(
            @JsonProperty("nev") String nev,
            @JsonProperty("tavolsagKm") double tavolsagKm,
            @JsonProperty("alapFogyasztas") double alapFogyasztas,
            @JsonProperty("uzemanyagAr") double uzemanyagAr,
            @JsonProperty("szintEmelkedesMeter") int szintEmelkedesMeter,
            @JsonProperty("hajtukanyarokSzama") int hajtukanyarokSzama) {
        super(nev, tavolsagKm, alapFogyasztas, uzemanyagAr);
        if (szintEmelkedesMeter < 0) {
            throw new ErvenytelenAdatException("A szintemelkedés nem lehet negatív.");
        }
        if (hajtukanyarokSzama < 0) {
            throw new ErvenytelenAdatException("A hajtűkanyarok száma nem lehet negatív.");
        }

        this.szintEmelkedesMeter = szintEmelkedesMeter;
        this.hajtukanyarokSzama = hajtukanyarokSzama;
    }

    public int getSzintEmelkedesMeter() {
        return szintEmelkedesMeter;
    }

    public int getHajtukanyarokSzama() {
        return hajtukanyarokSzama;
    }

    /**
     * Kiszámítja a szakaszra jellemző effektív hegyi átlagsebességet km/h-ban.
     *
     * @return becsült átlagsebesség km/h-ban (legalább {@value #MIN_HEGYI_SEBESSEG_KMH} km/h)
     */
    @JsonIgnore
    public double getBecsultAtlagSebessegKmh() {
        double csokkentettSebesseg = ALAP_HEGYI_SEBESSEG_KMH
                - (hajtukanyarokSzama * KANYAR_SEBESSEG_CSOKKENES_KMH)
                - ((szintEmelkedesMeter / 500.0) * EMELKEDES_SEBESSEG_CSOKKENES_500M);
        return Math.max(MIN_HEGYI_SEBESSEG_KMH, csokkentettSebesseg);
    }

    @Override
    public double szamoltKoltseg() {
        double fogyasztasiSzorzo = 1.0 + (szintEmelkedesMeter / 100.0) * FOGYASZTAS_FELAR_100M_ENKENT;
        return alapUzemanyagKoltseg() * fogyasztasiSzorzo;
    }

    @Override
    public double becsultMenetidoOra() {
        return getTavolsagKm() / getBecsultAtlagSebessegKmh();
    }

    @Override
    @JsonIgnore
    public String getSzakaszTipus() {
        return "Hegyi hágó";
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        HegyiHagoSzakasz that = (HegyiHagoSzakasz) o;
        return szintEmelkedesMeter == that.szintEmelkedesMeter
                && hajtukanyarokSzama == that.hajtukanyarokSzama;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), szintEmelkedesMeter, hajtukanyarokSzama);
    }

    @Override
    public String toString() {
        return String.format("%s (szintemelkedés: %d m, hajtűkanyarok: %d db)",
                super.toString(), szintEmelkedesMeter, hajtukanyarokSzama);
    }
}
