package airport.sync;

import java.util.concurrent.Semaphore;

public class SemaphoreSynchronization implements AirportSynchronization {

    // Pistes
    private final Semaphore runwayPool;
    private final Semaphore landingQueue = new Semaphore(0, true);
    private final Semaphore takeoffQueue = new Semaphore(0, true);
    private final Semaphore mutex = new Semaphore(1, true);

    private int waitingLandingRunway = 0;
    private int waitingTakeoffRunway = 0;

    // Portes
    private final Semaphore gatePool;

    public SemaphoreSynchronization(int runways, int gates) {
        this.runwayPool = new Semaphore(runways, true);
        this.gatePool = new Semaphore(gates, true);
    }

    @Override
    public void requestRunwayForLanding() throws InterruptedException {
        mutex.acquire();
        if (runwayPool.tryAcquire()) {
            mutex.release();
            return;
        } else {
            waitingLandingRunway++;
            mutex.release();
            landingQueue.acquire();
            return;
        }
    }

    @Override
    public void requestRunwayForTakeoff() throws InterruptedException {
        mutex.acquire();
        if (runwayPool.tryAcquire() && waitingLandingRunway == 0) {
            mutex.release();
            return;
        } else {
            waitingTakeoffRunway++;
            mutex.release();
            takeoffQueue.acquire();
            return;
        }
    }

    @Override
    public void releaseRunway() {
        try {
            mutex.acquire();
            if (waitingLandingRunway > 0) {
                waitingLandingRunway--;
                landingQueue.release();
            } else if (waitingTakeoffRunway > 0) {
                waitingTakeoffRunway--;
                takeoffQueue.release();
            } else {
                runwayPool.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override
    public void requestGate() throws InterruptedException {
        gatePool.acquire();
    }

    @Override
    public void releaseGate() {
        gatePool.release();
    }
}