package airport.sync;

public interface AirportSynchronization {

    void requestRunwayForLanding() throws InterruptedException;

    void requestRunwayForTakeoff() throws InterruptedException;

    void releaseRunway();

    void requestGate() throws InterruptedException;

    void releaseGate();
}