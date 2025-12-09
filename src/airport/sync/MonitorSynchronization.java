package airport.sync;

public class MonitorSynchronization implements AirportSynchronization {

    private int freeRunways;
    private int freeGates;

    private int waitingLandingRunway = 0; // utilisé pour la priorité

    public MonitorSynchronization(int runways, int gates) {
        this.freeRunways = runways;
        this.freeGates = gates;
    }

    @Override
    public synchronized void requestRunwayForLanding() throws InterruptedException {
        waitingLandingRunway++;
        try {
            while (freeRunways == 0) {
                wait();
            }
            freeRunways--;
        } finally {
            waitingLandingRunway--;
        }
    }

    @Override
    public synchronized void requestRunwayForTakeoff() throws InterruptedException {
        // priorité aux arrivées : un départ ne prend une piste
        // que si aucune arrivée n'attend
        while (freeRunways == 0 || waitingLandingRunway > 0) {
            wait();
        }
        freeRunways--;
    }

    @Override
    public synchronized void releaseRunway() {
        freeRunways++;
        notifyAll();
    }

    @Override
    public synchronized void requestGate() throws InterruptedException {
        while (freeGates == 0) {
            wait();
        }
        freeGates--;
    }

    @Override
    public synchronized void releaseGate() {
        freeGates++;
        notifyAll();
    }
}