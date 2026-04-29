package ui.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel klasa za jedan red u tabeli rute ({@link javafx.scene.control.TableView}).
 * <p>
 * Predstavlja jedan korak rute (polazak) u obliku pogodnom za JavaFX binding.
 * Sva polja su definisana kao JavaFX properties kako bi TableView mogao
 * automatski reagovati na promjene.
 * </p>
 */
public class RouteRow {

    /** Tekstualni opis polaska (stanica + vrijeme polaska). */
    private final StringProperty polazak = new SimpleStringProperty();

    /** Tekstualni opis dolaska (stanica + vrijeme dolaska). */
    private final StringProperty dolazak = new SimpleStringProperty();

    /** Tip prevoza (Autobus, Voz ili Transfer). */
    private final StringProperty tip = new SimpleStringProperty();

    /** Cijena karte za ovaj korak rute. */
    private final IntegerProperty cijena = new SimpleIntegerProperty();

    /**
     * Kreira jedan red tabele rute.
     *
     * @param polazak opis polaska (npr. "A_1_2 (08:30)")
     * @param dolazak opis dolaska (npr. "Z_2_2 (09:10)")
     * @param tip tip prevoza (Autobus / Voz / Transfer)
     * @param cijena cijena karte za ovaj korak
     */
    public RouteRow(String polazak, String dolazak, String tip, int cijena) {
        this.polazak.set(polazak);
        this.dolazak.set(dolazak);
        this.tip.set(tip);
        this.cijena.set(cijena);
    }

    /**
     * Property za kolonu "Polazak".
     *
     * @return StringProperty polaska
     */
    public StringProperty polazakProperty() {
        return polazak;
    }

    /**
     * Property za kolonu "Dolazak".
     *
     * @return StringProperty dolaska
     */
    public StringProperty dolazakProperty() {
        return dolazak;
    }

    /**
     * Property za kolonu "Tip".
     *
     * @return StringProperty tipa prevoza
     */
    public StringProperty tipProperty() {
        return tip;
    }

    /**
     * Property za kolonu "Cijena".
     *
     * @return IntegerProperty cijene
     */
    public IntegerProperty cijenaProperty() {
        return cijena;
    }
}
