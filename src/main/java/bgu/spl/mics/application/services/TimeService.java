
package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    int duration;
    int tickTime;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("Time Service");
        this.tickTime = TickTime;
        this.duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        System.out.println("The service: " + getName() + " started");

        subscribeBroadcast(TerminatedBroadcast.class, tb -> {
            if (tb.getMicroServiceClass() == FusionSlamService.class) {
                Thread.currentThread().interrupt();
                terminate();
                synchronized (GurionRockRunner.lock) {
                    GurionRockRunner.lock.notifyAll();
                }
            }
        });

        subscribeBroadcast(TickBroadcast.class, tb -> {
            try {
                int currentTick = tb.getTick();
                while (currentTick < this.duration && !Thread.currentThread().isInterrupted()) {
                    if (GurionRockRunner.isFinished) {
                        terminate();
                        synchronized (GurionRockRunner.lock) {
                            GurionRockRunner.lock.notifyAll();
                        }
                        break;
                    }

                    currentTick++;
                    sendBroadcast(new TickBroadcast(currentTick));
                    StatisticalFolder.getInstance().setSystemRuntime(currentTick);

                    // Instead of a hard Thread.sleep, handle interruption more gracefully
                    long sleepTime = tickTime * 1000;
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < sleepTime) {
                        if (Thread.currentThread().isInterrupted()) {
                            return; // Exit if interrupted
                        }
                        Thread.sleep(10);  // Sleep in small intervals to allow interruption
                    }
                }

                if (currentTick >= this.duration) {
                    StatisticalFolder.getInstance().setSystemRuntime(currentTick);
                    terminate();
                    sendBroadcast(new TerminatedBroadcast(this.getClass()));
                    synchronized (GurionRockRunner.lock) {
                        GurionRockRunner.lock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                if (GurionRockRunner.isFinished) {
                    terminate();
                    synchronized (GurionRockRunner.lock) {
                        GurionRockRunner.lock.notifyAll();
                    }
                } else {
                    Thread.currentThread().interrupt(); // Re-interrupt the thread to so it will terminate
                }
            } catch (Exception e) {
                System.err.println("An error occurred in " + getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        sendBroadcast(new TickBroadcast(0));
    }




}

