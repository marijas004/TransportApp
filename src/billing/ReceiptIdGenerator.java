package billing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generator jedinstvenih identifikatora za račune.
 * Vrijednost ID-a se trajno čuva u fajlu kako bi se
 * osiguralo da je svaki novi račun jedinstven
 * i nakon ponovnog pokretanja aplikacije.
 */
public class ReceiptIdGenerator {

    /**
     * Putanja do fajla u kojem se čuva posljednji generisani ID.
     */
    private static final Path PATH =
            Path.of("resources/IdGenerator/IdGenerator.txt");

    /**
     * Generiše i vraća novi jedinstveni identifikator računa.
     * <p>
     * Ako fajl sa ID-em ne postoji, kreira se direktorij
     * i inicijalna vrijednost ID-a se postavlja na 1.
     * </p>
     *
     * @return novi jedinstveni ID računa
     * @throws RuntimeException ako dođe do greške pri čitanju,
     * pisanju ili parsiranju ID vrijednosti
     */
    public int getNewId() {
        try {
            // ako fajl ne postoji, kreira direktorij i inicijalnu vrijednost
            if (!Files.exists(PATH)) {
                Files.createDirectories(PATH.getParent());
                Files.writeString(PATH, "1");
            }

            // učitavanje trenutne vrijednosti ID-a iz fajla
            String content = Files.readString(PATH).trim();

            // parsiranje ID-a (ako je fajl prazan, počinje od 0)
            int id = content.isEmpty() ? 0 : Integer.parseInt(content);

            // povećanje ID-a
            id++;

            // upis nove vrijednosti nazad u fajl
            Files.writeString(PATH, String.valueOf(id));

            return id;

        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Ne mogu generisati novi Receipt ID", e);
        }
    }
}
