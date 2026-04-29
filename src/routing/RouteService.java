package routing;

import model.City;
import model.Station;

import java.util.List;

/**
 * Servisni sloj za pronalaženje optimalne rute između dva grada.
 * <p>
 * Koristi {@link Graph} kao transportnu mrežu i {@link Dijkstra} algoritam za pronalazak
 * najboljeg puta. Podržava optimizaciju po različitim kriterijumima kroz strategiju
 * {@link CostFunction} (vrijeme, cijena ili broj presjedanja).
 * </p>
 */
public class RouteService {

    /**
     * Graf sa stanicama i polascima.
     */
    private final Graph graph;

    /**
     * Dijkstra algoritam za pronalaženje optimalnog puta.
     */
    private final Dijkstra dijkstra;

    /**
     * Kriterijum optimizacije rute.
     */
    public enum Criterion {
        /** Optimizacija po ukupnom vremenu putovanja. */
        TIME,
        /** Optimizacija po ukupnoj cijeni putovanja. */
        COST,
        /** Optimizacija po minimalnom broju presjedanja. */
        TRANSFERS
    }

    /**
     * Kreira servis za rutiranje nad već izgrađenim grafom.
     *
     * @param graph transportni graf (stanice + polasci)
     */
    public RouteService(Graph graph) {
        this.graph = graph;
        this.dijkstra = new Dijkstra();
    }

    /**
     * Pronalazi najbolju rutu između dva grada prema zadatom kriterijumu.
     * <p>
     * Pošto svaki grad ima autobusku i željezničku stanicu, metoda pokušava
     * sve kombinacije početne i krajnje stanice (2×2) i bira rutu sa najmanjim
     * rezultatom (score) prema kriterijumu.
     * </p>
     *
     * @param fromCity polazni grad
     * @param toCity odredišni grad
     * @param c kriterijum optimizacije
     * @return najbolja {@link Route} ili {@code null} ako ne postoji nijedna ruta
     */
    public Route findBestRoute(City fromCity, City toCity, Criterion c) {

        CostFunction costFun = switch (c) {
            case TIME -> new TimeCost();
            case COST -> new PriceCost();
            case TRANSFERS -> new TransferCost();
        };

        List<Station> starts =
                List.of(fromCity.getBusStation(), fromCity.getTrainStation());

        List<Station> ends =
                List.of(toCity.getBusStation(), toCity.getTrainStation());

        Route best = null;
        double bestCost = Double.POSITIVE_INFINITY;

        for (Station s : starts) {
            for (Station e : ends) {

                Route r = dijkstra.findPath(graph, s, e, costFun);
                if (r == null) continue;

                double score = scoreByCriterion(r, c);

                if (score < bestCost) {
                    bestCost = score;
                    best = r;
                }
            }
        }

        return best;
    }

    /**
     * Vraća numerički rezultat (score) rute za dati kriterijum.
     * <p>
     * Koristi se za poređenje ruta i izbor najbolje.
     * </p>
     *
     * @param r ruta koja se ocjenjuje
     * @param c kriterijum optimizacije
     * @return vrijednost score-a (vrijeme, cijena ili broj presjedanja)
     */
    private double scoreByCriterion(Route r, Criterion c) {
        return switch (c) {
            case TIME -> r.getTotalTime();
            case COST -> r.getTotalCost();
            case TRANSFERS -> r.getTotalTransfers();
        };
    }
}
