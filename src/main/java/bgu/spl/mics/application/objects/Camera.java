package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {

    private int id;
    private int frequency;
    private STATUS status;
    private ArrayList<StampedDetectedObjects> detectedStampedObjectsList;

    public Camera(int id, int frequency, ArrayList<StampedDetectedObjects> detectedStampedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedStampedObjectsList = detectedStampedObjectsList;

    }
    public ArrayList <StampedDetectedObjects> getDetectedObjectsList() {
        return detectedStampedObjectsList;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
    public int getId() {
        return id;
    }

    public StampedDetectedObjects getDetectedObjectsAtTime(int tick) {
        for(StampedDetectedObjects s: detectedStampedObjectsList) {
            if(s.getTime() + getFrequency() == tick){
                detectedStampedObjectsList.remove(s);
                return s;
            }
        }
        return null;
    }
}
