package airport.sync;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockConditionSynchronization implements AirportSynchronization {

    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition runwayCond = lock.newCondition();
    private final Condition gateCond = lock.newCondition();

    private int freeRunways;
    private int freeGates;

    private int waitingLandingRunway = 0; // utilisé pour priorité

    public LockConditionSynchronization(int runways, int gates) {
        this.freeRunways = runways;
        this.freeGates = gates;
    }

    @Override
    public void requestRunwayForLanding() throws InterruptedException {
        lock.lock();
        try {
            waitingLandingRunway++;
            try {
                while (freeRunways == 0) {
                    runwayCond.await();
                }
                freeRunways--;
            } finally {
                waitingLandingRunway--;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void requestRunwayForTakeoff() throws InterruptedException {
        lock.lock();
        try {
            // priorité aux arrivées
            while (freeRunways == 0 || waitingLandingRunway > 0) {
                runwayCond.await();
            }
            freeRunways--;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void releaseRunway() {
        lock.lock();
        try {
            freeRunways++;
            runwayCond.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void requestGate() throws InterruptedException {
        lock.lock();
        try {
            while (freeGates == 0) {
                gateCond.await();
            }
            freeGates--;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void releaseGate() {
        lock.lock();
        try {
            freeGates++;
            gateCond.signalAll();
        } finally {
            lock.unlock();
        }
    }
}