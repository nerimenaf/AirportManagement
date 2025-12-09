package airport.model;

import airport.core.Airport;
import airport.utils.SimLogger;

public class Airplane extends Thread {

    private final String id;
    private final FlightType type;
    private volatile AirplaneState state;
    private final Airport airport;
    private final SimLogger logger;
    private final double speedFactor;

    public Airplane(String id, FlightType type,
                    Airport airport, SimLogger logger,
                    double speedFactor) {
        this.id = id;
        this.type = type;
        this.airport = airport;
        this.logger = logger;
        this.speedFactor = speedFactor;
        this.state = (type == FlightType.ARRIVEE)
                ? AirplaneState.EN_VOL
                : AirplaneState.A_LA_PORTE;
    }

    public String getAirplaneId() {
        return id;
    }

    public FlightType getType() {
        return type;
    }

    public AirplaneState getAirplaneState() {
        return state;
    }

    private void setState(AirplaneState newState) {
        this.state = newState;
        airport.onAirplaneStateChanged(this);
    }

    private void sleepSim(long ms) throws InterruptedException {
        Thread.sleep((long) (ms / speedFactor));
    }

    @Override
    public void run() {
        try {
            if (type == FlightType.ARRIVEE) {
                runArrival();
            } else {
                runDeparture();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            setState(AirplaneState.TERMINE);
            logger.log("Avion " + id + " terminé.");
        }
    }

    private void runArrival() throws InterruptedException {
        logger.log("Avion " + id + " en vol.");
        sleepSim(1000 + (long) (Math.random() * 2000));

        setState(AirplaneState.ATTENTE_ATTERRISSAGE);
        airport.addToLandingQueue(this);

        airport.getSync().requestRunwayForLanding();
        Runway r = airport.occupyRunway(this);
        airport.removeFromLandingQueue(this);

        setState(AirplaneState.ATTERRISSAGE);
        logger.log("Avion " + id + " atterrit sur piste " + r.getId());
        sleepSim(2000);

        airport.freeRunway(r);
        airport.getSync().releaseRunway();

        setState(AirplaneState.ATTENTE_PORTE);
        airport.getSync().requestGate();
        Gate g = airport.occupyGate(this);

        setState(AirplaneState.A_LA_PORTE);
        logger.log("Avion " + id + " à la porte " + g.getId());
        sleepSim(3000);

        airport.freeGate(g);
        airport.getSync().releaseGate();

        setState(AirplaneState.AU_SOL);
    }

    private void runDeparture() throws InterruptedException {
        setState(AirplaneState.ATTENTE_PORTE);
        airport.getSync().requestGate();
        Gate g = airport.occupyGate(this);

        setState(AirplaneState.A_LA_PORTE);
        logger.log("Avion " + id + " à la porte " + g.getId() + " (préparation au départ)");
        sleepSim(3000);

        setState(AirplaneState.ATTENTE_DECOLLAGE);
        airport.addToTakeoffQueue(this);

        airport.freeGate(g);
        airport.getSync().releaseGate();

        airport.getSync().requestRunwayForTakeoff();
        Runway r = airport.occupyRunway(this);
        airport.removeFromTakeoffQueue(this);

        setState(AirplaneState.DECOLLAGE);
        logger.log("Avion " + id + " décolle de la piste " + r.getId());
        sleepSim(2000);

        airport.freeRunway(r);
        airport.getSync().releaseRunway();
    }
}