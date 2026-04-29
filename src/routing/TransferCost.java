package routing;

import model.Departure;
import java.time.LocalTime;

/**
 * Implementacija {@link CostFunction} koja optimizuje rutu po kriterijumu
 * minimalnog broja presjedanja.
 *
 * Logika troška:
 * <ul>
 *   <li>Početna vožnja ne povećava broj presjedanja.</li>
 *   <li>Transfer grana (BUS ↔ TRAIN) broji se kao jedno presjedanje.</li>
 *   <li>Prelazak sa jedne stvarne vožnje na drugu (bez transfera između)
 *       takođe se smatra presjedanjem.</li>
 * </ul>
 *
 */
public class TransferCost implements CostFunction {

    /**
     * Izračunava trošak prelaska preko jedne grane po kriterijumu
     * broja presjedanja.
     *
     * @param dep trenutni polazak koji se razmatra
     * @param currentTime vrijeme dolaska na stanicu polaska
     *                    (nije bitno za ovaj kriterijum)
     * @param prev prethodni polazak u ruti
     *             (može biti {@code null} za početni korak)
     * @return vrijednost troška (0 ili 1) koja predstavlja promjenu
     *         broja presjedanja
     */
    @Override
    public double cost(Departure dep, LocalTime currentTime, Departure prev) {

        if (prev == null) {
            return 0.0;
        }

        if (dep.isTransfer()) {
            return 1.0;
        }

        if (prev.isTransfer()) {
            return 0.0;
        }

        return 1.0;
    }
}
