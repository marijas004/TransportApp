package routing;

import model.Departure;
import model.Station;

import java.util.*;
import java.util.function.Predicate;

/**
 * Pomoćna klasa za pronalaženje do 5 najboljih (različitih) ruta između dvije stanice.
 *
 * Ideja:
 * <ul>
 *   <li>Prvo se pronađe najbolju rutu standardnim {@link Dijkstra} algoritmom.</li>
 *   <li>Zatim se generišu alternative tako što se “zabrani” jedna (ili par) stvarnih ivica
 *       iz najbolje rute i ponovo pokrene Dijkstra.</li>
 *   <li>Rute se deduplikuju na osnovu potpisa (sekvence stanica).</li>
 * </ul>
 */
public class top5Routes {

    /**
     * Pronalazi do 5 najboljih ruta između dvije stanice po zadatom kriterijumu.
     *
     * @param graph graf transportne mreže
     * @param start početna stanica
     * @param end krajnja stanica
     * @param costFun funkcija troška (kriterijum optimizacije)
     * @return lista ruta (maksimalno 5), sortirana po rastućoj težini
     */
    public List<Route> findTop5(Graph graph, Station start, Station end, CostFunction costFun) {
        Dijkstra dijkstra = new Dijkstra();

        Route first = dijkstra.findPath(graph, start, end, costFun);
        if (first == null) return List.of();

        List<Route> results = new ArrayList<>();
        results.add(first);

        Set<String> seen = new HashSet<>();
        seen.add(routeSig(first.getSteps()));

        List<Departure> baseSteps = first.getSteps();

        // 1) zabrani jednu ivicu iz najbolje rute
        if (baseSteps != null) {
            for (Departure bannedStep : baseSteps) {
                if (bannedStep == null || bannedStep.isTransfer()) continue;

                Predicate<Departure> skip = dep -> sameEdge(dep, bannedStep);

                Route alt = dijkstra.findPath(graph, start, end, costFun, skip);
                if (alt == null) continue;

                String sig = routeSig(alt.getSteps());
                if (seen.add(sig)) results.add(alt);

                if (results.size() >= 5) break;
            }
        }

        // 2) ako nema dovoljno, zabrani parove ivica
        if (results.size() < 5 && baseSteps != null) {
            expandWithEdgePairs(graph, start, end, costFun, results, seen, baseSteps, 5);
        }

        // sortiraj i vrati top 5 ()
        results.sort(Comparator.comparingDouble(Route::getTotalWeight));

        if (results.size() > 5) {
            return new ArrayList<>(results.subList(0, 5));
        }
        return results;
    }

    /**
     * Potpis rute (signature) za deduplikaciju u GUI (npr. između 4 kombinacije stanica).
     */
    public String signature(Route r) {
        if (r == null) return "NULL";
        return routeSig(r.getSteps());
    }

    private void expandWithEdgePairs(
            Graph graph,
            Station start,
            Station end,
            CostFunction costFun,
            List<Route> current,
            Set<String> seen,
            List<Departure> baseSteps,
            int limit
    ) {
        Dijkstra dijkstra = new Dijkstra();

        for (int i = 0; i < baseSteps.size(); i++) {
            Departure b1 = baseSteps.get(i);
            if (b1 == null || b1.isTransfer()) continue;

            for (int j = i + 1; j < baseSteps.size(); j++) {
                Departure b2 = baseSteps.get(j);
                if (b2 == null || b2.isTransfer()) continue;

                Predicate<Departure> skip = dep -> sameEdge(dep, b1) || sameEdge(dep, b2);

                Route alt = dijkstra.findPath(graph, start, end, costFun, skip);
                if (alt == null) continue;

                String sig = routeSig(alt.getSteps());
                if (seen.add(sig)) current.add(alt);

                if (current.size() >= limit) return;
            }
        }
    }

    private boolean sameEdge(Departure a, Departure b) {
        if (a == null || b == null) return false;

        return Objects.equals(a.getFrom(), b.getFrom())
                && Objects.equals(a.getTo(), b.getTo())
                && Objects.equals(a.getDepartureTime(), b.getDepartureTime());
    }

    private String stationKey(Station s) {
        return String.valueOf(s);
    }

    private String routeSig(List<Departure> steps) {
        if (steps == null || steps.isEmpty()) return "EMPTY";

        StringBuilder sb = new StringBuilder();
        for (Departure d : steps) {
            sb.append(stationKey(d.getFrom()))
                    .append(">")
                    .append(stationKey(d.getTo()))
                    .append("@")
                    .append(d.getDepartureTime());
            sb.append("|");
        }
        return sb.toString();
    }

}
