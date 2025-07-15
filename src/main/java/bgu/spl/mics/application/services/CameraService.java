package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * <p>
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private Camera camera;
    private StampedDetectedObjects lastframe;


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera service" + camera.getId());
        this.camera = camera;
        this.lastframe = null;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println("The service: " + getName() + " started");
        subscribeBroadcast(TickBroadcast.class, tb ->
        {
            if(camera.getDetectedObjectsList().isEmpty()){
                finishCameraService();
            }
            StampedDetectedObjects s = camera.getDetectedObjectsAtTime(tb.getTick());
            if (s != null) {
                int error = errorsCheck(s);
                if(error == -1){//no error
                    lastframe = s;
                    StampedDetectedObjects duplicate = duplicateStampedDetectedObjects(s);
                    DetectObjectsEvent doe = new DetectObjectsEvent(duplicate);
                    sendEvent(doe);
                    StatisticalFolder.getInstance().addNumDetectedObjects(s.getDetectedObjects().size());

                } else { //error
                    //s is the one with error, lastframe is the last frame and error is the index
                    errorOperation(lastframe, s, error);

                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, tb -> {
            if (tb.getMicroServiceClass() == TimeService.class) {
                this.camera.setStatus(STATUS.DOWN);
                terminate();
            }

        });
        subscribeBroadcast(CrashedBroadcast.class, cb -> {
            this.camera.setStatus(STATUS.ERROR);
            terminate();
        });
    }


    private int errorsCheck(StampedDetectedObjects DetectedObjectsList) {
        int result = -1;
        int i = 0;
        ArrayList<DetectedObject> detectedObjects = DetectedObjectsList.getDetectedObjects();
        for(DetectedObject d: detectedObjects){
            if (d.getId().equals("ERROR")) {
                result = i;
                break;
            }
            i++;
        }
        return result;
    }

    private void errorOperation(StampedDetectedObjects lastFrame, StampedDetectedObjects stampedError, int errorIndex) {
        this.camera.setStatus(STATUS.ERROR);
        String reason = stampedError.getDetectedObjects().get(errorIndex).getDescription();
        CrashedBroadcast cb = new CrashedBroadcast(stampedError.getTime());
        StampedDetectedObjects lastFrames = new StampedDetectedObjects(0, new ArrayList<DetectedObject>());
        if (lastFrame != null)
            lastFrames = lastFrame;
        ErrorGenerator.getInstance().setCrashReason(reason);
        ErrorGenerator.getInstance().setFaultySensor("Camera " + this.camera.getId());
        ErrorGenerator.getInstance().setLastFramesCamera(lastFrames);
        sendBroadcast(cb);
    }

    private void finishCameraService() {
        this.camera.setStatus(STATUS.DOWN);
        terminate();
        sendBroadcast(new TerminatedBroadcast(this.getClass()));
    }

    private StampedDetectedObjects duplicateStampedDetectedObjects(StampedDetectedObjects ToDuplicate) {
        StampedDetectedObjects newStampedDetectedObjects = new StampedDetectedObjects(ToDuplicate.getTime(), new ArrayList<>());
        for (int i = 0; i < ToDuplicate.getDetectedObjects().size(); i++)
            newStampedDetectedObjects.getDetectedObjects().add(ToDuplicate.getDetectedObjects().get(i));
        return newStampedDetectedObjects;

    }

}

