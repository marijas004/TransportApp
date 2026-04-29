package routing;

import model.Departure;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Implementacija {@link CostFunction} koja optimizuje rutu po kriterijumu vremena.
 *
 * Trošak jedne grane predstavlja ukupno vrijeme potrebno za njen prelazak:
 * <ul>
 *   <li>vrijeme transfera (ako postoji prethodni polazak)</li>
 *   <li>vrijeme čekanja do polaska</li>
 *   <li>trajanje same vožnje</li>
 * </ul>
 * Transfer grane (BUS ↔ TRAIN) ne troše vrijeme.
 *
 */
public class TimeCost implements CostFunction {

    /**
     * Izračunava trošak prelaska preko jedne grane po kriterijumu vremena.
     *
     * @param dep trenutni polazak koji se razmatra
     * @param currentTime vrijeme dolaska na stanicu polaska
     * @param prev prethodni polazak u ruti (može biti {@code null} za prvu vožnju)
     * @return vremenski trošak prelaska u minutama
     */
    @Override
    public double cost(Departure dep, LocalTime currentTime, Departure prev) {

        // transfer grane (BUS ↔ TRAIN) ne troše vrijeme
        if (dep.isTransfer()) {
            return 0.0;
        }

        // prva stvarna vožnja: trošak je samo trajanje vožnje
        if (prev == null) {
            return dep.getDuration();
        }

        // minimalno vrijeme potrebno za transfer prije ovog polaska
        long transfer = dep.getMinTransferTime();

        // najranije moguće vrijeme kada se može uhvatiti ovaj polazak
        LocalTime earliest = currentTime.plusMinutes(transfer);

        // vrijeme čekanja do stvarnog polaska
        long wait = Duration
                .between(earliest, dep.getDepartureTime())
                .toMinutes();

        // ako je polazak sutradan (negativno čekanje), koriguje se za 24h
        if (wait < 0) {
            wait += 24 * 60;
        }

        // ukupan vremenski trošak
        return transfer + wait + dep.getDuration();
    }
}
