package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Generator ulaznih podataka za transportnu aplikaciju.
 *
 * Generiše:
 * <ul>
 *   <li>mapu države kao matricu gradova (format: {@code G_x_y})</li>
 *   <li>autobuske i željezničke stanice za svaki grad (format: {@code A_x_y} i {@code Z_x_y})</li>
 *   <li>polaske (BUS/TRAIN) između susjednih gradova sa vremenom, trajanjem, cijenom i minTransferTime</li>
 * </ul>
 * Podaci se mogu sačuvati u JSON fajl.
 *
 */
public class TransportDataGenerator {

    /**
     * Broj polazaka po stanici (odvojeno za BUS i TRAIN).
     */
    private static int DEPARTURES_PER_STATION = 5;

    /**
     * Generator slučajnih vrijednosti.
     */
    private static final Random random = new Random();

    /**
     * Postavlja broj polazaka po stanici.
     *
     * @param value broj polazaka po stanici
     */
    public static void setDeparturesPerStation(int value) {
        DEPARTURES_PER_STATION = value;
    }

    /**
     * Struktura podataka koja sadrži sve generisane ulazne podatke:
     * mapu države, stanice i polaske.
     */
    public static class TransportData {

        /** Mapa države kao matrica gradova (n x m). */
        public String[][] countryMap;

        /** Lista stanica (po jedna struktura po gradu). */
        public List<Station> stations;

        /** Lista svih polazaka (BUS i TRAIN). */
        public List<Departure> departures;
    }

    /**
     * Predstavlja stanice jednog grada (autobuska i željeznička).
     */
    public static class Station {

        /** Oznaka grada (npr. {@code G_0_0}). */
        public String city;

        /** Oznaka autobuske stanice (npr. {@code A_0_0}). */
        public String busStation;

        /** Oznaka željezničke stanice (npr. {@code Z_0_0}). */
        public String trainStation;
    }

    /**
     * Predstavlja jedan polazak (vožnju) između dvije tačke.
     */
    public static class Departure {

        /** Tip polaska: npr. {@code "BUS"} ili {@code "TRAIN"}. */
        public String type;

        /** Polazna stanica (npr. {@code A_x_y} ili {@code Z_x_y}). */
        public String from;

        /** Odredište (u trenutnoj implementaciji: susjedni grad kao {@code G_x_y}). */
        public String to;

        /** Vrijeme polaska u formatu {@code HH:mm}. */
        public String departureTime;

        /** Trajanje putovanja u minutama. */
        public int duration;

        /** Cijena karte. */
        public int price;

        /** Minimalno vrijeme potrebno za transfer u minutama. */
        public int minTransferTime;
    }

    /**
     * Generiše kompletan skup podataka (mapa + stanice + polasci).
     *
     * @param n broj redova (visina) mape države
     * @param m broj kolona (širina) mape države
     * @param departures broj polazaka po stanici (posebno za BUS i TRAIN)
     * @return generisani transportni podaci
     */
    public TransportData generateData(int n, int m, int departures) {
        TransportData data = new TransportData();
        setDeparturesPerStation(departures);
        data.countryMap = generateCountryMap(n, m);
        data.stations = generateStations(n, m);
        data.departures = generateDepartures(data.stations, n, m);
        return data;
    }

