package model;

import java.util.Objects;

/**
 * Predstavlja stanicu u gradu.
 * <p>
 * Stanica može biti autobuska (BUS) ili željeznička (TRAIN)
 * i uvijek pripada tačno jednom {@link City}.
 * </p>
 */
public class Station {

    /**
     * Tip stanice.
     */
    public enum Type {
        /** Autobuska stanica. */
        BUS,
        /** Željeznička stanica. */
        TRAIN
    }

    /**
     * Tip stanice (BUS ili TRAIN).
     */
    private Type type;

    /**
     * Grad kojem stanica pripada.
     */
    private City city;

    /**
     * Naziv stanice (npr. {@code A_2_3} ili {@code Z_2_3}).
     */
    private String name;

    /**
     * Kreira novu stanicu sa zadatim nazivom, tipom i gradom.
     * <p>
     * Svaka stanica je vezana tačno za jedan grad.
     * </p>
     *
     * @param name naziv stanice
     * @param type tip stanice ({@link Type#BUS} ili {@link Type#TRAIN})
     * @param city grad kojem stanica pripada
     */
    public Station(String name, Type type, City city) {
        this.name = name;
        this.type = type;
        this.city = city;
    }

    /**
     * Vraća naziv stanice.
     *
     * @return naziv stanice
     */
    public String getName() {
        return name;
    }

    /**
     * Vraća grad kojem stanica pripada.
     *
     * @return grad stanice
     */
    public City getCity() {
        return city;
    }

    /**
     * Vraća tip stanice.
     *
     * @return tip stanice
     */
    public Type getType() {
        return type;
    }

    /**
     * Vraća tekstualni prikaz stanice.
     * Koristi se za debug i GUI prikaz.
     *
     * @return tekstualni prikaz stanice
     */
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

    /**
     * Provjerava jednakost dvije stanice.
     * Dvije stanice se smatraju jednakim ako imaju
     * isto ime i isti tip.
     *
     * @param o objekat za poređenje
     * @return {@code true} ako su stanice jednake, inače {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;

        Station station = (Station) o;
        return Objects.equals(name, station.name)
                && type == station.type;
    }

    /**
     * Vraća hash kod stanice, usklađen sa {@link #equals(Object)} metodom.
     *
     * @return hash kod stanice
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
