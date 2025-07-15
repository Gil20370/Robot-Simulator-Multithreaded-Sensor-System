package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    GPSIMU gpsimu;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("pose Service");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        System.out.println("The service: " + getName() + " started");
        subscribeBroadcast(TickBroadcast.class, tb -> {
            Pose currPose=this.gpsimu.getCurrentPose(tb.getTick());
            if (currPose!=null)
                sendEvent(new PoseEvent(currPose));
            if (this.gpsimu.getStatus()==STATUS.DOWN) {
                terminate();
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
            }

            });
        subscribeBroadcast(TerminatedBroadcast.class, tb -> {
            if (tb.getMicroServiceClass()==TimeService.class) {
                this.gpsimu.setStatus(STATUS.DOWN);
                terminate();
            }

            });
        subscribeBroadcast(CrashedBroadcast.class, cb -> {
            this.gpsimu.setStatus(STATUS.ERROR);
            this.gpsimu.getPosesUpToTime(cb.getErrorTime());
            terminate();
            });


    }
}
