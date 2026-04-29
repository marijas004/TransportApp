package app;

import io.TransportDataGenerator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ui.controller.MainController;
import ui.viewmodel.MainView;

import java.util.Optional;

/**
 * Glavna JavaFX aplikacija (entry point).
 *
 * Tok pokretanja:
 * <ol>
 *   <li>Traži od korisnika dimenzije mape države (n × m).</li>
 *   <li>Traži broj polazaka po stanici (DEPARTURES_PER_STATION).</li>
 *   <li>Generiše test podatke i snima ih u JSON fajl.</li>
 *   <li>Inicijalizuje {@link MainView} i {@link MainController}.</li>
 *   <li>Postavlja {@link Scene} i prikazuje prozor aplikacije.</li>
 * </ol>
 *
 */
public class MainFX extends Application {

    /**
     * JavaFX start metoda — poziva je JavaFX runtime nakon {@link #launch(String...)}.
     * <p>
     * Ako korisnik odustane od unosa dimenzija ili broja polazaka, aplikacija se uredno gasi.
     * </p>
     *
     * @param primaryStage glavni prozor aplikacije
     */
    @Override
    public void start(Stage primaryStage) {
        int[] dims = askForDimensions();
        if (dims == null) {
            Platform.exit();
            return;
        }

        Integer departures = askForDeparturesPerStation();
        if (departures == null) {
            Platform.exit();
            return;
        }

        int n = dims[0];
        int m = dims[1];

        // Generisanje ulaznih podataka i snimanje u JSON koji kasnije učitava loader.
        TransportDataGenerator generator = new TransportDataGenerator();
        TransportDataGenerator.TransportData data = generator.generateData(n, m, departures);
        generator.saveToJson(data, "resources/transport_data.json", n, m);

        // Inicijalizacija UI (View) i povezivanje logike kroz Controller.
        MainView view = new MainView();
        new MainController(view);

        Scene scene = new Scene(view.getRoot(), 1050, 750);

        primaryStage.setTitle("Transporting Routing App");

        // Učitavanje ikone prozora iz resources (route.png treba biti u istom package-u kao MainFX).
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("route.png")));

        primaryStage.setScene(scene);
        primaryStage.show();

        // Stilizovanje header-a tabele nakon prikaza (lookup radi tek kad se UI kreira).
        Platform.runLater(() -> {
            view.getTable().lookupAll(".column-header-background")
                    .forEach(g -> g.setStyle("-fx-background-color: #D87CAC;"));
            view.getTable().lookupAll(".column-header")
                    .forEach(g -> g.setStyle("-fx-background-color: #D87CAC;"));
        });
    }

    /**
     * Prikazuje dijalog za unos broja polazaka po stanici.
     * <p>
     * Validacija:
     * <ul>
     *   <li>dozvoljen je samo pozitivan cijeli broj</li>
     *   <li>ako korisnik otkaže unos, vraća se {@code null}</li>
     * </ul>
     * </p>
     *
     * @return broj polazaka po stanici ili {@code null} ako je korisnik odustao
     */
    private Integer askForDeparturesPerStation() {
        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Podešavanje generatora");
        dialog.setHeaderText("Unesi broj polazaka po stanici (DEPARTURES_PER_STATION)");
        dialog.setContentText("Broj:");

        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return null;

            try {
                int val = Integer.parseInt(result.get().trim());
                if (val <= 0) throw new NumberFormatException();
                return val;
            } catch (NumberFormatException e) {
                dialog.setHeaderText("Greška: unesi pozitivan cijeli broj.");
                dialog.getEditor().setText("5");
            }
        }
    }

    /**
     * Standardni Java entry point.
     *
     * @param args argumenti komandne linije
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Prikazuje dijalog za unos dimenzija mape države (n × m).
     * <p>
     * Vraća {@code int[]{n, m}} ako su dimenzije validne (pozitivne),
     * inače vraća {@code null}. Ako korisnik klikne Cancel, vraća {@code null}.
     * </p>
     *
     * @return niz od 2 elementa: {@code [n, m]} ili {@code null} ako je unos otkazan/neispravan
     */
    private int[] askForDimensions() {

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Generisanje mape");
        dialog.setHeaderText("Unesite dimenzije mape države (n × m)");

        ButtonType ok = new ButtonType("Generiši", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField tfN = new TextField();
        tfN.setPromptText("n (redovi)");

        TextField tfM = new TextField();
        tfM.setPromptText("m (kolone)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("n:"), 0, 0);
        grid.add(tfN, 1, 0);
        grid.add(new Label("m:"), 0, 1);
        grid.add(tfM, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ok) {
                try {
                    int n = Integer.parseInt(tfN.getText());
                    int m = Integer.parseInt(tfM.getText());

                    if (n <= 0 || m <= 0) return null;
                    return new int[]{n, m};

                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<int[]> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
