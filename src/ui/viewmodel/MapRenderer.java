package ui.viewmodel;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.City;
import model.Departure;
import model.Station;

import java.util.List;

/**
 * Renderuje (iscrtava) mapu države na JavaFX {@link Canvas}-u.
 *
 * Vizuelni prikaz:
 * <ul>
 *   <li>Gradovi se crtaju kao krugovi u mreži (matrica), gdje svaka ćelija ima fiksnu veličinu.</li>
 *   <li>Stanice (BUS/TRAIN) se crtaju kao male tačke unutar grada (BUS lijevo, TRAIN desno).</li>
 *   <li>Ruta se crta kao linije između stanica u koracima rute (po listi {@link Departure}).</li>
 * </ul>
 *
 *
 * Klasa ne posjeduje podatke o mreži, već dobija liste gradova/stanica/koraka rute
 * i iscrtava ih trenutnim {@link GraphicsContext}-om.
 *
 */
public class MapRenderer {

    /** GraphicsContext koji se koristi za crtanje na canvasu. */
    private final GraphicsContext gc;

    /**
     * Veličina jedne ćelije mreže (u pikselima). Grad na koordinatama (x,y)
     * se mapira u ćeliju [x * CELL_SIZE, y * CELL_SIZE].
     */
    private static final int CELL_SIZE = 80;

