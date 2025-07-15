package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private int id;
    private int frequency;
    private STATUS status;
    private ArrayList<TrackedObject> lastTrackedObjects;
    private ArrayList<StampedCloudPoints> data;
    private int lastIndex;


    public LiDarWorkerTracker(int id, int frequency, ArrayList<TrackedObject> lastTrackedObjects, LiDarDataBase database) {
      this.id = id;
      this.frequency = frequency;
      this.status = STATUS.UP;
      this.lastTrackedObjects = lastTrackedObjects;
      this.data = database.getData();
      lastIndex = 0;
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

    public void setLastTrackedObjects(ArrayList<TrackedObject> lastTrackObjects) {
        this.lastTrackedObjects=lastTrackObjects;
    }


    public int getId() {
        return id;
    }

    public ArrayList<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;

    }


    public ArrayList<TrackedObject> trackObjectsAtTime (int tick,ArrayList<StampedDetectedObjects> waitingList) {
        ArrayList<TrackedObject> result=new ArrayList<>();
        int j;
        /// going over the objects in the db
        for (j = lastIndex; j < data.size() && data.get(j).getTime() <= tick && data.get(j).getId() != "ERROR" && !waitingList.isEmpty(); j++) {  /// checking the lidar can track the object depending on its frequency

            if (data.get(j).getTime() + getFrequency() <= tick) {    /// going over the waiting list of detected objects
                for (int k = 0; k < waitingList.size() && waitingList.get(k).getTime() <= data.get(j).getTime(); k++) {      /// getting the objects which has been detected on the same time as the object and check if the track object is one of them
                    if (waitingList.get(k).getTime() == data.get(j).getTime()) {
                        for (int l = 0; l < waitingList.get(k).getDetectedObjects().size(); l++) {
                            if (data.get(j).getId().equals(waitingList.get(k).getDetectedObjects().get(l).getId())) {
                                TrackedObject trackObject = trackObjectPreperation(waitingList.get(k).getDetectedObjects().get(l), data.get(j));
                                result.add(trackObject);
                                lastIndex = j + 1;
                                waitingList.get(k).getDetectedObjects().remove(waitingList.get(k).getDetectedObjects().get(l));
                                if (waitingList.get(k).getDetectedObjects().isEmpty()) {
                                    waitingList.remove(k);
                                    break;
                                }
                            }

                        }

                    }
                }
            }
        }

        if (j < data.size() && data.get(j).getId().equals("ERROR")) {
            setStatus(STATUS.ERROR);
            result.add(new TrackedObject("error",data.get(j).getTime(),"error",new ArrayList<>()));   ///will be used to get the crash time

        }
        else if (result.size() > 0) {
            this.lastTrackedObjects = duplicateTrackObjects(result);
            StatisticalFolder.getInstance().addNumTrackedObjects(lastTrackedObjects.size());
        }
        return result;

    }
    public boolean isLastIndex() {
        return lastIndex==data.size();
    }

    private TrackedObject trackObjectPreperation(DetectedObject detectedObject, StampedCloudPoints stampedCloudPoints) {
        ArrayList<CloudPoint> coordinates = new ArrayList();
        for (int i = 0; i < stampedCloudPoints.getCloudPoints().size(); i++)
            coordinates.add(new CloudPoint(stampedCloudPoints.getCloudPoints().get(i).get(0), stampedCloudPoints.getCloudPoints().get(i).get(1)));
        return new TrackedObject(stampedCloudPoints.getId(), stampedCloudPoints.getTime(), detectedObject.getDescription(), coordinates);
    }

    private ArrayList<TrackedObject>  duplicateTrackObjects (ArrayList<TrackedObject> ToDuplicate) {
        ArrayList<TrackedObject> newTrackObjects = new ArrayList();
        for (int i=0; i<ToDuplicate.size(); i++)
            newTrackObjects.add(new TrackedObject(ToDuplicate.get(i).getId(),ToDuplicate.get(i).getTime(),ToDuplicate.get(i).getDescription(),ToDuplicate.get(i).getCoordinates()));
        return newTrackObjects;

    }
}
