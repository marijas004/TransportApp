package billing;

import model.Departure;
import routing.Route;

import java.time.LocalTime;
import java.time.LocalDate;

/**
 * Predstavlja račun za kupljenu rutu putovanja.
 * Račun se generiše na osnovu pronađene rute i sadrži
 * informacije o polaznoj i dolaznoj stanici, cijeni,
 * vremenu polaska i izdavanja, kao i jedinstveni ID.
 */
public class Receipt {

    /**
     * Naziv polazne stanice (prva stvarna vožnja u ruti).
     */
    private final String from;

    /**
     * Naziv dolazne stanice (posljednja stvarna vožnja u ruti).
     */
    private final String to;

    /**
     * Ukupna cijena rute.
     */
    private final double price;

    /**
     * Vrijeme polaska prve stvarne vožnje.
     */
    private final LocalTime departureTime;

    /**
     * Datum izdavanja računa.
     */
    private final LocalDate date;

    /**
     * Vrijeme izdavanja računa.
     */
    private final LocalTime billTime;

    /**
     * Jedinstveni identifikator računa.
     */
    private final int idBill;

    /**
     * Kreira novi račun na osnovu pronađene rute.
     * Transfer grane se ignorišu prilikom određivanja
     * polazne i dolazne stanice.
     *
     * @param route pronađena ruta putovanja
     * @param date datum izdavanja računa
     * @param billTime vrijeme izdavanja računa
     * @param idBill jedinstveni identifikator računa
     * @throws IllegalArgumentException ako ruta ne sadrži nijednu stvarnu vožnju
     */
    public Receipt(Route route, LocalDate date, LocalTime billTime, int idBill) {

        // pronalazak prve stvarne vožnje (ignorisu se transfer grane)
        Departure firstRide = route.getSteps().stream()
                .filter(d -> !d.isTransfer())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Ruta nema stvarnu vožnju")
                );

        // pronalazak posljednje stvarne vožnje (ignorisu se transfer grane)
        Departure lastRide = route.getSteps().stream()
                .filter(d -> !d.isTransfer())
                .reduce((a, b) -> b)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ruta nema stvarnu vožnju")
                );

        this.from = firstRide.getFrom().getName();
        this.to = lastRide.getTo().getName();
        this.price = route.getTotalCost();
        this.departureTime = firstRide.getDepartureTime();
        this.date = date;
        this.billTime = billTime;
        this.idBill = idBill;
    }

    /**
     * Vraća naziv polazne stanice.
     *
     * @return naziv polazne stanice
     */
    public String getFrom() {
        return from;
    }

    /**
     * Vraća naziv dolazne stanice.
     *
     * @return naziv dolazne stanice
     */
    public String getTo() {
        return to;
    }

    /**
     * Vraća ukupnu cijenu rute.
     *
     * @return ukupna cijena
     */
    public double getPrice() {
        return price;
    }

    /**
     * Vraća vrijeme polaska prve stvarne vožnje.
     *
     * @return vrijeme polaska
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Vraća vrijeme izdavanja računa.
     *
     * @return vrijeme izdavanja računa
     */
    public LocalTime getBillTime() {
        return billTime;
    }

    /**
     * Vraća datum izdavanja računa.
     *
     * @return datum izdavanja računa
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Vraća jedinstveni identifikator računa.
     *
     * @return ID računa
     */
    public int getIdBill() {
        return idBill;
    }
}
