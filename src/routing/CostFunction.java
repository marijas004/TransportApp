package routing;

import model.Departure;
import java.time.LocalTime;

/**
 * Strategija za izračunavanje troška prelaska preko jedne grane (polaska)
 * u algoritmu pronalaženja najkraće rute (npr. Dijkstra).
 * <p>
 * Različite implementacije ovog interfejsa omogućavaju
 * optimizaciju rute po različitim kriterijumima
 * (vrijeme, cijena, broj transfera, itd.).
 * </p>
 */
public interface CostFunction {

    /**
     * Izračunava trošak prelaska preko jedne grane (polaska).
     *
     * @param dep trenutni polazak koji se razmatra
     * @param currentTime vrijeme dolaska na stanicu polaska
     * @param prev prethodni polazak u ruti (može biti {@code null} na početku)
     * @return numerička vrijednost troška koja se koristi u Dijkstra algoritmu
     */
    double cost(Departure dep, LocalTime currentTime, Departure prev);
}
