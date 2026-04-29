package routing;

import model.City;
import model.Station;
import model.Departure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.time.LocalTime;

/**
 * Predstavlja usmjereni graf transportne mreže.
 * <p>
 * Čvorovi grafa su {@link Station} objekti, a grane su {@link Departure} polasci
 * koji vode od jedne stanice ka drugoj.
 * Za svaku stanicu čuva se lista svih polazaka iz te stanice (adjacency list).
 * </p>
 */
public class Graph {

    /**
     * Adjacency lista: za svaku stanicu čuva listu polazaka (grana) iz te stanice.
     */
    private Map<Station, List<Departure>> graph = new HashMap<>();

    /**
     * Dodaje stanicu kao čvor u graf.
     * Ako stanica već postoji, metoda ne mijenja stanje grafa.
     *
     * @param s stanica koja se dodaje
     */
    public void addStation(Station s) {
        graph.putIfAbsent(s, new ArrayList<>());
    }

    /**
     * Dodaje polazak (granu) u graf.
     * <p>
     * Ako polazna stanica ({@code dep.getFrom()}) nije ranije dodata kao čvor,
     * biće kreirana prazna lista i polazak će se zatim dodati.
     * </p>
     *
     * @param dep polazak koji se dodaje u graf
     */
    public void addDeparture(Departure dep) {
        graph.computeIfAbsent(dep.getFrom(), k -> new ArrayList<>())
                .add(dep);
    }

    /**
     * Dodaje transfer grane (BUS ↔ TRAIN) unutar istog grada.
     * <p>
     * Transfer grane su besplatne (0 minuta i 0 cijena) i ne troše vrijeme,
     * ali se mogu brojati kao presjedanje (npr. u kriterijumu minimalnog broja presjedanja).
     * Dodaju se u oba smjera: BUS → TRAIN i TRAIN → BUS.
     * </p>
     */
    public void addTransfers() {

        Map<City, List<Station>> byCity = new HashMap<>();
        for (Station s : graph.keySet()) {
            byCity.computeIfAbsent(s.getCity(), k -> new ArrayList<>()).add(s);
        }

        for (List<Station> stationsInCity : byCity.values()) {
            Station bus = null;
            Station train = null;

            for (Station s : stationsInCity) {
                if (s.getType() == Station.Type.BUS) bus = s;
                if (s.getType() == Station.Type.TRAIN) train = s;
            }

            if (bus != null && train != null) {

                Departure bt = new Departure(
                        bus,
                        train,
                        LocalTime.MIDNIGHT,
                        0,
                        0,
                        0,
                        true
                );

                Departure tb = new Departure(
                        train,
                        bus,
                        LocalTime.MIDNIGHT,
                        0,
                        0,
                        0,
                        true
                );

                addDeparture(bt);
                addDeparture(tb);
            }
        }
    }

    /**
     * Vraća listu svih polazaka iz date stanice.
     *
     * @param s stanica iz koje se traže polasci
     * @return lista polazaka iz stanice (prazna lista ako nema polazaka)
     */
    public List<Departure> getDepartures(Station s) {
        return graph.getOrDefault(s, List.of());
    }

    /**
     * Vraća skup svih stanica (čvorova) u grafu.
     *
     * @return skup stanica
     */
    public Set<Station> getStation() {
        return graph.keySet();
    }

    /**
     * Ispisuje osnovne informacije o grafu na konzolu:
     * za svaku stanicu prikazuje broj polazaka i listu odredišta sa vremenima polaska.
     * Koristi se za debug i provjeru učitanih/generisanih podataka.
     */
    public void printGraph() {
        for (Map.Entry<Station, List<Departure>> e : graph.entrySet()) {
            System.out.println("FROM " + e.getKey().getName() +
                    " -> departures: " + e.getValue().size());

            for (Departure d : e.getValue()) {
                System.out.println("   -> TO " + d.getTo().getName()
                        + " at " + d.getDepartureTime());
            }
        }
    }
}
