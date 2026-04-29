package ui.controller;

import billing.ReceiptsInfo;
import billing.ReceiptPrinter;
import io.TransportDataLoader;
import io.TransportDataMapper;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import model.City;
import model.Departure;
import model.Station;
import routing.*;
import ui.view.AdditionalRoutesWindow;
import ui.viewmodel.MainView;
import ui.viewmodel.MapRenderer;
import ui.viewmodel.RouteRow;
import ui.viewmodel.TopRouteItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Glavni kontroler aplikacije (JavaFX MVC).
 *
 * Zadužen je za:
 * <ul>
 *   <li>učitavanje podataka iz JSON-a i izgradnju grafa</li>
 *   <li>povezivanje UI događaja (pretraga, kupovina, top-5 rute)</li>
 *   <li>pozivanje logike rutiranja i ažuriranje prikaza (mapa + tabela)</li>
 *   <li>rad sa računima (kupovina, statistika)</li>
 * </ul>
 *
 */
public class MainController {

    /** Glavni View sloj aplikacije. */
    private final MainView view;

    /** Renderer za crtanje gradova, stanica i ruta na mapi. */
    private final MapRenderer renderer;

    /** Servis za pronalaženje optimalnih ruta. */
    private RouteService routeService;

    /** Trenutno prikazana/izabrana ruta. */
    private Route currentRoute;

    /** Lista svih gradova u državi. */
    private List<City> cities;

    /** Lista svih stanica. */
    private List<Station> stations;

    /** Transportni graf (stanice + polasci). */
    private Graph graph;

    /** Lista svih polazaka. */
    private List<Departure> departures;

    /**
     * Kreira glavni kontroler i inicijalizuje aplikaciju.
     *
     * @param view glavni View objekat
     */
    public MainController(MainView view) {
        this.view = view;
        this.renderer = new MapRenderer(view.getMapCanvas());
        init();
    }

    /**
     * Inicijalizuje UI i logiku aplikacije.
     * <p>
     * Popunjava kriterijume, učitava podatke, crta početno stanje mape
     * i registruje event-handlere za dugmad.
     * </p>
     */
    private void init() {
        view.getCbCriteria().getItems().addAll("TIME", "COST", "TRANSFERS");

        loadAllDataAndBuildGraph();

        renderer.drawCities(cities);
        renderer.drawStations(stations);

        updateReceiptsInfo();

        view.getBtnSearch().setOnAction(e -> onSearch());
        view.getBtnBuy().setOnAction(e -> onBuyCurrentRoute());
        view.getBtnTop5().setOnAction(e -> openTop5());
    }

    /**
     * Učitava transportne podatke iz JSON-a i gradi graf.
     * <p>
     * Koraci:
     * <ol>
     *   <li>učitavanje JSON fajla</li>
     *   <li>kreiranje gradova</li>
     *   <li>kreiranje stanica i polazaka</li>
     *   <li>izgradnja grafa i dodavanje transfer grana</li>
     *   <li>inicijalizacija {@link RouteService}</li>
     * </ol>
     * </p>
     */
    private void loadAllDataAndBuildGraph() {
        try {
            TransportDataLoader data =
                    TransportDataLoader.load("resources/transport_data.json");

            cities = TransportDataMapper.createCities(data)
                    .stream()
                    .distinct()
                    .toList();

            view.getCbStart().getItems().setAll(cities);
            view.getCbEnd().getItems().setAll(cities);

            stations = TransportDataMapper.createStations(data, cities);
            departures = TransportDataMapper.createDepartures(data, stations);

            graph = new Graph();
            for (Station s : stations) graph.addStation(s);
            for (Departure d : departures) graph.addDeparture(d);

            graph.addTransfers();

            routeService = new RouteService(graph);

            view.getLblSummary().setText(
                    "Graf učitan: " + stations.size() +
                            " stanica, " + departures.size() + " polazaka."
            );

        } catch (Exception e) {
            view.getLblSummary().setText(
                    "Greška pri učitavanju podataka: " + e.getMessage()
            );
        }
    }

    /**
     * Obrada klika na dugme „Pronađi rutu”.
     * <p>
     * Pronalazi optimalnu rutu prema izabranom kriterijumu,
     * iscrtava je na mapi i popunjava tabelu koraka.
     * </p>
     */
    private void onSearch() {
        City start = view.getCbStart().getValue();
        City end = view.getCbEnd().getValue();
        String crit = view.getCbCriteria().getValue();

        view.getTable().getItems().clear();
        view.getLblSummary().setText("");

        if (start == null || end == null || crit == null) {
            view.getLblSummary().setText("Popuni polazište, odredište i kriterijum.");
            return;
        }

        RouteService.Criterion criterion =
                RouteService.Criterion.valueOf(crit);

        Route route = routeService.findBestRoute(start, end, criterion);
        currentRoute = route;

        if (route == null) {
            view.getLblSummary().setText(
                    "Nema rute za " + start.getName() + " -> " + end.getName()
            );
            return;
        }

        renderer.drawCities(cities);
        renderer.drawRoute(route.getSteps());
        renderer.drawStations(stations);

        route.getSteps().forEach(dep -> {
            if (!dep.isTransfer()) {
                view.getTable().getItems().add(toRow(dep));
            }
        });

        view.getLblSummary().setText(
                "Ukupno: " +
                        formatMinutes(route.getTotalTime()) +
                        ", " +
                        route.getTotalCost() +
                        " novčanih jedinica."
        );
    }

