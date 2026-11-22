package hu.pte.mik.roadtrip.exception;

/**
 * Érvénytelen tartománybeli (domain) adat vagy bemeneti paraméter esetén dobott kivétel.
 */
public class ErvenytelenAdatException extends RuntimeException {

    public ErvenytelenAdatException(String uzenet) {
        super(uzenet);
    }

    public ErvenytelenAdatException(String uzenet, Throwable ok) {
        super(uzenet, ok);
    }
}
