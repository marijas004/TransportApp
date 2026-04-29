package routing;

import model.Departure;
import model.Station;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;

/**
 * Implementacija Dijkstra algoritma prilagođena transportnom problemu.
 *
 * Algoritam traži optimalnu rutu od početne do krajnje stanice koristeći
 * zadatu {@link CostFunction} (vrijeme, cijena, presjedanja, itd.).
 * Podržava:
 * <ul>
 *   <li>transfer grane (BUS ↔ TRAIN) koje mogu imati posebnu logiku troška</li>
 *   <li>čekanje i minimalno vrijeme transfera za stvarne vožnje</li>
 *   <li>prelazak preko ponoći (korekcija čekanja dodavanjem jednog dana)</li>
 *   <li>opcionalni filter grana ({@code skipEdge}) za preskakanje pojedinih polazaka</li>
 * </ul>
 *
 */
public class Dijkstra {

    /**
     * Broj minuta u jednom danu (koristi se za prelazak preko ponoći).
     */
    private static final long DAY_MINUTES = 24 * 60;

    /**
     * Pomoćna struktura za {@link PriorityQueue} koja predstavlja trenutno stanje pretrage.
     */
    static class State {

        /** Trenutna stanica. */
        Station station;

        /** Ukupni trošak do ove stanice (prema izabranom kriterijumu). */
        double cost;

        /** Trenutno lokalno vrijeme (vrijeme dolaska na stanicu). */
        LocalTime currentTime;

        /** Apsolutni protok vremena u minutama od početka rute. */
        long absMin;

        /** Prethodni polazak (koristi se za logiku čekanja/transfera); može biti {@code null} na početku. */
        Departure prevDep;

        /**
         * Kreira novo stanje pretrage.
         *
         * @param station trenutna stanica
         * @param cost ukupni trošak do ove stanice
         * @param currentTime trenutno vrijeme (dolazak na stanicu)
         * @param prevDep prethodni polazak (može biti {@code null})
         * @param absMin apsolutni minuti od starta rute
         */
        State(Station station, double cost, LocalTime currentTime, Departure prevDep, long absMin) {
            this.station = station;
            this.cost = cost;
            this.currentTime = currentTime;
            this.prevDep = prevDep;
            this.absMin = absMin;
        }
    }

    /**
     * Ključ za mape dist/prev: identifikuje stanje na stanici u određenom vremenu.
     * <p>
     * Uključuje i {@code absMin} kako bi se razlikovala stanja koja mogu imati isto lokalno vrijeme
     * nakon prelaska preko ponoći.
     * </p>
     */
    static class Key {

        /** Stanica. */
        Station s;

        /** Lokalno vrijeme. */
        LocalTime t;

        /** Apsolutno vrijeme (ukupni minuti od starta). */
        long absMin;

        /**
         * Kreira ključ stanja.
         *
         * @param s stanica
         * @param t lokalno vrijeme
         * @param absMin apsolutni minuti od starta
         */
        Key(Station s, LocalTime t, long absMin) {
            this.s = s;
            this.t = t;
            this.absMin = absMin;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key k = (Key) o;
            return absMin == k.absMin && s.equals(k.s) && t.equals(k.t);
        }

        @Override
        public int hashCode() {
            int result = s.hashCode();
            result = 31 * result + Long.hashCode(absMin);
            result = 31 * result + t.hashCode();
            return result;
        }
    }