    /**
     * Kreira renderer vezan za dati {@link Canvas}.
     *
     * @param canvas canvas na kojem se vrši iscrtavanje
     */
    public MapRenderer(Canvas canvas) {
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * Iscrtava sve gradove i čisti canvas prije crtanja.
     * <p>
     * Prije crtanja se prilagođava veličina canvasa tako da svi gradovi stanu
     * (uključujući marginu za tekst naziva grada).
     * </p>
     *
     * @param cities lista gradova koji se crtaju
     */
    public void drawCities(List<City> cities) {
        ensureCanvasSizeForCities(cities);

        Canvas canvas = gc.getCanvas();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (City c : cities) {
            drawCity(c);
        }
    }

    /**
     * Iscrtava jedan grad kao krug u njegovoj mrežnoj ćeliji i ispisuje naziv ispod kruga.
     * <p>
     * Boja grada se bira deterministički na osnovu koordinata (x,y) kako bi se dobila
     * raznolikost boja kroz mapu.
     * </p>
     *
     * @param city grad koji se crta
     */
    private void drawCity(City city) {

        // gornji lijevi ugao ćelije u kojoj se nalazi grad
        int cellX = city.getyCordinate() * CELL_SIZE;
        int cellY = city.getxCordinate() * CELL_SIZE;

        // centar ćelije
        double cx = cellX + CELL_SIZE / 2.0;
        double cy = cellY + CELL_SIZE / 2.0;

        // radius kruga grada
        double r = CELL_SIZE / 2.5;

        // izbor boje grada na osnovu koordinata
        int key = (city.getxCordinate() * 31 + city.getyCordinate()) % 4;
        Color fillColor;
        switch (key) {
            case 0 -> fillColor = Color.web("#E8F1FD"); // pastel blue
            case 1 -> fillColor = Color.web("#FDECEF"); // pastel pink
            case 2 -> fillColor = Color.web("#FFF3CD"); // pastel yellow
            default -> fillColor = Color.web("#E6F4EA"); // pastel green
        }

        gc.setFill(fillColor);
        gc.fillOval(cx - r, cy - r, 2 * r, 2 * r);

        // okvir kruga
        gc.setStroke(Color.web("#999999"));
        gc.strokeOval(cx - r, cy - r, 2 * r, 2 * r);

        // naziv grada (centriran ispod kruga)
        gc.setFill(Color.web("#333333"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        Text text = new Text(city.getName());
        text.setFont(gc.getFont());
        double textWidth = text.getLayoutBounds().getWidth();

        gc.fillText(
                city.getName(),
                cx - textWidth / 2,
                cy + r + 14
        );
    }

    /**
     * Iscrtava sve stanice na mapi.
     *
     * @param stations lista stanica koje se crtaju
     */
    public void drawStations(List<Station> stations) {
        for (Station s : stations) {
            drawStation(s);
        }
    }

    /**
     * Osigurava da je canvas dovoljno velik da prikaže sve gradove.
     * <p>
     * Računa maksimalne koordinate (x,y) iz liste gradova, te postavlja
     * širinu/visinu canvasa na (max+1)*CELL_SIZE uz dodatnu marginu.
     * </p>
     *
     * @param cities lista gradova
     */
    private void ensureCanvasSizeForCities(List<City> cities) {
        if (cities == null || cities.isEmpty()) return;

        int maxX = 0;
        int maxY = 0;

        for (City c : cities) {
            if (c.getxCordinate() > maxX) maxX = c.getxCordinate();
            if (c.getyCordinate() > maxY) maxY = c.getyCordinate();
        }

        // +1 jer koordinate su 0-based, +margina da tekst imena stane
        double w = (maxY + 1) * CELL_SIZE + 80;
        double h = (maxX + 1) * CELL_SIZE + 80;

        Canvas canvas = gc.getCanvas();
        if (canvas.getWidth() < w) canvas.setWidth(w);
        if (canvas.getHeight() < h) canvas.setHeight(h);
    }

    /**
     * Iscrtava jednu stanicu u okviru njenog grada.
     * <p>
     * Stanice se crtaju kao male tačke u centru ćelije, sa offsetom:
     * BUS lijevo, TRAIN desno.
     * </p>
     *
     * @param s stanica koja se crta
     */
    private void drawStation(Station s) {
        City c = s.getCity();

        double cellX = c.getyCordinate() * CELL_SIZE;
        double cellY = c.getxCordinate() * CELL_SIZE;

        double centerX = cellX + CELL_SIZE / 2.0;
        double centerY = cellY + CELL_SIZE / 2.0;

        // offset unutar grada: bus lijevo, train desno
        double offset = 18;

        double x = centerX + (s.getType() == Station.Type.BUS ? -offset : offset);
        double y = centerY;

        double r = 6;

        // boje stanica po tipu
        gc.setFill(
                s.getType() == Station.Type.BUS
                        ? Color.web("#8EC5FC")
                        : Color.web("#FFE8A3")
        );

        gc.fillOval(x - r, y - r, 2 * r, 2 * r);

        // oznaka tipa (B/T) iznad tačke
        gc.setFill(Color.BLACK);
        gc.fillText(s.getType() == Station.Type.BUS ? "B" : "T", x - 3, y - 10);
    }

    /**
     * Iscrtava rutu kao linije između stanica u listi koraka.
     * <p>
     * Za svaki {@link Departure} crta se linija između tačke polazne i dolazne stanice.
     * Boja linije se bira po tipu polazne stanice (BUS/TRAIN).
     * </p>
     *
     * @param steps lista koraka rute (polasci/transferi)
     */
    public void drawRoute(List<Departure> steps) {
        if (steps == null) return;

        gc.setLineWidth(4);

        for (Departure d : steps) {
            Point2D p1 = stationPoint(d.getFrom());
            Point2D p2 = stationPoint(d.getTo());

            // boja po tipu polazne stanice
            if (d.getFrom().getType() == Station.Type.BUS) {
                gc.setStroke(Color.web("#8EC5FC"));
            } else {
                gc.setStroke(Color.web("#FFE8A3"));
            }

            gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }

        gc.setLineWidth(1);
    }

    /**
     * Računa tačku (x,y) na canvasu na kojoj se crta stanica.
     * <p>
     * Tačka je u centru ćelije grada, uz offset:
     * BUS lijevo, TRAIN desno.
     * </p>
     *
     * @param s stanica
     * @return koordinata stanice na canvasu
     */
    private Point2D stationPoint(Station s) {
        City c = s.getCity();

        double cellX = c.getyCordinate() * CELL_SIZE;
        double cellY = c.getxCordinate() * CELL_SIZE;

        double cx = cellX + CELL_SIZE / 2.0;
        double cy = cellY + CELL_SIZE / 2.0;

        double offset = 18;
        double x = cx + (s.getType() == Station.Type.BUS ? -offset : offset);
        double y = cy;

        return new Point2D(x, y);
    }
}
