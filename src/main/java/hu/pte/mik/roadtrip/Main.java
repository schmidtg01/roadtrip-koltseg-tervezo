package hu.pte.mik.roadtrip;

import hu.pte.mik.roadtrip.persistence.JsonTuraTarolo;
import hu.pte.mik.roadtrip.service.TuraKezeloService;
import hu.pte.mik.roadtrip.ui.KonzolMenu;

import java.nio.file.Path;

/**
 * Az Autós Túra- és Költségmegosztó Tervező alkalmazás belépési pontja.
 */
public class Main {

    private static final String ALAPERTELMEZETT_ADATFAJL = "turak.json";

    public static void main(String[] args) {
        Path adatFajl = (args.length > 0 && !args[0].isBlank())
                ? Path.of(args[0])
                : Path.of(ALAPERTELMEZETT_ADATFAJL);

        TuraKezeloService turaKezeloService = new TuraKezeloService();
        JsonTuraTarolo jsonTuraTarolo = new JsonTuraTarolo();
        KonzolMenu konzolMenu = new KonzolMenu(turaKezeloService, jsonTuraTarolo, adatFajl);
        konzolMenu.indit();
    }
}
