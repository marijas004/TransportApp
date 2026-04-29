package ui.viewmodel;

import routing.Route;

/**
 * ViewModel klasa koja predstavlja jednu rutu u Top-5 listi.
 * <p>
 * Koristi se za prikaz dodatnih ruta u posebnom prozoru (Top-5),
 * gdje svaka stavka sadrži sažetak rute (opis, vrijeme, cijenu,
 * broj presjedanja) kao i referencu na stvarni {@link Route} objekat.
 * </p>
 */
public class TopRouteItem {

    /** Kratak opis rute (npr. "A_0_0 → Z_2_3"). */
    private final String opis;

    /** Ukupno trajanje rute izraženo u minutama. */
    private final long vrijemeMin;

    /** Ukupna cijena rute. */
    private final double cijena;

    /** Ukupan broj presjedanja na ruti. */
    private final int presjedanja;

    /** Stvarna Route instanca koja stoji iza ovog prikaza. */
    private final Route route;

    /**
     * Kreira stavku za Top-5 prikaz ruta.
     *
     * @param opis kratak tekstualni opis rute
     * @param vrijemeMin ukupno vrijeme rute u minutama
     * @param cijena ukupna cijena rute
     * @param presjedanja broj presjedanja
     * @param route referenca na stvarni {@link Route} objekat
     */
    public TopRouteItem(String opis,
                        long vrijemeMin,
                        double cijena,
                        int presjedanja,
                        Route route) {
        this.opis = opis;
        this.vrijemeMin = vrijemeMin;
        this.cijena = cijena;
        this.presjedanja = presjedanja;
        this.route = route;
    }

    /**
     * Vraća opis rute.
     *
     * @return opis rute
     */
    public String getOpis() {
        return opis;
    }

    /**
     * Vraća ukupno trajanje rute u minutama.
     *
     * @return vrijeme rute u minutama
     */
    public long getVrijemeMin() {
        return vrijemeMin;
    }

    /**
     * Vraća ukupnu cijenu rute.
     *
     * @return cijena rute
     */
    public double getCijena() {
        return cijena;
    }

    /**
     * Vraća broj presjedanja na ruti.
     *
     * @return broj presjedanja
     */
    public int getPresjedanja() {
        return presjedanja;
    }

    /**
     * Vraća stvarni {@link Route} objekat.
     * <p>
     * Koristi se kada korisnik izabere rutu iz Top-5 liste
     * (za prikaz na mapi ili kupovinu karte).
     * </p>
     *
     * @return ruta
     */
    public Route getRoute() {
        return route;
    }
}
