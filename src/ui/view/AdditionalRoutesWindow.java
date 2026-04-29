package ui.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.viewmodel.TopRouteItem;

import java.util.function.Consumer;

/**
 * Prozor za prikaz dodatnih (Top-5) ruta.
 *
 * Prikazuje listu ruta kao “kartice” u {@link ListView}-u. Svaka stavka sadrži:
 * <ul>
 *   <li>opis rute (npr. START → END)</li>
 *   <li>informacije: vrijeme, cijena i broj presjedanja</li>
 *   <li>dugme za kupovinu karte</li>
 * </ul>
 * Prozor je modalni u odnosu na vlasnički {@link Stage} (owner).
 *
 */
public class AdditionalRoutesWindow {

    /**
     * Prikazuje modalni prozor sa Top-5 rutama i akcijama nad izabranom rutom.
     *
     * Interakcija:
     * <ul>
     *   <li>Promjena selekcije u listi automatski poziva {@code onSelect}.</li>
     *   <li>Klik na “karticu” takođe selektuje stavku i poziva {@code onSelect}.</li>
     *   <li>Klik na dugme “Kupi kartu” poziva {@code onBuy} za tu stavku.</li>
     * </ul>
     *
     *
     * @param owner vlasnički prozor (Stage) nad kojim je ovaj prozor modalni
     * @param routes lista ruta za prikaz (TopRouteItem modeli za UI)
     * @param onSelect callback koji se poziva kada korisnik izabere rutu (prikaz u glavnom prozoru)
     * @param onBuy callback koji se poziva kada korisnik klikne “Kupi kartu”
     */
    public static void show(
            Stage owner,
            ObservableList<TopRouteItem> routes,
            Consumer<TopRouteItem> onSelect,
            Consumer<TopRouteItem> onBuy
    ) {

        Stage stage = new Stage();
        stage.setTitle("Top 5 ruta");
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        Label title = new Label("Top 5 ruta prema izabranom kriterijumu");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<TopRouteItem> list = new ListView<>(routes);

        // Selekcija kroz ListView (podržava i tastaturu) -> poziva se callback onSelect.
        list.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) onSelect.accept(sel);
        });

        list.setCellFactory(lv -> new ListCell<>() {

            /** Label za opis rute. */
            private final Label opis = new Label();

            /** Label za zbirne informacije (vrijeme, cijena, presjedanja). */
            private final Label info = new Label();

            /** Dugme za kupovinu karte za ovu rutu. */
            private final Button buy = new Button("Kupi kartu");

            /** Vertikalni blok sa tekstom (opis + info). */
            private final VBox text = new VBox(4, opis, info);

            /** Horizontalni red: tekst lijevo + dugme desno. */
            private final HBox row = new HBox(12, text, buy);

            {
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));
                HBox.setHgrow(text, Priority.ALWAYS);

                opis.setStyle("-fx-font-weight: bold;");
                info.setStyle("-fx-opacity: 0.85;");

                buy.setStyle("""
                    -fx-background-color: #D87CAC;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-background-radius: 6;
                    -fx-padding: 6 12 6 12;
                """);

                row.setStyle(
                        "-fx-border-color: #e6b3cc; " +
                                "-fx-border-radius: 10; " +
                                "-fx-background-radius: 10; " +
                                "-fx-background-color: #FFF5FA;"
                );
            }

            /**
             * Ažurira UI prikaz jedne stavke u listi.
             * <p>
             * Ako je stavka prazna, uklanja se grafika. U suprotnom:
             * <ul>
             *   <li>postavlja se tekst opisa i info linije</li>
             *   <li>klik na karticu selektuje stavku i poziva {@code onSelect}</li>
             *   <li>klik na “Kupi kartu” poziva {@code onBuy}</li>
             * </ul>
             * </p>
             *
             * @param item trenutna stavka (TopRouteItem)
             * @param empty {@code true} ako je ćelija prazna
             */
            @Override
            protected void updateItem(TopRouteItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    opis.setText(item.getOpis());
                    info.setText(
                            "Vrijeme: " + item.getVrijemeMin() + " min   |   Cijena: "
                                    + String.format("%.2f", item.getCijena()) + " KM   |   Presjedanja: "
                                    + item.getPresjedanja()
                    );

                    // Klik bilo gdje na karticu -> selektuj i pozovi onSelect.
                    row.setOnMouseClicked(e -> {
                        list.getSelectionModel().select(item);
                        onSelect.accept(item);
                    });

                    // Klik na dugme -> pozovi onBuy (kupovina karte).
                    buy.setOnAction(e -> onBuy.accept(item));

                    setGraphic(row);
                }
            }
        });

        Button close = new Button("Zatvori");
        close.setOnAction(e -> stage.close());

        VBox root = new VBox(10, title, list, close);
        root.setPadding(new Insets(12));
        root.setPrefSize(720, 450);

        stage.setScene(new Scene(root));
        stage.show();
    }
}
