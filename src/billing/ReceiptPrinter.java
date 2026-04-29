package billing;

import routing.Route;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Klasa zadužena za kupovinu karte i ispis računa u fajl.
 * Na osnovu pronađene rute generiše se račun sa jedinstvenim ID-em
 * koji se trajno čuva u tekstualnom fajlu.
 */
public class ReceiptPrinter {

    /**
     * Kupuje kartu za zadatu rutu.
     * <p>
     * Metoda kreira novi {@link Receipt}, generiše jedinstveni ID računa,
     * i upisuje račun u tekstualni fajl unutar direktorijuma {@code resources/racuni}.
     * Transfer koraci u ruti se interno ignorišu prilikom određivanja
     * polazne i dolazne stanice.
     * </p>
     *
     * @param route ruta za koju se kupuje karta
     * @return {@code true} ako je račun uspješno kreiran i sačuvan,
     *         {@code false} ako je došlo do greške prilikom upisa
     */
    public static boolean buyTicket(Route route) {

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        ReceiptIdGenerator generator = new ReceiptIdGenerator();
        int newId = generator.getNewId();

        Receipt receipt = new Receipt(route, today, nowTime, newId);

        try {
            Path dir = Path.of("resources", "racuni");
            Files.createDirectories(dir);

            Path file = dir.resolve("racun" + receipt.getIdBill() + ".txt");

            try (FileWriter fw = new FileWriter(file.toString())) {
                fw.write(
                        "Od: " + receipt.getFrom() +
                                " do: " + receipt.getTo() +
                                " u " + receipt.getDepartureTime() + "h\n" +
                                "Cijena: " + receipt.getPrice() + "\n" +
                                "Datum: " + receipt.getDate() + "\n" +
                                "Vrijeme: " + receipt.getBillTime() + "\n" +
                                "ID: " + receipt.getIdBill() + "\n"
                );
            }

            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
