package hu.pte.mik.roadtrip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;
import hu.pte.mik.roadtrip.exception.TulTerheltJarmuException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Egy teljes autós túra tervét összefogó domain osztály, amely kezeli a jármű adatait,
 * az útvonalszakaszokat és az utaslistát a férőhelykorlát betartásával.
 */
public class TuraTerv {

    private final String turaNev;
    private final String jarmuNev;
    private final int ferohelyekSzama;
    private final double alapFogyasztas;
    private final double uzemanyagAr;
    private final List<UtvonalSzakasz> szakaszok;
    private final List<Utas> utasok;

    /**
     * Létrehoz egy új, üres túratervet érvényesített alapadatokkal.
     *
     * @param turaNev         a túra neve (nem lehet üres)
     * @param jarmuNev        a jármű megnevezése (nem lehet üres)
     * @param ferohelyekSzama a jármű férőhelyeinek száma (legalább 1)
     * @param alapFogyasztas  a jármű alapfogyasztása l/100 km-ben (pozitív)
     * @param uzemanyagAr     az üzemanyag ára Ft/l-ben (pozitív)
     */
    public TuraTerv(
            String turaNev,
            String jarmuNev,
            int ferohelyekSzama,
            double alapFogyasztas,
            double uzemanyagAr) {
        if (turaNev == null || turaNev.isBlank()) {
            throw new ErvenytelenAdatException("A túra neve nem lehet üres.");
        }
        if (jarmuNev == null || jarmuNev.isBlank()) {
            throw new ErvenytelenAdatException("A jármű neve nem lehet üres.");
        }
        if (ferohelyekSzama < 1) {
            throw new ErvenytelenAdatException("A férőhelyek számának legalább 1-nek kell lennie.");
        }
        if (Double.isNaN(alapFogyasztas) || Double.isInfinite(alapFogyasztas) || alapFogyasztas <= 0.0) {
            throw new ErvenytelenAdatException("Az alapfogyasztásnak pozitív számnak kell lennie.");
        }
        if (Double.isNaN(uzemanyagAr) || Double.isInfinite(uzemanyagAr) || uzemanyagAr <= 0.0) {
            throw new ErvenytelenAdatException("Az üzemanyagárnak pozitív számnak kell lennie.");
        }

        this.turaNev = turaNev.trim();
        this.jarmuNev = jarmuNev.trim();
        this.ferohelyekSzama = ferohelyekSzama;
        this.alapFogyasztas = alapFogyasztas;
        this.uzemanyagAr = uzemanyagAr;
        this.szakaszok = new ArrayList<>();
        this.utasok = new ArrayList<>();
    }

    /**
     * Konstruktor teljes állapot visszaállításához (pl. JSON deszerializáció során).
     *
     * @throws TulTerheltJarmuException ha a megadott utaslista meghaladja a férőhelyek számát
     */
    @JsonCreator
    public TuraTerv(
            @JsonProperty("turaNev") String turaNev,
            @JsonProperty("jarmuNev") String jarmuNev,
            @JsonProperty("ferohelyekSzama") int ferohelyekSzama,
            @JsonProperty("alapFogyasztas") double alapFogyasztas,
            @JsonProperty("uzemanyagAr") double uzemanyagAr,
            @JsonProperty("szakaszok") List<UtvonalSzakasz> szakaszok,
            @JsonProperty("utasok") List<Utas> utasok) throws TulTerheltJarmuException {
        this(turaNev, jarmuNev, ferohelyekSzama, alapFogyasztas, uzemanyagAr);
        if (szakaszok != null) {
            for (UtvonalSzakasz szakasz : szakaszok) {
                szakaszHozzaadasa(szakasz);
            }
        }
        if (utasok != null) {
            for (Utas utas : utasok) {
                utasHozzaadasa(utas);
            }
        }
    }

    /**
     * Hozzáad egy útvonalszakaszt a túratervhez.
     *
     * @param szakasz a hozzáadandó szakasz (nem lehet null)
     */
    public void szakaszHozzaadasa(UtvonalSzakasz szakasz) {
        if (szakasz == null) {
            throw new ErvenytelenAdatException("A hozzáadandó útvonalszakasz nem lehet null.");
        }
        szakaszok.add(szakasz);
    }

