package model;

/**
 * Predstavlja mapu države kao matricu gradova.
 * <p>
 * Mapa je definisana dimenzijama {@code n × m}, gdje se
 * svaki element matrice odnosi na jedan {@link City}
 * smješten na odgovarajućim koordinatama.
 * </p>
 */
public class CountryMap {

    /**
     * Matrica gradova (dimenzija {@code n × m}).
     */
    private City[][] cityMap;

    /**
     * Broj redova mape (x dimenzija).
     */
    private int n;

    /**
     * Broj kolona mape (y dimenzija).
     */
    private int m;

    /**
     * Kreira novu mapu države sa zadatim dimenzijama.
     * <p>
     * Inicijalizuje praznu matricu gradova u koju se
     * naknadno mogu dodavati objekti {@link City}.
     * </p>
     *
     * @param n broj redova mape
     * @param m broj kolona mape
     */
    public CountryMap(int n, int m) {
        this.n = n;
        this.m = m;
        cityMap = new City[n][m];
    }

    /**
     * Dodaje grad u mapu na osnovu njegovih koordinata.
     * <p>
     * Prije dodavanja provjerava se da li su koordinate
     * grada unutar granica mape.
     * </p>
     *
     * @param c grad koji se dodaje u mapu
     * @throws IndexOutOfBoundsException ako su koordinate grada van granica mape
     */
    public void addCity(City c) {
        checkBounds(c.getxCordinate(), c.getyCordinate());
        cityMap[c.getxCordinate()][c.getyCordinate()] = c;
    }

    /**
     * Provjerava da li su zadate koordinate unutar granica mape.
     *
     * @param i x koordinata
     * @param j y koordinata
     * @throws IndexOutOfBoundsException ako su koordinate van dozvoljenih granica
     */
    private void checkBounds(int i, int j) {
        if (i < 0 || i >= n || j < 0 || j >= m) {
            throw new IndexOutOfBoundsException(
                    "Out of bounds " + i + " " + j
            );
        }
    }
}
