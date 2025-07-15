package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
private String id;
private int time;
ArrayList<ArrayList<Double>> cloudPoints;

public StampedCloudPoints(String id, int time, ArrayList<ArrayList<Double>> cloudPoints) {
    this.id = id;
    this.time = time;
    this.cloudPoints = cloudPoints;
}
public String getId() {
    return id;
}
public int getTime() {
    return time;
}

public ArrayList<ArrayList<Double>> getCloudPoints() {
    return cloudPoints;
}
}
