package ui.viewmodel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.City;

/**
 * Glavni JavaFX View aplikacije.
 *
 * Organizacija layout-a:
 * <ul>
 *   <li>LEFT: forma za izbor polazišta, odredišta i kriterijuma + info o računima</li>
 *   <li>CENTER: mapa (Canvas unutar ScrollPane-a)</li>
 *   <li>BOTTOM: tabela koraka rute + akcije (Top-5, kupovina) + summary</li>
 * </ul>
 *
 */
public class MainView {

    /** Korijenski layout (glavni container ekrana). */
    private final BorderPane root = new BorderPane();

    // ================= LEFT =================

    /** ComboBox za izbor polaznog grada. */
    private final ComboBox<City> cbStart = new ComboBox<>();

    /** ComboBox za izbor odredišnog grada. */
    private final ComboBox<City> cbEnd = new ComboBox<>();

    /** ComboBox za izbor kriterijuma (TIME / COST / TRANSFERS). */
    private final ComboBox<String> cbCriteria = new ComboBox<>();

    /** Dugme za pokretanje pretrage rute. */
    private final Button btnSearch = new Button("Pronadji rutu");

    /** Label za prikaz ukupnog broja računa. */
    private final Label lblReceiptsCount = new Label();

    /** Label za prikaz ukupne sume svih računa. */
    private final Label lblReceiptsSum = new Label();

    // ================= CENTER =================

    /** Canvas za crtanje mape (gradovi, stanice, ruta). */
    private final Canvas mapCanvas = new Canvas(600, 450);

    // ================= BOTTOM =================

    /** Tabela za prikaz koraka rute. */
    private final TableView<RouteRow> table = new TableView<>();

    /** Kolona: polazak (stanica + vrijeme). */
    private final TableColumn<RouteRow, String> colPolazak = new TableColumn<>("Polazak");

    /** Kolona: dolazak (stanica + vrijeme). */
    private final TableColumn<RouteRow, String> colDolazak = new TableColumn<>("Dolazak");

    /** Kolona: tip prevoza (autobus / voz / transfer). */
    private final TableColumn<RouteRow, String> colTip = new TableColumn<>("Tip");

    /** Kolona: cijena karte. */
    private final TableColumn<RouteRow, Number> colCijena = new TableColumn<>("Cijena");

    /** Label za prikaz zbirnog rezultata (ukupno vrijeme + cijena). */
    private final Label lblSummary = new Label();

    /** Dugme za prikaz dodatnih (Top-5) ruta. */
    private final Button btnTop5 = new Button("Prikaz dodatnih ruta");

    /** Dugme za kupovinu karte za trenutno izabranu rutu. */
    private final Button btnBuy = new Button("Kupovina karte");

    /**
     * Kreira glavni view, gradi UI i podešava prikaz ComboBox-ova.
     */
    public MainView() {
        build();
        setupComboBoxRendering();
    }

