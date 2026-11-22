package hu.pte.mik.roadtrip.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import hu.pte.mik.roadtrip.domain.TuraTerv;
import hu.pte.mik.roadtrip.exception.AdatMentesException;
import hu.pte.mik.roadtrip.exception.ErvenytelenAdatException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Túratervek listájának JSON formátumú mentéséért és betöltéséért felelős perzisztencia osztály.
 */
public class JsonTuraTarolo {

    private final ObjectMapper objectMapper;

    /**
     * Létrehoz egy alapértelmezett, szépen formázott JSON kimenetet előállító tárolót.
     */
    public JsonTuraTarolo() {
        this(letrehozAlapertelmezettMapper());
    }

    /**
     * Létrehoz egy tárolót a megadott {@link ObjectMapper} példánnyal.
     *
     * @param objectMapper a szerializáláshoz használt Jackson mapper (nem lehet null)
     */
    public JsonTuraTarolo(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new ErvenytelenAdatException("Az ObjectMapper nem lehet null.");
        }
        this.objectMapper = objectMapper;
    }

    private static ObjectMapper letrehozAlapertelmezettMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /**
     * Elmenti a túratervek listáját JSON formátumban a megadott fájlútvonalra.
     *
     * @param turak       a mentendő túratervek listája (nem lehet null)
     * @param fajlUtvonal a célfájl elérési útvonala (nem lehet null vagy üres)
     * @throws AdatMentesException I/O vagy szerializációs hiba esetén
     */
    public void ment(List<TuraTerv> turak, String fajlUtvonal) throws AdatMentesException {
        ment(turak, ervenyesitUtvonal(fajlUtvonal));
    }

    /**
     * Elmenti a túratervek listáját JSON formátumban a megadott {@link Path} elérési útvonalra.
     *
     * @param turak       a mentendő túratervek listája (nem lehet null)
     * @param fajlUtvonal a célfájl elérési útvonala (nem lehet null)
     * @throws AdatMentesException I/O vagy szerializációs hiba esetén
     */
    public void ment(List<TuraTerv> turak, Path fajlUtvonal) throws AdatMentesException {
        if (turak == null) {
            throw new ErvenytelenAdatException("A mentendő túralista nem lehet null.");
        }
        if (fajlUtvonal == null) {
            throw new ErvenytelenAdatException("A mentési fájl elérési útvonala nem lehet null.");
        }

        try {
            Path szuloMappa = fajlUtvonal.toAbsolutePath().getParent();
            if (szuloMappa != null && !Files.exists(szuloMappa)) {
                Files.createDirectories(szuloMappa);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(fajlUtvonal, StandardCharsets.UTF_8)) {
                objectMapper.writeValue(writer, turak);
            }
        } catch (IOException e) {
            throw new AdatMentesException(
                    "Nem sikerült elmenteni a túraterveket a következő fájlba: " + fajlUtvonal, e);
        }
    }

    /**
     * Betölti a túratervek listáját a megadott JSON fájlból.
     *
     * @param fajlUtvonal a forrásfájl elérési útvonala (nem lehet null vagy üres)
     * @return a betöltött túratervek módosítható listája
     * @throws AdatMentesException ha a fájl nem létezik, nem olvasható, vagy hibás a JSON tartalom
     */
    public List<TuraTerv> betolt(String fajlUtvonal) throws AdatMentesException {
        return betolt(ervenyesitUtvonal(fajlUtvonal));
    }

    /**
     * Betölti a túratervek listáját a megadott {@link Path} JSON fájlból.
     *
     * @param fajlUtvonal a forrásfájl elérési útvonala (nem lehet null)
     * @return a betöltött túratervek módosítható listája
     * @throws AdatMentesException ha a fájl nem létezik, nem olvasható, vagy hibás a JSON tartalom
     */
    public List<TuraTerv> betolt(Path fajlUtvonal) throws AdatMentesException {
        if (fajlUtvonal == null) {
            throw new ErvenytelenAdatException("A betöltendő fájl elérési útvonala nem lehet null.");
        }
        if (!Files.exists(fajlUtvonal)) {
            throw new AdatMentesException("A megadott adatfájl nem található: " + fajlUtvonal);
        }

        try (BufferedReader reader = Files.newBufferedReader(fajlUtvonal, StandardCharsets.UTF_8)) {
            List<TuraTerv> betoltott = objectMapper.readValue(reader, new TypeReference<List<TuraTerv>>() {
            });
            return betoltott != null ? new ArrayList<>(betoltott) : new ArrayList<>();
        } catch (IOException e) {
            throw new AdatMentesException(
                    "Nem sikerült betölteni a túraterveket a következő fájlból: " + fajlUtvonal, e);
        }
    }

    /**
     * Betölti a túraterveket, ha a fájl létezik; nem létező fájl esetén üres listát ad vissza.
     *
     * @param fajlUtvonal a forrásfájl elérési útvonala
     * @return a betöltött túratervek listája vagy üres lista
     * @throws AdatMentesException I/O vagy hibás JSON formátum esetén
     */
    public List<TuraTerv> betoltVagyUres(Path fajlUtvonal) throws AdatMentesException {
        if (fajlUtvonal == null) {
            throw new ErvenytelenAdatException("A betöltendő fájl elérési útvonala nem lehet null.");
        }
        if (!Files.exists(fajlUtvonal)) {
            return new ArrayList<>();
        }
        return betolt(fajlUtvonal);
    }

    private static Path ervenyesitUtvonal(String fajlUtvonal) {
        if (fajlUtvonal == null || fajlUtvonal.isBlank()) {
            throw new ErvenytelenAdatException("A fájl elérési útvonala nem lehet üres.");
        }
        return Path.of(fajlUtvonal.trim());
    }
}
