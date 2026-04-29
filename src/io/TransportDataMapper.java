package io;

import model.CountryMap;
import model.City;
import model.Station;
import model.Departure;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalTime;

/**
 * Mapira (pretvara) učitane transportne podatke iz {@link TransportDataLoader} (DTO strukture)
 * u domenske (model) klase aplikacije: {@link CountryMap}, {@link City}, {@link Station} i {@link Departure}.
 * <p>
 * Klasa služi kao “bridge” između JSON/DTO svijeta i model sloja.
 * </p>
 */
public class TransportDataMapper {

    /**
     * Kreira objekat {@link CountryMap} (matricu gradova) iz učitanog DTO-a.
     *
     * @param dto učitani podaci iz JSON-a
     * @return kreirana {@link CountryMap} sa dodanim gradovima
     */
    public static CountryMap createMap(TransportDataLoader dto) {
        int n = dto.countryMap.length;
        int m = dto.countryMap[0].length;

        CountryMap mapa = new CountryMap(n, m);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                City c = new City(dto.countryMap[i][j], i, j);
                mapa.addCity(c);
            }
        }
        return mapa;
    }

    /**
     * Kreira listu gradova ({@link City}) na osnovu matrice {@code countryMap}
     * iz DTO-a (format imena: {@code G_x_y}).
     *
     * @param dto učitani podaci iz JSON-a
     * @return lista kreiranih gradova
     */
    public static List<City> createCities(TransportDataLoader dto) {
        List<City> cities = new ArrayList<>();

        int n = dto.countryMap.length;
        int m = dto.countryMap[0].length;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                City c = new City(dto.countryMap[i][j], i, j);
                cities.add(c);
            }
        }
        return cities;
    }

    /**
     * Kreira stanice ({@link Station}) za sve gradove i vezuje ih za odgovarajući {@link City}.
     * <p>
     * Najprije se pravi mapa {@code nazivGrada -> City} radi brzog pronalaska grada.
     * Zatim se za svaki DTO zapis dodaju BUS i TRAIN stanice (ako postoje).
     * </p>
     *
     * @param dto učitani podaci iz JSON-a
     * @param cities lista gradova nad kojima se stanice vezuju
     * @return lista kreiranih stanica
     */
    public static List<Station> createStations(TransportDataLoader dto, List<City> cities) {

        HashMap<String, City> mapCity = new HashMap<>();
        for (City c : cities) {
            mapCity.put(c.getName(), c);
        }

        List<Station> stations = new ArrayList<>();

        for (TransportDataLoader.StationDTO sDTO : dto.stations) {

            City city = mapCity.get(sDTO.city);
            if (city == null) {
                continue;
            }

            if (sDTO.busStation != null) {
                stations.add(new Station(sDTO.busStation, Station.Type.BUS, city));
            }

            if (sDTO.trainStation != null) {
                stations.add(new Station(sDTO.trainStation, Station.Type.TRAIN, city));
            }
        }

        return stations;
    }

    /**
     * Kreira polaske ({@link Departure}) i povezuje {@code from} i {@code to} stanice.
     * <p>
     * {@code from} stanica se uzima direktno iz {@code dDTO.from}.
     * {@code to} stanica se određuje na osnovu grada {@code dDTO.to} (format {@code G_x_y})
     * i tipa prevoza {@code dDTO.type} (BUS/TRAIN) preko metode {@link #stationInCity(TransportDataLoader.DepartureDTO)}.
     * </p>
     * <p>
     * Ako ne postoji polazna ili dolazna stanica, polazak se preskače.
     * </p>
     *
     * @param dto učitani podaci iz JSON-a
     * @param stations lista dostupnih stanica
     * @return lista kreiranih polazaka
     * @throws NumberFormatException ako neki numerički field u DTO-u nije validan broj
     * @throws java.time.format.DateTimeParseException ako {@code departureTime} nije u formatu {@code HH:mm}
     */
    public static List<Departure> createDepartures(TransportDataLoader dto, List<Station> stations) {
        List<Departure> departures = new ArrayList<>();

        HashMap<String, Station> stations1 = new HashMap<>();
        for (Station s : stations) {
            stations1.put(s.getName(), s);
        }

        for (TransportDataLoader.DepartureDTO dDTO : dto.departures) {

            Station stationFrom = stations1.get(dDTO.from);
            Station stationTo = stations1.get(stationInCity(dDTO));

            if (stationFrom == null || stationTo == null) {
                System.out.println("SKIP departure: from=" + dDTO.from + ", to=" + dDTO.to);
                continue;
            }

            departures.add(
                    new Departure(
                            stationFrom,
                            stationTo,
                            LocalTime.parse(dDTO.departureTime),
                            Integer.parseInt(dDTO.duration),
                            Integer.parseInt(dDTO.price),
                            Integer.parseInt(dDTO.minTransferTime),
                            false
                    )
            );
        }

        return departures;
    }

    /**
     * Pomoćna metoda koja iz DTO polaska određuje naziv ciljne stanice u gradu {@code to}.
     * <ul>
     *   <li>Ako je {@code type=BUS} cilj je autobuska stanica u tom gradu: {@code A_x_y}</li>
     *   <li>Ako je {@code type=TRAIN} cilj je željeznička stanica u tom gradu: {@code Z_x_y}</li>
     * </ul>
     *
     * @param dep DTO polazak
     * @return naziv ciljne stanice ili {@code null} ako je ulaz neispravan ili tip nepoznat
     */
    public static String stationInCity(TransportDataLoader.DepartureDTO dep) {
        if (dep == null || dep.to == null || dep.type == null) return null;

        String[] parts = dep.to.split("_");
        if (parts.length != 3) return null;

        if ("BUS".equals(dep.type)) return "A_" + parts[1] + "_" + parts[2];
        if ("TRAIN".equals(dep.type)) return "Z_" + parts[1] + "_" + parts[2];

        return null;
    }
}