    /**
     * Pronalazi najbolju rutu od početne do krajnje stanice po zadatoj funkciji troška.
     * <p>
     * Opciono je moguće proslediti {@code skipEdge} filter koji preskače određene grane
     * (npr. za potrebe Top-K algoritama ili zabrane određenih polazaka).
     * </p>
     *
     * @param graph graf stanica i polazaka
     * @param start početna stanica
     * @param end krajnja stanica
     * @param costFun funkcija troška (kriterijum optimizacije)
     * @param skipEdge predikat za preskakanje grana (može biti {@code null})
     * @return optimalna {@link Route} ili {@code null} ako ruta ne postoji
     */
    public Route findPath(Graph graph,
                          Station start,
                          Station end,
                          CostFunction costFun,
                          Predicate<Departure> skipEdge) {

        Route route = new Route();

        Map<Key, Double> dist = new HashMap<>();
        Map<Key, Departure> prevEdge = new HashMap<>();
        Map<Key, Key> prevKey = new HashMap<>();

        PriorityQueue<State> pq =
                new PriorityQueue<>(Comparator.comparingDouble(s -> s.cost));

        Key startK = new Key(start, LocalTime.of(8, 0), 0);

        dist.put(startK, 0.0);
        pq.add(new State(start, 0.0, startK.t, null, 0));

        Key bestEndKey = null;
        double bestEndCost = Double.POSITIVE_INFINITY;

        while (!pq.isEmpty()) {
            State curr = pq.poll();

            if (bestEndKey != null && curr.cost >= bestEndCost) {
                break;
            }

            Key currK = new Key(curr.station, curr.currentTime, curr.absMin);

            double known = dist.getOrDefault(currK, Double.POSITIVE_INFINITY);
            if (curr.cost > known) continue;

            if (curr.station.equals(end)) {
                if (curr.cost < bestEndCost) {
                    bestEndCost = curr.cost;
                    bestEndKey = currK;
                }
            }

            for (Departure edge : graph.getDepartures(curr.station)) {

                if (skipEdge != null && skipEdge.test(edge)) continue;

                Station nextStation = edge.getTo();

                double edgeCost;
                LocalTime nextTime;
                Departure nextPrev;
                long nextAbsMin;

                if (edge.isTransfer()) {
                    edgeCost = costFun.cost(edge, curr.currentTime, curr.prevDep);
                    nextTime = curr.currentTime;
                    nextPrev = edge;
                    nextAbsMin = curr.absMin;

                } else if (curr.prevDep == null) {
                    long timeEdge = edge.getDuration();

                    edgeCost = costFun.cost(edge, curr.currentTime, curr.prevDep);

                    nextAbsMin = curr.absMin + timeEdge;
                    nextTime = edge.getArrivalTime();
                    nextPrev = edge;

                } else {
                    long transfer = edge.getMinTransferTime();

                    LocalTime earliest = curr.currentTime.plusMinutes(transfer);

                    long wait = Duration.between(earliest, edge.getDepartureTime()).toMinutes();
                    if (wait < 0) {
                        wait += DAY_MINUTES;
                    }

                    long timeEdge = transfer + wait + edge.getDuration();

                    edgeCost = costFun.cost(edge, curr.currentTime, curr.prevDep);

                    nextAbsMin = curr.absMin + timeEdge;
                    nextTime = curr.currentTime.plusMinutes(timeEdge);
                    nextPrev = edge;
                }

                Key nextK = new Key(nextStation, nextTime, nextAbsMin);

                double newCost = known + edgeCost;

                if (newCost < dist.getOrDefault(nextK, Double.POSITIVE_INFINITY)) {
                    dist.put(nextK, newCost);
                    prevEdge.put(nextK, edge);
                    prevKey.put(nextK, currK);
                    pq.add(new State(nextStation, newCost, nextTime, nextPrev, nextAbsMin));
                }
            }
        }

        if (bestEndKey == null) return null;

        List<Departure> steps = new ArrayList<>();
        Key curK = bestEndKey;

        while (!curK.equals(startK)) {
            Departure e = prevEdge.get(curK);
            if (e == null) return null;

            steps.add(e);

            curK = prevKey.get(curK);
            if (curK == null) return null;
        }

        Collections.reverse(steps);

        while (!steps.isEmpty() && steps.get(0).isTransfer()) {
            steps.remove(0);
        }

        while (!steps.isEmpty() && steps.get(steps.size() - 1).isTransfer()) {
            steps.remove(steps.size() - 1);
        }

        for (Departure dep : steps) {
            route.addStep(dep);
            route.addTotalCost(dep.getPrice());
        }

        route.setTotalTime(bestEndKey.absMin);

        int transfers = 0;
        Departure prev = null;

        for (Departure dep : steps) {
            if (prev == null) {
                prev = dep;
                continue;
            }
            if (dep.isTransfer()) transfers++;
            else if (!prev.isTransfer()) transfers++;
            prev = dep;
        }
        route.setTotalTransfers(transfers);

        route.setTotalWeight(bestEndCost);

        return route;
    }

    /**
     * Pronalazi najbolju rutu bez filtera grana (ne preskače nijedan polazak).
     *
     * @param graph graf stanica i polazaka
     * @param start početna stanica
     * @param end krajnja stanica
     * @param costFun funkcija troška (kriterijum optimizacije)
     * @return optimalna {@link Route} ili {@code null} ako ruta ne postoji
     */
    public Route findPath(Graph graph,
                          Station start,
                          Station end,
                          CostFunction costFun) {
        return findPath(graph, start, end, costFun, null);
    }
}
