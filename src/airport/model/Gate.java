package airport.model;

public class Gate {
    private final int id;
    private Airplane currentAirplane;

    public Gate(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public synchronized Airplane getCurrentAirplane() {
        return currentAirplane;
    }

    public synchronized void occupy(Airplane airplane) {
        this.currentAirplane = airplane;
    }

    public synchronized void release() {
        this.currentAirplane = null;
    }

    public synchronized boolean isFree() {
        return currentAirplane == null;
    }
}