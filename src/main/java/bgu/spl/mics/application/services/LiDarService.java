
package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.*;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 *
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private LiDarWorkerTracker tracker;
    private int terminatedCameras;
    private ArrayList<StampedDetectedObjects> waitingList;
    ArrayList<StampedCloudPoints> data;


    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDar Service" + LiDarWorkerTracker.getId());
        this.tracker = LiDarWorkerTracker;
        terminatedCameras = 0;
        waitingList = new ArrayList<>();

    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     *  TrackedObject trackObject = trackObjectPreperation(detectedObject, stampedCloudPoints);
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println("The service: " + getName() + " started");
        /*
        when a lidar service receives the detected object event, he adds them to "last tracked object list" and then completes the event.
        in the tick time, it checks if the list includes something that needs to be sent now and creates a tracked event and sends it.
        The handling of the sending is done in the lidar service, but the "adding to the last tracked object list" must be done in the LidarWorker along with the "check if it includes something that needs to be sent"
        The error handling is in the service but the check for error (.equals("ERROR")) is in the Worker (it changes status to error and that's how the service knows there is an error)
         */
        subscribeBroadcast(TickBroadcast.class, tb -> {
            if(this.tracker.isLastIndex() && waitingList.isEmpty()){
                    this.tracker.setStatus(STATUS.DOWN);
                    terminate();
                    sendBroadcast(new TerminatedBroadcast(this.getClass()));
                }

                ArrayList<TrackedObject> s = tracker.trackObjectsAtTime(tb.getTick(),waitingList);
                if (this.tracker.getStatus() != STATUS.ERROR)
                {
                    if (s != null && s.size() > 0)
                    sendEvent(new TrackedObjectsEvent(s));
                }
                     else  //error
                        errorOperation(s.get(0).getTime());


            });


        subscribeBroadcast(TerminatedBroadcast.class, tb -> {
            if (tb.getMicroServiceClass() == TimeService.class) {
                this.tracker.setStatus(STATUS.DOWN);
                terminate();
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
            }
            else
            if (tb.getMicroServiceClass() == CameraService.class && waitingList.isEmpty()) {
                terminatedCameras++;
                if (terminatedCameras == GurionRockRunner.camerasNum/* && noEventMessages() */) {
                    terminate();
                    sendBroadcast(new TerminatedBroadcast(this.getClass()));
                }

            }
        });
        subscribeBroadcast(CrashedBroadcast.class, cb -> {
            this.tracker.setStatus(STATUS.ERROR);
            ErrorGenerator.getInstance().setLastFramesLidar(this.tracker.getLastTrackedObjects());
            terminate();
        });

        subscribeEvent(DetectObjectsEvent.class, doe -> {
            waitingList.add(doe.getStamps());
            complete(doe, true);

        });

    }



    private void errorOperation(int errorTime) {
        ErrorGenerator.getInstance().setCrashReason("sensor LiDar disconnected");
        ErrorGenerator.getInstance().setFaultySensor("Lidar " + this.tracker.getId());
        ErrorGenerator.getInstance().setLastFramesLidar(this.tracker.getLastTrackedObjects());
        CrashedBroadcast cb = new CrashedBroadcast(errorTime);
        sendBroadcast(cb);

    }






}
