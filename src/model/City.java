package model;

import java.util.Objects;

/**
 * Predstavlja grad u matrici države.
 * <p>
 * Svaki grad ima jedinstveno ime (npr. {@code G_2_3}),
 * koordinate u matrici i automatski kreirane
 * autobusku i željezničku stanicu.
 * </p>
 */
public class City {

    /**
     * X koordinata grada u matrici države.
     */
    int xCordinate;

    /**
     * Y koordinata grada u matrici države.
     */
    int yCordinate;

    /**
     * Naziv grada (npr. {@code G_2_3}).
     */
    String name;

    /**
     * Autobuska stanica grada (kreira se automatski).
     */
    private final Station busStation;

    /**
     * Željeznička stanica grada (kreira se automatski).
     */
    private final Station trainStation;

    /**
     * Kreira novi grad sa zadatim imenom i koordinatama.
     * <p>
     * Prilikom kreiranja grada automatski se kreiraju
     * i autobuska i željeznička stanica.
     * </p>
     *
     * @param name naziv grada
     * @param xCordinate x koordinata grada u matrici
     * @param yCordinate y koordinata grada u matrici
     */
    public City(String name, int xCordinate, int yCordinate) {
        this.name = name;
        this.xCordinate = xCordinate;
        this.yCordinate = yCordinate;

        this.busStation =
                new Station("A_" + xCordinate + "_" + yCordinate,
                        Station.Type.BUS,
                        this);

        this.trainStation =
                new Station("Z_" + xCordinate + "_" + yCordinate,
                        Station.Type.TRAIN,
                        this);
    }

    /**
     * Vraća naziv grada.
     *
     * @return naziv grada
     */
    public String getName() {
        return name;
    }

    /**
     * Vraća x koordinatu grada.
     *
     * @return x koordinata
     */
    public int getxCordinate() {
        return xCordinate;
    }

    /**
     * Vraća y koordinatu grada.
     *
     * @return y koordinata
     */
    public int getyCordinate() {
        return yCordinate;
    }

    /**
     * Vraća autobusku stanicu grada.
     *
     * @return autobuska stanica
     */
    public Station getBusStation() {
        return busStation;
    }

    /**
     * Vraća željezničku stanicu grada.
     *
     * @return željeznička stanica
     */
    public Station getTrainStation() {
        return trainStation;
    }

    /**
     * Vraća tekstualni prikaz grada.
     * Koristi se u GUI komponentama (npr. {@code ComboBox})
     * i za potrebe debuggovanja.
     *
     * @return tekstualni prikaz grada
     */
    @Override
    public String toString() {
        return "City " + getName() +
                " location(coordinates): " +
                getxCordinate() + " " + getyCordinate();
    }

    /**
     * Provjerava jednakost dva grada.
     * Dva grada se smatraju jednakim ako imaju isto ime.
     *
     * @param o objekat za poređenje
     * @return {@code true} ako su gradovi jednaki, inače {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City city = (City) o;
        return Objects.equals(name, city.name);
    }

    /**
     * Vraća hash kod grada zasnovan na njegovom imenu.
     *
     * @return hash kod
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
