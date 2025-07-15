package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam(new ArrayList <LandMark>() , new ArrayList<Pose>() );
    }
    private ArrayList <LandMark> landmarks;
    private ArrayList <Pose> poses;

    private FusionSlam(ArrayList <LandMark> landmarks, ArrayList <Pose> poses) {
        this.landmarks = landmarks;
        this.poses = poses;
    }
    public synchronized static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    public synchronized ArrayList <LandMark> getLandmarks() {
        return landmarks;
    }
    public synchronized ArrayList <Pose> getPoses() {
        return poses;
    }

    public synchronized void addPose(Pose pose) {
        this.poses.add(pose);
    }
    public synchronized Pose getLastPose() {
        if (poses.size() > 0) {
            return this.poses.get(this.poses.size() - 1);
        }
        else return null;
    }

    public synchronized void createCoordinates(ArrayList<ArrayList<TrackedObject>> trackedObjects) {
        for (int i = 0; i < trackedObjects.size(); i++) {
            for (int j = 0; j < trackedObjects.get(i).size(); j++) {
                if (trackedObjects.get(i).get(j).getTime() <= getLastPose().getTime()) {
                    processTrackedObjectsEvent(trackedObjects.get(i).get(j), currPose(trackedObjects.get(i).get(j)));
                    trackedObjects.get(i).remove(j);
                    if (trackedObjects.get(i).isEmpty()) {
                        trackedObjects.remove(i);
                        break;
                    }
                }
            }

        }
    }

    public ArrayList<CloudPoint> convertToGlobalCoordinates(ArrayList<CloudPoint> localcoordinates, Pose pose) {
        ArrayList<CloudPoint> globalCoordinates = new ArrayList<>();
        double radianAngle = pose.getYaw() * Math.PI / 180;
        double cosx = Math.cos(radianAngle);
        double sinx = Math.sin(radianAngle);
        for (int i = 0; i < localcoordinates.size(); i++) {
            double x = localcoordinates.get(i).getX() * cosx - localcoordinates.get(i).getY() * sinx + pose.getX();
            double y = localcoordinates.get(i).getX() * sinx + localcoordinates.get(i).getY() * cosx + pose.getY();
            globalCoordinates.add(new CloudPoint(x, y));
        }
        return globalCoordinates;
    }


    private ArrayList<CloudPoint> calculateAverageCoordinates(ArrayList<CloudPoint> oldCoordinates, ArrayList<CloudPoint> newCoordinates) {
        ArrayList<CloudPoint> averagedCoordinates = new ArrayList<>();

        // Iterate over the new coordinates and average with the corresponding old coordinates
        for (int i = 0; i < newCoordinates.size(); i++) {
            double x, y;

            // Check if the old coordinates have a matching point at this index
            if (i < oldCoordinates.size()) {
                x = (oldCoordinates.get(i).getX() + newCoordinates.get(i).getX()) / 2;
                y = (oldCoordinates.get(i).getY() + newCoordinates.get(i).getY()) / 2;
            } else {
                // If no corresponding old coordinate, use the new one as is
                x = newCoordinates.get(i).getX();
                y = newCoordinates.get(i).getY();
            }

            averagedCoordinates.add(new CloudPoint(x, y));
        }

        return averagedCoordinates;
    }


    private void processTrackedObjectsEvent(TrackedObject trackedObject, Pose currPose) {
        ArrayList<LandMark> currLandmarks = getLandmarks();
        boolean newObject = true;
        int index = -1;
        for (int i = 0; i < currLandmarks.size(); i++) {
            if (currLandmarks.get(i).getId().equals(trackedObject.getId())) {
                newObject = false;
                index = i;
                break;
            }
        }

        ArrayList<CloudPoint> globalCoordinates = convertToGlobalCoordinates(trackedObject.getCoordinates(), currPose);

        if (newObject) {
            currLandmarks.add(new LandMark(trackedObject.getId(), trackedObject.getDescription(), globalCoordinates));
            StatisticalFolder.getInstance().incNumLandmarks();
        } else {
            ArrayList<CloudPoint> oldCoordinates = currLandmarks.get(index).getCoordinates();
            ArrayList<CloudPoint> newCoordinates = calculateAverageCoordinates(oldCoordinates, globalCoordinates);
            currLandmarks.get(index).setCoordinates(newCoordinates);
        }
    }

    private Pose currPose(TrackedObject trackedObject) {
        ArrayList<Pose> poses = getPoses();
        for (int i = 0; i < poses.size(); i++) {
            if (poses.get(i).getTime() == trackedObject.getTime()) {
                return poses.get(i);
            }
        }
        return new Pose(0,0,0,0);

    }
}
