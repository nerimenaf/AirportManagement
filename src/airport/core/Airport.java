package airport.core;

import java.util.ArrayList;
import java.util.List;

import airport.model.Airplane;
import airport.model.FlightType;
import airport.model.Gate;
import airport.model.Runway;
import airport.sync.AirportSynchronization;
import airport.utils.SimLogger;

public class Airport {

    private final List<Runway> runways = new ArrayList<>();
    private final List<Gate> gates = new ArrayList<>();

    private final List<Airplane> landingQueue = new ArrayList<>();
    private final List<Airplane> takeoffQueue = new ArrayList<>();

    private AirportSynchronization sync;
    private final SimLogger logger;

    private AirportObserver observer;

    private int nextPlaneId = 1;

    public Airport(int nbRunways, int nbGates,
                   AirportSynchronization sync,
                   SimLogger logger) {
        this.sync = sync;
        this.logger = logger;
        for (int i = 0; i < nbRunways; i++) {
            runways.add(new Runway(i + 1));
        }
        for (int i = 0; i < nbGates; i++) {
            gates.add(new Gate(i + 1));
        }
    }

    public synchronized void setObserver(AirportObserver observer) {
        this.observer = observer;
    }

    private synchronized void notifyObserver() {
        if (observer != null) {
            observer.onAirportStateChanged();
        }
    }

    public AirportSynchronization getSync() {
        return sync;
    }

    public synchronized void setSync(AirportSynchronization sync) {
        this.sync = sync;
    }

    public List<Runway> getRunways() {
        return runways;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public synchronized List<Airplane> getLandingQueueSnapshot() {
        return new ArrayList<>(landingQueue);
    }

    public synchronized List<Airplane> getTakeoffQueueSnapshot() {
        return new ArrayList<>(takeoffQueue);
    }

    public synchronized void addToLandingQueue(Airplane a) {
        landingQueue.add(a);
        notifyObserver();
    }

    public synchronized void removeFromLandingQueue(Airplane a) {
        landingQueue.remove(a);
        notifyObserver();
    }

    public synchronized void addToTakeoffQueue(Airplane a) {
        takeoffQueue.add(a);
        notifyObserver();
    }

    public synchronized void removeFromTakeoffQueue(Airplane a) {
        takeoffQueue.remove(a);
        notifyObserver();
    }

    public synchronized void onAirplaneStateChanged(Airplane a) {
        // Pour l’instant on se contente de rafraîchir la GUI
        notifyObserver();
    }

    public synchronized Runway occupyRunway(Airplane a) {
        for (Runway r : runways) {
            if (r.isFree()) {
                r.occupy(a);
                notifyObserver();
                return r;
            }
        }
        throw new IllegalStateException("Aucune piste libre alors que la synchro a accordé une piste.");
    }

    public synchronized void freeRunway(Runway r) {
        r.release();
        notifyObserver();
    }

    public synchronized Gate occupyGate(Airplane a) {
        for (Gate g : gates) {
            if (g.isFree()) {
                g.occupy(a);
                notifyObserver();
                return g;
            }
        }
        throw new IllegalStateException("Aucune porte libre alors que la synchro a accordé une porte.");
    }

    public synchronized void freeGate(Gate g) {
        g.release();
        notifyObserver();
    }

    private synchronized String nextPlaneId() {
        return "A" + (nextPlaneId++);
    }

    public Airplane createAndStartPlane(FlightType type, double speedFactor) {
        String id = nextPlaneId();
        Airplane a = new Airplane(id, type, this, logger, speedFactor);
        a.start();
        logger.log("Création avion " + id + " (" + type + ")");
        return a;
    }
}