    /**
     * Otvara prozor sa dodatnim (Top-5) rutama.
     * <p>
     * Kombinuje rute dobijene iz svih kombinacija
     * početne i krajnje stanice (BUS/TRAIN),
     * deduplikuje ih i sortira po težini.
     * </p>
     */
    private void openTop5() {
        City startCity = view.getCbStart().getValue();
        City endCity = view.getCbEnd().getValue();
        String crit = view.getCbCriteria().getValue();

        if (startCity == null || endCity == null || crit == null) {
            view.getLblSummary().setText("Popuni polazište, odredište i kriterijum.");
            return;
        }

        CostFunction costFun = switch (crit) {
            case "TIME" -> new TimeCost();
            case "COST" -> new PriceCost();
            case "TRANSFERS" -> new TransferCost();
            default -> new TimeCost();
        };

        List<Station> starts =
                List.of(startCity.getBusStation(), startCity.getTrainStation());
        List<Station> ends =
                List.of(endCity.getBusStation(), endCity.getTrainStation());

        top5Routes top5 = new top5Routes();
        List<Route> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Station s : starts) {
            for (Station e : ends) {
                List<Route> partial = top5.findTop5(graph, s, e, costFun);
                for (Route r : partial) {
                    if (seen.add(top5.signature(r))) {
                        all.add(r);
                    }
                }
            }
        }

        all.sort(
                Comparator.comparingDouble(Route::getTotalWeight)
                        .thenComparing(top5::signature)
        );

        List<Route> best5 = all.size() > 5 ? all.subList(0, 5) : all;

        var items = FXCollections.observableArrayList(
                best5.stream()
                        .map(this::toTopRouteItem)
                        .collect(Collectors.toList())
        );

        Stage owner = (Stage) view.getRoot().getScene().getWindow();

        AdditionalRoutesWindow.show(
                owner,
                items,
                selected -> applyChosenRoute(selected.getRoute()),
                selected -> {
                    applyChosenRoute(selected.getRoute());
                    onBuyCurrentRoute();
                }
        );
    }

    /**
     * Prikazuje izabranu rutu (npr. iz Top-5 prozora) u glavnom UI-ju.
     *
     * @param chosen izabrana ruta
     */
    private void applyChosenRoute(Route chosen) {
        currentRoute = chosen;
        if (chosen == null) return;

        renderer.drawCities(cities);
        renderer.drawRoute(chosen.getSteps());
        renderer.drawStations(stations);

        view.getTable().getItems().clear();
        chosen.getSteps().forEach(dep -> {
            if (!dep.isTransfer()) {
                view.getTable().getItems().add(toRow(dep));
            }
        });

        view.getLblSummary().setText(
                "Izabrana TOP ruta: " +
                        formatMinutes(chosen.getTotalTime()) +
                        ", " + chosen.getTotalCost()
        );
    }

    /**
     * Kupuje kartu za trenutno izabranu rutu i ažurira statistiku računa.
     */
    private void onBuyCurrentRoute() {
        if (currentRoute == null) {
            view.getLblSummary().setText("Prvo pronađi rutu.");
            return;
        }

        boolean ok = ReceiptPrinter.buyTicket(currentRoute);
        if (ok) updateReceiptsInfo();

        view.getLblSummary().setText(
                ok ? "Karta kupljena ✅" : "Kupovina nije uspjela ❌"
        );
    }

    /**
     * Ažurira prikaz broja računa i ukupne sume.
     */
    private void updateReceiptsInfo() {
        int count = ReceiptsInfo.countReceipts();
        double sum = ReceiptsInfo.sumAllPrices();

        view.getLblReceiptsCount().setText("Ukupan broj računa: " + count);
        view.getLblReceiptsSum().setText("Ukupna suma: " + String.format("%.2f", sum));
    }

    /**
     * Pomoćna metoda za formatiranje vremena izraženog u minutama.
     *
     * @param totalMinutes ukupno minuta
     * @return formatirani string (npr. {@code 2h 15min})
     */
    private String formatMinutes(long totalMinutes) {
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        if (h == 0) return m + "min";
        return h + "h " + m + "min";
    }

    /**
     * Kreira red za tabelarni prikaz rute.
     *
     * @param d polazak
     * @return objekat {@link RouteRow} za tabelu
     */
    private RouteRow toRow(Departure d) {
        String polazak = d.getFrom().getName() + " (" + d.getDepartureTime() + ")";
        String dolazak = d.getTo().getName() + " (" + d.getArrivalTime() + ")";

        String tip = d.isTransfer() ? "Transfer" :
                (d.getFrom().getType() == Station.Type.BUS ? "Autobus" : "Voz");

        return new RouteRow(polazak, dolazak, tip, d.getPrice());
    }

    /**
     * Kreira stavku za Top-5 listu ruta.
     *
     * @param r ruta
     * @return {@link TopRouteItem} za UI prikaz
     */
    private TopRouteItem toTopRouteItem(Route r) {
        String opis = shortRouteText(r);
        return new TopRouteItem(
                opis,
                r.getTotalTime(),
                r.getTotalCost(),
                r.getTotalTransfers(),
                r
        );
    }

    /**
     * Kratak tekstualni opis rute (start → end).
     *
     * @param r ruta
     * @return kratak opis rute
     */
    private String shortRouteText(Route r) {
        if (r.getSteps() == null || r.getSteps().isEmpty()) return "Ruta";
        Departure first = r.getSteps().get(0);
        Departure last = r.getSteps().get(r.getSteps().size() - 1);
        return first.getFrom().getName() + " → " + last.getTo().getName();
    }
}
