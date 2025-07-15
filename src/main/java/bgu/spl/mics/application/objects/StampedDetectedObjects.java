package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private ArrayList<DetectedObject> detectedObjecs;

    public StampedDetectedObjects(int time, ArrayList<DetectedObject> detectedObjecs) {
        this.time = time;
        this.detectedObjecs = detectedObjecs;
    }

    public int getTime() {
        return time;
    }

    public ArrayList<DetectedObject> getDetectedObjects() {
        return detectedObjecs;
    }


}
