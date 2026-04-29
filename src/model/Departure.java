package model;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Predstavlja jedan polazak (vožnju) između dvije stanice.
 * <p>
 * Polazak ima definisanu polaznu i dolaznu stanicu,
 * vrijeme polaska i dolaska, trajanje, cijenu karte
 * i informaciju da li predstavlja transfer granu
 * (npr. autobus ↔ voz unutar istog grada).
 * </p>
 */
public class Departure {

    /**
     * Stanica polaska.
     */
    private Station from;

    /**
     * Stanica dolaska.
     */
    private Station to;

    /**
     * Vrijeme polaska.
     */
    private LocalTime departureTime;

    /**
     * Vrijeme dolaska (računa se iz vremena polaska i trajanja).
     */
    private LocalTime arrivalTime;

    /**
     * Trajanje vožnje u minutama.
     */
    private int duration;

    /**
     * Cijena karte.
     */
    private int price;

    /**
     * Minimalno vrijeme potrebno za transfer (u minutama).
     */
    private int minTransferTime;

    /**
     * Oznaka da li je ovo transfer grana (BUS ↔ TRAIN).
     */
    private boolean transfer;

    /**
     * Kreira novi polazak sa zadatim parametrima.
     * <p>
     * Vrijeme dolaska se automatski računa kao
     * {@code departureTime + duration}.
     * </p>
     *
     * @param from polazna stanica
     * @param to dolazna stanica
     * @param departureTime vrijeme polaska
     * @param duration trajanje vožnje u minutama
     * @param price cijena karte
     * @param minTransferTime minimalno vrijeme potrebno za transfer
     * @param transfer {@code true} ako je ovo transfer grana, inače {@code false}
     */
    public Departure(Station from,
                     Station to,
                     LocalTime departureTime,
                     int duration,
                     int price,
                     int minTransferTime,
                     boolean transfer) {

        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.duration = duration;
        this.price = price;
        this.minTransferTime = minTransferTime;

        this.arrivalTime = departureTime.plusMinutes(duration);
        this.transfer = transfer;
    }

    /**
     * Vraća polaznu stanicu.
     *
     * @return polazna stanica
     */
    public Station getFrom() {
        return from;
    }

    /**
     * Vraća dolaznu stanicu.
     *
     * @return dolazna stanica
     */
    public Station getTo() {
        return to;
    }

    /**
     * Vraća vrijeme polaska.
     *
     * @return vrijeme polaska
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Vraća vrijeme dolaska.
     *
     * @return vrijeme dolaska
     */
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Vraća trajanje vožnje u minutama.
     *
     * @return trajanje vožnje
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Vraća cijenu karte.
     *
     * @return cijena karte
     */
    public int getPrice() {
        return price;
    }

    /**
     * Vraća minimalno vrijeme potrebno za transfer.
     *
     * @return minimalno vrijeme transfera u minutama
     */
    public int getMinTransferTime() {
        return minTransferTime;
    }

    /**
     * Postavlja oznaku da li je polazak transfer grana.
     *
     * @param b {@code true} ako je transfer, {@code false} inače
     */
    public void setTransfer(boolean b) {
        this.transfer = b;
    }

    /**
     * Provjerava da li je ovaj polazak transfer grana.
     *
     * @return {@code true} ako je transfer, inače {@code false}
     */
    public boolean isTransfer() {
        return transfer;
    }

    /**
     * Vraća tekstualni prikaz polaska.
     * Koristi se za debug i ispis rute.
     *
     * @return tekstualni prikaz polaska
     */
    @Override
    public String toString() {
        return from + " -> " + to +
                " (" + departureTime + ", " +
                arrivalTime + ", " +
                duration + " min, " +
                price + "€)";
    }

    /**
     * Provjerava jednakost dva polaska.
     * Dva polaska se smatraju jednakim ako imaju iste
     * osnovne atribute.
     *
     * @param o objekat za poređenje
     * @return {@code true} ako su polasci jednaki, inače {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Departure)) return false;

        Departure d = (Departure) o;

        return duration == d.duration
                && price == d.price
                && minTransferTime == d.minTransferTime
                && transfer == d.transfer
                && Objects.equals(from, d.from)
                && Objects.equals(to, d.to)
                && Objects.equals(departureTime, d.departureTime);
    }

    /**
     * Vraća hash kod polaska, usklađen sa {@link #equals(Object)} metodom.
     *
     * @return hash kod polaska
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                from,
                to,
                departureTime,
                duration,
                price,
                minTransferTime,
                transfer
        );
    }
}