    /**
     * Eltávolít egy útvonalszakaszt a túratervből.
     *
     * @param szakasz az eltávolítandó szakasz
     * @return {@code true}, ha a szakasz szerepelt a listában és eltávolításra került
     */
    public boolean szakaszEltavolitasa(UtvonalSzakasz szakasz) {
        return szakaszok.remove(szakasz);
    }

    /**
     * Hozzáad egy utast a túratervhez, amennyiben van még szabad férőhely a járműben.
     *
     * @param utas a hozzáadandó utas (nem lehet null)
     * @throws TulTerheltJarmuException ha a jármű összes férőhelye betelt
     */
    public void utasHozzaadasa(Utas utas) throws TulTerheltJarmuException {
        if (utas == null) {
            throw new ErvenytelenAdatException("A hozzáadandó utas nem lehet null.");
        }
        if (utasok.size() >= ferohelyekSzama) {
            throw new TulTerheltJarmuException(String.format(
                    "A(z) %s jármű férőhelyei (%d fő) beteltek, %s már nem fér be.",
                    jarmuNev, ferohelyekSzama, utas.getNev()));
        }
        utasok.add(utas);
    }

    /**
     * Eltávolít egy utast a túratervből.
     *
     * @param utas az eltávolítandó utas
     * @return {@code true}, ha az utas szerepelt a listában és eltávolításra került
     */
    public boolean utasEltavolitasa(Utas utas) {
        return utasok.remove(utas);
    }

    public String getTuraNev() {
        return turaNev;
    }

    public String getJarmuNev() {
        return jarmuNev;
    }

    public int getFerohelyekSzama() {
        return ferohelyekSzama;
    }

    public double getAlapFogyasztas() {
        return alapFogyasztas;
    }

    public double getUzemanyagAr() {
        return uzemanyagAr;
    }

    public List<UtvonalSzakasz> getSzakaszok() {
        return Collections.unmodifiableList(szakaszok);
    }

    public List<Utas> getUtasok() {
        return Collections.unmodifiableList(utasok);
    }

    @JsonIgnore
    public int getSzabadFerohelyekSzama() {
        return ferohelyekSzama - utasok.size();
    }

    @JsonIgnore
    public double osszesTavolsagKm() {
        return szakaszok.stream()
                .mapToDouble(UtvonalSzakasz::getTavolsagKm)
                .sum();
    }

    @JsonIgnore
    public double osszesKoltseg() {
        return szakaszok.stream()
                .mapToDouble(UtvonalSzakasz::szamoltKoltseg)
                .sum();
    }

    @JsonIgnore
    public double osszesMenetidoOra() {
        return szakaszok.stream()
                .mapToDouble(UtvonalSzakasz::becsultMenetidoOra)
                .sum();
    }

    /**
     * Visszaadja az útvonalszakaszokat távolság szerint növekvő sorrendben rendezve.
     *
     * @return rendezett szakaszlista
     */
    @JsonIgnore
    public List<UtvonalSzakasz> getSzakaszokTavolsagSzerintRendezve() {
        List<UtvonalSzakasz> rendezett = new ArrayList<>(szakaszok);
        Collections.sort(rendezett);
        return Collections.unmodifiableList(rendezett);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TuraTerv that)) {
            return false;
        }
        return ferohelyekSzama == that.ferohelyekSzama
                && Double.compare(that.alapFogyasztas, alapFogyasztas) == 0
                && Double.compare(that.uzemanyagAr, uzemanyagAr) == 0
                && Objects.equals(turaNev, that.turaNev)
                && Objects.equals(jarmuNev, that.jarmuNev)
                && Objects.equals(szakaszok, that.szakaszok)
                && Objects.equals(utasok, that.utasok);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turaNev, jarmuNev, ferohelyekSzama, alapFogyasztas, uzemanyagAr, szakaszok, utasok);
    }

    @Override
    public String toString() {
        return String.format("Túra: %s | Jármű: %s (%d/%d fő) | Szakaszok: %d db (%.1f km, %.0f Ft, %.2f óra)",
                turaNev, jarmuNev, utasok.size(), ferohelyekSzama,
                szakaszok.size(), osszesTavolsagKm(), osszesKoltseg(), osszesMenetidoOra());
    }
}
