package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.FileWriter;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fusionSlam;
    private int currTime;
    private volatile int terminatedCameras;
    private volatile int terminatedLiDars;
    private boolean poseFinished;
    private ArrayList<ArrayList<TrackedObject>> trackedObjects;


    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("fusion slam service");
        this.fusionSlam = fusionSlam;
        terminatedCameras = 0;
        terminatedLiDars = 0;
        poseFinished = false;
        trackedObjects = new ArrayList<>();

    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        System.out.println("The service: " + getName() + " started");
        subscribeBroadcast(TickBroadcast.class, tb -> {
                if (this.fusionSlam.getLastPose()!=null & trackedObjects!=null && !trackedObjects.isEmpty())
                    this.fusionSlam.createCoordinates(trackedObjects);
        });

        subscribeBroadcast(TerminatedBroadcast.class, tb -> {
            if (tb.getMicroServiceClass() == TimeService.class) {
                terminate();

                synchronized (GurionRockRunner.lock) {
                    GurionRockRunner.lock.notifyAll();
                }
            } else if (tb.getMicroServiceClass() == CameraService.class) {
                terminatedCameras++;

                if (canTermiante()) {
                    terminate();
                    sendBroadcast(new TerminatedBroadcast(this.getClass()));

                    synchronized (GurionRockRunner.lock) {
                        GurionRockRunner.lock.notifyAll();
                    }
                }

            } else if (tb.getMicroServiceClass() == LiDarService.class) {
                terminatedLiDars++;
                if (canTermiante()) {
                    terminate();
                    sendBroadcast(new TerminatedBroadcast(this.getClass()));
                    synchronized (GurionRockRunner.lock) {
                        GurionRockRunner.lock.notifyAll();
                    }
                }
            } else {
                if (tb.getMicroServiceClass() == PoseService.class) {
                    poseFinished = true;
                    if (canTermiante()) {
                        terminate();
                        sendBroadcast(new TerminatedBroadcast(this.getClass()));

                        synchronized (GurionRockRunner.lock) {
                            GurionRockRunner.lock.notifyAll();
                        }
                    }
                }
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, cb -> {
            ErrorGenerator.getInstance().setLastCoordinates(this.fusionSlam.getLandmarks());
            terminate();
            synchronized (GurionRockRunner.lock) {
                GurionRockRunner.lock.notifyAll();
            }

        });

        subscribeEvent(TrackedObjectsEvent.class, toe -> {
            trackedObjects.add(toe.getTrackedObjects());
            complete(toe, true);

        });


        subscribeEvent(PoseEvent.class, pe -> {
            this.fusionSlam.addPose(pe.getCurrPose());
            complete(pe, true);

        });

    }



    // Helper method to get poses up to the current error time

    private boolean canTermiante() {
        boolean result = false;
        if (terminatedCameras == GurionRockRunner.camerasNum && terminatedLiDars == GurionRockRunner.LiDarNum && poseFinished/*&& noEventMessages() */ )
        {
            if (!trackedObjects.isEmpty())
            {
                if (this.fusionSlam.getLastPose()!=null & trackedObjects!=null && trackedObjects.size()>0)
                    this.fusionSlam.createCoordinates(trackedObjects);

            }
            result = true;
        }
        return result;
    }

}

