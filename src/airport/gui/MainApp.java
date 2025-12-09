package airport.gui;

import airport.core.Airport;
import airport.core.AirportObserver;
import airport.model.Airplane;
import airport.model.FlightType;
import airport.model.Gate;
import airport.model.Runway;
import airport.sync.AirportSynchronization;
import airport.sync.LockConditionSynchronization;
import airport.sync.MonitorSynchronization;
import airport.sync.SemaphoreSynchronization;
import airport.utils.SimLogger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application implements AirportObserver {

    private Airport airport;
    private SimLogger logger;

    private TextArea logArea;
    private ListView<String> landingQueueView;
    private ListView<String> takeoffQueueView;
    private FlowPane runwaysPane;
    private FlowPane gatesPane;

    private Spinner<Integer> runwaySpinner;
    private Spinner<Integer> gateSpinner;
    private Slider speedSlider;

    private ToggleGroup syncGroup;

    private final List<Region> runwayRects = new ArrayList<>();
    private final List<Label> runwayLabels = new ArrayList<>();
    private final List<Region> gateRects = new ArrayList<>();
    private final List<Label> gateLabels = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // Composants de base
        logArea = new TextArea();
        logArea.setEditable(false);

        landingQueueView = new ListView<>();
        takeoffQueueView = new ListView<>();

        runwaysPane = new FlowPane(5, 5);
        gatesPane = new FlowPane(5, 5);

        runwaySpinner = new Spinner<>(1, 5, 2);
        gateSpinner = new Spinner<>(1, 10, 4);

        speedSlider = new Slider(0.5, 3.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);

        logger = new SimLogger(logArea);

        // Synchro par défaut : Moniteur
        AirportSynchronization sync = new MonitorSynchronization(2, 4);
        airport = new Airport(2, 4, sync, logger);
        airport.setObserver(this);

        BorderPane root = new BorderPane();
        // Fond "ciel de nuit"
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #021529, #074f8a);"
        );
        root.setTop(createControlPanel());
        root.setCenter(createCenterPanel());
        root.setBottom(createLogPanel());

        drawRunwaysAndGates();

        Scene scene = new Scene(root, 1400, 700);
        stage.setScene(scene);
        stage.setTitle("Airport Management");
        stage.show();
    }

    // --------- Panneau de contrôle (haut) ----------

    private Pane createControlPanel() {
        // Conteneur vertical : titre + barre de contrôle
        VBox rootBox = new VBox(6);
        rootBox.setPadding(new Insets(10, 10, 0, 10));

        Label title = new Label("Control Tower");
        title.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label("Simulation de trafic aérien");
        subtitle.setStyle(
                "-fx-text-fill: #bbdefb;" +
                "-fx-font-size: 11px;"
        );

        // Barre principale de contrôle
        HBox box = new HBox(12);
        box.setPadding(new Insets(8));
        box.setStyle(
                "-fx-background-color: linear-gradient(to right,#eceff1,#ffffff);" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: rgba(0,0,0,0.25);" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0, 0, 4);"
        );

        // Choix de la synchro
        syncGroup = new ToggleGroup();
        RadioButton rbMonitor = new RadioButton("Moniteur");
        RadioButton rbSemaphore = new RadioButton("Sémaphore");
        RadioButton rbLock = new RadioButton("ReentrantLock");

        rbMonitor.setToggleGroup(syncGroup);
        rbSemaphore.setToggleGroup(syncGroup);
        rbLock.setToggleGroup(syncGroup);
        rbMonitor.setSelected(true);

        rbMonitor.setStyle("-fx-font-weight: bold;");
        rbSemaphore.setStyle("-fx-font-weight: bold;");
        rbLock.setStyle("-fx-font-weight: bold;");

        syncGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                RadioButton rb = (RadioButton) newT;
                restartAirport(rb.getText());
            }
        });

        // Styles pour les boutons
        String greenBtnStyle =
                "-fx-background-color: linear-gradient(#66bb6a,#2e7d32);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;";
        String orangeBtnStyle =
                "-fx-background-color: linear-gradient(#ffb74d,#f57c00);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;";
        String redBtnStyle =
                "-fx-background-color: linear-gradient(#ef5350,#c62828);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;";

        // Boutons
        Button addArrivalBtn = new Button("Ajouter ARRIVÉE");
        addArrivalBtn.setStyle(greenBtnStyle);
        addArrivalBtn.setOnAction(e -> onAddArrival());

        Button addDepartureBtn = new Button("Ajouter DÉPART");
        addDepartureBtn.setStyle(orangeBtnStyle);
        addDepartureBtn.setOnAction(e -> onAddDeparture());

        Button stressBtn = new Button("Stress test");
        stressBtn.setStyle(redBtnStyle);
        stressBtn.setOnAction(e -> onStressTest());

        // Un spacer pour pousser les boutons à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(
                new Label("Synchro :"), rbMonitor, rbSemaphore, rbLock,
                new Label("Pistes :"), runwaySpinner,
                new Label("Portes :"), gateSpinner,
                new Label("Vitesse :"), speedSlider,
                spacer,  // pousse les boutons vers la droite
                addArrivalBtn, addDepartureBtn, stressBtn
        );

        rootBox.getChildren().addAll(title, subtitle, box);
        return rootBox;
    }

    // --------- Centre : pistes / portes / files ----------

    private Pane createCenterPanel() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(15);
        grid.setVgap(10);

        VBox runwaysBox = new VBox(5, new Label("Pistes"), runwaysPane);
        runwaysBox.setPadding(new Insets(10));
        runwaysBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.90);" +
                "-fx-background-radius: 10;"
        );

        VBox gatesBox = new VBox(5, new Label("Portes"), gatesPane);
        gatesBox.setPadding(new Insets(10));
        gatesBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.90);" +
                "-fx-background-radius: 10;"
        );

        VBox queuesBox = new VBox(5,
                new Label("File Arrivées"), landingQueueView,
                new Label("File Décollages"), takeoffQueueView
        );
        queuesBox.setPadding(new Insets(10));
        queuesBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.90);" +
                "-fx-background-radius: 10;"
        );
        VBox.setVgrow(landingQueueView, Priority.ALWAYS);
        VBox.setVgrow(takeoffQueueView, Priority.ALWAYS);

        grid.add(runwaysBox, 0, 0);
        grid.add(gatesBox, 1, 0);
        grid.add(queuesBox, 2, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        return grid;
    }

    // --------- Bas : logs ----------

    private Pane createLogPanel() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: rgba(0,0,0,0.65);" +
                "-fx-background-radius: 10 10 0 0;"
        );

        Label title = new Label("Logs");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        logArea.setStyle(
                "-fx-control-inner-background: black;" +
                "-fx-text-fill: #00ff9d;" +
                "-fx-font-family: Consolas, monospace;"
        );

        box.getChildren().addAll(title, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        return box;
    }

    // --------- Dessin des pistes / portes ----------

    private void drawRunwaysAndGates() {
        runwaysPane.getChildren().clear();
        gatesPane.getChildren().clear();
        runwayRects.clear();
        runwayLabels.clear();
        gateRects.clear();
        gateLabels.clear();

        // Pistes
        for (Runway r : airport.getRunways()) {
            Region rect = new Region();
            rect.setPrefSize(80, 30);
            rect.setStyle(
                    "-fx-border-color: #1b5e20;" +
                    "-fx-border-radius: 5;" +
                    "-fx-background-radius: 5;" +
                    "-fx-background-color: linear-gradient(to bottom,#66bb6a,#2e7d32);"
            );
            Label label = new Label("Piste " + r.getId());
            VBox vbox = new VBox(2, rect, label);
            runwaysPane.getChildren().add(vbox);
            runwayRects.add(rect);
            runwayLabels.add(label);
        }

        // Portes
        for (Gate g : airport.getGates()) {
            Region rect = new Region();
            rect.setPrefSize(60, 30);
            rect.setStyle(
                    "-fx-border-color: #0d47a1;" +
                    "-fx-border-radius: 5;" +
                    "-fx-background-radius: 5;" +
                    "-fx-background-color: linear-gradient(to bottom,#64b5f6,#1e88e5);"
            );
            Label label = new Label("Gate " + g.getId());
            VBox vbox = new VBox(2, rect, label);
            gatesPane.getChildren().add(vbox);
            gateRects.add(rect);
            gateLabels.add(label);
        }
    }

    // --------- Redémarrage de l'aéroport selon la synchro ----------

    private void restartAirport(String syncName) {
        int nbRunways = runwaySpinner.getValue();
        int nbGates = gateSpinner.getValue();

        AirportSynchronization sync;
        if ("Sémaphore".equals(syncName)) {
            sync = new SemaphoreSynchronization(nbRunways, nbGates);
        } else if ("Moniteur".equals(syncName)) {
            sync = new MonitorSynchronization(nbRunways, nbGates);
        } else { // ReentrantLock
            sync = new LockConditionSynchronization(nbRunways, nbGates);
        }

        airport = new Airport(nbRunways, nbGates, sync, logger);
        airport.setObserver(this);
        drawRunwaysAndGates();
        landingQueueView.getItems().clear();
        takeoffQueueView.getItems().clear();
        logger.log("Aéroport redémarré avec " + syncName);
    }

    // --------- Actions boutons ARRIVEE / DEPART ----------

    private void onAddArrival() {
        double speedFactor = speedSlider.getValue();
        airport.createAndStartPlane(FlightType.ARRIVEE, speedFactor);
    }

    private void onAddDeparture() {
        double speedFactor = speedSlider.getValue();
        airport.createAndStartPlane(FlightType.DEPART, speedFactor);
    }

    // --------- Callback de l'aéroport (observer) ----------

    @Override
    public void onAirportStateChanged() {
        Platform.runLater(this::refreshGui);
    }

    // --------- Rafraîchissement graphique ----------

    private void refreshGui() {
        // Files d’attente
        landingQueueView.getItems().clear();
        for (Airplane a : airport.getLandingQueueSnapshot()) {
            landingQueueView.getItems().add(a.getAirplaneId() + " - " + a.getAirplaneState());
        }

        takeoffQueueView.getItems().clear();
        for (Airplane a : airport.getTakeoffQueueSnapshot()) {
            takeoffQueueView.getItems().add(a.getAirplaneId() + " - " + a.getAirplaneState());
        }

        // Pistes
        List<Runway> runways = airport.getRunways();
        for (int i = 0; i < runways.size(); i++) {
            Runway r = runways.get(i);
            Region rect = runwayRects.get(i);
            Label label = runwayLabels.get(i);
            Airplane current = r.getCurrentAirplane();
            if (current == null) {
                rect.setStyle(
                        "-fx-border-color: #1b5e20;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-color: linear-gradient(to bottom,#66bb6a,#2e7d32);"
                );
                label.setText("Piste " + r.getId());
            } else {
                rect.setStyle(
                        "-fx-border-color: #b71c1c;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-color: linear-gradient(to bottom,#ef5350,#c62828);"
                );
                label.setText("Piste " + r.getId() + " : " + current.getAirplaneId());
            }
        }

        // Portes
        List<Gate> gates = airport.getGates();
        for (int i = 0; i < gates.size(); i++) {
            Gate g = gates.get(i);
            Region rect = gateRects.get(i);
            Label label = gateLabels.get(i);
            Airplane current = g.getCurrentAirplane();
            if (current == null) {
                rect.setStyle(
                        "-fx-border-color: #0d47a1;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-color: linear-gradient(to bottom,#64b5f6,#1e88e5);"
                );
                label.setText("Gate " + g.getId());
            } else {
                rect.setStyle(
                        "-fx-border-color: #f57c00;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-color: linear-gradient(to bottom,#ffb74d,#f57c00);"
                );
                label.setText("Gate " + g.getId() + " : " + current.getAirplaneId());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    private void onStressTest() {
        double speedFactor = speedSlider.getValue();

        int nbArrivals = 10;   // nombre d’arrivées à lancer
        int nbDepartures = 5;  // nombre de départs à lancer

        for (int i = 0; i < nbArrivals; i++) {
            airport.createAndStartPlane(FlightType.ARRIVEE, speedFactor);
        }
        for (int i = 0; i < nbDepartures; i++) {
            airport.createAndStartPlane(FlightType.DEPART, speedFactor);
        }

        logger.log("Stress test lancé : " + nbArrivals + " arrivées et " +
                   nbDepartures + " départs.");
    }
}