package billing;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Pruža statističke informacije o sačuvanim računima.
 * Omogućava dobijanje ukupnog broja računa i zbira
 * svih cijena iz direktorijuma {@code resources/racuni}.
 */
public class ReceiptsInfo {

    /**
     * Vraća ukupan broj sačuvanih računa.
     * <p>
     * Računi se traže u direktorijumu {@code resources/racuni}
     * i podrazumijeva se da su sačuvani kao {@code .txt} fajlovi.
     * </p>
     *
     * @return ukupan broj sačuvanih računa
     */
    public static int countReceipts() {
        File folder = new File("resources/racuni");

        if (!folder.exists() || !folder.isDirectory()) {
            return 0;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        return files == null ? 0 : files.length;
    }

    /**
     * Računa zbir svih cijena sa svih sačuvanih računa.
     * <p>
     * Ako direktorijum sa računima ne postoji ili je prazan,
     * metoda vraća {@code 0.0}.
     * </p>
     *
     * @return zbir svih cijena sačuvanih računa
     */
    public static double sumAllPrices() {
        File folder = new File("resources/racuni");

        if (!folder.exists() || !folder.isDirectory()) {
            return 0.0;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return 0.0;

        double sum = 0.0;
        for (File file : files) {
            sum += readPrice(file);
        }

        return sum;
    }

    /**
     * Čita cijenu karte iz pojedinačnog računa.
     * <p>
     * Metoda pretražuje fajl liniju po liniju i traži
     * liniju koja počinje sa {@code "Cijena:"}.
     * Ako se cijena ne može pročitati ili je račun neispravan,
     * metoda vraća {@code 0.0}.
     * </p>
     *
     * @param file fajl računa iz kojeg se čita cijena
     * @return cijena karte ili {@code 0.0} u slučaju greške
     */
    private static double readPrice(File file) {
        try (Scanner sc = new Scanner(file)) {

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                if (line.startsWith("Cijena:")) {
                    String value = line.substring("Cijena:".length()).trim();
                    return Double.parseDouble(value);
                }
            }

        } catch (IOException | NumberFormatException e) {
            // neispravan ili oštećen račun se ignoriše
        }

        return 0.0;
    }
}