    /**
     * Generiše mapu države kao matricu gradova u formatu {@code G_x_y}.
     *
     * @param n broj redova
     * @param m broj kolona
     * @return matrica gradova
     */
    private String[][] generateCountryMap(int n, int m) {
        String[][] countryMap = new String[n][m];
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                countryMap[x][y] = "G_" + x + "_" + y;
            }
        }
        return countryMap;
    }

    /**
     * Generiše stanice za svaki grad:
     * autobusku {@code A_x_y} i željezničku {@code Z_x_y}.
     *
     * @param n broj redova
     * @param m broj kolona
     * @return lista stanica za sve gradove
     */
    private List<Station> generateStations(int n, int m) {
        List<Station> stations = new ArrayList<>();
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                Station station = new Station();
                station.city = "G_" + x + "_" + y;
                station.busStation = "A_" + x + "_" + y;
                station.trainStation = "Z_" + x + "_" + y;
                stations.add(station);
            }
        }
        return stations;
    }

    /**
     * Generiše polaske za sve stanice.
     * Za svaku stanicu se generiše {@link #DEPARTURES_PER_STATION} BUS polazaka
     * i {@link #DEPARTURES_PER_STATION} TRAIN polazaka.
     *
     * @param stations lista stanica
     * @param n broj redova mape
     * @param m broj kolona mape
     * @return lista polazaka
     */
    private List<Departure> generateDepartures(List<Station> stations, int n, int m) {
        List<Departure> departures = new ArrayList<>();

        for (Station station : stations) {
            int x = Integer.parseInt(station.city.split("_")[1]);
            int y = Integer.parseInt(station.city.split("_")[2]);

            for (int i = 0; i < DEPARTURES_PER_STATION; i++) {
                departures.add(generateDeparture("BUS", station.busStation, x, y, n, m));
            }

            for (int i = 0; i < DEPARTURES_PER_STATION; i++) {
                departures.add(generateDeparture("TRAIN", station.trainStation, x, y, n, m));
            }
        }
        return departures;
    }

    /**
     * Generiše jedan polazak za zadati tip prevoza i polaznu stanicu.
     * Odredište se bira slučajno među susjednim gradovima (gore/dolje/lijevo/desno).
     *
     * @param type tip polaska ({@code "BUS"} ili {@code "TRAIN"})
     * @param from oznaka polazne stanice
     * @param x x-koordinata grada
     * @param y y-koordinata grada
     * @param n broj redova mape
     * @param m broj kolona mape
     * @return generisani polazak
     */
    private Departure generateDeparture(String type, String from, int x, int y, int n, int m) {
        Departure departure = new Departure();
        departure.type = type;
        departure.from = from;

        List<String> neighbors = getNeighbors(x, y, n, m);
        departure.to = neighbors.isEmpty() ? from : neighbors.get(random.nextInt(neighbors.size()));

        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15; // 0, 15, 30, 45
        departure.departureTime = String.format("%02d:%02d", hour, minute);

        departure.duration = 30 + random.nextInt(151);
        departure.price = 100 + random.nextInt(901);

        departure.minTransferTime = 5 + random.nextInt(26);

        return departure;
    }

    /**
     * Vraća listu susjednih gradova (4-smjerno: gore/dolje/lijevo/desno),
     * u formatu {@code G_x_y}.
     *
     * @param x x-koordinata grada
     * @param y y-koordinata grada
     * @param n broj redova mape
     * @param m broj kolona mape
     * @return lista susjeda
     */
    private List<String> getNeighbors(int x, int y, int n, int m) {
        List<String> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < n && ny >= 0 && ny < m) {
                neighbors.add("G_" + nx + "_" + ny);
            }
        }
        return neighbors;
    }

    /**
     * Čuva generisane podatke u JSON fajl.
     * <p>
     * JSON sadrži tri polja: {@code countryMap}, {@code stations} i {@code departures}.
     * </p>
     *
     * @param data generisani podaci
     * @param filename naziv/putanja izlaznog fajla
     * @param n broj redova mape
     * @param m broj kolona mape
     */
    public void saveToJson(TransportData data, String filename, int n, int m) {
        try (FileWriter file = new FileWriter(filename)) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            json.append("  \"countryMap\": [\n");
            for (int i = 0; i < n; i++) {
                json.append("    [");
                for (int j = 0; j < m; j++) {
                    json.append("\"").append(data.countryMap[i][j]).append("\"");
                    if (j < m - 1) json.append(", ");
                }
                json.append("]");
                if (i < n - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");

            json.append("  \"stations\": [\n");
            for (int i = 0; i < data.stations.size(); i++) {
                Station s = data.stations.get(i);
                json.append("    {\"city\": \"").append(s.city)
                        .append("\", \"busStation\": \"").append(s.busStation)
                        .append("\", \"trainStation\": \"").append(s.trainStation)
                        .append("\"}");
                if (i < data.stations.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");

            json.append("  \"departures\": [\n");
            for (int i = 0; i < data.departures.size(); i++) {
                Departure d = data.departures.get(i);
                json.append("    {\"type\": \"").append(d.type)
                        .append("\", \"from\": \"").append(d.from)
                        .append("\", \"to\": \"").append(d.to)
                        .append("\", \"departureTime\": \"").append(d.departureTime)
                        .append("\", \"duration\": ").append(d.duration)
                        .append(", \"price\": ").append(d.price)
                        .append(", \"minTransferTime\": ").append(d.minTransferTime)
                        .append("}");
                if (i < data.departures.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");

            json.append("}");
            file.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
