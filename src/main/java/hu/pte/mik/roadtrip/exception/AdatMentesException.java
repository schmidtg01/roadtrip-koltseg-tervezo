package hu.pte.mik.roadtrip.exception;

/**
 * Adatmentési vagy adatbetöltési (I/O, illetve JSON szerializációs/deszerializációs)
 * hiba esetén dobott ellenőrzött kivétel.
 */
public class AdatMentesException extends Exception {

    public AdatMentesException(String uzenet) {
        super(uzenet);
    }

    public AdatMentesException(String uzenet, Throwable ok) {
        super(uzenet, ok);
    }
}