    /**
     * Gradi kompletan raspored UI elemenata i primjenjuje osnovno stilizovanje.
     * <p>
     * LEFT panel: forma + info o računima. <br>
     * CENTER panel: mapa u ScrollPane-u (pannable). <br>
     * BOTTOM panel: tabela + akcije + summary.
     * </p>
     */
    private void build() {

        // ================= LEFT: FORMA =================
        VBox form = new VBox(10);
        form.setPrefWidth(220);
        form.setStyle("-fx-padding: 10; -fx-background-color: #f8c8dc;");

        btnSearch.setStyle("""
         -fx-background-color: #D87CAC;
         -fx-text-fill: white;
         -fx-font-size: 13px;
         -fx-font-weight: bold;
         -fx-background-radius: 2;
         -fx-padding: 6 12 6 12;
        """);

        Label lblStart = new Label("Polaziste");
        lblStart.setStyle("""
            -fx-text-fill: #333333;
            -fx-font-weight: bold;
        """);

        Label lblEnd = new Label("Odrediste");
        lblEnd.setStyle("""
            -fx-text-fill: #333333;
            -fx-font-weight: bold;
        """);

        Label criteria = new Label("Kriterijum:");
        criteria.setStyle("""
            -fx-text-fill: #333333;
            -fx-font-weight: bold;
        """);

        lblReceiptsCount.setStyle("""
            -fx-text-fill: #333333;
            -fx-font-weight: bold;
        """);

        lblReceiptsSum.setStyle("""
            -fx-text-fill: #333333;
            -fx-font-weight: bold;
        """);

        form.getChildren().addAll(
                lblStart, cbStart,
                lblEnd, cbEnd,
                criteria, cbCriteria,
                btnSearch,
                new Separator(),
                lblReceiptsCount,
                lblReceiptsSum
        );

        // ================= CENTER: MAPA =================
        ScrollPane mapScroll = new ScrollPane(mapCanvas);
        mapScroll.setPannable(true);
        mapScroll.setFitToWidth(false);
        mapScroll.setFitToHeight(false);
        mapScroll.setStyle("""
            -fx-background: transparent;
            -fx-background-color: linear-gradient(to bottom, #f8f9fb, #eef1f6);
        """);

        // ================= BOTTOM: TABELA + ACTIONS =================
        table.setPrefHeight(200);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colPolazak.setCellValueFactory(data -> data.getValue().polazakProperty());
        colDolazak.setCellValueFactory(data -> data.getValue().dolazakProperty());
        colTip.setCellValueFactory(data -> data.getValue().tipProperty());
        colCijena.setCellValueFactory(data -> data.getValue().cijenaProperty());

        table.getColumns().setAll(colPolazak, colDolazak, colTip, colCijena);

        // Naizmjenično bojenje redova radi čitljivosti.
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RouteRow item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) setStyle("-fx-background-color: #FFF5FA;");
                    else setStyle("-fx-background-color: #FDE4EF;");
                }
            }
        });

        lblSummary.setStyle("-fx-padding: 10; -fx-font-weight: bold;");
        lblSummary.setMinHeight(24);
        lblSummary.setWrapText(true);

        HBox actions = new HBox(10, btnTop5, btnBuy);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(5, 0, 5, 0));

        VBox bottom = new VBox(8, table, actions, lblSummary);
        bottom.setPadding(new Insets(10));

        // ================= POVEZIVANJE ZONA =================
        root.setLeft(form);
        root.setCenter(mapScroll);
        root.setBottom(bottom);
    }

    /**
     * Podešava prikaz vrijednosti u ComboBox-ovima za gradove.
     * <p>
     * Umjesto {@code City.toString()}, u listi i na dugmetu prikazuje se samo naziv grada
     * (npr. {@code G_2_3}).
     * </p>
     */
    private void setupComboBoxRendering() {
        cbStart.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(City item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        cbStart.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(City item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // End ComboBox koristi isti prikaz kao start.
        cbEnd.setCellFactory(cbStart.getCellFactory());
        cbEnd.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(City item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    /**
     * Vraća root node koji se postavlja u Scene.
     *
     * @return glavni {@link Parent} node view-a
     */
    public Parent getRoot() { return root; }

    /** @return ComboBox za polazni grad */
    public ComboBox<City> getCbStart() { return cbStart; }

    /** @return ComboBox za odredišni grad */
    public ComboBox<City> getCbEnd() { return cbEnd; }

    /** @return ComboBox za kriterijum */
    public ComboBox<String> getCbCriteria() { return cbCriteria; }

    /** @return dugme za pretragu rute */
    public Button getBtnSearch() { return btnSearch; }

    /** @return label za ukupan broj računa */
    public Label getLblReceiptsCount() { return lblReceiptsCount; }

    /** @return label za ukupnu sumu svih računa */
    public Label getLblReceiptsSum() { return lblReceiptsSum; }

    /** @return canvas za mapu */
    public Canvas getMapCanvas() { return mapCanvas; }

    /** @return label za summary rute */
    public Label getLblSummary() { return lblSummary; }

    /** @return dugme za prikaz Top-5 ruta */
    public Button getBtnTop5() { return btnTop5; }

    /** @return dugme za kupovinu karte */
    public Button getBtnBuy() { return btnBuy; }

    /** @return tabela koraka rute */
    public TableView<RouteRow> getTable() { return table; }
}
