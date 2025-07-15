package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import bgu.spl.mics.MessageBusImpl;

public class ErrorGenerator{

    private static ErrorGenerator instance = null;
    private String crashReason;
    private String faultySensor;
    private StampedDetectedObjects lastFramesCamera;
    private ArrayList<TrackedObject> lastFramesLidar;
    private ArrayList<LandMark> coordinates;
    private ArrayList<Pose> lastPoses;


    private ErrorGenerator(){
        crashReason="all_good";
        faultySensor="";
        lastFramesCamera=null;
        lastFramesLidar=null;
        lastPoses=null;
        coordinates=null;

    }
    public synchronized static ErrorGenerator getInstance() {
		if(instance == null){
			instance = new ErrorGenerator();
		}
		return instance;
	}

    public synchronized void setCrashReason(String crashReason) {
        this.crashReason = crashReason;
    }

    public synchronized String getCrashReason() {
        return this.crashReason;
    }

    public ArrayList<TrackedObject>  getLastFramesLidar() {
        return lastFramesLidar;
    }

    public StampedDetectedObjects getLastFramesCamera() {
        return lastFramesCamera;
    }

    public ArrayList<Pose> getLastPoses() {
        return lastPoses;
    }

    public synchronized void setLastFramesCamera(StampedDetectedObjects lastFrames) {
        this.lastFramesCamera = lastFrames;
    }

    public synchronized void setLastFramesLidar(ArrayList<TrackedObject> lastFrames) {
        this.lastFramesLidar = lastFrames;
    }

    public synchronized String getFaultySensor() {
        return faultySensor;
    }

    public synchronized void setFaultySensor(String faultySensor) {
    this.faultySensor = faultySensor;
    }

    public synchronized void setLastPoses(ArrayList<Pose> lastPoses) {
    this.lastPoses = lastPoses;
    }

    public synchronized ArrayList<LandMark> getCoordinates() {
        return coordinates;
    }

    public synchronized void setLastCoordinates(ArrayList<LandMark> coordinates) {
        this.coordinates = coordinates;
    }
}
