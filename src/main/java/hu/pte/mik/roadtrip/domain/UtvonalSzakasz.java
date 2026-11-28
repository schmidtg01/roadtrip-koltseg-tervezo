package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.util.Objects;

/**
 * Egy autós túra egyetlen útvonalszakaszát modellező absztrakt ősosztály.
 * A szakaszok természetes rendezése a távolságuk alapján történik.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipus"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AutopalyaSzakasz.class, name = "AUTOPALYA"),
        @JsonSubTypes.Type(value = HegyiHagoSzakasz.class, name = "HEGYI_HAGO"),
        @JsonSubTypes.Type(value = VarosiSzakasz.class, name = "VAROSI"),
        @JsonSubTypes.Type(value = OrszagutiSzakasz.class, name = "ORSZAGUTI")
})
public abstract class UtvonalSzakasz implements Comparable<UtvonalSzakasz> {

    private final String nev;
    private final double tavolsagKm;
    private final double alapFogyasztas;
    private final double uzemanyagAr;

    /**
     * Létrehoz egy érvényesített alapadatokkal rendelkező útvonalszakaszt.
     *
     * @param nev            a szakasz megnevezése (nem lehet üres)
     * @param tavolsagKm     a szakasz hossza kilométerben (pozitív)
     * @param alapFogyasztas a jármű alapfogyasztása liter/100 km egységben (pozitív)
     * @param uzemanyagAr    az üzemanyag egységára Ft/liter egységben (pozitív)
     */
    protected UtvonalSzakasz(String nev, double tavolsagKm, double alapFogyasztas, double uzemanyagAr) {
        if (nev == null || nev.isBlank()) {
            throw new ErvenytelenAdatException("A szakasz neve nem lehet üres.");
        }
        ellenorizPozitivSzam(tavolsagKm, "A szakasz távolságának pozitív számnak kell lennie.");
        ellenorizPozitivSzam(alapFogyasztas, "Az alapfogyasztásnak pozitív számnak kell lennie.");
        ellenorizPozitivSzam(uzemanyagAr, "Az üzemanyagárnak pozitív számnak kell lennie.");

        this.nev = nev.trim();
        this.tavolsagKm = tavolsagKm;
        this.alapFogyasztas = alapFogyasztas;
        this.uzemanyagAr = uzemanyagAr;
    }

    protected static void ellenorizPozitivSzam(double ertek, String hibauzenet) {
        if (Double.isNaN(ertek) || Double.isInfinite(ertek) || ertek <= 0.0) {
            throw new ErvenytelenAdatException(hibauzenet);
        }
    }

    protected static void ellenorizNemNegativSzam(double ertek, String hibauzenet) {
        if (Double.isNaN(ertek) || Double.isInfinite(ertek) || ertek < 0.0) {
            throw new ErvenytelenAdatException(hibauzenet);
        }
    }

    /**
     * Kiszámítja a szakasz alap üzemanyagköltségét extra felárak és járulékos díjak nélkül.
     *
     * @return alap üzemanyagköltség Ft-ban
     */
    protected double alapUzemanyagKoltseg() {
        return (tavolsagKm / 100.0) * alapFogyasztas * uzemanyagAr;
    }

    /**
     * Kiszámítja a szakasz teljes becsült költségét Ft-ban.
     *
     * @return a szakasz teljes költsége Ft-ban
     */
    public abstract double szamoltKoltseg();

    /**
     * Visszaadja a szakaszhoz kapcsolódó extra útdíjakat (pl. matrica- vagy parkolási díj) Ft-ban.
     * Alapértelmezés szerint 0 Ft.
     *
     * @return extra díjak összege Ft-ban
     */
    @JsonIgnore
    public double extraDijKoltseg() {
        return 0.0;
    }

    /**
     * Visszaadja a szakasz üzemanyagköltségét az extra díjak (matrica, parkolás) nélkül, Ft-ban.
     *
     * @return üzemanyagköltség Ft-ban
     */
    @JsonIgnore
    public double uzemanyagKoltseg() {
        return szamoltKoltseg() - extraDijKoltseg();
    }

    /**
     * Kiszámítja a szakasz becsült menetidejét órában.
     *
     * @return a szakasz becsült menetideje órában
     */
    public abstract double becsultMenetidoOra();

    /**
     * Visszaadja a szakasz típusának ember által olvasható magyar megnevezését.
     *
     * @return a szakasztípus megnevezése
     */
    @JsonIgnore
    public abstract String getSzakaszTipus();

    public String getNev() {
        return nev;
    }

    public double getTavolsagKm() {
        return tavolsagKm;
    }

    public double getAlapFogyasztas() {
        return alapFogyasztas;
    }

    public double getUzemanyagAr() {
        return uzemanyagAr;
    }

    @Override
    public int compareTo(UtvonalSzakasz masik) {
        Objects.requireNonNull(masik, "Az összehasonlítandó útvonalszakasz nem lehet null.");
        return Double.compare(this.tavolsagKm, masik.tavolsagKm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UtvonalSzakasz that = (UtvonalSzakasz) o;
        return Double.compare(that.tavolsagKm, tavolsagKm) == 0
                && Double.compare(that.alapFogyasztas, alapFogyasztas) == 0
                && Double.compare(that.uzemanyagAr, uzemanyagAr) == 0
                && Objects.equals(nev, that.nev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), nev, tavolsagKm, alapFogyasztas, uzemanyagAr);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %.1f km | Költség: %.0f Ft | Menetidő: %.2f óra",
                getSzakaszTipus(), nev, tavolsagKm, szamoltKoltseg(), becsultMenetidoOra());
    }
}
