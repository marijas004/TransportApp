package routing;

import model.Departure;
import java.time.LocalTime;

/**
 * Implementacija {@link CostFunction} koja optimizuje rutu po kriterijumu cijene.
 * <p>
 * Za stvarne vožnje trošak je cijena karte, dok su transfer grane (BUS ↔ TRAIN)
 * besplatne i ne dodaju trošak.
 * </p>
 */
public class PriceCost implements CostFunction {

    /**
     * Izračunava trošak prelaska preko grane po kriterijumu cijene.
     * <p>
     * Ako je grana transfer, vraća {@code 0.0}. Inače vraća cijenu karte
     * za dati polazak.
     * </p>
     *
     * @param dep trenutni polazak koji se razmatra
     * @param currentTime vrijeme dolaska na stanicu polaska (nije bitno za cijenu)
     * @param prev prethodni polazak (nije bitno za cijenu; može biti {@code null})
     * @return trošak prelaska (cijena karte ili 0 za transfer)
     */
    @Override
    public double cost(Departure dep, LocalTime currentTime, Departure prev) {

        if (dep.isTransfer()) {
            return 0.0;
        }

        return dep.getPrice();
    }
}
