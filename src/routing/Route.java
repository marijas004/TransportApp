package routing;

import model.Departure;
import java.util.ArrayList;
import java.util.List;

/**
 * Predstavlja kompletnu rutu putovanja kao niz koraka (polazaka).
 * <p>
 * Ruta sadrži listu {@link Departure} koraka (stvarne vožnje i eventualne transfer grane),
 * kao i zbirne informacije: ukupna cijena, ukupno trajanje, broj presjedanja
 * i ukupna “tezina” (vrijednost dobijena po izabranoj {@link CostFunction}).
 * </p>
 */
public class Route {

    /**
     * Lista svih koraka rute (polasci + eventualni transferi).
     */
    private List<Departure> steps = new ArrayList<>();

    /**
     * Ukupna cijena rute.
     */
    private double totalCost;

    /**
     * Ukupno trajanje rute u minutama.
     */
    private long totalTime;

    /**
     * Ukupan broj presjedanja.
     */
    private int totalTransfers;

    /**
     * Ukupna “tezina” rute (vrijednost po izabranom {@link CostFunction}-u).
     * Npr. kod kriterijuma vremena ovo može biti ukupno vrijeme (sa čekanjima),
     * kod cijene ukupna cijena, itd.
     */
    private double totalWeight;

    /**
     * Dodaje jedan korak ({@link Departure}) u rutu.
     *
     * @param dep polazak koji se dodaje kao korak rute
     */
    public void addStep(Departure dep) {
        steps.add(dep);
    }

    /**
     * Vraća listu svih koraka rute.
     *
     * @return lista koraka rute
     */
    public List<Departure> getSteps() {
        return steps;
    }

    /**
     * Vraća ukupnu cijenu rute.
     *
     * @return ukupna cijena
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Dodaje iznos na ukupnu cijenu rute.
     * <p>
     * Tipično se koristi prilikom rekonstrukcije rute,
     * gdje se sabira cijena svakog koraka.
     * </p>
     *
     * @param totalCost iznos koji se dodaje na ukupnu cijenu
     */
    public void addTotalCost(double totalCost) {
        this.totalCost += totalCost;
    }

    /**
     * Dodaje trajanje (u minutama) na ukupno vrijeme rute.
     *
     * @param totalTime trajanje koje se dodaje (u minutama)
     */
    public void addTotalTime(long totalTime) {
        this.totalTime += totalTime;
    }

    /**
     * Povećava broj presjedanja.
     *
     * @param totalTransfer broj presjedanja koje treba dodati
     */
    public void addTotalTransfers(double totalTransfer) {
        this.totalTransfers += totalTransfer;
    }

    /**
     * Vraća ukupan broj presjedanja.
     *
     * @return broj presjedanja
     */
    public int getTotalTransfers() {
        return totalTransfers;
    }

    /**
     * Postavlja ukupan broj presjedanja.
     *
     * @param totalTransfers broj presjedanja
     */
    public void setTotalTransfers(int totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    /**
     * Vraća ukupno trajanje rute u minutama.
     *
     * @return ukupno trajanje u minutama
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * Vraća ukupnu “tezinu” rute (vrijednost izračunatu po {@link CostFunction}-u).
     *
     * @return ukupna težina rute
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Postavlja ukupnu “tezinu” rute (vrijednost po izabranom kriterijumu).
     *
     * @param totalWeight ukupna težina rute
     */
    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    /**
     * Postavlja ukupno vrijeme rute u minutama.
     *
     * @param totalTime ukupno vrijeme u minutama
     */
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * Postavlja ukupnu cijenu rute.
     *
     * @param totalCost ukupna cijena
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Vraća tekstualni prikaz rute, uključujući sve korake i zbirne podatke.
     * Koristi se za debug i ispis.
     *
     * @return tekstualni prikaz rute
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ROUTE\n");

        for (int i = 0; i < steps.size(); i++) {
            sb.append(i + 1)
                    .append(") ")
                    .append(steps.get(i).toString())
                    .append("\n");
        }

        sb.append("Total time: ").append(totalTime).append(" min\n");
        sb.append("Total cost: ").append(totalCost).append("\n");
        sb.append("Transfers: ").append(totalTransfers).append("\n");

        return sb.toString();
    }
}
