package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Učitava transportne podatke iz JSON fajla koristeći Jackson {@link ObjectMapper}.
 * <p>
 * Ova klasa je direktan “odraz” JSON strukture i služi kao DTO-kontejner:
 * sadrži mapu države ({@code countryMap}), listu stanica ({@code stations})
 * i listu polazaka ({@code departures}).
 * </p>
 */
public class TransportDataLoader {

    /**
     * Mapa države učitana iz JSON-a (format gradova: {@code G_x_y}).
     */
    public String[][] countryMap;

    /**
     * Lista svih stanica učitanih iz JSON-a (DTO – koristi se samo za prenos podataka).
     */
    public List<StationDTO> stations;

    /**
     * Lista svih polazaka učitanih iz JSON-a (DTO – koristi se samo za prenos podataka).
     */
    public List<DepartureDTO> departures;

    /**
     * DTO za stanicu (tačan odraz JSON strukture).
     * Sadrži oznaku grada i nazive autobuske/željezničke stanice.
     */
    public static class StationDTO {

        /** Oznaka grada (npr. {@code G_0_0}). */
        public String city;

        /** Oznaka autobuske stanice (npr. {@code A_0_0}). */
        public String busStation;

        /** Oznaka željezničke stanice (npr. {@code Z_0_0}). */
        public String trainStation;
    }

    /**
     * DTO za polazak (vrijednosti se učitavaju kao {@link String} iz JSON-a).
     * Naknadno mapiranje u domen-klase (npr. u {@code model.Departure}) radi se u mapperu.
     */
    public static class DepartureDTO {

        /** Tip prevoza: {@code BUS} ili {@code TRAIN}. */
        public String type;

        /** Naziv/oznaka stanice polaska. */
        public String from;

        /** Naziv/oznaka grada dolaska. */
        public String to;

        /** Vrijeme polaska u formatu {@code HH:mm}. */
        public String departureTime;

        /** Trajanje vožnje (string iz JSON-a). */
        public String duration;

        /** Cijena karte (string iz JSON-a). */
        public String price;

        /** Minimalno vrijeme transfera (string iz JSON-a). */
        public String minTransferTime;
    }

    /**
     * Učitava kompletne transportne podatke iz JSON fajla.
     *
     * @param path putanja do JSON fajla
     * @return objekat {@link TransportDataLoader} popunjen učitanim podacima
     * @throws IOException ako dođe do greške pri čitanju fajla ili parsiranju JSON-a
     */
    public static TransportDataLoader load(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), TransportDataLoader.class);
    }
}
