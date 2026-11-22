package hu.pte.mik.roadtrip.exception;

/**
 * Akkor dobódik, ha egy túraterv járművébe a rendelkezésre álló férőhelyeknél
 * több utast próbálnak hozzáadni.
 */
public class TulTerheltJarmuException extends Exception {

    public TulTerheltJarmuException(String uzenet) {
        super(uzenet);
    }

    public TulTerheltJarmuException(String uzenet, Throwable ok) {
        super(uzenet, ok);
    }
}
